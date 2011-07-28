package controllers

import play._
import play.mvc._
import org.openid4java._
import org.openid4java.consumer._
import org.openid4java.discovery._
import org.openid4java.message._
import org.openid4java.message.ax._
import results.Redirect
import javax.mail.internet.ParameterList
import play.db.Model.Manager

object OpenID extends Controller {

  val manager = new ConsumerManager
  val returnURL = "http://edge.gintellect.com:9000/openid"

  def form() = {

    // Create list discoveries
    val discoveries = manager.discover("https://www.google.com/accounts/o8/id")

    // Associate discoveries with OpenID provider and attempt to grab one endpoint
    val discovered = manager.associate(discoveries)
    session.put("OpenIDDiscovered", discovered)

    // Obtain AuthRequest
    val authReq = manager.authenticate(discovered, returnURL)

    Redirect(authReq.getDestinationUrl(true))
  }

  def create() = {
    val openidResp = new org.openid4java.message.ParameterList(params.all)
    val discovered = session("OpenIDDiscovered").asInstanceOf[DiscoveryInformation]
    val qs = request.querystring
    val url = request.url + (if (qs.length > 0) "?%s".format(qs))
    val verificationResult = manager.verify(url, openidResp, discovered)
    val identified = verificationResult.getVerifiedId
    Html(identified)
  }

}