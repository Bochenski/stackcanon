package models

import com.mongodb.casbah.Imports._
import scala.xml._

abstract class DBBase[T: Manifest](collName: String) {
  def coll = MongoDB.getDB(collName)

  def contentField = MongoDBObject()

  def newT1(x: DBObject) = manifest[T].erasure.getConstructor(classOf[DBObject]).newInstance(x)
  def newT(x: DBObject) = newT1(x).asInstanceOf[T]

  def all() = {
    for (x <- coll.find(MongoDBObject(), contentField).toIterable) yield newT(x)
  }

  def allJson() = {
    for (x <- coll.find(MongoDBObject(), contentField).toIterable) yield newT1(x).asInstanceOf[DBInstance].toJson
  }

  def allXML() = {
    val nodes = (for (x <- coll.find(MongoDBObject(), contentField).toIterable) yield newT1(x).asInstanceOf[DBInstance].toXml).toSeq
    new Elem(null, collName, xml.Null, xml.TopScope, nodes: _*)
  }

  def findById(id: ObjectId): Option[T] = {
    val q = MongoDBObject("_id" -> id)
    coll.findOne(q, contentField) match {
      case Some(x) => Some(newT(x))
      case None => None
    }
  }

  def findOneBy(field: String, search: Any): Option[T] = {
    val q = MongoDBObject(field -> search)
    coll.findOne(q, contentField) match {
      case Some(x) => Some(newT(x))
      case None => None
    }
  }

  def findOneBy(q: MongoDBObject): Option[T] = {
      coll.findOne(q, contentField) match {
        case Some(x) => Some(newT(x))
        case None => None
      }
    }

  def findManyBy(field: String, search: Any): Iterable[T] = {
    val q = MongoDBObject(field -> search)
    for (x <- coll.find(q, contentField).toIterable) yield newT(x)
  }

  def findManyByMatchingArrayContent(arrayField: String, arrayContentToMatch :MongoDBObject) : Iterable[T] = {
    val q = arrayField $elemMatch arrayContentToMatch
    for (x <- coll.find(q,contentField).toIterable) yield newT(x)
  }

  def addField(o: DBInstance, key: String, value: Object) {
    o.dbo += key -> value
//    o.dbo += "schedule" -> Schedule.serialiseSchedule(schedule)
  }

  def update(o: DBInstance): WriteResult = {
    coll.save(o.dbo)
  }

  def update(query: DBObject, ob: DBObject ) =
  {
    coll.update(query,ob)
  }

  def delete(o: DBInstance): WriteResult = {
    coll.remove(o.dbo)
  }

  def remove(query: DBObject) = {
    coll.remove(query)
  }

}