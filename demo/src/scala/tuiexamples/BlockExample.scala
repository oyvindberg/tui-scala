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

    val chunks = Layout(
      direction = Direction.Vertical,
      margin = Margin(4),
      constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))
    )
      .split(f.size)

    // Top two inner blocks
    val top_chunks = Layout(
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))
    )
      .split(chunks(0))

    // Top left inner block with green background
    val block_top0 = BlockWidget(
      title = Some(
        Spans.from(
          Span.styled("With", Style.DEFAULT.fg(Color.Yellow)),
          Span.nostyle(" background")
        )
      ),
      style = Style.DEFAULT.bg(Color.Green)
    )
    f.renderWidget(block_top0, top_chunks(0))

    // Top right inner block with styled title aligned to the right
    val block_top1 = BlockWidget(
      title = Some(Spans.from(Span.styled("Styled title", Style(fg = Some(Color.White), bg = Some(Color.Red), addModifier = Modifier.BOLD)))),
      titleAlignment = Alignment.Right
    )
    f.renderWidget(block_top1, top_chunks(1))

    // Bottom two inner blocks
    val bottom_chunks = Layout(
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))
    )
      .split(chunks(1))

    // Bottom left block with all default borders
    val block_bottom_0 = BlockWidget(title = Some(Spans.nostyle("With borders")), borders = Borders.ALL)
    f.renderWidget(block_bottom_0, bottom_chunks(0))

    // Bottom right block with styled left and right border
    val block_bottom_1 = BlockWidget(
      title = Some(Spans.nostyle("With styled borders and doubled borders")),
      borderStyle = Style.DEFAULT.fg(Color.Cyan),
      borders = Borders.LEFT | Borders.RIGHT,
      borderType = BlockWidget.BorderType.Double
    )
    f.renderWidget(block_bottom_1, bottom_chunks(1))
  }
}
