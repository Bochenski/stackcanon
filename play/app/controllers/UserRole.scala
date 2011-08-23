package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import results._
import scala.collection.JavaConverters._

object UserRole extends Controller with Admin {

  def create() = {
    //need to check that a user is a super-user if they're trying to add a user to the superusers role
    Logger.info("in create user role userId: "  + params.get("userId") + " roleid " + params.get("roleId"))
    models.Role.findById(new ObjectId(params.get("roleId")))  match {
      case Some(role) => {
        if (role == "sysadmin") {
          session("user_id") match {
            case Some(user_id) => {
              models.User.findById(new ObjectId(user_id)) match {
                case Some(user) => {
                  if (user.isInRole("sysadmin")) {
                   models.User.addUserToRole(params.get("userId"),params.get("roleId"))
                  }
                }
              }
            }
          }
        } else {
          models.User.addUserToRole(params.get("userId"),params.get("roleId"))
        }
        Action(controllers.User.show(params.get("userId")))
      }
    }


  }

  def destroy() = {
    //only sys admins can remove other sysadmins from the sysadmin role
    //there must always be at least one sys admin (which we do by ensuring that you
    //can't remove yourself as a superuser
    models.Role.findById(new ObjectId(params.get("roleId")))  match {
      case Some(role) => {
        if (role == "sysadmin") {
          session("user_id") match {
            case Some(user_id) => {
              models.User.findById(new ObjectId(user_id)) match {
                case Some(user) => {
                  if (user.isInRole("sysadmin")) {
                    if (user_id != params.get("userId")){
                      models.User.removeUserFromRole(params.get("userId"),params.get("roleId"))
                    }
                    Logger.info("can't unassign yourself from being a sysadmin")
                  }
                }
              }
            }
          }
        } else {
          models.User.removeUserFromRole(params.get("userId"),params.get("roleId"))
        }
        Action(controllers.User.show(params.get("userId")))
      }
    }
  }

  def index() = {
    //this would be the page where you see all the users for a given role
  }

}
