import Chatroom.myConf
import akka.actor.{ActorSystem, Props}

object ChatroomServer extends App {
  val isServer = true
  val myConf = Chatroom.getConfig(isServer)
  val system = ActorSystem("chat", myConf)
  //create server actor
  val serverRef = system.actorOf(Props[Server](), "server")
}
