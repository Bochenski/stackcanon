package models

import play._
import play.libs.Codec;
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import org.h2.command.ddl.CreateRole

class Role(o: DBObject) extends DBInstance("Role", o) {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val name = o.getAs[String]("name")

  def getName = {
    name match {
      case Some(x) => x
      case None => ""
    }
  }

  def getIdString = {
    oid match {
      case Some(x) => x.toString
      case None => ""
    }
  }

}

object Role extends DBBase[Role]("Roles") {

  def create(name: String) = {
    val builder = MongoDBObject.newBuilder
    builder += "name" -> name
    val newObj = builder.result().asDBObject
    coll += newObj
    true
  }

  def destroy(id: String) = {
    remove(MongoDBObject("_id" -> new ObjectId(id)))
  }

  def getNames : Iterable[String] = {
    all map { role => role.name.get}
  }

  def findByName(name: String) = {
    findOneBy("name", name)
  }

  def init()  {
    //make sure we have a sysadmin, admin and user roles defined
    CreateRoleIfMissing("sysadmin")
    CreateRoleIfMissing("admin")
    CreateRoleIfMissing("user")
  }

  def getRoleIdList(roles : List[String] )   = {
    val roleIdList = roles.map (role => CreateRoleIfMissing(role) )
    roleIdList
  }

  private def CreateRoleIfMissing(role: String) = {
     findByName(role) match {
      case None => {
        create(role)
        var newRole = findByName(role)
        newRole.get.getIdString
      }
      case Some(existingRole) => {
        existingRole.getIdString
      }
    }
  }
}