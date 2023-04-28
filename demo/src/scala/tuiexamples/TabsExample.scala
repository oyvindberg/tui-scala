package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets.tabs.TabsWidget
import tui.widgets.BlockWidget

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

    BlockWidget(style = Style(bg = Some(Color.White), fg = Some(Color.Black))).render(f.size, f.buffer)

    val titles = app.titles
      .map { t =>
        val (first, rest) = t.splitAt(1)
        Spans.from(
          Span.styled(first, Style(fg = Some(Color.Yellow))),
          Span.styled(rest, Style(fg = Some(Color.Green)))
        )
      }
    Layout(direction = Direction.Vertical, margin = Margin(5, 5))(
      Constraint.Length(3) -> TabsWidget(
        titles = titles,
        block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("Tabs")))),
        selected = app.index,
        style = Style(fg = Some(Color.Cyan)),
        highlightStyle = Style(addModifier = Modifier.BOLD, bg = Some(Color.Black))
      ),
      Constraint.Min(0) -> (app.index match {
        case 0 => BlockWidget(title = Some(Spans.nostyle("Inner 0")), borders = Borders.ALL)
        case 1 => BlockWidget(title = Some(Spans.nostyle("Inner 1")), borders = Borders.ALL)
        case 2 => BlockWidget(title = Some(Spans.nostyle("Inner 2")), borders = Borders.ALL)
        case 3 => BlockWidget(title = Some(Spans.nostyle("Inner 3")), borders = Borders.ALL)
        case _ => sys.error("unreachable")
      })
    ).render(f.size, f.buffer)
  }
}
