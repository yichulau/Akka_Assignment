import Client.{SendMessage, StartJoin}
import akka.actor.ActorRef
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Label, ListView, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class WindowsController (
                         private val txtName: TextField,
                         private val friendList: ListView[Person],
                         private val joinStatusLabel: Label,
                         private val ChatMessage: TextField,
                         private val ChatBox: ListView[String],
                        ){

  var clientActorRef: Option[ActorRef] = None
  var serverActorRef: Option[ActorRef] = None

  def handleSend(actionEvent: ActionEvent): Unit = {
  clientActorRef foreach{
    ref =>
      serverActorRef foreach {
        serverRef =>
          ref ! SendMessage(serverRef, ChatMessage.text.value)
      }
  }
  }

  def handleJoin(actionEvent: ActionEvent) {
    //ask the client actor to joined the server based on IP
    clientActorRef foreach { ref =>
      serverActorRef foreach {
        serverRef =>
          ref ! StartJoin(serverRef, txtName.text.value)
      }
    }
  }
    def displayJoinStatus(text: String): Unit = {
      joinStatusLabel.text = text
    }

    def displayMemberList(x: List[Person]): Unit ={
      friendList.items = ObservableBuffer(x)
    }

  def displayMessagesList(messages: List[Message]): Unit = {
    val messageTexts: List[String] = messages.map {
      message => message.text
    }
    ChatBox.items = ObservableBuffer(messageTexts)
  }

}
