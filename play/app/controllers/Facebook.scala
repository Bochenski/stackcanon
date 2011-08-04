package controllers

import play._
import play.mvc._
import play.libs._
import play.mvc.Scope.Params
import results.Redirect
import models.ApplicationSetting._

object Facebook extends Controller with Authentication {

  private val _client_id = "facebook_app_id"
  private val _secret = "facebook_app_secret"
  private val _uri_auth = "website_address"

  def form() = {
    Redirect("https://www.facebook.com/dialog/oauth?client_id=" + getSetting(_client_id) + "&redirect_uri=" + getSetting(_uri_auth) + "/facebook")
  }

  def create = {
    Logger.info("create")
    if (params._contains("error")) {
      flash += ("error" -> "Failed to authenticate with Facebook")
      Action(Login.form)
    } else {
      // Grab code
      val code = params.get("code")

      // Send back to facebook
      val phase2_response = WS.url("https://graph.facebook.com/oauth/access_token?client_id=" + getSetting(_client_id) + "&redirect_uri=" + getSetting(_uri_auth) + "/facebook" +
        "&client_secret=" + getSetting(_secret) + "&code=" + code).body().get().getString

      Logger.info(phase2_response)
      val split = phase2_response.split('&')
      val access_token = split(0)
      //val expires = split(1)
      val whoami = WS.url("https://graph.facebook.com/me?" + access_token).body().get().getString
      Html(whoami)
    }
  }
}

