package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets._

import java.time.{Duration, Instant}
import scala.Ordering.Implicits._

object ChartExample {
  val DATA: Array[Point] = Array(Point.Zero, Point(1.0, 1.0), Point(2.0, 2.0), Point(3.0, 3.0), Point(4.0, 4.0))
  val DATA2: Array[Point] = Array(Point.Zero, Point(10.0, 1.0), Point(20.0, 0.5), Point(30.0, 1.5), Point(40.0, 1.0), Point(50.0, 2.5), Point(60.0, 3.0))

  case class SinSignal(interval: Double, period: Double, scale: Double) extends Iterator[Point] {
    var x: Double = 0.0

    override def hasNext: Boolean = true

    override def next(): Point = {
      val point = Point(x, math.sin(x * 1.0 / period) * scale)
      x += interval
      point
    }
  }

  case class App(
      signal1: SinSignal,
      var data1: Array[Point],
      signal2: SinSignal,
      var data2: Array[Point],
      var window: Point
  ) {
    def on_tick(): Unit = {
      data1 = data1.drop(5) ++ signal1.take(5)
      data2 = data2.drop(5) ++ signal2.take(5)
      window = window match {
        case Point(x, y) => Point(x + 1.0, y + 1.0)
      }
    }
  }

  object App {
    def apply(): App = {
      val signal1 = SinSignal(0.2, 3.0, 18.0)
      val signal2 = SinSignal(0.1, 2.0, 10.0)
      val data1 = signal1.iterator.take(200).toArray
      val data2 = signal2.iterator.take(200).toArray
      App(signal1, data1, signal2, data2, window = Point(0.0, 20.0))
    }

  }
  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val tick_rate = Duration.ofMillis(250)
    val app = App()

    run_app(terminal, app, tick_rate, jni)
  }

  def run_app(terminal: Terminal, app: App, tick_rate: java.time.Duration, jni: CrosstermJni): Unit = {
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
    val size = f.size
    val chunks = Layout(
      direction = Direction.Vertical,
      constraints = Array(Constraint.Ratio(1, 3), Constraint.Ratio(1, 3), Constraint.Ratio(1, 3))
    ).split(size)

    val Bold = Style(add_modifier = Modifier.BOLD)

    val x_labels = Array(
      Span.styled(app.window.x.toString, Bold),
      Span.nostyle(((app.window.x + app.window.y) / 2.0).toString),
      Span.styled(app.window.y.toString, Bold)
    )

    {
      val datasets = Array(
        ChartWidget.Dataset(name = "data2", marker = symbols.Marker.Dot, style = Style(fg = Some(Color.Cyan)), data = app.data1),
        ChartWidget.Dataset(name = "data3", marker = symbols.Marker.Braille, style = Style(fg = Some(Color.Yellow)), data = app.data2)
      )

      val chart = ChartWidget(
        datasets = datasets,
        block = Some(
          BlockWidget(title = Some(Spans.from(Span.styled("Chart 1", Style(fg = Some(Color.Cyan), add_modifier = Modifier.BOLD)))), borders = Borders.ALL)
        ),
        x_axis = ChartWidget.Axis(title = Some(Spans.nostyle("X Axis")), style = Style(fg = Some(Color.Gray)), labels = Some(x_labels), bounds = app.window),
        y_axis = ChartWidget.Axis(
          title = Some(Spans.nostyle("Y Axis")),
          style = Style(fg = Some(Color.Gray)),
          labels = Some(
            Array(
              Span.styled("-20", Bold),
              Span.nostyle("0"),
              Span.styled("20", Bold)
            )
          ),
          bounds = Point(-20.0, 20.0)
        )
      )
      f.render_widget(chart, chunks(0))
    }

    {
      val datasets = Array(
        ChartWidget.Dataset(
          name = "data",
          marker = symbols.Marker.Braille,
          style = Style(fg = Some(Color.Yellow)),
          graph_type = ChartWidget.GraphType.Line,
          data = DATA
        )
      )
      val chart = ChartWidget(
        datasets = datasets,
        block = Some(
          BlockWidget(
            title = Some(Spans.from(Span.styled("Chart 2", Style(fg = Some(Color.Cyan), add_modifier = Modifier.BOLD)))),
            borders = Borders.ALL
          )
        ),
        x_axis = ChartWidget.Axis(
          title = Some(Spans.nostyle("X Axis")),
          style = Style(fg = Some(Color.Gray)),
          bounds = Point(0.0, 5.0),
          labels = Some(Array(Span.styled("0", Bold), Span.nostyle("2.5"), Span.styled("5.0", Bold)))
        ),
        y_axis = ChartWidget.Axis(
          title = Some(Spans.nostyle("Y Axis")),
          style = Style(fg = Some(Color.Gray)),
          bounds = Point(0.0, 5.0),
          labels = Some(Array(Span.styled("0", Bold), Span.nostyle("2.5"), Span.styled("5.0", Bold)))
        )
      )
      f.render_widget(chart, chunks(1))
    }

    {
      val datasets = Array(
        ChartWidget.Dataset(
          name = "data",
          marker = symbols.Marker.Braille,
          style = Style(fg = Some(Color.Yellow)),
          graph_type = ChartWidget.GraphType.Line,
          data = DATA2
        )
      )
      val chart = ChartWidget(
        datasets = datasets,
        block = Some(
          BlockWidget(title = Some(Spans.from(Span.styled("Chart 3", Style(fg = Some(Color.Cyan), add_modifier = Modifier.BOLD)))), borders = Borders.ALL)
        ),
        x_axis = ChartWidget.Axis(
          title = Some(Spans.nostyle("X Axis")),
          bounds = Point(0.0, 50.0),
          labels = Some(Array(Span.styled("0", Bold), Span.nostyle("25"), Span.styled("50", Bold))),
          style = Style(fg = Some(Color.Gray))
        ),
        y_axis = ChartWidget.Axis(
          title = Some(Spans.nostyle("Y Axis")),
          style = Style(fg = Some(Color.Gray)),
          bounds = Point(0.0, 5.0),
          labels = Some(Array(Span.styled("0", Bold), Span.nostyle("2.5"), Span.styled("5", Bold)))
        )
      )
      f.render_widget(chart, chunks(2))
    }
  }
}
