package tuiexamples

import tui._
import tui.crossterm.CrosstermJni

object CustomWidgetExample {
  case class Label(text: String) extends Widget {
    override def render(area: Rect, buf: Buffer): Unit = {
      buf.set_string(area.left, area.top, text, Style.DEFAULT)
      ()
    }
  }

  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    run_app(terminal, jni)
  }

  def run_app(terminal: Terminal, jni: CrosstermJni): Unit =
    while (true) {
      terminal.draw(f => ui(f))
      jni.read() match {
        case key: tui.crossterm.Event.Key =>
          key.keyEvent.code match {
            case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => return
            case _                                                   => ()
          }
        case _ => ()
      }
    }

  def ui(f: Frame): Unit = {
    val size = f.size
    val label = Label(text = "Test")
    f.render_widget(label, size)
  }
}
