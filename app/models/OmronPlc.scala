package models
import javax.inject.{ Inject, Singleton }
import play.api._
import com.typesafe.config._
import scala.concurrent._
import scala.collection.JavaConverters._
import HslCommunication.Profinet.Omron._
import play.api.inject.ApplicationLifecycle
import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global

case class OmronPlcConfig(ip: String, port: Int, freq: Int, sa1: Int, da1: Int, da2: Int, format: String, simulateDate: Boolean)
case class MonitorType(id: String, desp: String, unit: String, addr: String, prec: Int = 2,
                       std_law:      Option[Double] = None,
                       std_internal: Option[Double] = None) {
  def formatRecord(r: Option[Double]) = {
    if (r.isEmpty)
      "-"
    else {
      val value = s"%.${prec}f".format(r.get)
      s"$value $unit"
    }
  }
}
case class OmronPlcReport(ch: Int, status: Int, values: Seq[Double], mfc: Double, flow: Double, coeff: Int)
case class RecordData(mt: MonitorType, value: Option[Double])
case class ShownRecord(mt: MonitorType, value: String)

object OmronCollector {
  case object CollectData
  case object GetLatestData
  case object WriteRecord
}

class OmronCollector(plc: OmronPlc, system: ActorSystem) extends Actor {
  import OmronCollector._
  val timer = {
    import scala.concurrent.duration._
    system.scheduler.schedule(Duration(3, SECONDS), Duration(plc.plcConfig.freq, SECONDS), self, CollectData)
  }

  val recordTimer = {
    import scala.concurrent.duration._
    system.scheduler.schedule(Duration(3, SECONDS), Duration(1, MINUTES), self, WriteRecord)
  }

  def handler(latestData: Seq[RecordData]): Receive = {
    case CollectData =>
      blocking {
        context become handler(plc.readRealtimeValues)
      }
    case GetLatestData =>
      sender ! latestData
    case WriteRecord =>
      plc.writeDB
  }

  def receive = handler(Seq.empty[RecordData])

  override def postStop(): Unit = {
    timer.cancel()
  }
}

@Singleton
class OmronPlc @Inject() (config: Configuration, system: ActorSystem, appLifecycle: ApplicationLifecycle, recordOps: RecordOps) {

  implicit val mtLoader: ConfigLoader[Seq[MonitorType]] = new ConfigLoader[Seq[MonitorType]] {
    def load(rootConfig: Config, path: String): Seq[MonitorType] = {
      val mtConfigList = rootConfig.getConfigList(path)
      mtConfigList.asScala map {
        mtConfig =>
          val id = mtConfig.getString("id")
          val desp = if (mtConfig.hasPath("desp"))
            mtConfig.getString("desp")
          else
            id
          val addr = mtConfig.getString("addr")
          val unit = mtConfig.getString("unit")
          val prec = mtConfig.getInt("prec")
          val std_law = if (mtConfig.hasPath("std_law"))
            Some(mtConfig.getDouble("std_law"))
          else
            None
          MonitorType(id, desp, unit, addr, prec, std_law)
      }
    }
  }

  implicit val configLoader: ConfigLoader[OmronPlcConfig] = new ConfigLoader[OmronPlcConfig] {
    def load(rootConfig: Config, path: String): OmronPlcConfig = {
      val config = rootConfig.getConfig(path)
      val ip = config.getString("ip")
      val port = config.getInt("port")
      val freq = config.getInt("freq")
      val sa1 = config.getInt("SA1")
      val da1 = config.getInt("DA1")
      val da2 = config.getInt("DA2")
      val format = config.getString("format")
      val simulateDate = config.getBoolean("simulateDate")
      OmronPlcConfig(ip, port, freq, sa1, da1, da2, format, simulateDate)
    }
  }

  val plcConfig = config.get[OmronPlcConfig]("OmronPLC")
  val mtList = config.get[Seq[MonitorType]]("OmronPLC.monitorTypes")
  val mtIdxMap = {
    val mtIds = mtList map (_.id) 
    val mtIdx = mtIds.zipWithIndex
    mtIdx.toMap
  }
  val mtCaseMap = {
    val mtIdCasePair = mtList map (mt=>mt.id -> mt)
    mtIdCasePair.toMap
  }
  

  val finsNet = {
    import HslCommunication.Core.Transfer._
    val net = new OmronFinsNet(plcConfig.ip, plcConfig.port)
    net.setSA1(plcConfig.sa1.toByte)
    net.DA1 = plcConfig.da1.toByte
    net.DA2 = plcConfig.da2.toByte
    val trans = new ReverseWordTransform()
    trans.setDataFormat(DataFormat.valueOf(plcConfig.format))
    net.setByteTransform(trans)
    val ret = net.ConnectServer()
    if (ret.IsSuccess)
      Logger.info("Omrom is connected!")
    else
      Logger.warn("Omrom connect failed!")

    net
  }

  val collector = system.actorOf(Props(new OmronCollector(this, system)), "omron-collector")

  def readRealtimeValues = {
    val ret = finsNet.ReadDouble("D0240", 20)
    if (ret.IsSuccess)
      (mtList.zip(ret.Content map { Some(_) })) map { r => RecordData(r._1, r._2) }
    else {
      Logger.error(s"Failed to read values ${ret.ErrorCode}")
      mtList map { RecordData(_, None) }
    }
  }

  def readRecord = {
    import java.time._
    import HslCommunication.Core.Types._
    def handleRet[T](ret: OperateResultExOne[T]) = {
      if (!ret.IsSuccess)
        throw new Exception(s"read failed ${ret.ErrorCode} ${ret.Message}")

      ret.Content
    }

    try {
      val ch = handleRet(finsNet.ReadInt16("D5270")).toInt
      val stat = handleRet(finsNet.ReadInt16("D5271")).toInt
      val valueArray = handleRet(finsNet.ReadDouble("D5272", 21))
      val mtValues = valueArray.take(20)
      val flow = valueArray(20)
      val shortArray = handleRet(finsNet.ReadInt16("D5314", 6))
      val coeff = shortArray(0).toInt
      val year = shortArray(1).toInt
      val month = shortArray(2).toInt
      val day = shortArray(3).toInt
      val hour = shortArray(4).toInt
      val min = shortArray(5).toInt
      val dt = if (plcConfig.simulateDate)
        LocalDateTime.now()
      else
        LocalDateTime.of(year, month, day, hour, min)

      val rec = Record(dt, ch, stat, mtValues, flow, coeff)
      Logger.debug(rec.toString())
      Some(rec)
    } catch {
      case ex: Exception =>
        Logger.error("failed to read", ex)
        None
    }
  }

  def writeDB = {
    for (record <- readRecord) {
      for (ret <- recordOps.create(record))
        Logger.info("record has been written!")
    }
  }

  def getRealtimeData = {
    import OmronCollector._
    import akka.pattern.ask
    import akka.util.Timeout
    import scala.concurrent.duration._
    implicit val timeout = Timeout(Duration(3, SECONDS))

    val f = collector ? GetLatestData
    f.mapTo[Seq[RecordData]].recoverWith {
      case ex: akka.pattern.AskTimeoutException =>
        Future {
          mtList map { RecordData(_, None) }
        }
    }
  }
  appLifecycle.addStopHook { () =>
    Logger.info(s"Omrom PLC stop.")
    finsNet.ConnectClose()
    collector ! PoisonPill
    Future.successful(())
  }
}