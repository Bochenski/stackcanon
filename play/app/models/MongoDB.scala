package models

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._

class MongoDB() {
  // We want JODA dates, not weird Java ones
  RegisterJodaTimeConversionHelpers()

  val host = Play.configuration.getProperty("db.host")
  val port = Play.configuration.getProperty("db.port").toInt
  val name = Play.configuration.getProperty("db.name")
  val username = Play.configuration.getProperty("db.username")
  val password = Play.configuration.getProperty("db.password")
  Logger.info(host + " " + port + " " + name + " " + username + " " + password)
  val server = MongoConnection(host, port)
  val db = server(name)
  val connected = db.authenticate(username, password)

  if (!connected) {
    Logger.fatal("Unable to authenticate against database")
  }
}

object MongoDB {
  private val mongodbInstance = new MongoDB

  def getDB = mongodbInstance.db

  def isValid = mongodbInstance.connected

  def reset = {
    val colls = getDB.getCollectionNames()

    colls.foreach(col => {
      if (col.startsWith("system."))
        Logger.info("Not dropping collection: " + col)
      else {
        Logger.info("Dropping collection: " + col)
        getDB.getCollection(col).drop()
      }
    })
    true
  }
}
