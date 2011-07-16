package controllers

import play._
import play.mvc._
import play.mvc.results._
import com.mongodb.casbah.Imports._

object Authentication extends Controller {

  import views.Authentication._

  def login = html.login()

  def doLogin(): ScalaAction = {
    models.User.login(params.get("email"), params.get("password")) match {
      case Some(user) =>
        Logger.info("User ID %s logged in", user.oid.get)
        setSessionUser(user)
        Action(Application.index)
      case None =>
        flash += ("error" -> "Invalid email and/or password")
        Action(login)
    }
  }

  def doLogout() = {
    clearSessionUser()
    Action(login)
  }

  def register = html.register()

  def doRegister() = {
    (models.User.create(params.get("email"), params.get("first_name"), params.get("surname"), params.get("password"), true)) match {
      case true =>
        val user = models.User.login(params.get("email"), params.get("password"))
        setSessionUser(user.get)
        Action(Application.index)
      case false =>
        flash += ("error" -> "Registration failed")
        Action(register)
    }
  }

  def clearSessionUser() {
    session.put("user_id", None)
  }

  def setSessionUser(user: models.User) {
    session.put("user_id", user.oid.get)
  }

  def getLoggedInUser =
    session("user_id") match {
      case Some(user_id) => models.User.findByID(new ObjectId(user_id))
      case None => None
    }
}
