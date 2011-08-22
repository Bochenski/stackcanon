import org.bouncycastle.asn1.x509.RoleSyntax
import play.jobs._
 
@OnApplicationStart
class Bootstrap extends Job {
    
    override def doJob() {
      //update the schema if necessary
      models.Role.init()
      models.Version.check()
    }
    
}