package controllers

import play._
import play.libs._
import play.mvc._
import java.io.{FileInputStream, File}

object Upload extends Controller {

  import views.Upload._

  def index = {
    html.index()
  }
  def getphoto = {
    val bob = "public/images/testImage.png"
    bob
  }

  def create(image:Option[File]) = {
    image match {
      case Some(image) =>
        Logger.info("Name:" + image.getName())
        Logger.info("path:" + image.getPath())
        //val photoStream = new FileInputStream(image)
        val file = new File("public/images/testImage.png")
        Logger.info("new path:" + file.getAbsolutePath())
        play.libs.Files.copy(image, file)
        index

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