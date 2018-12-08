import Client.Joined
import akka.actor.{Actor, ActorRef}
import scalafx.collections.ObservableHashSet
import Server.Join
import akka.pattern.ask
import akka.remote.DisassociatedEvent
import akka.stream.Server
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class Person(ref: ActorRef, name: String){
  override def toString: String ={
    name
  }
}

class Server extends Actor{
  implicit val timeout: Timeout = Timeout(10 second)
  context.system.eventStream.subscribe(self, classOf[akka.remote.DisassociatedEvent])
  def receive = {
    case Join(my,name) =>
      Server.players += new Person(my,name)
      sender() ! Joined

  }

  def started: Receive = {
    case DisassociatedEvent(local,remote,_) =>
      context.unbecome()
      Server.players.clear()
    case _=>

  }
}

object Server{
  val players = new ObservableHashSet[Person]
  case class Join(myRef: ActorRef, name:String)
}