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
  ): Unit =  {
    var last_tick = Instant.now()

    def elapsed = java.time.Duration.between(last_tick, java.time.Instant.now())
    def timeout = {
      val timeout = tick_rate.minus(elapsed)
      new tui.crossterm.Duration(timeout.toSeconds, timeout.getNano)
    }

    while(true) {
        terminal.draw(f => ui(f, app))

        if (jni.poll(timeout)) {
            jni.read() match {
              case key: tui.crossterm.Event.Key =>
                key.keyEvent.code match {
                  case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => return
                  case _ => ()
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
      val verticalChunks = Layout(
        direction = Direction.Vertical,
        margin = Margin(2, 2),
        constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50)),
      ).split(f.size)

      val barchart1 = BarChartWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Data1")), borders = Borders.ALL)),
        data = app.data,
        bar_width = 9,
        bar_style = Style(fg = Some(Color.Yellow)),
        value_style = Style(fg = Some(Color.Black), bg = Some(Color.Yellow))
      )
      f.render_widget(barchart1, verticalChunks(0))

    val horizontalChunks = Layout(
        direction = Direction.Horizontal,
        constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))
      ).split(verticalChunks(1))

    val barchart2 = BarChartWidget(
      block = Some(BlockWidget(title = Some(Spans.nostyle("Data2")), borders = Borders.ALL)),
        bar_width = 5,
        bar_gap = 3,
        bar_style = Style(fg = Some(Color.Green)),
        value_style = Style(bg = Some(Color.Green), add_modifier = Modifier.BOLD),
        data = app.data
      )
      f.render_widget(barchart2, horizontalChunks(0))

    val barchart3 = BarChartWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Data3")), borders = Borders.ALL)),
        data = app.data,
        bar_style = Style(fg = Some(Color.Red)),
        bar_width = 7,
        bar_gap = 0,
        value_style = Style(bg = Some(Color.Red)),
        label_style = Style(fg = Some(Color.Cyan), add_modifier = Modifier.ITALIC)
      )
      f.render_widget(barchart3, horizontalChunks(1))
  }
}
