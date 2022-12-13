package tui
package examples
package popup

import tui.widgets._
import tui.widgets.clear.Clear
import tui.widgets.paragraph.{Paragraph, Wrap}

case class App(
    var show_popup: Boolean = false
)

object Main {
  def main(args: Array[String]): Unit =
    withTerminal { (jni, terminal) =>
      val app = App();
      run_app(terminal, app, jni);

    }
  def run_app(
      terminal: Terminal,
      app: App,
      jni: tui.crossterm.CrosstermJni
  ): Unit =
    while (true) {
      terminal.draw(f => ui(f, app));

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
    val size = f.size

    val chunks = Layout(constraints = Array(Constraint.Percentage(20), Constraint.Percentage(80)))
      .split(size)

    val text = if (app.show_popup) { "Press p to close the popup" }
    else { "Press p to show the popup" };
    val paragraph = Paragraph(
      text = Text.from(Span.styled(text, Style(add_modifier = Modifier.SLOW_BLINK))),
      alignment = Alignment.Center,
      wrap = Some(Wrap(trim = true))
    )
    f.render_widget(paragraph, chunks(0));

    val block = Block(
      title = Some(Spans.from("Content")),
      borders = Borders.ALL,
      style = Style.DEFAULT.bg(Color.Blue)
    )

    f.render_widget(block, chunks(1));

    if (app.show_popup) {
      val block = Block(title = Some(Spans.from("Popup")), borders = Borders.ALL)
      val area = centered_rect(60, 20, size);
      f.render_widget(Clear, area); // this clears out the background
      f.render_widget(block, area);
    }
  }

/// helper function to create a centered rect using up certain percentage of the available rect `r`
  def centered_rect(percent_x: Int, percent_y: Int, r: Rect): Rect = {
    val popup_layout = Layout(
      direction = Direction.Vertical,
      constraints = Array(
        Constraint.Percentage((100 - percent_y) / 2),
        Constraint.Percentage(percent_y),
        Constraint.Percentage((100 - percent_y) / 2)
      )
    )
      .split(r);

    Layout(
      direction = Direction.Horizontal,
      constraints = Array(
        Constraint.Percentage((100 - percent_x) / 2),
        Constraint.Percentage(percent_x),
        Constraint.Percentage((100 - percent_x) / 2)
      )
    )
      .split(popup_layout(1))(1)
  }
}
