package tui
package examples
package demo

import tui.crossterm.CrosstermJni
import tui.terminal.Terminal
import tui.withTerminal

import java.time.{Duration, Instant}
import scala.Ordering.Implicits._

object Main {
  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val tick_rate = Duration.ofMillis(250)
    val app = App(title = "Crossterm Demo", enhanced_graphics = true)

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
      terminal.draw(f => ui.draw(f, app))

      if (jni.poll(timeout)) {
        jni.read() match {
          case key: tui.crossterm.Event.Key =>
            key.keyEvent.code match {
              case char: tui.crossterm.KeyCode.Char => app.on_key(char.c())
              case _: tui.crossterm.KeyCode.Left    => app.on_left()
              case _: tui.crossterm.KeyCode.Up      => app.on_up()
              case _: tui.crossterm.KeyCode.Right   => app.on_right()
              case _: tui.crossterm.KeyCode.Down    => app.on_down()
              case _                                => ()
            }
          case _ => ()
        }
      }
      if (elapsed >= tick_rate) {
        app.on_tick()
        last_tick = Instant.now()
      }
      if (app.should_quit) {
        return
      }
    }
  }
}
