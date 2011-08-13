package controllers

import play.mvc._
import org.bson.types.ObjectId
import com.thoughtworks.xstream.core.util.ObjectIdDictionary

trait SysAdmin {
  self: Controller =>

  @Before
  def checkSysAdmin = {
    session("user_id") match {
      case Some(user_id) => {
        models.User.findById(new ObjectId(user_id)) match {
          case Some(user) => {
            if (user.isInRole("sysadmin")) { Continue }
            else {
              request.format match {
                case "html" => Action(controllers.Login.form)
                case _ => Unauthorized
              }
            }
          }
        }
      }
      case None => {
        request.format match {
          case "html" => Action(controllers.Login.form)
          case _ => Unauthorized
        }
      }
    }
  }
}
