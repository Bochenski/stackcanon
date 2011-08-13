package controllers

import play.mvc._
import org.bson.types.ObjectId

object Application extends Controller with Secure {

  import views.Application._

  def index = html.index(models.User.findById(new ObjectId(session("user_id").get)).get)

}
