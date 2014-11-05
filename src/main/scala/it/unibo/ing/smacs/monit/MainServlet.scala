package it.unibo.ing.smacs.monit



import java.io.{IOException, InputStreamReader, BufferedReader}
import it.unibo.ing.smacs.monit.model.MonitInfo

import scala.collection.JavaConversions._
import scala.collection.immutable.Stream.Empty

import org.scalatra._

import spray.json._

import it.unibo.ing.smacs.monit.parsers.MonitOutputParser
import it.unibo.ing.smacs.monit.model.JsonConversions._

class MainServlet extends MonitrestfulinterfaceStack {

  before() {
    contentType = "json"
  }

  get("/"){
    try {
      val p = Runtime.getRuntime().exec("/var/vcap/bosh/bin/monit status")
      val stdInput = new BufferedReader(new InputStreamReader(p.getInputStream))
      val stdError = new BufferedReader(new InputStreamReader(p.getErrorStream))
      var s = ""
      for (line <- stdInput.lines().iterator())
        s += line + "\n"
      if (stdError.lines().iterator().hasNext)
        JsObject("error" -> JsString("System Error")).toString
      else {
        import DefaultJsonProtocol._
        MonitOutputParser.parseOption(s) match{
          case Some(x : Seq[MonitInfo]) => {
            x.toJson
          }
          case None => JsObject("error" -> JsString("Parse Error")).toString
        }
      }
    }
    catch{
      case e : IOException => JsObject("error" -> JsString("I/O Error")).toString
    }
  }
  
}