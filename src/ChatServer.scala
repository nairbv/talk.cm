
package net.brianvaughan.scala.chat;

import com.sun.net.httpserver._
import java.io._
import java.net._
import scala.actors._
import scala.actors.Actor._
import scala.xml._
import net.brianvaughan.http.ParameterFilter

import java.security.SecureRandom


/**"main" method.  create the web server, add handlers, and start.*/
object ChatServer extends App {

  val server:HttpServer = HttpServer.create(new InetSocketAddress(8081),0)



  val context1 = server.createContext("/message",new MessageHandler())
  context1.getFilters().add(new ParameterFilter())
  val context2 = server.createContext("/listen",new ListenHandler())
  context2.getFilters().add(new ParameterFilter())


  val context3 = server.createContext("/", 
        new CreateContextHandler(server,new ParameterFilter(),new SecureRandom()))
  val context4 = server.createContext("/c/", new IndexOutputHandler())


  server.setExecutor(null)
  server.start()



}

