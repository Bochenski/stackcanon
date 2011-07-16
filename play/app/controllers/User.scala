package controllers

import play.mvc._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
object User extends Controller {

  import views.Authentication._

  def index() = {
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
}
