package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import results._
import scala.collection.JavaConverters._

object Role extends Controller with Admin with CurrentUser {

  import views.Role._

  def index = {
    request.format match {
      case "json" => {
        Json(compact(JsonAST.render(models.Role.allJson)))
      }
      case _ => html.index(models.Role.all,getCurrentUser)
    }
  }

  def create() = {
  implicit val formats = DefaultFormats
    request.format match {
      case "html" => models.Role.create(params.get("name")) match {
        case true => Action(index)
      }
      case "json" => {
        val o = (JsonParser.parse(params.get("body")).children).head
        val value = (o \\ "name").values.toString
        models.Resource.create(value) match {
          case true => OK
          case _ => ERROR
        }
      }
    }
  }

  def destroy(id: String) = {
    models.Role.destroy(id)
  }

  def form = {
    html.form()
  }

  def show(id: String) = {
    html.show(models.Role.findById(new ObjectId(id)).get,getCurrentUser)
  }
  def update(id: String) = {}

  def edit(id: String) = {
    html.edit(models.Role.findById(new ObjectId(id)).get)
  }
}
