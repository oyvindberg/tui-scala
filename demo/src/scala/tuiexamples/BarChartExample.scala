package tuiexamples

import tui._
import tui.widgets.{BarChartWidget, BlockWidget}

import java.time.{Duration, Instant}
import scala.Ordering.Implicits._

object BarChartExample {

  case class App(var data: Array[(String, Int)]) {
    def on_tick(): Unit =
      data = data.last +: data.dropRight(1)
  }

  object App {
    // format: off
    val data: Array[(String, Int)] = Array(("B1", 9), ("B2", 12), ("B3", 5), ("B4", 8), ("B5", 2), ("B6", 4), ("B7", 5), ("B8", 9), ("B9", 14), ("B10", 15), ("B11", 1), ("B12", 0), ("B13", 4), ("B14", 6), ("B15", 4), ("B16", 6), ("B17", 4), ("B18", 7), ("B19", 13), ("B20", 8), ("B21", 11), ("B22", 9), ("B23", 3), ("B24", 5))
    // format: on
  }

  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val tick_rate = Duration.ofMillis(250)
    val app = new App(App.data)

    run_app(terminal, app, tick_rate, jni)
  }

  def run_app(
      terminal: Terminal,
      app: App,
      tick_rate: java.time.Duration,
      jni: tui.crossterm.CrosstermJni
  ): Unit = {
    var last_tick = Instant.now()

    def elapsed = java.time.Duration.between(last_tick, java.time.Instant.now())
    def timeout = {
      val timeout = tick_rate.minus(elapsed)
      new tui.crossterm.Duration(timeout.toSeconds, timeout.getNano)
    }

    while (true) {
      terminal.draw(f => ui(app).render(f.buffer.area, f.buffer))

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

  def ui(app: App): Widget =
    Layout(direction = Direction.Vertical, margin = Margin(2, 2))(
      Constraint.Percentage(50) ->
        BarChartWidget(
          block = Some(BlockWidget(title = Some(Spans.nostyle("Data1")), borders = Borders.ALL)),
          data = app.data,
          barWidth = 9,
          barStyle = Style(fg = Some(Color.Yellow)),
          valueStyle = Style(fg = Some(Color.Black), bg = Some(Color.Yellow))
        ),
      Constraint.Percentage(50) ->
        Layout(direction = Direction.Horizontal)(
          Constraint.Percentage(50) ->
            BarChartWidget(
              block = Some(BlockWidget(title = Some(Spans.nostyle("Data2")), borders = Borders.ALL)),
              barWidth = 5,
              barGap = 3,
              barStyle = Style(fg = Some(Color.Green)),
              valueStyle = Style(bg = Some(Color.Green), addModifier = Modifier.BOLD),
              data = app.data
            ),
          Constraint.Percentage(50) ->
            BarChartWidget(
              block = Some(BlockWidget(title = Some(Spans.nostyle("Data3")), borders = Borders.ALL)),
              data = app.data,
              barStyle = Style(fg = Some(Color.Red)),
              barWidth = 7,
              barGap = 0,
              valueStyle = Style(bg = Some(Color.Red)),
              labelStyle = Style(fg = Some(Color.Cyan), addModifier = Modifier.ITALIC)
            )
        )
    )
}
