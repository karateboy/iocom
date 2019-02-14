package models
import javax.inject.{ Inject, Singleton }
import play.api._
import com.typesafe.config._
import scala.concurrent._
import scala.collection.JavaConverters._

@Singleton
class MonitorTypeDB @Inject() (config: Configuration) {
  implicit val configLoader: ConfigLoader[Seq[MonitorType]] = new ConfigLoader[Seq[MonitorType]] {
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
          MonitorType(id, desp, addr, unit, prec)
      }
    }
  }
  private val mtList = config.get[Seq[MonitorType]]("monitorTypes")
  Logger.info(s"Total ${mtList.size} MonitorTypes")

  val map: Map[String, MonitorType] = {
    val pairs = mtList map { mtCase => mtCase.id -> mtCase }
    pairs.toMap
  }

  def getMonitorTypeByID(id: String) = map.get(id)

  def format(mt: MonitorType, v: Option[Double]) = {
    if (v.isEmpty)
      "-"
    else {
      val prec = mt.prec
      s"%.${prec}f".format(v.get)
    }
  }

  def overStd(mtCase: MonitorType, v: Double) = {
    val overInternal =
      if (mtCase.std_internal.isDefined) {
        if (v > mtCase.std_internal.get)
          true
        else
          false
      } else
        false
    val overLaw =
      if (mtCase.std_law.isDefined) {
        if (v > mtCase.std_law.get)
          true
        else
          false
      } else
        false
    (overInternal, overLaw)
  }

  def getOverStd(mtCase: MonitorType, r: Option[Double]) = {
    if (r.isEmpty)
      false
    else {
      val (overInternal, overLaw) = overStd(mtCase, r.get)
      overInternal || overLaw
    }
  }

  def formatRecord(mtCase: MonitorType, r: Option[Double]) = {
    if (r.isEmpty)
      "-"
    else {
      val (overInternal, overLaw) = overStd(mtCase, r.get)
      val prec = mtCase.prec
      val value = s"%.${prec}f".format(r.get)
      s"$value"
    }
  }

  //  def getCssClassStr(mt: MonitorType.Value, r: Option[Record]) = {
  //    if (r.isEmpty)
  //      ""
  //    else {
  //      val v = r.get.value
  //      val (overInternal, overLaw) = overStd(mt, v)
  //      MonitorStatus.getCssClassStr(r.get.status, overInternal, overLaw)
  //    }
  //  }
  //
  //  def getCssClassStr(mt: MonitorType.Value, r: Record2) = {
  //    val v = r.value
  //    val (overInternal, overLaw) = overStd(mt, v)
  //    MonitorStatus.getCssClassStr(r.status, overInternal, overLaw)
  //  }
}