package models
import javax.inject.{ Inject, Singleton }
import play.api._
import com.typesafe.config._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent._
import scala.collection.JavaConverters._
case class User(id: String, password: String, name: String)
@Singleton
class UserDB @Inject() (config: Configuration) {
  implicit val configLoader: ConfigLoader[Seq[User]] = new ConfigLoader[Seq[User]] {
    def load(rootConfig: Config, path: String): Seq[User] = {
      val userConfigList = rootConfig.getConfigList(path)
      val users = for (userConfig <- userConfigList.asScala) yield {
        val id = userConfig.getString("id")
        val password = userConfig.getString("password")
        val name = userConfig.getString("name")
        User(id, password, name)
      }
      users
    }
  }

  val people = config.get[Seq[User]]("users")

  Logger.info(s"User = ${people.size}")

  def getUserById(id: String) = people.find(_.id == id)

}