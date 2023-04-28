package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets._

object PopupExample {
  case class App(
      var show_popup: Boolean = false
  )

  def main(args: Array[String]): Unit =
    withTerminal { (jni, terminal) =>
      val app = App()
      run_app(terminal, app, jni);
    }

  def run_app(terminal: Terminal, app: App, jni: CrosstermJni): Unit =
    while (true) {
      terminal.draw(f => ui(f, app))

      jni.read() match {
        case key: tui.crossterm.Event.Key =>
          key.keyEvent.code match {
            case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => return
            case char: tui.crossterm.KeyCode.Char if char.c() == 'p' => app.show_popup = !app.show_popup
            case _                                                   => ()
          }
        case _ => ()
      }
    }

  def ui(f: Frame, app: App): Unit = {
    val text = if (app.show_popup) "Press p to close the popup" else "Press p to show the popup"
    Layout
      .detailed()(
        Constraint.Percentage(20) -> ParagraphWidget(
          text = Text.from(Span.styled(text, Style(addModifier = Modifier.SLOW_BLINK))),
          alignment = Alignment.Center,
          wrap = Some(ParagraphWidget.Wrap(trim = true))
        ),
        Constraint.Percentage(80) -> BlockWidget.noChildren(
          title = Some(Spans.nostyle("Content")),
          borders = Borders.ALL,
          style = Style.DEFAULT.bg(Color.Blue)
        )
      )
      .render(f.size, f.buffer)

    if (app.show_popup) {
      val block = BlockWidget.noChildren(title = Some(Spans.nostyle("Popup")), borders = Borders.ALL)
      val area = centered_rect(60, 20, f.size)
      f.renderWidget(ClearWidget, area); // this clears out the background
      f.renderWidget(block, area)
    }
  }

/// helper function to create a centered rect using up certain percentage of the available rect `r`
  def centered_rect(percent_x: Int, percent_y: Int, r: Rect): Rect = {
    val popup_layout = Layout.cached(
      area = r,
      constraints = Array(
        Constraint.Percentage((100 - percent_y) / 2),
        Constraint.Percentage(percent_y),
        Constraint.Percentage((100 - percent_y) / 2)
      ),
      margin = Margin.None,
      direction = Direction.Vertical,
      expandToFill = false
    )

    Layout.cached(
      area = popup_layout(1),
      constraints = Array(
        Constraint.Percentage((100 - percent_x) / 2),
        Constraint.Percentage(percent_x),
        Constraint.Percentage((100 - percent_x) / 2)
      ),
      margin = Margin.None,
      direction = Direction.Horizontal,
      expandToFill = false
    )(1)
  }
}
