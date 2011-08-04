package models

import scala.xml._
import play._
import play.libs.Codec;
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import scala.collection._

class ApplicationSetting(o: DBObject) extends DBInstance("Setting", o) {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val key = o.getAs[String]("key")
  lazy val value = o.getAs[String]("value")
}

object ApplicationSetting extends DBBase[ApplicationSetting]("Settings") {
  override def allXML = <ApplicationSetting>
    {super.allXML}
  </ApplicationSetting>

  def findByKey(key: String) = findOneBy("key", key)

  def create(key: String, value: String): Boolean = {

    Logger.info("in model create appplication setting")
    //check whether the user exists
    val setting = findByKey(key)
    setting match {
      case Some(_) => false
      case None => {
        val builder = MongoDBObject.newBuilder
        builder += "key" -> key
        builder += "value" -> value
        val newObj = builder.result().asDBObject
        coll += newObj
        Logger.info("Created setting %s", key)
        true
      }
    }
  }

  def update(key: String, value: String): Boolean = {
    val setting = findByKey(key)
    setting match {
      case None => {
        Logger.info("AppSetting not found to update " + key)
        false
      }
      case Some(_) => {
        ApplicationSetting.update(MongoDBObject("_id" -> setting.get.oid.get), $set("value" -> value))
        if (settings.contains(key)) {
          settings(key) = value;
        }
        true
      }
    }
  }

  private var settings = mutable.Map[String, String]()

  def getSetting(key: String) = {
    if (!settings.contains(key)) {
      models.ApplicationSetting.findByKey(key) match {
        case Some(x) => {
          settings += key -> x.value.get
        }
        case None => {
          create(key,"")
          settings += key -> ""
        }
      }
    }
    settings.get(key).get
  }
}
