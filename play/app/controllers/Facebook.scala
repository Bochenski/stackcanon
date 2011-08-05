package controllers

import play._
import play.mvc._
import play.libs._
import play.mvc.Scope.Params
import results.Redirect
import models.ApplicationSetting._
import net.liftweb.json.JsonParser

object Facebook extends Controller with Authentication {

  private val _client_id = "facebook_app_id"
  private val _secret = "facebook_app_secret"
  private val _uri_auth = "website_address"

  def form() = {
    Redirect("https://www.facebook.com/dialog/oauth?client_id=" + getSetting(_client_id) + "&redirect_uri=" + getSetting(_uri_auth) + "/facebook&scope=email")
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

      val o = (JsonParser.parse(whoami))
      val facebook_id = (o \ "id").values.toString
      val first_name = (o \\ "first_name").values.toString
      val last_name = (o \\ "last_name").values.toString
      Logger.info("facebook returned: id: " + facebook_id + " name: " + first_name + " surname: " + last_name)
      val email = (o \\ "email").values.toString

      Html(whoami)

      if (facebook_id != null && facebook_id != "") {

        // Do we know about this ID?
        models.User.findByFacebookID(facebook_id) match {
          case Some(x) => {
            setSessionUser(x);
          }
          case None => {
            if (isUserLoggedIn) {
              // Associate facebook id with User
              Logger.info("user already logged into site, associating account with facebookid")
              val user = currentUserObject
              models.User.associateWithFacebookID(user, facebook_id)
            } else {

              models.User.findByUsername(email) match {
                case Some(user) => {
                  //email address matching from facebook is good enough for us, let's associate that user with the account
                  Logger.info("match on username/email, associating with existing account")
                  models.User.associateWithFacebookID(user, facebook_id)
                }
                case None => {
                  // Create user
                  Logger.info("no match on username/email, creating new user")
                  models.User.create(email, first_name, last_name, "", false, false, false, "", facebook_id)
                }
              }
              //in either case we want to log the user in as auth was sucessful
              setSessionUser(models.User.findByFacebookID(facebook_id).get)
              Logger.info("Logged in via facebook id" + facebook_id)
            }
          }
        }
        Action(Application.index)

      } else {
        flash += ("error" -> "Failed to authenticate with Facebook")
        Action(Login.form)
      }

    }
  }
}

