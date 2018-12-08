import Server.Join
import akka.actor.{Actor, ActorRef}
import Client._
import Server._
import Chatroom.system
import akka.pattern.ask
import akka.remote.DisassociatedEvent
import akka.util.Timeout
import scalafx.application.Platform

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Client extends Actor {
  implicit val mytimeout = new Timeout(10 seconds)
  context.system.eventStream.subscribe(self, classOf[akka.remote.DisassociatedEvent])

  def receive = {
    case StartJoin(server, port, name) =>
      val serverRef = context.actorSelection(s"akka.tcp://ball@$server:$port/user/server")
      val result = serverRef ? Join(self, name)
      result.foreach(x => {
        if (x == Client.Joined) {
          Platform.runLater {
            Chatroom.controller.displayJoinStatus("You have Joined")
          }
          context.become(joined)
        } else {
          Platform.runLater {
            Chatroom.controller.displayJoinStatus("Error")
          }
        }
      })
    case _ =>
  }

  def joined: Receive = {
    case StartJoin(x, y, z) =>
      Platform.runLater {
        Chatroom.controller.displayJoinStatus("You have already Joined")
      }

    case Members(list) =>
      Client.memberList = Option(list.filterNot(x => x.ref == self))
      Client.memberIter = Option(Iterator.continually(Client.memberList.get.map(_.ref)).flatten)
      Platform.runLater {
        Chatroom.controller.displayMemberList(memberList.getOrElse(List()).toList)
      }
  }
}

object Client {
  var memberList: Option[Iterable[Person]] = None
  var memberIter: Option[Iterator[ActorRef]] = None
  case object Joined
  case class StartJoin(serverIp: String, port: String,name: String)
  case class Members(member:Iterable[Person])

}
