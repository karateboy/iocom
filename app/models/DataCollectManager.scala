package models
import play.api._
import akka.actor._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import javax.inject.{ Inject, Singleton }
import akka.actor.ActorSystem
object DataCollectManager {
  case object CollectData
  case object GetLatestData
  def props = Props[DataCollectManager]
  
//  def getLatestData() = {
//    import akka.pattern.ask
//    import akka.util.Timeout
//    import scala.concurrent.duration._
//    implicit val timeout = Timeout(Duration(3, SECONDS))
//
//    val f = manager ? GetLatestData
//    f.mapTo[Map[MonitorType.Value, Record]]
//  }
}

@Singleton
class DataCollectManagerService @Inject()(system:ActorSystem){
  val manager = system.actorOf(DataCollectManager.props, "collect-manager")
}

class DataCollectManager extends Actor {
  import DataCollectManager._

//  val timer = {
//    import java.time._
//    import scala.concurrent.duration._
//    //Try to trigger at 30 sec
//    val next30 = LocalDateTime.now().withSecond(30).plusMinutes(1)
//    val postSeconds = new org.joda.time.Duration(DateTime.now, next30).getStandardSeconds
////    Akka.system.scheduler.schedule(Duration(postSeconds, SECONDS), Duration(1, MINUTES), self, CalculateData)
//  }

  def receive = handler()

  def handler(): Receive = {
    case GetLatestData =>
      
  }

//  override def postStop(): Unit = {
//    timer.cancel()
//  }

}