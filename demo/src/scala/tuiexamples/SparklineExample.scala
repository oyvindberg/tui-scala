package tuiexamples

import tui._
import tui.widgets.{BlockWidget, SparklineWidget}

import java.time.{Duration, Instant}
import scala.Ordering.Implicits._
import scala.collection.mutable
import scala.util.Random

object SparklineExample {
  case class RandomSignal(
      lower: Int,
      upper: Int,
      random: Random = new Random()
  ) {
    def iterator: Iterator[Int] =
      new Iterator[Int] {
        override def hasNext: Boolean = true

        override def next(): Int = random.nextInt(upper - lower) + lower
      }
  }

  case class App(
      signal: Iterator[Int],
      data1: mutable.ArrayDeque[Int],
      data2: mutable.ArrayDeque[Int],
      data3: mutable.ArrayDeque[Int]
  ) {
    def on_tick(): Unit = {
      val value1 = signal.next()
      data1.removeLast()
      data1.prepend(value1)
      val value2 = signal.next()
      data2.removeLast()
      data2.prepend(value2)
      val value3 = signal.next()
      data3.removeLast()
      data3.prepend(value3)
    }
  }

  object App {
    def apply(): App = {
      val signal = RandomSignal(lower = 0, upper = 100).iterator
      val data1 = mutable.ArrayDeque.from(signal.take(200))
      val data2 = mutable.ArrayDeque.from(signal.take(200))
      val data3 = mutable.ArrayDeque.from(signal.take(200))
      App(signal, data1, data2, data3)
    }
  }

  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val tick_rate = Duration.ofMillis(250)
    val app = App()
    run_app(terminal, app, tick_rate, jni);
  }

  def run_app(
      terminal: Terminal,
      app: App,
      tick_rate: Duration,
      jni: tui.crossterm.CrosstermJni
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
    Layout(direction = Direction.Vertical, margin = Margin(2, 2))(
      Constraint.Length(3) -> SparklineWidget(
        block = Some(
          BlockWidget(
            title = Some(Spans.nostyle("Data1")),
            borders = Borders.LEFT | Borders.RIGHT
          )
        ),
        data = app.data1,
        style = Style(fg = Some(Color.Yellow))
      ),
      Constraint.Length(3) -> SparklineWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Data2")), borders = Borders.LEFT | Borders.RIGHT)),
        data = app.data2,
        style = Style(bg = Some(Color.Green))
      ),
      // Multiline
      Constraint.Length(7) -> SparklineWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Data3")), borders = Borders.LEFT | Borders.RIGHT)),
        style = Style(fg = Some(Color.Red)),
        data = app.data3
      ),
      Constraint.Min(0) -> Widget.Empty
    ).render(f.size, f.buffer)
}
