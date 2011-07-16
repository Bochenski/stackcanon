package models

import scala.xml._
import play._
import play.libs.Codec;
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

class User(o: DBObject) extends DBInstance {
  lazy val oid = o.getAs[ObjectId]("_id")
  lazy val username = o.getAs[String]("username")
  lazy val first_name = o.getAs[String]("first_name")
  lazy val surname = o.getAs[String]("surname")
  lazy val password = o.getAs[String]("password")
  lazy val isAdmin = o.getAs[Boolean]("isAdmin")
  override val toXml = <User>
    <oid>{ oid.getOrElse("").toString }</oid>
    <username>{ username.getOrElse("") }</username>
    <first_name>{ first_name.getOrElse("") }</first_name>
    <surname>{ surname.getOrElse("") }</surname>
    <isAdmin>{ isAdmin.getOrElse(false) }</isAdmin>
  </User>
  override val toJson = ("oid" -> oid.getOrElse("").toString) ~ ("username" -> username.getOrElse("")) ~ ("first_name" -> first_name.getOrElse("")) ~ ("surname" -> surname.getOrElse("")) ~ ("isAdmin" -> isAdmin.getOrElse(false))
}

object User extends DBBase[User]("Users") {
  override def allXML = <Users>{ super.allXML }</Users>

  def login(username: String, password: String) = findOneBy(MongoDBObject("username" -> username, "password" -> Codec.hexMD5(password)))

  def findByUsername(username: String) = findOneBy("username", username)

  def create(username: String, first_name: String, surname: String, password: String, isAdmin: Boolean) = {
    //check whether the user exists
    val user = findByUsername(username)
    user match {
      case Some(_) => false
      case None => {
        val builder = MongoDBObject.newBuilder
        builder += "username" -> username
        builder += "first_name" -> first_name
        builder += "surname" -> surname
        builder += "password" -> Codec.hexMD5(password)
        builder += "isAdmin" -> isAdmin
        val newObj = builder.result().asDBObject
        coll += newObj
        Logger.info("Created username %s", username)
        true
      }
    }
  }
}
