package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models._
import play._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
case class Credential(userName: String, password: String)

/**
 * @author user
 */
@Singleton
class Login @Inject() (userDB: UserDB, cc: ControllerComponents)(implicit assetsFinder: AssetsFinder)
  extends Authentication(cc) {
  implicit val credentialReads = Json.reads[Credential]
  implicit val userWrites = Json.writes[User]

  def authenticate = Action(cc.parsers.json) {
    implicit request =>
      val credentail = request.body.validate[Credential]
      credentail.fold(
        {
          error =>
            BadRequest(Json.obj("ok" -> false, "msg" -> JsError.toJson(error)))
        },
        crd => {
          val optUser = userDB.getUserById(crd.userName)
          if (optUser.isEmpty || optUser.get.password != crd.password)
            Ok(Json.obj("ok" -> false, "msg" -> "密碼或帳戶錯誤"))
          else {
            val user = optUser.get
            implicit val userInfoWrite = Json.writes[UserInfo]
            val userInfo = UserInfo(user.id, user.name, "Admin")
            Ok(Json.obj("ok" -> true, "user" -> userInfo)).withSession(setUserinfo(request, userInfo))
          }
        })
  }

  def logout = Action {
    Ok(Json.obj(("ok" -> true))).withNewSession
  }

  def getUserInfo = Authenticated {
    implicit request =>
      val user = request.user
      Ok("")
  }
}