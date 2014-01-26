package code.comet

import code.model.User
import net.liftweb.http.{SHtml, CometActor}
import net.liftweb.util.Helpers
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{OnLoad, Script, SetHtml}
import scala.xml.Text
import net.liftweb.http.js.JsCmds
import net.liftweb.common.Full
import code.util.Coords
import Helpers._


class ChatUser extends User with CometActor {
  var message = ""
  lazy val id = Helpers.nextFuncName

  override def lowPriority = {
    case NewConnection(user) => partialUpdate(JsRaw("earth.createConnection('"+user.id+"',{latitude:"+user.latToString+",longitude:"+user.longToString+"}, "+user.colorToHexInt+")").cmd)
    case ClosedConnection(user) => partialUpdate(JsRaw("earth.removeConnection('"+user.id+"')").cmd)
    case Message(user, msg) => partialUpdate(JsRaw("earth.newMessage('"+user.id+"','"+msg+"')").cmd)
    case _ => //no no
  }

  def render = {
    ChatServer ! ClosedConnection(this)
    val exp = SHtml.ajaxInvoke{() => SetHtml("userform", userForm) }

    val chatNode = ("#message" #> SHtml.ajaxText(message, str => {
      message = str
      JsCmds.Noop
    }) &
      "#sendMessage" #> SHtml.ajaxButton("Send", () => {
        ChatServer ! Message(this, message)
        JsCmds.Noop
      }))(defaultHtml)
    chatNode ++ <div syle="z-index:999999">{Script(OnLoad(exp.cmd))}</div>
  }

  private def userForm = {
    SHtml.a(() => {
      this.color = "#FFFFFF"
      this.coords = Full(Coords(-9.449062,-54.843750 ))
      ChatServer ! NewConnection(this)
      JsCmds.JsHideId("userform")
    }, Text("click me"))
  }
}