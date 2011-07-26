package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import results._
import scala.collection.JavaConverters._

object Resource extends Controller {

  import views.Resource._

  def index = {
    request.format match {
      case "json" => {
        Json(compact(JsonAST.render(models.Resource.allJson)))
      }
      case _ => html.index(models.Resource.all)
    }
  }

  def create() = {
  implicit val formats = DefaultFormats
    request.format match {
      case "html" => models.Resource.create(params.get("value")) match {
        case true => Action(index)
      }
      case "json" => {
        val o = (JsonParser.parse(params.get("body")).children).head
        val value = (o \\ "value").values.toString
        models.Resource.create(value) match {
          case true => OK
          case _ => ERROR
        }
      }
    }
  }

  def form = {
    html.newform()
  }

  def update(id: String) = {}
}
