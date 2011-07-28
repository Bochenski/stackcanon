package models

import scala.xml._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import com.mongodb.casbah.Imports._

abstract class DBInstance(xmlEntity: String, o: DBObject) {
  val dbo = o
  def nullOrString(o: Object) = if (o == null) "" else o.toString
  val ignoreOutput = List[String]()
  lazy val fields = this.getClass.getDeclaredFields.filter(x => x.getType.toString == "class scala.Option" && !ignoreOutput.contains(x.getName))
  lazy val fieldmap = (for (x <- fields) yield(x.getName, this.getClass.getDeclaredMethod(x.getName))).toMap
  def toXml = {
    val nodes = (for (x <- fieldmap) yield Elem(null, x._1, xml.Null, xml.TopScope, Text(nullOrString(x._2.invoke(this).asInstanceOf[Option[AnyRef]].get)))).toSeq
    new Elem(null, xmlEntity, xml.Null, xml.TopScope, nodes: _*)
  }

  def toJson = new JObject((for (x <- fieldmap) yield new JField(x._1, JString(nullOrString(x._2.invoke(this).asInstanceOf[Option[AnyRef]].get)))).toList)
}