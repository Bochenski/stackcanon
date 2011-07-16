package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._

trait Secure {
  self: Controller =>

  @Before
  def checkSecurity = {
    session("user_id") match {
      case Some(user_id) => Continue
      case None => Action(controllers.Login.newform)
    }
  }

  def userObjectId = new ObjectId(session("user_id").get)
}
