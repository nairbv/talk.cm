package net.brianvaughan.scala.chat;
import com.sun.net.httpserver._

import java.io._
import java.net._
import scala.actors._
import scala.actors.Actor._
import scala.xml._

import java.security.SecureRandom
import java.math.BigInteger



class CreateContextHandler(val server:HttpServer,
                           val filter:net.brianvaughan.http.ParameterFilter,
                           val random:SecureRandom) 
  extends HttpHandler 
{
  def handle(t:HttpExchange) = {

    try {
      val request = t.getRequestURI().getPath()
      val regex = "\\.".r
      if( request != "/" ) {
        (new StaticHandler()).handle(t)
      } else {
        println("handling request with createcontexthandler")
        val identifier = new BigInteger(40,random).toString(32)
        val path = "/c/"+identifier
        println("new path generated: " + path)
//        val context = server.createContext(path, new IndexOutputHandler(identifier))
//        context.getFilters().add(filter)

        val os:OutputStream = t.getResponseBody();
        //t.getResponseHeaders().set("Content-Type","text/xml")
        t.getResponseHeaders().set("Location",path)
        t.sendResponseHeaders(302, -1)
//      os.write(response.getBytes())
        os.close()
      }
    } catch {
      case e: Exception => {
        println("Something failed: " + e.getMessage())
      }
    }
  }
}

class IndexOutputHandler extends HttpHandler
{
  //use japid instead here?
  val templateRegex = "\\{%THE_IDENTIFIER%\\}".r
  val getIdRegex = "/c/(.*)".r

  val index = io.Source.fromFile("/var/www/chat/htdocs/index.html").mkString

  def handle(t:HttpExchange) = {
    println("handling request with indexoutputhandler")
    try {
      val request = t.getRequestURI().getPath()
      if( getIdRegex.findFirstIn(request).isEmpty ) {
        println("404 from index handler: " + request)
        t.sendResponseHeaders(404,-1)
      } else {
        val getIdRegex(identifier) = request
        val response = templateRegex.replaceAllIn(index,identifier)
        val os:OutputStream = t.getResponseBody();
        t.getResponseHeaders().set("Content-Type","text/html")
        t.sendResponseHeaders(200,response.length())
        os.write(response.getBytes())
        os.close()
      }
    } catch {
      case e: Exception => {
        println("something failed: " + e.getMessage())
      }
    }
  }
}

class StaticHandler extends HttpHandler
{

  val basePath = "/var/www/chat/htdocs/"
  val findAttackRegex = "\\.\\.".r
  val errorPage = "404.html"

  def fourOfour(t:HttpExchange,path:String) {
    println("404 from : " + path)
    t.sendResponseHeaders(404,-1)
  }

  def handle(t:HttpExchange) = {
    println("handling request with static handler")
    try {
      var responseCode = 200
      var request = t.getRequestURI().getPath()
      if( ! findAttackRegex.findFirstIn(request).isEmpty ) {
        responseCode = 404
        request = errorPage
      }
      var fullPath = basePath + request
      var file = new File(fullPath)
      if( ! file.exists() ) {
        responseCode = 404
        request = errorPage
        fullPath = basePath + errorPage
        file = new File(fullPath)
      }

      val os:OutputStream = t.getResponseBody();
      t.getResponseHeaders().set("Content-Type", 
          MimeManager.getMimeType(MimeManager.getExtension(request)) )
      t.sendResponseHeaders(responseCode,file.length())
      val is = new FileInputStream(file)
      var reading = true
      while(reading) {
        is.read() match {
          case -1 => reading=false
          case c => os.write(c)
        }
      }
      os.flush()
      os.close()
      is.close()
    } catch {
      case e: Exception => {
        println("something failed: " + e.getMessage())
      }
    }
  }
}


object MimeManager {
  val typesFile = io.Source.fromFile("/etc/mime.types").getLines
  val types = 
        typesFile.filter(a => !"\t".r.findFirstIn(a).isEmpty).foldLeft(Map[String,String]()){ 
          case (map,s) => {
            val both = s.split("\t+"); 
            map.updated(both(1),both(0)) 
          }
        }
  def getMimeType(extension:String) = {
    types.getOrElse(extension, "text/html")
  }
  def getExtension(filename:String) = filename.split("\\.").last
}


