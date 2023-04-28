package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets._

object LayoutExample {
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

  def ui(f: Frame): Unit =
    Layout
      .detailed(direction = Direction.Vertical)(
        Constraint.Percentage(10) -> BlockWidget.noChildren(title = Some(Spans.nostyle("Block")), borders = Borders.ALL),
        Constraint.Percentage(80) -> Widget.Empty,
        Constraint.Percentage(10) -> BlockWidget.noChildren(title = Some(Spans.nostyle("Block 2")), borders = Borders.ALL)
      )
      .render(f.size, f.buffer)
}
