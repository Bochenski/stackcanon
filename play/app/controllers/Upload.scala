package controllers

import play._
import play.libs._
import play.mvc._
import java.io._

object Upload extends Controller {

  import views.Upload._

  def index = {
    html.index()
  }
  def getphoto = {
    val bob = models.MongoDB.getGridFS.findOne("bob.png")
    response.setContentTypeIfNotSet(bob.get.contentType)
    bob.get.inputStream
  }

  def create(image:Option[File]) = {
    image match {
      case Some(image) =>
        Logger.info("Name:" + image.getName())
        Logger.info("path:" + image.getPath())
        val photoStream = new FileInputStream(image)

        models.MongoDB.getGridFS(photoStream) { fh =>
            fh.filename = "bob.png"
            fh.contentType = "image/png"
        }

      case None =>
        Logger.info("No Data Received")
        Error("No File Received")
    }

  }

  def form = {
    html.form()
  }

  def update(id: String) = {}
}