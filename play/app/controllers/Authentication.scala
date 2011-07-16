package controllers

import play._
import play.mvc._

trait Authentication {

  self: Controller =>

  protected def clearSessionUser() {
    session.remove("user_id")
  }

  protected def setSessionUser(user: models.User) {
    session.put("user_id", user.oid.get)
  }
}