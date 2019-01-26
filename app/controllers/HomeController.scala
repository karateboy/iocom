package controllers

import javax.inject._

import play.api.mvc._
import models._
import play._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (users: UserDB, repo: MinRecord, cc: ControllerComponents)(implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    import scala.concurrent._
    import scala.concurrent.duration._
    Ok(views.html.index("Your new application is ready."))
  }

}
