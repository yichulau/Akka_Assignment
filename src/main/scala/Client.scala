import Server.Join
import akka.actor.{Actor, ActorRef, Props}
import Client._
import Server._
import Chatroom.system
import akka.pattern.ask
import akka.remote.DisassociatedEvent
import akka.util.Timeout

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scalafx.application.Platform
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Client extends Actor {

  val actorString = "akka.tcp://chat@127.0.0.1:6000/user/server"
  val remoteServerRef = Await.result(context.actorSelection(actorString).resolveOne()(10 seconds), 10 seconds)

  implicit val mytimeout = new Timeout(10 seconds)
  context.system.eventStream.subscribe(self, classOf[akka.remote.DisassociatedEvent])

  def receive = {
    case StartJoin(serverActorRef, name) =>

      val result = remoteServerRef ? Join(self, serverActorRef, name)
      result.foreach {
        case true =>
          Platform.runLater {
            Chatroom.controller.displayJoinStatus("You have \n  joined")
          }
        case false =>
          Platform.runLater {
            Chatroom.controller.displayJoinStatus("Error")
          }
      }
    case StartLeave() =>
      remoteServerRef ! Leave(self)
//      context.become(joined)
    case SendMessage(serverActorRef, messageStr) =>
      val result = remoteServerRef ? NewMessage(self, serverActorRef, messageStr)
    case _ =>
  }

  def joined: Receive = {
    case StartJoin(serverActorRef, x) =>
      Platform.runLater {
        Chatroom.controller.displayJoinStatus("You have \n already joined")
      }
  }
}

object Client {
  var members: Option[Iterable[Person]] = None

//  case class Joined(members: Iterable[Person])
  case class SendMessage(serverActorRef: ActorRef, message: String)
  case class StartJoin(serverActorRef: ActorRef, name: String)
  case class StartLeave()
//  case class Members(member: List[Person])

//  case class SendMessage()

}
