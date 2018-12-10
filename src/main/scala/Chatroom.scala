import java.net.NetworkInterface

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import javafx.scene.{layout => jfsl}

import Client.StartLeave

import scalafx.Includes._
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scala.collection.JavaConverters._

object Chatroom extends JFXApp{

  val isServer = false
  val myConf = getConfig(isServer)
  val system = ActorSystem("chat", myConf)
//  val serverRef = system.actorOf(Props[Server](), "server")
  //create client actor

  val r = scala.util.Random
  val randomInt = r.nextInt(100)

  val clientRef = system.actorOf(Props[Client], "client"+randomInt.toString)
  val serverRef = system.actorOf(Props[Server], "server")

  val loader = new FXMLLoader(null, NoDependencyResolver)
  loader.load(getClass.getResourceAsStream("window.fxml"))

  val borderPane: jfsl.BorderPane = loader.getRoot[jfsl.BorderPane]
  val controller = loader.getController [WindowsController#Controller]()
  controller.clientActorRef = Option(clientRef)
  controller.serverActorRef = Option(serverRef)

  stage = new PrimaryStage{
    scene = new Scene(){
      root = borderPane
    }
  }

  stage.onCloseRequest = handle{
    controller.clientActorRef foreach {
      ref =>
        ref ! StartLeave()
    }
    system.terminate()
  }

  def getConfig(isServer: Boolean): Config = {
//    var count = -1
//    val addresses = (for (inf <- NetworkInterface.getNetworkInterfaces.asScala;
//                          add <- inf.getInetAddresses.asScala) yield {
//      count = count + 1
//      (count -> add)
//    }).toMap
//    for((i, add) <- addresses){
//      println(s"$i = $add")
//    }
//    println("please select which interface to bind")
//    var selection: Int = 0
//    do {
//      selection = scala.io.StdIn.readInt()
//    } while(!(selection >= 0 && selection < addresses.size))
//
//    val ipaddress = addresses(selection)

    val ipaddress = "127.0.0.1"

    val port = isServer match {
      case true => 6000
      case false => 0
    }

    val overrideConf = ConfigFactory.parseString(
      s"""
         |akka {
         |  loglevel = "INFO"
         |
 |  actor {
         |    provider = "akka.remote.RemoteActorRefProvider"
         |    enable-additional-serialization-bindings = on
         |  }
         |
 |  remote {
         |    enabled-transports = ["akka.remote.netty.tcp"]
         |    netty.tcp {
         |      hostname = "${ipaddress}"
         |      port = ${port}
         |    }
         |
 |    log-sent-messages = on
         |    log-received-messages = on
         |  }
         |
 |}
         |
     """.stripMargin)

    val myConf = overrideConf.withFallback(ConfigFactory.load())
    return myConf
  }

  def showErrorDialog(message: String): Unit = {
    Platform.runLater{
      new Alert(AlertType.Error){
        initOwner(stage)
        title = "System"
        headerText = "System Errors"
        contentText = message
      }.showAndWait()
    }
  }


}
