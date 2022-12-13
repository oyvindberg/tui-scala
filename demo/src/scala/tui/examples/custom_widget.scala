package tui
package examples
package custom_widget

import tui.buffer.Buffer
import tui.crossterm.CrosstermJni
import tui.layout.Rect
import tui.terminal.{Frame, Terminal}
import tui.widgets._

case class Label(text: String) extends Widget {
  override def render(area: Rect, buf: Buffer): Unit =
    buf.set_string(area.left, area.top, text, Style.DEFAULT);
}

object Main {
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
    val size = f.size;
    val label = Label(text = "Test");
    f.render_widget(label, size);
  }
}
