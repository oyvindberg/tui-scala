package tui
package examples
package layout

import tui.crossterm.CrosstermJni
import tui.widgets._

object Main {
  def main(args: Array[String]): Unit = withTerminal((jni, terminal) => run_app(terminal, jni))

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
    val chunks = Layout(
      direction = Direction.Vertical,
      constraints = Array(Constraint.Percentage(10), Constraint.Percentage(80), Constraint.Percentage(10))
    )
      .split(f.size)

    val block0 = Block(title = Some(Spans.from("Block")), borders = Borders.ALL)
    f.render_widget(block0, chunks(0))
    val block1 = Block(title = Some(Spans.from("Block 2")), borders = Borders.ALL)
    f.render_widget(block1, chunks(2))
  }
}
