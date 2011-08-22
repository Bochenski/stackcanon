package controllers

import play.mvc._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

object User extends Controller with Authentication with CurrentUser{

  import views.User._

  def index = {
    request.format match {
      case "html" => html.index(models.User.all,getCurrentUser)
      case "xml" => Xml(models.User.allXML)
      case "json" => Json(compact(JsonAST.render(models.User.allJson)))
    }
   }

  def show(id: String) = {
    val oid = new ObjectId(id)
    request.format match {
      case "html" => html.show(models.User.findById(oid).get,getCurrentUser)
      case "xml" => Xml(models.User.findById(oid).get.toXml)
      case "json" => Json(compact(JsonAST.render(models.User.findById(oid).get.toJson)))
    }
   }

  def form = html.form()

  def create() = {
    (models.User.create(params.get("email"), params.get("first_name"), params.get("surname"), params.get("password"),List("user"))) match {
      case true =>
        val user = models.User.login(params.get("email"), params.get("password"))
        setSessionUser(user.get)
        Action(Application.index)
      case false =>
        flash += ("error" -> "Registration failed")
        Action(form)
    }
  }
}
