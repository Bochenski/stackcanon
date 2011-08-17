package models

import play._
import play.libs.Codec;
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

class Role(o: DBObject) extends DBInstance("Role", o) {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val name = o.getAs[String]("name")

  def getName = {
    name match {
      case Some(x) => x
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

}