package models

import play._
import play.libs.Codec;
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import com.thoughtworks.xstream.core.util.ObjectIdDictionary

class Version(o: DBObject) extends DBInstance("Version", o) {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val version = o.getAs[Int]("version")

  def getIdString = {
    oid match {
      case Some(x) => x.toString
      case None => ""
    }
  }
}

object Version extends DBBase[Version]("Versions") {


  def create(version: Int) = {
    val builder = MongoDBObject.newBuilder
    builder += "version" -> version
    val newObj = builder.result().asDBObject
    coll += newObj
    true
  }

  def check() = {
    //first find our current DB version
    Logger.info("checking DB version and updating if required)")
    val appVersion = Play.configuration.getProperty("version").toInt
    Logger.info("appVersion " + appVersion.toString)
    findOne match {
      case Some(entry) => {

        entry.version match {
          case Some(dbVersion) => {
            migrate(dbVersion,appVersion)
          }
          case None => {
            Logger.error("no version number found")
          }
        }
      }
      case None => {
        //so we need to apply all schema changes
        migrate(0,appVersion)
      }
    }
  }

  private def migrate(fromVersion: Int, toVersion: Int) = {
    Logger.info("in migrate")
    var currentVersion = fromVersion

    if (fromVersion < toVersion) {
      while (currentVersion < toVersion ) {
        currentVersion match {
          case 0 => {
            //upgrade to version 1
            models.User.all() foreach { user =>
              var currentRoles = user.getUserRoles
              user.getUserRoles foreach { role =>
                models.Role.findByName(role)  match {
                  case Some(dbRole) => {
                    //so here we already have a role by that name,  just need to alter the object
                    currentRoles = migrateRoleNamesToRoleIds(user,role,dbRole,currentRoles)
                  }
                  case None => {
                    models.Role.create(role)
                    val dbRole = models.Role.findByName(role).get
                    currentRoles = migrateRoleNamesToRoleIds(user,role,dbRole,currentRoles)
                  }
                }
              }
            }
            update(1)
            currentVersion += 1
          }
        }
      }
    } else {
      //TODO: Rollbacks
    }
  }

  def removeString(s: String, li: List[String]) = {
    val (left, right) = li.span(_ != s)
    left ::: right.drop(1)
  }

  private def migrateRoleNamesToRoleIds(user: User, role: String, dbRole: Role, currentRoles: List[String]) = {
    Logger.info("migrating user role: " + role)

    val newRoles = removeString(role,currentRoles) ++  List(dbRole.getIdString)

    Logger.info("newRoles " + newRoles.toString)
    Logger.info("user id" + user.getIdString)
    models.User.update(MongoDBObject("_id" -> user.oid.get), $set("roles" -> newRoles))
    newRoles
  }

  private def update(toVersion: Int) {
    findOne() match {
      case Some(entry) => {
        models.User.update(MongoDBObject("_id" -> entry.oid.get),$set("version"-> toVersion))
      }
      case None => {
        create(toVersion)
      }
    }
  }
}