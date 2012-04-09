package net.brianvaughan.scala.chat;
import com.sun.net.httpserver._

import java.io._
import java.net._
import scala.actors._
import scala.actors.Actor._
import scala.xml._

class MessageHandler extends HttpHandler {
  def handle(t:HttpExchange) = {
    try {
      val params:java.util.HashMap[String,Object] = 
          t.getAttribute("parameters").asInstanceOf[java.util.HashMap[String,Object]];
      val msg=params.get("message").asInstanceOf[String]
      val identifier = params.get("identifier").asInstanceOf[String]
      println("ID:msg:  "+identifier+": " + msg)
      val response = if( msg == null || identifier == null ) {
        """<rsp stat="fail"><err msg="identifier and message must be specified"/></rsp>"""
      } else {
        ListenHandler.messageAll(identifier,msg);
        """<rsp stat="ok"/>"""
      }

      val os:OutputStream = t.getResponseBody();
      t.getResponseHeaders().set("Content-Type","text/xml")
      t.sendResponseHeaders(200,response.length())
      os.write(response.getBytes())
      os.close()
    } catch {
      case e: Exception => {
        println("Something failed: " + e.getMessage())
      }
    }
  }
}





