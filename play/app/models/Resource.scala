package models

import play._
import play.libs.Codec;
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

class Resource(o: DBObject) extends DBInstance("Resource") {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val value = o.getAs[String]("value")
}

object Resource extends DBBase[Resource]("Resources") {

  def create(value: String) = {
    val builder = MongoDBObject.newBuilder
    builder += "value" -> value
    val newObj = builder.result().asDBObject
    coll += newObj
    true
  }
}