package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets.{BlockWidget, GaugeWidget}

import java.time.{Duration, Instant}
import scala.math.Ordering.Implicits._

object GaugeExample {
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

  def ui(f: Frame, app: App): Unit =
    Layout(direction = Direction.Vertical, margin = Margin(2))(
      GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Gauge1")), borders = Borders.ALL)),
        gaugeStyle = Style(fg = Some(Color.Yellow)),
        ratio = GaugeWidget.Ratio.percent(app.progress1)
      ),
      GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Gauge2")), borders = Borders.ALL)),
        gaugeStyle = Style(fg = Some(Color.Magenta), bg = Some(Color.Green)),
        ratio = GaugeWidget.Ratio.percent(app.progress2),
        label = Some(Span.nostyle(s"${app.progress2}/100"))
      ),
      GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Gauge3")), borders = Borders.ALL)),
        gaugeStyle = Style(fg = Some(Color.Yellow)),
        ratio = GaugeWidget.Ratio(app.progress3),
        label = Some(
          Span.styled(
            "%.2f".format(app.progress3 * 100.0),
            Style(fg = Some(Color.Red), addModifier = Modifier.ITALIC | Modifier.BOLD)
          )
        ),
        useUnicode = true
      ),
      GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Gauge4")))),
        gaugeStyle = Style(fg = Some(Color.Cyan), addModifier = Modifier.ITALIC),
        ratio = GaugeWidget.Ratio.percent(app.progress4),
        label = Some(Span.nostyle(s"${app.progress4}/100"))
      )
    )
      .render(f.size, f.buffer)
}
