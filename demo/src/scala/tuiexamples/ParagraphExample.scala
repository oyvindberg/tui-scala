package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets.{BlockWidget, ParagraphWidget}

import java.time.{Duration, Instant}
import scala.math.Ordering.Implicits._

object ParagraphExample {
  case class App(var scroll: Int = 0) {
    def on_tick(): Unit = {
      scroll += 1
      scroll %= 10
    }
  }

  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val tick_rate = Duration.ofMillis(250)
    val app = App()

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

    val block = BlockWidget(style = Style(bg = Some(Color.White), fg = Some(Color.Black)))
    f.renderWidget(block, f.size)

    val text = Text.fromSpans(
      Spans.nostyle("This is a line "),
      Spans.styled("This is a line   ", Style.DEFAULT.fg(Color.Red)),
      Spans.styled("This is a line", Style.DEFAULT.bg(Color.Blue)),
      Spans.styled("This is a longer line", Style.DEFAULT.addModifier(Modifier.CROSSED_OUT)),
      Spans.styled(long_line, Style.DEFAULT.bg(Color.Green)),
      Spans.styled("This is a line", Style.DEFAULT.fg(Color.Green).addModifier(Modifier.ITALIC))
    )

    def create_block(title: String): BlockWidget =
      BlockWidget(
        borders = Borders.ALL,
        style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
        title = Some(Spans.from(Span.styled(title, Style.DEFAULT.addModifier(Modifier.BOLD))))
      )

    Layout(direction = Direction.Vertical, margin = Margin(5))(
      Constraint.Percentage(25) -> ParagraphWidget(
        text = text,
        style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
        block = Some(create_block("Left, no wrap")),
        alignment = Alignment.Left
      ),
      Constraint.Percentage(25) -> ParagraphWidget(
        text = text,
        style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
        block = Some(create_block("Left, wrap")),
        alignment = Alignment.Left,
        wrap = Some(ParagraphWidget.Wrap(trim = true))
      ),
      Constraint.Percentage(25) -> ParagraphWidget(
        text = text,
        style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
        block = Some(create_block("Center, wrap")),
        alignment = Alignment.Center,
        wrap = Some(ParagraphWidget.Wrap(trim = true)),
        scroll = (app.scroll, 0)
      ),
      Constraint.Percentage(25) -> ParagraphWidget(
        text = text,
        style = Style(bg = Some(Color.White), fg = Some(Color.Black)),
        block = Some(create_block("Right, wrap")),
        alignment = Alignment.Right,
        wrap = Some(ParagraphWidget.Wrap(trim = true))
      )
    ).render(f.size, f.buffer)
  }
}
