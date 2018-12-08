import Client.StartJoin
import akka.actor.ActorRef
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Label, ListView, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class WindowsController (private val serverIP: TextField,
                         private val port: TextField,
                         private val txtName: TextField,
                         private val friends: ListView[Person],
                         private val joinStatusLabel: Label,
                        ){

  var clientActorRef: Option[ActorRef] = None

  Server.players.onChange((x, y) => {
    Platform.runLater {
      friends.items = ObservableBuffer(x.toList)
    }
  })

  def handleJoin(actionEvent: ActionEvent) {
    //ask the client actor to joined the server based on IP
    clientActorRef foreach { ref =>
      ref ! StartJoin(serverIP.text.value, port.text.value, txtName.text.value)
    }
  }
    def displayJoinStatus(text: String): Unit = {
      joinStatusLabel.text = text
    }

    def displayMemberList(x: List[Person]): Unit ={
      friends.items = ObservableBuffer(x)
    }

}
