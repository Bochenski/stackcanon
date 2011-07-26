package controllers

import play._
import play.mvc._
import play.mvc.results._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import scala.collection.JavaConverters._

object Login extends Controller with Authentication {

  import views.Login._

  def form = html.form()

  def create() = {
    request.format match {
      case "html" => {
        models.User.login(params.get("email"), params.get("password")) match {
          case Some(user) =>
            Logger.info("User ID %s logged in", user.oid.get)
            setSessionUser(user)
            Action(Application.index)
          case None =>
            flash += ("error" -> "Invalid email and/or password")
            Action(form)
        }
      }
      case "json" => {
        val o = (JsonParser.parse(params.get("body")).children).first
        val email = (o \\ "email").values.toString
        val password = (o \\ "password").values.toString
        models.User.login(email, password) match {
          case Some(user) =>
            Logger.info("User ID %s logged in", user.oid.get)
            setSessionUser(user)
            Ok
          case None =>
            Logger.info("Invalid credentials for login.json")
            clearSessionUser()
            Unauthorized
        }
      }
    }
  }

  def destroy() = {
    clearSessionUser()
    Action(Login.form)
  }
}
