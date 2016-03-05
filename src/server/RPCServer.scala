package server

import org.apache.xmlrpc.webserver.WebServer 
import org.apache.xmlrpc.server.XmlRpcServer
import org.apache.xmlrpc.server.PropertyHandlerMapping
import org.apache.xmlrpc.XmlRpcException


/**
 * @author 16drs1
 */
object RPCServer{
  
}


class Server{
  def SumAndDiff(x: Int, y: Int){
        
    
  }
  
  def main() = {
    try{
      val phm: PropertyHandlerMapping = new PropertyHandlerMapping();
      val server = new WebServer(8080);
      val rpcServer = server.getXmlRpcServer();
      
      
    }catch{
      case ex: XmlRpcException => {
        println("Server error: " + ex);
      } 
    }
  }
}