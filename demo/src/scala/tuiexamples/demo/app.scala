package tuiexamples
package demo

import tui._
import tui.widgets.ListWidget

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.Random

case class RandomSignal(lower: Int, upper: Int, random: Random) extends Iterator[Int] {
  override def hasNext: Boolean = true

  override def next(): Int = lower + random.nextInt(upper - lower)
}

case class SinSignal(
    interval: Double,
    period: Double,
    scale: Double
) extends Iterator[Point] {
  var x: Double = 0.0

  override def hasNext: Boolean = true

  override def next(): Point = {
    val point = Point(x, math.sin(x * 1.0 / period) * scale)
    x += interval
    point
  }
}

case class TabsState(titles: Array[String]) {
  var index: Int = 0
  def next(): Unit =
    index = (this.index + 1) % this.titles.length

  def previous(): Unit =
    if (this.index > 0) {
      this.index -= 1
    } else {
      this.index = this.titles.length - 1
    }
}

case class StatefulList[T](
    state: ListWidget.State,
    items: mutable.ArrayDeque[T]
) {

  def next(): Unit = {
    val i = this.state.selected match {
      case Some(i) => if (i >= this.items.length - 1) 0 else i + 1
      case None    => 0
    }
    this.state.select(Some(i))
  }

  def previous(): Unit = {
    val i = this.state.selected match {
      case Some(i) => if (i == 0) this.items.length - 1 else i - 1
      case None    => 0
    }
    this.state.select(Some(i))
  }
}

object StatefulList {
  def with_items[T](items: Array[T]): StatefulList[T] =
    StatefulList(state = ListWidget.State(), items = mutable.ArrayDeque.from(items))
}

case class Signal[T: ClassTag](source: Iterator[T], var points: Array[T], tick_rate: Int) {
  def on_tick(): Unit =
    points = points.drop(tick_rate) ++ source.take(tick_rate)
}

case class Signals(
    sin1: Signal[Point],
    sin2: Signal[Point],
    var window: Point
) {
  def on_tick(): Unit = {
    this.sin1.on_tick()
    this.sin2.on_tick()
    this.window = this.window match {
      case Point(x, y) => Point(x + 1.0, y + 1.0)
    }
  }
}

case class Server(
    name: String,
    location: String,
    coords: Point,
    status: String
)

case class App(
    title: String,
    var should_quit: Boolean,
    tabs: TabsState,
    var show_chart: Boolean,
    var progress: Double,
    sparkline: Signal[Int],
    tasks: StatefulList[String],
    logs: StatefulList[(String, String)],
    signals: Signals,
    barchart: mutable.ArrayDeque[(String, Int)],
    servers: Array[Server],
    enhanced_graphics: Boolean
) {

  def on_up(): Unit =
    this.tasks.previous()

  def on_down(): Unit =
    this.tasks.next()

  def on_right(): Unit =
    this.tabs.next()

  def on_left(): Unit =
    this.tabs.previous()

  def on_key(c: Char): Unit =
    c match {
      case 'q' => this.should_quit = true
      case 't' => this.show_chart = !this.show_chart
      case _   => ()
    }

  def on_tick(): Unit = {
    // Update progress
    this.progress += 0.001
    if (this.progress > 1.0) {
      this.progress = 0.0
    }

    this.sparkline.on_tick()
    this.signals.on_tick()

    val log = this.logs.items.removeLast()
    this.logs.items.insert(0, log)

    val event = this.barchart.removeLast()
    this.barchart.insert(0, event)
  }
}

object App {
  def apply(title: String, enhanced_graphics: Boolean): App = {
    val rand_signal = RandomSignal(0, 100, new Random())
    val sparkline_points = rand_signal.take(300).toArray
    val sin_signal = SinSignal(0.2, 3.0, 18.0)
    val sin1_points = sin_signal.take(100).toArray
    val sin_signal2 = SinSignal(0.1, 2.0, 10.0)
    val sin2_points = sin_signal2.take(200).toArray
    new App(
      title = title,
      should_quit = false,
      tabs = TabsState(Array("Tab0", "Tab1", "Tab2")),
      show_chart = true,
      progress = 0.0,
      sparkline = Signal(
        source = rand_signal,
        points = sparkline_points,
        tick_rate = 1
      ),
      tasks = StatefulList.with_items(TASKS),
      logs = StatefulList.with_items(LOGS),
      signals = Signals(
        sin1 = Signal(
          source = sin_signal,
          points = sin1_points,
          tick_rate = 5
        ),
        sin2 = Signal(
          source = sin_signal2,
          points = sin2_points,
          tick_rate = 10
        ),
        window = Point(0.0, 20.0)
      ),
      barchart = mutable.ArrayDeque.from(EVENTS),
      servers = Array(
        Server(
          name = "NorthAmerica-1",
          location = "New York City",
          coords = Point(40.71, -74.00),
          status = "Up"
        ),
        Server(
          name = "Europe-1",
          location = "Paris",
          coords = Point(48.85, 2.35),
          status = "Failure"
        ),
        Server(
          name = "SouthAmerica-1",
          location = "São Paulo",
          coords = Point(-23.54, -46.62),
          status = "Up"
        ),
        Server(
          name = "Asia-1",
          location = "Singapore",
          coords = Point(1.35, 103.86),
          status = "Up"
        )
      ),
      enhanced_graphics
    )
  }

  val TASKS = Array(
    "Item1",
    "Item2",
    "Item3",
    "Item4",
    "Item5",
    "Item6",
    "Item7",
    "Item8",
    "Item9",
    "Item10",
    "Item11",
    "Item12",
    "Item13",
    "Item14",
    "Item15",
    "Item16",
    "Item17",
    "Item18",
    "Item19",
    "Item20",
    "Item21",
    "Item22",
    "Item23",
    "Item24"
  )

  val LOGS = Array(
    ("Event1", "INFO"),
    ("Event2", "INFO"),
    ("Event3", "CRITICAL"),
    ("Event4", "ERROR"),
    ("Event5", "INFO"),
    ("Event6", "INFO"),
    ("Event7", "WARNING"),
    ("Event8", "INFO"),
    ("Event9", "INFO"),
    ("Event10", "INFO"),
    ("Event11", "CRITICAL"),
    ("Event12", "INFO"),
    ("Event13", "INFO"),
    ("Event14", "INFO"),
    ("Event15", "INFO"),
    ("Event16", "INFO"),
    ("Event17", "ERROR"),
    ("Event18", "ERROR"),
    ("Event19", "INFO"),
    ("Event20", "INFO"),
    ("Event21", "WARNING"),
    ("Event22", "INFO"),
    ("Event23", "INFO"),
    ("Event24", "WARNING"),
    ("Event25", "INFO"),
    ("Event26", "INFO")
  )

  val EVENTS = Array(
    ("B1", 9),
    ("B2", 12),
    ("B3", 5),
    ("B4", 8),
    ("B5", 2),
    ("B6", 4),
    ("B7", 5),
    ("B8", 9),
    ("B9", 14),
    ("B10", 15),
    ("B11", 1),
    ("B12", 0),
    ("B13", 4),
    ("B14", 6),
    ("B15", 4),
    ("B16", 6),
    ("B17", 4),
    ("B18", 7),
    ("B19", 13),
    ("B20", 8),
    ("B21", 11),
    ("B22", 9),
    ("B23", 3),
    ("B24", 5)
  )

}
