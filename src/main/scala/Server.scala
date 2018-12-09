import Chatroom.myConf
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scalafx.collections.ObservableHashSet
import Server.{Join, Members, Messages, NewMessage}
import akka.pattern.ask
import akka.remote.DisassociatedEvent
import akka.stream.Server
import akka.util.Timeout

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scalafx.application.Platform

case class Person(clientRef: ActorRef, serverRef: ActorRef, name: String){
  override def toString: String ={
    name
  }
}

case class Message(sender: Person, text: String, timestamp: Long) {
  override def toString: String = {
    sender.name.toString() + timestamp.toString
  }
}

class Server extends Actor{
  implicit val timeout: Timeout = Timeout(10 second)
  context.system.eventStream.subscribe(self, classOf[akka.remote.DisassociatedEvent])

  def receive = {
    // Server side code
    case Join(clientRef, serverRef, name) =>
      val person = new Person(clientRef, serverRef, name)
      Server.members.append(person)
      sender ! true

      Server.members.foreach { member =>
        member.serverRef ! Server.Members(Server.members)
      }
      // Client side code
    case Members(members) =>
      Platform.runLater {
        Chatroom.controller.displayMemberList(members.toList)
      }
    case NewMessage(clientRef,serverRef,messageStr) =>
      val timestamp: Long = System.currentTimeMillis / 1000

      val person = Server.members.find {
        person => person.clientRef == clientRef
      }
      person.foreach {
        p =>
          val message = new Message(p, messageStr, timestamp)
          Server.messages.append(message)

          Server.members.foreach { member =>
            member.serverRef ! Server.Messages(Server.messages)
          }
      }

    case Messages(messages) =>
      Platform.runLater {
        Chatroom.controller.displayMessagesList(messages.toList)

      }
  }

  def started: Receive = {
    case DisassociatedEvent(local,remote,_) =>
      context.unbecome()
      Server.members.clear()
    case _=>

  }
}

object Server{
  var members: ArrayBuffer[Person] = new ArrayBuffer[Person]()
  var messages: ArrayBuffer[Message] = new ArrayBuffer[Message]()

  case class Join(myRef: ActorRef, serverActorRef: ActorRef, name:String)
  case class Members(members: Iterable[Person])

  case class NewMessage(myRef: ActorRef, serverActorRef: ActorRef, message: String)
  case class Messages(messages: Iterable[Message])
}