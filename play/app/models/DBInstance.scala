package models

import scala.xml._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

abstract class DBInstance {
   val toXml: Node = <Empty></Empty>
   val toJson: JObject = null
}