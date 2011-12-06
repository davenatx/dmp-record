package bootstrap.liftweb

import net.liftweb.http.{LiftRules, NotFoundAsTemplate, ParsePath, S}
import net.liftweb.sitemap.{SiteMap, Menu, Loc}
import net.liftweb.util.{ NamedPF, Props, LoanWrapper }
import net.liftweb.squerylrecord.SquerylRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.common.{Box, Full, Empty, Logger}

import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.Session

import com.jolbox.bonecp.BoneCP
import com.jolbox.bonecp.BoneCPConfig

import com.dmp.model.MySchema

/**
 * User: dmp
 * Date: 11/18/11
 * Time: 4:48 PM
 */

class Boot {
  def boot {
    // Setup DB access.
    SquerylRecord.initWithSquerylSession({
      // Retrieve connection from pool
      val session = ConnectionPool.getSession
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



    // wrap all web requests in squeryl transaction
    S.addAround(new LoanWrapper{
      override def apply[T](f: => T): T = {
    	  inTransaction{
    	      f
    	  }
      }
    })
  }
}

/**
 * Connection pool using BoneCP
 */
object ConnectionPool {
  var pool: Box[BoneCP] = Empty

  try {
    Class.forName(Props.get("db.driver").openOr(""))

    val config = new BoneCPConfig
    config.setJdbcUrl(Props.get("db.url").openOr(""))
    config.setUsername(Props.get("db.user").openOr(""))
    config.setPassword(Props.get("db.pass").openOr(""))
    config.setMinConnectionsPerPartition(5);
    config.setMaxConnectionsPerPartition(10);
    config.setPartitionCount(1);

    pool = Full(new BoneCP(config))
  } catch{
    case e : Exception => e.printStackTrace
    println("BoneCP - FAILED to initialize the connection pool")
  }

  def getSession: Session = {
    Session.create(pool.open_!.getConnection, new PostgreSqlAdapter)
  }
}
