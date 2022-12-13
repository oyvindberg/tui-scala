package tui
package examples
package paragraph

import tui.crossterm.CrosstermJni
import tui.widgets.paragraph._
import tui.widgets.{Block, Borders}

import java.time.{Duration, Instant}
import scala.math.Ordering.Implicits._

case class App(var scroll: Int = 0) {
  def on_tick(): Unit = {
    scroll += 1
    scroll %= 10
  }
}
object App {
  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val tick_rate = Duration.ofMillis(250)
    val app = new App()

    run_app(terminal, app, tick_rate, jni)
  }

  def run_app(
      terminal: Terminal,
      app: App,
      tick_rate: Duration,
      jni: CrosstermJni
  ): Unit = {
    var last_tick = Instant.now()

    def elapsed = java.time.Duration.between(last_tick, java.time.Instant.now())

    def timeout = {
      val timeout = tick_rate.minus(elapsed)
      new tui.crossterm.Duration(timeout.toSeconds, timeout.getNano)
    }

    while (true) {
      terminal.draw(f => ui(f, app))

      if (jni.poll(timeout)) {
        jni.read() match {
          case key: tui.crossterm.Event.Key =>
            key.keyEvent.code match {
              case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => return
              case _                                                   => ()
            }
          case _ => ()
        }
      }
      if (elapsed >= tick_rate) {
        app.on_tick()
        last_tick = Instant.now()
      }
    }
  }

  def ui(f: Frame, app: App): Unit = {
    // Words made "loooong" to demonstrate line breaking.
    val s = "Veeeeeeeeeeeeeeeery    loooooooooooooooooong   striiiiiiiiiiiiiiiiiiiiiiiiiing.   "
    val long_line = s.repeat(f.size.width / s.length + 4) + "\n"

    val block = Block(style = Style(bg = Some(Color.White), fg = Some(Color.Black)))
    f.render_widget(block, f.size)

    val chunks = Layout(
      direction = Direction.Vertical,
      margin = Margin(5),
      constraints = Array(Constraint.Percentage(25), Constraint.Percentage(25), Constraint.Percentage(25), Constraint.Percentage(25))
    ).split(f.size)

    val text = Array(
      Spans.from("This is a line "),
      Spans.from(Span.styled("This is a line   ", Style.DEFAULT.fg(Color.Red))),
      Spans.from(Span.styled("This is a line", Style.DEFAULT.bg(Color.Blue))),
      Spans.from(Span.styled("This is a longer line", Style.DEFAULT.add_modifier(Modifier.CROSSED_OUT))),
      Spans.from(Span.styled(long_line, Style.DEFAULT.bg(Color.Green))),
      Spans.from(Span.styled("This is a line", Style.DEFAULT.fg(Color.Green).add_modifier(Modifier.ITALIC)))
    )

    def create_block(title: String): Block =
      Block(
        borders = Borders.ALL,
        style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
        title = Some(Spans.from(Span.styled(title, Style.DEFAULT.add_modifier(Modifier.BOLD))))
      )

    val paragraph0 = Paragraph(
      text = Text(text),
      style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
      block = Some(create_block("Left, no wrap")),
      alignment = Alignment.Left
    )
    f.render_widget(paragraph0, chunks(0))
    val paragraph1 = Paragraph(
      text = Text(text),
      style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
      block = Some(create_block("Left, wrap")),
      alignment = Alignment.Left,
      wrap = Some(Wrap(trim = true))
    )
    f.render_widget(paragraph1, chunks(1))

    val paragraph2 = Paragraph(
      text = Text(text),
      style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
      block = Some(create_block("Center, wrap")),
      alignment = Alignment.Center,
      wrap = Some(Wrap(trim = true)),
      scroll = (app.scroll, 0)
    )
    f.render_widget(paragraph2, chunks(2))

    val paragraph3 = Paragraph(
      text = Text(text),
      style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
      block = Some(create_block("Right, wrap")),
      alignment = Alignment.Right,
      wrap = Some(Wrap(trim = true))
    )
    f.render_widget(paragraph3, chunks(3))
  }
}
