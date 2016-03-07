import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

/**
 * @author 16drs1
 */
object RPCServer {
  val DATABASE = "books.db"
  def main(args: Array[String]) = {
    
    println("here")
    val server = new Server()
    server.startServer()
  }
}

class Server {
  val DATABASE = "books.db"

    /* allows people to perform a purchase of a book */
  def buy(itemNum: Int): String = {
    var s = "You have successfully bought 1 of ";

    try {
      Class.forName("org.sqlite.JDBC");
      val c: Connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE + ".db");
      c.setAutoCommit(false);
      val stm = c.createStatement();

      /* getting stock */
      var sql = "SELECT TITLE, STOCK FROM BOOKS WHERE ID=" + itemNum + ";";
      val res: ResultSet = stm.executeQuery(sql);
      var stock = -1;
      var title = "";
      if (res.next()) {
        stock = res.getInt("stock");
        title = res.getString("title");
      }

      if (stock > 0) { /* item is in stock. buy underway. */
        stock = stock - 1;
        var sql = "UPDATE BOOKS SET STOCK=" + stock + " WHERE ID=" + itemNum + ";";
        stm.executeUpdate(sql);
        c.commit();
        s = s + title;
      } else { /* item is out of stock */
        s = "Unfortunately, " + title + " is out of stock. More is on the way.";
      }
      res.close();
      stm.close();
      c.close();
    } catch {
      case ex: Throwable => {
        println("Server error: " + ex);
      }
    }
    return s;
  }

  /* allows people to search table by topic */
  def search(topic: String): List[List[String]] = {
    var res: List[List[String]] = List()
    try {
      Class.forName("org.sqlite.JDBC");
      val c: Connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE + ".db");
      val stm: Statement = c.createStatement();
      val sql = "SELECT ID, TITLE, STOCK FROM BOOKS WHERE TOPIC='" + topic.toLowerCase() + "';";
      val set: ResultSet = stm.executeQuery(sql);
      var stock = -1;
      while (set.next()) {
        stock = set.getInt("stock");
        if (stock > 0) {
          val element: List[String] = List("" + set.getInt("id"), set.getString("title"))
          res = res :+ element
        }
      }
      set.close();
      stm.close();
      c.close();
    } catch {
      case ex: Throwable => {
        println("Server error: " + ex);
      }
    }
    return res;
  }

  def lookup(itemNum: Int): Array[String] = {
    /* allows user to lookup books by their ids */
    var res: Array[String] = Array()
    try {
      Class.forName("org.sqlite.JDBC");
      val c = DriverManager.getConnection("jdbc:sqlite:" + DATABASE + ".db");
      c.setAutoCommit(false);
      val stm: Statement = c.createStatement();

      val sql = "SELECT TITLE, STOCK, COST, TOPIC FROM BOOKS WHERE ID=" + itemNum + ";";
      val set: ResultSet = stm.executeQuery(sql);

      if (set.next()) {
        res = res :+ (set.getInt("stock") > 0 match {
          case true  => "In Stock"
          case false => "Out of Stock"
        })
        res = res :+ set.getString("title")
        res = res :+ ("" + set.getFloat("cost"))
        res = res :+ set.getString("topic")
      }
      set.close();
      stm.close();
      c.close();
    } catch {
      case ex: Throwable => {
        println("Server error: " + ex);
      }
    }
    return res;
  }

  def startServer() = {
    try {
      val phm: PropertyHandlerMapping = new PropertyHandlerMapping();
      val ws = new WebServer(8080);
      val xmlRpcServer: XmlRpcServer = ws.getXmlRpcServer();
      phm.addHandler("sample", this.getClass)
      xmlRpcServer.setHandlerMapping(phm)
      ws.start()
    } catch {
      case ex: Throwable => {
        println("Server error: " + ex);
      }
    }
  

    /* opening database */
    var c: Connection = null

    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:" + DATABASE + ".db")
    } catch {
      case ex: Throwable => {
        println("Cannot open database " + ex)
      }
    }
    System.out.println("Opened database successfully.");

    /* creating table */
    var stm: Statement = null;
    try {
      stm = c.createStatement();
      val sql = "CREATE TABLE IF NOT EXISTS BOOKS " +
        "(ID INT PRIMARY KEY     NOT NULL," +
        "TITLE       CHAR(50)  NOT NULL," +
        "STOCK       INT     NOT NULL," +
        "COST        REAL    NOT NULL," +
        "TOPIC       CHAR(50)  NOT NULL)";
      stm.executeUpdate(sql);
      stm.close();
    } catch {
      case ex: Throwable => {
        println("Cannot create table" + ex)
      }
    }

    /* fill table if not already filled out*/
    try {
      stm = c.createStatement();
      val sql = "SELECT * FROM BOOKS WHERE ID=" + 53477 + ";";
      val set: ResultSet = stm.executeQuery(sql);
      if (!set.next()) {
        fillDB(c);
      }
      set.close();
      stm.close();
    } catch {
      case ex: Throwable => {
        println("Cannot open database " + ex)
      }
    }

    /* done with table opening, creating, and filling out. Close the connectin to it. */
    try {
      c.close();
    } catch {
      case ex: Throwable => {
        println("Cannot open database " + ex)
      }
    }

    /* spawning new thread to re-stock every 5 minutes */
    val dsu = new DBStockUpdater()
    dsu.start()
  }

  /* populates database */
  def fillDB(c: Connection) {
    try {
      c.setAutoCommit(false);
      var stm = c.createStatement()
      var sql = "INSERT INTO BOOKS (ID,TITLE,STOCK,COST,TOPIC)" +
        "VALUES (53477, 'Achieving Less Bugs with More Hugs in CSCI 339', 5, 39.99, 'distributed systems');";
      stm.executeUpdate(sql);

      sql = "INSERT INTO BOOKS (ID,TITLE,STOCK,COST,TOPIC)" +
        "VALUES (53573, 'Distributed Systems for Dummies', 5, 59.99, 'distributed systems');";
      stm.executeUpdate(sql);

      sql = "INSERT INTO BOOKS (ID,TITLE,STOCK,COST,TOPIC)" +
        "VALUES (12365, 'Surviving College', 5, 29.99, 'college life');";
      stm.executeUpdate(sql);

      sql = "INSERT INTO BOOKS (ID,TITLE,STOCK,COST,TOPIC)" +
        "VALUES (12498, 'Cooking for the Impatient Undergraduate', 5, 12.95, 'college life');";
      stm.executeUpdate(sql);

      stm.close();
      c.commit();
    } catch {
      case ex: Throwable => {
        println("Cannot open database " + ex)
      }
    }
  }
}

class DBStockUpdater() extends Runnable {
    var t: Option[Thread] = None;
    val threadName = "DBStockUpdate"
    val updateInterval = 300000; /* update every 5 minutes */

    /* implementing the runnable interface */
    /* 1. run() */
    def run() {
      while (true) {
        /* wait updateInterval, then re-stock */
        try {
          Thread.sleep(updateInterval);
        } catch {
          case e: InterruptedException => println(e)
        }

        try {
          Class.forName("org.sqlite.JDBC");
          val c = DriverManager.getConnection("jdbc:sqlite:" + RPCServer.DATABASE + ".db");
          c.setAutoCommit(false);
          val stm = c.createStatement();

          val sql = "UPDATE BOOKS SET STOCK=25;";
          stm.executeUpdate(sql);
          c.commit();
          stm.close();
          c.close();
        } catch {
          case ex: Throwable => {
            println("Cannot open database " + ex)
        }
      }
    }
  }
    
    def start() = {
      t match{
        case None => {
          t = Some(new Thread(this, threadName))
          t.get.start()
        }
        case Some(x) => {
          println("Thread already started. Stop trying to start it again")
        }
      }
   }
}

