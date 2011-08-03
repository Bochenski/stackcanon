package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

object ApplicationSetting extends Controller with Secure with SysAdmin {

  import views.ApplicationSetting._

  def index() = {
        request.format match {
      case "json" => {
        Json(compact(JsonAST.render(models.ApplicationSetting.allJson)))
      }
      case _ => html.index(models.ApplicationSetting.all)
    }
  }

  def create() = {
  implicit val formats = DefaultFormats
    request.format match {
      case "html" => models.ApplicationSetting.create(params.get("key"),params.get("value")) match {
        case true => Action(index)
      }
      case "json" => {
        val o = (JsonParser.parse(params.get("body")).children).head
        val key = (o \\ "key").values.toString
        val value = (o \\ "value").values.toString
        models.ApplicationSetting.create(key,value) match {
          case true => OK
          case _ => ERROR
        }
      }
    }
  }

  def update(id :String) = {
    implicit val formats = DefaultFormats
      request.format match {
        case "html" => models.ApplicationSetting.update(params.get("key"),params.get("value")) match {
          case true=> Action(index)
        }
        case "json" => {
          val o = (JsonParser.parse(params.get("body")).children).head
          val key = (o \\ "key").values.toString
          val value = (o \\ "value").values.toString
          models.ApplicationSetting.update(key,value) match {
            case true => OK
            case _ => ERROR
          }
        }
      }
  }

  def form = {
    html.form()
  }

  def show(id: String) = {
    html.show(models.ApplicationSetting.findByID(new ObjectId(id)).get)
  }

  def edit(id: String) = {
    html.edit(models.ApplicationSetting.findByID(new ObjectId(id)).get)
  }
}