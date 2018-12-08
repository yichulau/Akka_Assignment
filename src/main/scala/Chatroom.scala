import java.net.NetworkInterface

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import javafx.scene.{layout => jfsl}
import scalafx.Includes._
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

import scala.collection.JavaConverters._

object Chatroom extends JFXApp{

  var count = -1
  val addresses = (for (inf <- NetworkInterface.getNetworkInterfaces.asScala;
                        add <- inf.getInetAddresses.asScala) yield {
    count = count + 1
    (count -> add)
  }).toMap
  for((i, add) <- addresses){
    println(s"$i = $add")
  }
  println("please select which interface to bind")
  var selection: Int = 0
  do {
    selection = scala.io.StdIn.readInt()
  } while(!(selection >= 0 && selection < addresses.size))

  val ipaddress = addresses(selection)

  val overrideConf = ConfigFactory.parseString(
    s"""
       |akka {
       |  loglevel = "INFO"
       |
 |  actor {
       |    provider = "akka.remote.RemoteActorRefProvider"
       |  }
       |
 |  remote {
       |    enabled-transports = ["akka.remote.netty.tcp"]
       |    netty.tcp {
       |      hostname = "${ipaddress.getHostAddress}"
       |      port = 6000
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
  val system = ActorSystem("chat", myConf)
  //create server actor
  val serverRef = system.actorOf(Props[Server](), "server")
  //create client actor
  val clientRef = system.actorOf(Props[Client], "client")

  val loader = new FXMLLoader(null, NoDependencyResolver)
  loader.load(getClass.getResourceAsStream("window.fxml"))

  val borderPane: jfsl.BorderPane = loader.getRoot[jfsl.BorderPane]
  val controller = loader.getController [WindowsController#Controller]()
  controller.clientActorRef = Option(clientRef)

  stage = new PrimaryStage{
    scene = new Scene(){
      root = borderPane
    }
  }

  stage.onCloseRequest = handle{
    system.terminate()
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
