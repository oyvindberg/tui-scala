package tui
package examples
package gauge

import tui.crossterm.CrosstermJni
import tui.layout.{Constraint, Direction, Layout, Margin}
import tui.terminal.{Frame, Terminal}
import tui.text.{Span, Spans}
import tui.widgets.{Block, Borders, Gauge, Ratio}

import java.time.{Duration, Instant}
import scala.math.Ordering.Implicits._

case class App(
    var progress1: Int = 0,
    var progress2: Int = 0,
    var progress3: Double = 0.45,
    var progress4: Int = 0
) {

  def on_tick(): Unit = {
    progress1 += 1
    if (progress1 > 100) {
      progress1 = 0
    }
    progress2 += 2
    if (progress2 > 100) {
      progress2 = 0
    }
    progress3 += 0.001
    if (progress3 > 1.0) {
      progress3 = 0.0
    }
    progress4 += 1
    if (progress4 > 100) {
      progress4 = 0
    }
  }
}

object Main {
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
    val chunks = Layout(
      direction = Direction.Vertical,
      margin = Margin(2),
      constraints = Array(Constraint.Percentage(25), Constraint.Percentage(25), Constraint.Percentage(25), Constraint.Percentage(25))
    )
      .split(f.size)

    val gauge0 = Gauge(
      block = Some(Block(title = Some(Spans.from("Gauge1")), borders = Borders.ALL)),
      gauge_style = Style(fg = Some(Color.Yellow)),
      ratio = Ratio.percent(app.progress1)
    )
    f.render_widget(gauge0, chunks(0))

    val gauge1 = Gauge(
      block = Some(Block(title = Some(Spans.from("Gauge2")), borders = Borders.ALL)),
      gauge_style = Style(fg = Some(Color.Magenta), bg = Some(Color.Green)),
      ratio = Ratio.percent(app.progress2),
      label = Some(Span.from(s"${app.progress2}/100"))
    )
    f.render_widget(gauge1, chunks(1))

    val gauge2 = Gauge(
      block = Some(Block(title = Some(Spans.from("Gauge3")), borders = Borders.ALL)),
      gauge_style = Style(fg = Some(Color.Yellow)),
      ratio = Ratio(app.progress3),
      label = Some(
        Span.styled(
          "%.2f".format(app.progress3 * 100.0),
          Style(fg = Some(Color.Red), add_modifier = Modifier.ITALIC | Modifier.BOLD)
        )
      ),
      use_unicode = true
    )
    f.render_widget(gauge2, chunks(2))

    val gauge3 = Gauge(
      block = Some(Block(title = Some(Spans.from("Gauge4")))),
      gauge_style = Style(fg = Some(Color.Cyan), add_modifier = Modifier.ITALIC),
      ratio = Ratio.percent(app.progress4),
      label = Some(Span.from(s"${app.progress4}/100"))
    )
    f.render_widget(gauge3, chunks(3))
  }
}
