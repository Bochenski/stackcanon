package controllers

import play.mvc._
import org.bson.types.ObjectId

trait SysAdmin {
  self: Controller =>

  @Before
  def checkSysAdmin = {
    session("user_id") match {
      case Some(user_id) => {
        if (models.User.isSysAdmin(currentUserObject))
          Continue
        else {
          request.format match {
            case "html" => Action(controllers.Login.form)
            case _ => Unauthorized
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

    def currentUserObject = models.User.findByID(new ObjectId(session("user_id").get)).get

}
