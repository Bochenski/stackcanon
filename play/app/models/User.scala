package models

import scala.xml._
import play._
import data.validation.Valid
import mvc.Scope.Flash
import play.libs.Codec;
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

class User(o: DBObject) extends DBInstance("User", o) {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val username = o.getAs[String]("username")
  lazy val first_name = o.getAs[String]("first_name")
  lazy val surname = o.getAs[String]("surname")
  lazy val password = o.getAs[String]("password")
  lazy val google_open_id = o.getAs[String]("google_open_id")
  lazy val facebook_id = o.getAs[String]("facebook_id")
  lazy val _roles = o.getAs[BasicDBList]("roles")

  def isInRole(role: String) = {
    //first find the role in the Roles Table
    models.Role.findByName(role) match {
      case Some(dbRole) => {
         _roles match {
            case Some(roles) => {
              val roleStrings = roles.toList collect { case s: String => s}
              val result = roleStrings.contains(dbRole.getIdString)
              Logger.info(username +  " is in role " + role + "? :" + result.toString)
              result
            }
            case _ => {
              Logger.info(username + " is in no roles")
              false
            }
         }
      }
      case None => {
        Logger.error("role" + role + " not found in Roles List")
        false
      }

    }

  }

  def getUserRoles() :List[String] = {
    _roles match {
      case Some(roles) => {
        val roleList = (roles.toList map (role => role.toString))
        roleList
      }
      case None => {
        val empty = List("")
        empty
      }
    }
  }

  def getId() = {
    oid match {
      case Some(id) => id
      case None => null
    }
  }

  def getIdString() = {
    oid match {
      case Some(id) => id.toString
      case None => ""
    }
  }
}

object User extends DBBase[User]("Users") {
  override def allXML = <Users>{super.allXML}</Users>

  def login(username: String, password: String) = findOneBy(MongoDBObject("username" -> username.toLowerCase, "password" -> Codec.hexMD5(password)))

  def findByUsername(username: String) = findOneBy("username", username.toLowerCase)

  def findByGoogleOpenID(id: String) = findOneBy("google_open_id", id)

  def findByFacebookID(id: String) = findOneBy("facebook_id", id)

  def create(username: String, first_name: String, surname: String, password: String, google_open_id: String, facebook_id: String, roles: List[String]) = {
    Logger.info("in model create user")
    //check whether the user exists
    val lowerUser = username.toLowerCase
    val user = findByUsername(lowerUser)
    user match {
      case Some(_) => false
      case None => {
        val builder = MongoDBObject.newBuilder
        builder += "username" -> lowerUser
        builder += "first_name" -> first_name
        builder += "surname" -> surname
        builder += "password" -> Codec.hexMD5(password)
        if (!roles.contains("sysadmin")) {
          builder += "roles" -> checkForFirstUser(roles)
        } else {
          builder += "roles" -> models.Role.getRoleIdList(roles)
        }
        builder += "google_open_id" -> google_open_id
        builder += "facebook_id" -> facebook_id
        val newObj = builder.result().asDBObject
        coll += newObj
        Logger.info("Created username %s", lowerUser)
        true
      }
    }
  }

  def getUsersInRole(role: String) =  {

    findManyByMatchingArrayContent("roles", MongoDBObject(role -> 1))
  }

  def addUserToRole(userId: String, roleId: String) {
       findById(new ObjectId(userId)) match {
       case Some(user) => {
         models.Role.findById(new ObjectId(roleId)) match {
           case Some(role) => {
             if(!(user.isInRole(role.getName))){
                update(MongoDBObject("_id" -> new ObjectId(userId)), $set("roles" -> (user._roles.get :+ role.getIdString)))
             }
           }
           case None => {
             Logger.error("role not found to add to user")
           }
         }

       }
       case None => {
         Logger.error("user not found to add role to")
       }
     }
  }

  def removeUserFromRole(userId: String, roleId: String) {
    findById(new ObjectId(userId)) match {
      case Some(user) => {
        models.Role.findById(new ObjectId(roleId)) match {
          case Some(role) => {
            user._roles.get.removeField(role.getIdString)

            update(MongoDBObject("_id" -> new ObjectId(userId)), $set("roles" ->  user._roles.get))
          }
          case None => {
            Logger.error("role not found to remove from user")
          }
        }
      }
      case none => {
        Logger.error("user not found to remove role from")
      }
    }
  }

  def create(username: String, first_name: String, surname: String, password: String, roles: List[String]): Boolean =
    create(username, first_name, surname, password, "", "", roles)

  private var _hasUsers = false

  private def checkForFirstUser(roles: List[String]) : List[String] = {
    //called as each user is created, is this our first user, if so, make them a super user
    if (!_hasUsers) {
      if (coll.count == 0) {
        Logger.info("making this user a sys admin")
        _hasUsers = true
        return models.Role.getRoleIdList(roles) ++ models.Role.getRoleIdList(List("sysadmin"))
      }
    }
    roles
  }

  def associateWithGoogleOpenID(user: User, id: String) {
    User.addField(user, "google_open_id", id)
    update(user)
  }

  def dissasociateWithGoogleOpenID(user: User) {
    User.addField(user, "google_open_id", "")
    update(user)
  }

  def associateWithFacebookID(user: User, id: String) {
    User.addField(user, "facebook_id", id)
    update(user)
  }

  def dissacociateWithFacebookID(user: User) {
    User.addField(user, "facebook_id", "")
    update(user)
  }
}
