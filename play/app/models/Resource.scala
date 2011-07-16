package models

import play._
import play.libs.Codec;
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

class Resource(o: DBObject) {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val first_name = o.getAs[String]("first_name")
  val toXML = <Resource>
    <oid>
      {oid.getOrElse("").toString}
    </oid>
    <first_name>
      {first_name.getOrElse("")}
    </first_name>
  </Resource>
  val toJson = ("oid" -> oid.getOrElse("").toString) ~ ("first_name" -> first_name.getOrElse(""))
}

object Resource {
  def coll = MongoDB.getDB("Resources")

  def contentField = MongoDBObject("content" -> 0)

  def fromJson(o: JValue) {
    create((o \\ "first_name").values.toString)
  }

  def all() = {
    for (x <- coll.find(MongoDBObject(), contentField).toIterable) yield new Resource(x)
  }

  def allJson() = {
    for (x <- coll.find(MongoDBObject(), contentField).toIterable) yield new Resource(x).toJson
  }

  def create(first_name: String) = {
    val builder = MongoDBObject.newBuilder
    builder += "first_name" -> first_name
    val newObj = builder.result().asDBObject
    coll += newObj
    true
  }
}