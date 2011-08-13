package models

import play._
import java.io._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs._

object FileManager {
   def saveImage(image: File, parentType: String, parentId: String, sequence: Double): String = {
     //remove anything we already have at this address as we don't want to bloat the DB unnecessarily

     deleteFile(parentType,parentId,sequence)
     // Get extension
     val extensionRegex = """^.*\.([^.]+)$""".r
     val extension = image.getName match {
         case extensionRegex(x) => Some(x)
         case _ => None
     }

     Logger.info("Name:" + image.getName())
     Logger.info("path:" + image.getPath())
     Logger.info("ext: " + extension.get.toString)

     val photoStream = new FileInputStream(image)
     models.MongoDB.getGridFS(photoStream) {
        fh =>
          fh.filename = image.getName()
          fh.contentType = "image/" + extension.get.toString
          fh.metaData = MongoDBObject("parentType" -> parentType, "parentId" -> parentId, "sequence" -> sequence)
     }
     parentType + "/" + parentId + "/image/"+ sequence.toInt
  }

  def saveImage(image:Option[File], parentType: String, parentId: String, sequence: Double): String = {
    image match {
      case Some(image) =>
        saveImage(image,parentType,parentId,sequence)
      case None =>
        Logger.info("No Data Received")
        ""
    }
  }

  def saveImage(image:Option[File], parentType: String, parentId: String): String = {
    saveImage(image,parentType,parentId,0)
  }

  def saveImage(image: File, parentType: String, parentId: String): String = {
    saveImage(image,parentType,parentId,0)
  }

  def getFile(parentType: String, parentId: String, sequence: Int): Option[GridFSDBFile] = {
    MongoDB.getGridFS.findOne(MongoDBObject("metadata.parentType" -> parentType, "metadata.parentId" -> parentId, "metadata.sequence" -> sequence))
  }

  def getFileCount(parentType: String, parentId: String) = {
    val count = MongoDB.getDB("fs.files").count(MongoDBObject("metadata.parentType" -> parentType, "metadata.parentId" -> parentId))
    Logger.info("found " + count.toString + " files")
    count
  }

  def getFile(parentType: String, parentId: String): Option[GridFSDBFile] = {
    getFile(parentType,parentId,0)
  }

  def deleteFile(parentType: String, parentId: String)  {
    deleteFile(parentType,parentId,0)
  }

  def deleteFile(parentType: String, parentId: String, sequence: Double)  {

    MongoDB.getGridFS.remove(MongoDBObject("metadata.parentType" -> parentType, "metadata.parentId" -> parentId, "metadata.sequence" -> sequence))
  }

}