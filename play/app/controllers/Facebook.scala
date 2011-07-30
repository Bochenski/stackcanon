package controllers

import play._
import play.mvc._
import play.libs._
import play.mvc.Scope.Params
import results.Redirect

object Facebook extends Controller with Authentication {

  private val client_id = models.ApplicationSetting.findByKey("facebook_app_id").get.value.get
  private val secret = models.ApplicationSetting.findByKey("facebook_app_secret").get.value.get
  private val uri_auth = models.ApplicationSetting.findByKey("website_address").get.value.get + "/facebook"

  def form() = {
    Redirect("https://www.facebook.com/dialog/oauth?client_id=" + client_id + "&redirect_uri=" + uri_auth)
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
      val phase2_response = WS.url("https://graph.facebook.com/oauth/access_token?client_id=" + client_id + "&redirect_uri=" + uri_auth +
        "&client_secret=" + secret + "&code=" + code).body().get().getString

      Logger.info(phase2_response)
      val split = phase2_response.split('&')
      val access_token = split(0)
      val expires = split(1)
      val whoami = WS.url("https://graph.facebook.com/me?" + access_token).body().get().getString
      Html(whoami)
    }
  }
}

