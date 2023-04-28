package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.internal.ranges
import tui.widgets.{BlockWidget, ListWidget}

import java.time.{Duration, Instant}
import scala.collection.mutable
import scala.math.Ordering.Implicits._

object ListExample {

  case class StatefulList[T](
      state: ListWidget.State = ListWidget.State(),
      items: Array[T]
  ) {
    def next(): Unit = {
      val i = state.selected match {
        case Some(i) => if (i >= items.length - 1) 0 else i + 1
        case None    => 0
      }
      state.select(Some(i))
    }

    def previous(): Unit = {
      val i = state.selected match {
        case Some(i) =>
          if (i == 0) {
            items.length - 1
          } else {
            i - 1
          }
        case None => 0
      }
      state.select(Some(i))
    }

    def unselect(): Unit =
      state.select(None)
  }

  /// This struct holds the current state of the app. In particular, it has the `items` field which is a wrapper
  /// around `ListState`. Keeping track of the items state let us render the associated widget with its state
  /// and have access to features such as natural scrolling.
  ///
  /// Check the event handling at the bottom to see how to change the state on incoming events.
  /// Check the drawing logic for items on how to specify the highlighting style for selected items.
  case class App(
      items: StatefulList[(String, Int)],
      events: mutable.ArrayDeque[(String, String)]
  ) {
    /// Rotate through the event list.
    /// This only exists to simulate some kind of "progress"
    def on_tick(): Unit = {
      val event = events.removeHead()
      events.append(event)
    }
  }

  val items: Array[(String, Int)] = Array(
    ("Item0", 1),
    ("Item1", 2),
    ("Item2", 1),
    ("Item3", 3),
    ("Item4", 1),
    ("Item5", 4),
    ("Item6", 1),
    ("Item7", 3),
    ("Item8", 1),
    ("Item9", 6),
    ("Item10", 1),
    ("Item11", 3),
    ("Item12", 1),
    ("Item13", 2),
    ("Item14", 1),
    ("Item15", 1),
    ("Item16", 4),
    ("Item17", 1),
    ("Item18", 5),
    ("Item19", 4),
    ("Item20", 1),
    ("Item21", 2),
    ("Item22", 1),
    ("Item23", 3),
    ("Item24", 1)
  )
  val events: Array[(String, String)] = Array(
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

  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val tick_rate = Duration.ofMillis(250)
    val app = App(StatefulList(items = items), events = mutable.ArrayDeque.from(events))

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
              case _: tui.crossterm.KeyCode.Left                       => app.items.unselect()
              case _: tui.crossterm.KeyCode.Down                       => app.items.next()
              case _: tui.crossterm.KeyCode.Up                         => app.items.previous()
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
    // Iterate through all elements in the `items` app and append some debug text to it.
    val items0 = app.items.items
      .map { case (str, int) =>
        val lines = Array.newBuilder[Spans]
        lines += Spans.nostyle(str)
        ranges.range(0, int) { _ =>
          lines += Spans.from(
            Span.styled(
              "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
              Style(addModifier = Modifier.ITALIC)
            )
          )
        }
        ListWidget.Item(Text(lines.result()), Style(fg = Some(Color.Black), bg = Some(Color.White)))
      }

    // Let's do the same for the events.
    // The event list doesn't have any state and only displays the current state of the list.
    def events(area: Rect): Array[ListWidget.Item] = app.events.toArray.reverse
      .map { case (event, level) =>
        // Colorcode the level depending on its type
        val s = level match {
          case "CRITICAL" => Style(fg = Some(Color.Red))
          case "ERROR"    => Style(fg = Some(Color.Magenta))
          case "WARNING"  => Style(fg = Some(Color.Yellow))
          case "INFO"     => Style(fg = Some(Color.Blue))
          case _          => Style.DEFAULT
        }
        // Add a example datetime and apply proper spacing between them
        val header = Spans.from(
          Span.styled(level.padTo(9, ' '), s),
          Span.nostyle(" "),
          Span.styled(
            "2020-01-01 10:00:00",
            Style(addModifier = Modifier.ITALIC)
          )
        )
        // The event gets its own line
        val log = Spans.nostyle(event)

        // Here several things happen:
        // 1. Add a `---` spacing line above the final list entry
        // 2. Add the Level + datetime
        // 3. Add a spacer line
        // 4. Add the actual event
        ListWidget.Item(
          Text.fromSpans(
            Spans.nostyle("-".repeat(area.width)),
            header,
            Spans.nostyle(""),
            log
          )
        )
      }

    // Create two chunks with equal horizontal screen space
    Layout(direction = Direction.Horizontal)(
      // Create a List from all list items and highlight the currently selected one
      ListWidget(
        state = app.items.state,
        block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("List")))),
        items = items0,
        highlightStyle = Style(bg = Some(Color.LightGreen), addModifier = Modifier.BOLD),
        highlightSymbol = Some(">> ")
      ),
      Widget { (area, buf) =>
        ListWidget(
          state = ListWidget.State(),
          items = events(area),
          block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("List")))),
          startCorner = Corner.BottomLeft
        ).render(area, buf)
      }
    )
      .render(f.size, f.buffer)
  }
}
