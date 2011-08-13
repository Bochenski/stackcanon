package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import net.liftweb.json._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import results._
import scala.collection.JavaConverters._
import org.joda.time._
import org.joda.time.format._
import java.io._
import scala.collection.mutable.ListBuffer

object FileManager extends Controller {

  def create(file: Option[File]) = {
  //implicit val formats = DefaultFormats
    Logger.info(file.toString)
    file match {
      case Some(x) => {
        Logger.info("some file")
        val sequence = models.FileManager.getFileCount(params.get("resourceName"),params.get("resourceId"))
        val photoPath = "/" +  models.FileManager.saveImage(x,params.get("resourceName"),params.get("resourceId"), sequence)
        Logger.info(photoPath)
        request.format match {
          case "json" => {
            val deleteUrl =  "/filemanager" + photoPath + "/delete"
            val response = List(("name" -> x.getName) ~ ("size" -> x.length()) ~ ("url" -> photoPath) ~ ("thumbnail_url" -> photoPath) ~ ("delete_url" -> deleteUrl ) ~ ("delete_type" -> "POST"))
            Logger.info(compact(JsonAST.render(response)).toString)
            compact(JsonAST.render(response))
          }
          case "html" => {
            Ok
          }
        }
      }
      case None =>  {
        Logger.info("Case none")
        val response = List(("name" -> "testing.jpg") ~ ("size" -> 123))
        compact(JsonAST.render(response))
      }
    }
  }

  def destroy(id: String) =
  {
    Logger.info("made it into destroy with id: " + id)
    models.FileManager.deleteFile("resource","id",0)
    OK
  }

  def show = {
    models.FileManager.getFile(params.get("resourceName"),params.get("resourceId"), params.get("sequence").toInt) match {
      case Some(file) => {
        response.setContentTypeIfNotSet(file.contentType)
        file.inputStream
      }
      case None => {}

    }
  }
}
