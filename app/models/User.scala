package models
import javax.inject.{ Inject, Singleton }
import play.api._
import com.typesafe.config._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent._

case class User(id: String, password: String, name: String)
@Singleton
class UserDB @Inject() (config: Configuration){
  implicit val configLoader: ConfigLoader[Seq[User]] = new ConfigLoader[Seq[User]] {
    def load(rootConfig: Config, path: String): Seq[User] = {
      val config = rootConfig.getConfig(path)
      Seq(User("1","1","1"))
    }
  }
  
  val people = config.get[Seq[User]]("")

  
}