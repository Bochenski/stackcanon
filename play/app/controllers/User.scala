package controllers

import play.mvc._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
object User extends Controller with Authentication {

  import views.User._

  def index = {
    request.format match {
//      case "html" => html.index()
      case "xml" => Xml(models.User.allXML)
      case "json" => Json(compact(JsonAST.render(models.User.allJson)))
    }
   }

  def show(id: String) = {
    val oid = new ObjectId(id)
    request.format match {
//      case "html" => html.show(id)
      case "xml" => Xml(models.User.findByID(oid).get.toXml)
      case "json" => Json(compact(JsonAST.render(models.User.findByID(oid).get.toJson)))
    }
   }

  def form = html.form()

  def create() = {
    (models.User.create(params.get("email"), params.get("first_name"), params.get("surname"), params.get("password"), true)) match {
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
