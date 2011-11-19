package bootstrap.liftweb

import net.liftweb.http.{LiftRules, NotFoundAsTemplate, ParsePath, S}
import net.liftweb.sitemap.{SiteMap, Menu, Loc}
import net.liftweb.util.{ NamedPF, Props, LoanWrapper }
import net.liftweb.squerylrecord.SquerylRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.common.Logger

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.Session

import com.dmp.model.MySchema

import java.sql.DriverManager

/**
 * User: dmp
 * Date: 11/18/11
 * Time: 4:48 PM
 */

class Boot {
  def boot {
    Class.forName("org.postgresql.Driver")

     // Setup DB
    SquerylRecord.initWithSquerylSession({
      val session = Session.create(
        DriverManager.getConnection("jdbc:postgresql://localhost/dmp-record", "lift", "liftweb"),
        new PostgreSqlAdapter)
      session.setLogger(sql => Logger("SqlLog:").debug(sql))
      session
    })

    transaction {
      MySchema.drop
      MySchema.create
      MySchema.createTestData
    }
  
    // where to search snippet
    LiftRules.addToPackages("com.dmp")

    // build sitemap
    val entries = List(Menu("Home") / "index") :::
                  Nil
    
    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate(
        ParsePath(List("exceptions","404"),"html",false,false))
    })
    
    LiftRules.setSiteMap(SiteMap(entries:_*))
    
    // set character encoding
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))



    // wrap all requests in squeryl transaction
    S.addAround(new LoanWrapper{
      override def apply[T](f: => T): T = {
    	  inTransaction{
    	      f
    	  }
      }
    })
  }
}
