package controllers

import play._
import play.mvc._
import play.mvc.results._
import com.mongodb.casbah.Imports._

object Login extends Controller with Authentication {

  import views.Login._

  def newform = html.newform()

  def create(): ScalaAction = {
    models.User.login(params.get("email"), params.get("password")) match {
      case Some(user) =>
        Logger.info("User ID %s logged in", user.oid.get)
        setSessionUser(user)
        Action(Application.index)
      case None =>
        flash += ("error" -> "Invalid email and/or password")
        Action(newform)
    }
  }

  def destroy() = {
    clearSessionUser()
    Action(newform)
  }





/*  private def getLoggedInUser =
    session("user_id") match {
      case Some(user_id) => models.User.findByID(new ObjectId(user_id))
      case None => None
    }*/
}
