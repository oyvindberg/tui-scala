package tui
package examples
package tabs

import tui.backend.CrosstermBackend
import tui.crossterm.{Command, CrosstermJni}
import tui.layout.{Constraint, Direction, Layout, Margin}
import tui.terminal.{Frame, Terminal}
import tui.text.{Span, Spans}
import tui.widgets.tabs.Tabs
import tui.widgets.{Block, Borders}

case class App(
    titles: Array[String],
    var index: Int = 0
) {

  def next(): Unit =
    index = (index + 1) % titles.length

  def previous(): Unit =
    if (index > 0) {
      index -= 1;
    } else {
      index = titles.length - 1;
    }
}

object Main {
  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val app = App(titles = Array("Tab0", "Tab1", "Tab2", "Tab3"));
    run_app(terminal, app, jni);
  }

  def run_app(terminal: Terminal, app: App, jni: CrosstermJni): Unit =
    while (true) {
      terminal.draw(f => ui(f, app));

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
    ).split(f.size);

    val block = Block(style = Style(bg = Some(Color.White), fg = Some(Color.Black)))
    f.render_widget(block, f.size);
    val titles = app.titles
      .map { t =>
        val (first, rest) = t.splitAt(1);
        Spans.from(
          Array(
            Span.styled(first, Style(fg = Some(Color.Yellow))),
            Span.styled(rest, Style(fg = Some(Color.Green)))
          )
        )
      }

    val tabs = Tabs(
      titles = titles,
      block = Some(Block(borders = Borders.ALL, title = Some(Spans.from("Tabs")))),
      selected = app.index,
      style = Style(fg = Some(Color.Cyan)),
      highlight_style = Style(add_modifier = Modifier.BOLD, bg = Some(Color.Black))
    )
    f.render_widget(tabs, chunks(0));
    val inner = app.index match {
      case 0 => Block(title = Some(Spans.from("Inner 0")), borders = Borders.ALL)
      case 1 => Block(title = Some(Spans.from("Inner 1")), borders = Borders.ALL)
      case 2 => Block(title = Some(Spans.from("Inner 2")), borders = Borders.ALL)
      case 3 => Block(title = Some(Spans.from("Inner 3")), borders = Borders.ALL)
      case _ => sys.error("unreachable")
    };
    f.render_widget(inner, chunks(1));
  }
}
