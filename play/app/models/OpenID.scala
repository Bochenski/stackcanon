package models

import play._
import com.mongodb.casbah.Imports._

class OpenID(o: DBObject) extends DBInstance("OpenID", o) {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val discovered = o.getAs[Array[Byte]]("discovered")
}

object OpenID extends DBBase[OpenID]("OpenIDs") {

  def create(discovered: Array[Byte]): ObjectId = {
    val builder = MongoDBObject.newBuilder
    builder += "discovered" -> discovered
    val newObj = builder.result().asDBObject
    coll += newObj
    newObj._id.get
  }
}
