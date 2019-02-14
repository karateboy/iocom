package controllers
import play.api._
import play.api.mvc.Security._
import play.api.mvc._
import scala.concurrent._

case class UserInfo(id: String, name: String, groupId: String)

class Authentication(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  val idKey = "ID"
  val nameKey = "Name"
  val groupKey = "Group"

  def getUserinfo(request: RequestHeader): Option[UserInfo] = {

    for {
      id <- request.session.get(idKey)
      group <- request.session.get(groupKey)
      name <- request.session.get(nameKey)
    } yield UserInfo(id, name, group)

  }

  def onUnauthorized(request: RequestHeader) = Results.Unauthorized("Login first...")

  //def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
  //  AuthenticatedBuilder(getUserinfo _, onUnauthorized)
  //})

  //def isAuthenticated(f: => String => Request[AnyContent] => Result) = {
  //  Authenticated(getUserinfo, onUnauthorized) { user =>
  //    Action(request => f(user)(request))
  //  }
  // }

  def setUserinfo[A](request: Request[A], userInfo: UserInfo) = {
    request.session +
      (idKey -> userInfo.id.toString()) + (groupKey -> userInfo.groupId.toString()) +
      (nameKey -> userInfo.name)
  }

  def getUserInfo[A]()(implicit request: Request[A]): Option[UserInfo] = getUserinfo(request)

  def Authenticated = AuthenticatedBuilder(getUserinfo, cc.parsers.defaultBodyParser)
}