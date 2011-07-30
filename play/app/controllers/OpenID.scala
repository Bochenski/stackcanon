package controllers

import play._
import play.mvc._
import org.openid4java._
import org.openid4java.consumer._
import org.openid4java.discovery._
import org.openid4java.message._
import org.openid4java.message.ax._
import results.Redirect
import javax.mail.internet._
import play.db.Model.Manager
import java.io._
import com.mongodb.casbah.Imports._
import scala.collection.JavaConverters._

object OpenID extends Controller with Authentication {

  val manager = new ConsumerManager
  val returnURL = models.ApplicationSetting.findByKey("website_address").get.value.get +"/openid?oid="
  private val AX_firstname = "FirstName"
  private val AX_surname = "Surname"
  private val AX_email= "Email"

  def form() = {

    // Create list discoveries
    val discoveries = manager.discover("https://www.google.com/accounts/o8/id")

    // Associate discoveries with OpenID provider and attempt to grab one endpoint
    val discovered = manager.associate(discoveries)

    // Serialise and save in session
    val baos = new ByteArrayOutputStream()
    val o = new ObjectOutputStream(baos)
    o.writeObject(discovered)

    // Save to OpenID mongo collection
    val oid = models.OpenID.create(baos.toByteArray)

    // Obtain AuthRequest
    val authReq = manager.authenticate(discovered, returnURL + oid.toString)

    // Create fetch request
    val fetch = FetchRequest.createFetchRequest
    fetch.addAttribute(AX_firstname, "http://axschema.org/namePerson/first", true)
    fetch.addAttribute(AX_surname, "http://axschema.org/namePerson/last", true)
    fetch.addAttribute(AX_email, "http://axschema.org/contact/email", true)
    fetch.setCount("Email", 1)
    authReq.addExtension(fetch)

    Redirect(authReq.getDestinationUrl(true))
  }

  def create() = {
    val openidResp = new org.openid4java.message.ParameterList(params.all)

    // Get discovered object
    val openidobj = models.OpenID.findByID(new ObjectId(params.get("oid"))).get
    val o = new ObjectInputStream(new ByteArrayInputStream(openidobj.discovered.get))
    val discovered = o.readObject().asInstanceOf[DiscoveryInformation]

    // Continue
    val url = "HTTP://" + request.host + request.url
    val verificationResult = manager.verify(url, openidResp, discovered)
    val identified = verificationResult.getVerifiedId

    if (identified != null) {

      // Do we know about this ID?
      models.User.findByGoogleOpenID(identified.getIdentifier) match {
        case Some(x) => {
          setSessionUser(x);
        }
        case None => {
          if (isUserLoggedIn) {
            // Associate google id with User
            val user = currentUserObject
            models.User.associateWithGoogleOpenID(user, identified.getIdentifier)
          } else {
            // Brand new user, create user and log in
            var firstname = ""
            var surname = ""
            var email = ""
            val message = verificationResult.getAuthResponse
            if (message.hasExtension(AxMessage.OPENID_NS_AX)) {
              val ext = message.getExtension(AxMessage.OPENID_NS_AX)
              if (ext.isInstanceOf[org.openid4java.message.ax.FetchResponse]) {
                val fetchResp = ext.asInstanceOf[org.openid4java.message.ax.FetchResponse]

                firstname = fetchResp.getAttributeValue(AX_firstname)
                surname = fetchResp.getAttributeValue(AX_surname)
                email = fetchResp.getAttributeValues(AX_email).get(0).asInstanceOf[String]
              }
            }

            // Create user
            models.User.create(email, firstname, surname, "", false,false,false, identified.getIdentifier,"")
            Logger.info("Logged in " + identified.getIdentifier)
            setSessionUser(models.User.findByGoogleOpenID(identified.getIdentifier).get)
          }
        }
      }
      Action(Application.index)

    } else {
      flash += ("error" -> "Failed to authenticate with Google")
      Action(Login.form)
    }
  }

}