package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._

trait Authentication {

  self: Controller =>

  protected def clearSessionUser() {
    session.remove("user_id")
  }

  protected def setSessionUser(user: models.User) {
    session.put("user_id", user.oid.get)
  }

  def isUserLoggedIn = {
    session("user_id") match {
      case Some(user_id) => true
      case None => false
    }
  }

  def currentUserObjectId = new ObjectId(session("user_id").get)

  def currentUserObject = models.User.findByID(new ObjectId(session("user_id").get)).get
}