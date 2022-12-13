package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets.BlockWidget
import tui.widgets.tabs.TabsWidget

object TabsExample {
  case class App(
      titles: Array[String],
      var index: Int = 0
  ) {

    def next(): Unit =
      index = (index + 1) % titles.length

    def previous(): Unit =
      if (index > 0) {
        index -= 1
      } else {
        index = titles.length - 1
      }
  }
  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val app = App(titles = Array("Tab0", "Tab1", "Tab2", "Tab3"))
    run_app(terminal, app, jni);
  }

  def run_app(terminal: Terminal, app: App, jni: CrosstermJni): Unit =
    while (true) {
      terminal.draw(f => ui(f, app))

      jni.read() match {
        case key: tui.crossterm.Event.Key =>
          key.keyEvent.code match {
            case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => return
            case _: tui.crossterm.KeyCode.Right                      => app.next()
            case _: tui.crossterm.KeyCode.Left                       => app.previous()
            case _                                                   => ()
          }
        case _ => ()
      }
    }

  def ui(f: Frame, app: App): Unit = {
    val chunks = Layout(
      direction = Direction.Vertical,
      margin = Margin(5, 5),
      constraints = Array(Constraint.Length(3), Constraint.Min(0))
    ).split(f.size)

    val style = Style(bg = Some(Color.White), fg = Some(Color.Black))

    f.buffer.fill(f.size, Cell.Empty.withStyle(style))

    val titles = app.titles
      .map { t =>
        val (first, rest) = t.splitAt(1)
        Spans.from(
          Span.styled(first, Style(fg = Some(Color.Yellow))),
          Span.styled(rest, Style(fg = Some(Color.Green)))
        )
      }

    val tabs = TabsWidget(
      titles = titles,
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("Tabs")))),
      selected = app.index,
      style = style.fg(Color.Cyan),
      highlight_style = Style(add_modifier = Modifier.BOLD, bg = Some(Color.Black))
    )
    f.render_widget(tabs, chunks(0))
    val inner = app.index match {
      case 0 => BlockWidget(title = Some(Spans.nostyle("Inner 0")), borders = Borders.ALL, style = style)
      case 1 => BlockWidget(title = Some(Spans.nostyle("Inner 1")), borders = Borders.ALL, style = style)
      case 2 => BlockWidget(title = Some(Spans.nostyle("Inner 2")), borders = Borders.ALL, style = style)
      case 3 => BlockWidget(title = Some(Spans.nostyle("Inner 3")), borders = Borders.ALL, style = style)
      case _ => sys.error("unreachable")
    }
    f.render_widget(inner, chunks(1))
  }
}
