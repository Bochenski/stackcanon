package controllers

import play.mvc._
import com.mongodb.casbah.Imports._
/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 16/08/2011
 * Time: 21:25
 * To change this template use File | Settings | File Templates.
 */

trait CurrentUser {

    self: Controller =>

    protected def getCurrentUser() = {
    session("user_id") match {
      case Some(userid) => {
        models.User.findById(new ObjectId(userid)) match {
          case Some(user) => user
          case None => null
        }
      }
      case None => null
    }
  }
}