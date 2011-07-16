package controllers

import play.mvc._

object Application extends Controller with Secure {

  import views.Application._

  def index = html.index()

}
