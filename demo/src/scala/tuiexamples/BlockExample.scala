package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets.BlockWidget

object BlockExample {
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
    // Wrapping block for a group
    // Just draw the block and the group on the same area and build the group
    // with at least a margin of 1
    val size = f.size

    // Surrounding block
    val block0 = BlockWidget(
      borders = Borders.ALL,
      title = Some(Spans.nostyle("Main block with round corners")),
      titleAlignment = Alignment.Center,
      borderType = BlockWidget.BorderType.Rounded
    )
    f.renderWidget(block0, size)

    val layout = Layout(direction = Direction.Vertical, margin = Margin(4))(
      Layout(direction = Direction.Horizontal)(
        // Top left inner block with green background
        BlockWidget(
          title = Some(
            Spans.from(
              Span.styled("With", Style.DEFAULT.fg(Color.Yellow)),
              Span.nostyle(" background")
            )
          ),
          style = Style.DEFAULT.bg(Color.Green)
        ),
        // Top right inner block with styled title aligned to the right
        BlockWidget(
          title = Some(Spans.from(Span.styled("Styled title", Style(fg = Some(Color.White), bg = Some(Color.Red), addModifier = Modifier.BOLD)))),
          titleAlignment = Alignment.Right
        )
      ),
      Layout(direction = Direction.Horizontal)(
        BlockWidget(title = Some(Spans.nostyle("With borders")), borders = Borders.ALL),
        BlockWidget(
          title = Some(Spans.nostyle("With styled borders and doubled borders")),
          borderStyle = Style.DEFAULT.fg(Color.Cyan),
          borders = Borders.LEFT | Borders.RIGHT,
          borderType = BlockWidget.BorderType.Double
        )
      )
    )
    layout.render(f.size, f.buffer)
  }
}
