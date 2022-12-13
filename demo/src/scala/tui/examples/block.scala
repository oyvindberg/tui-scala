package tui
package examples
package block

import tui.layout._
import tui.terminal.Frame
import tui.text.{Span, Spans}
import tui.widgets.{Block, BorderType, Borders}

object Main {
  def main(args: Array[String]): Unit = withTerminal((jni, terminal) => run_app(terminal, jni))

  def run_app(
      terminal: tui.terminal.Terminal,
      jni: tui.crossterm.CrosstermJni
  ): Unit =
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
    val block0 = Block(
      borders = Borders.ALL,
      title = Some(Spans.from("Main block with round corners")),
      title_alignment = Alignment.Center,
      border_type = BorderType.Rounded
    )
    f.render_widget(block0, size)

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
    val block_top0 = Block(
      title = Some(
        Spans.from(
          Array(
            Span.styled("With", Style.DEFAULT.fg(Color.Yellow)),
            Span.from(" background")
          )
        )
      ),
      style = Style.DEFAULT.bg(Color.Green)
    )
    f.render_widget(block_top0, top_chunks(0))

    // Top right inner block with styled title aligned to the right
    val block_top1 = Block(
      title = Some(Spans.from(Span.styled("Styled title", Style(fg = Some(Color.White), bg = Some(Color.Red), add_modifier = Modifier.BOLD)))),
      title_alignment = Alignment.Right
    )
    f.render_widget(block_top1, top_chunks(1))

    // Bottom two inner blocks
    val bottom_chunks = Layout(
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))
    )
      .split(chunks(1))

    // Bottom left block with all default borders
    val block_bottom_0 = Block(title = Some(Spans.from("With borders")), borders = Borders.ALL)
    f.render_widget(block_bottom_0, bottom_chunks(0))

    // Bottom right block with styled left and right border
    val block_bottom_1 = Block(
      title = Some(Spans.from("With styled borders and doubled borders")),
      border_style = Style.DEFAULT.fg(Color.Cyan),
      borders = Borders.LEFT | Borders.RIGHT,
      border_type = BorderType.Double
    )
    f.render_widget(block_bottom_1, bottom_chunks(1))
  }
}
