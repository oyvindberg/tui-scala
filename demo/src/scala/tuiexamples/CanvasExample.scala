package tuiexamples

import tui._
import tui.crossterm.CrosstermJni
import tui.widgets.BlockWidget
import tui.widgets.canvas.{CanvasWidget, MapResolution, Rectangle, WorldMap}

import java.time.{Duration, Instant}
import scala.Ordering.Implicits._

object CanvasExample {
  case class App(
      var x: Double = 0.0,
      var y: Double = 0.0,
      var ball: Rectangle = Rectangle(
        x = 10.0,
        y = 30.0,
        width = 10.0,
        height = 10.0,
        color = Color.Yellow
      ),
      playground: Rect = Rect(10, 10, 100, 100),
      vx: Double = 1.0,
      vy: Double = 1.0,
      var dir_x: Boolean = true,
      var dir_y: Boolean = true
  ) {

    def on_tick(): Unit = {
      if (this.ball.x < this.playground.left.toDouble || this.ball.x + this.ball.width > this.playground.right.toDouble) {
        this.dir_x = !this.dir_x
      }
      if (this.ball.y < this.playground.top.toDouble || this.ball.y + this.ball.height > this.playground.bottom.toDouble) {
        this.dir_y = !this.dir_y
      }

      if (this.dir_x) {
        ball = ball.copy(x = ball.x + this.vx)
      } else {
        ball = ball.copy(x = ball.x - this.vx)
      }

      if (this.dir_y) {
        ball = ball.copy(y = ball.x + this.vy)
      } else {
        ball = ball.copy(y = ball.x - this.vy)
      }
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
              case _: tui.crossterm.KeyCode.Down                       => app.y += 1.0
              case _: tui.crossterm.KeyCode.Up                         => app.y -= 1.0
              case _: tui.crossterm.KeyCode.Right                      => app.x += 1.0
              case _: tui.crossterm.KeyCode.Left                       => app.x -= 1.0
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
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))
    )
      .split(f.size)

    val canvas0 = CanvasWidget(
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("World")))),
      y_bounds = Point(-90.0, 90.0),
      x_bounds = Point(-180.0, 180.0),
      painter = Some { ctx =>
        ctx.draw(WorldMap(color = Color.White, resolution = MapResolution.High))
        ctx.print(app.x, -app.y, Spans.from(Span.styled("You are here", Style(fg = Some(Color.Yellow)))))
      }
    )
    f.render_widget(canvas0, chunks(0))

    val canvas1 = CanvasWidget(
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("Pong")))),
      y_bounds = Point(10.0, 110.0),
      x_bounds = Point(10.0, 110.0),
      painter = Some(ctx => ctx.draw(app.ball))
    )
    f.render_widget(canvas1, chunks(1))
  }
}
