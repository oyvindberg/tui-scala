package tuiexamples.demo;

import java.time.Duration;
import java.time.Instant;
import tui.Terminal;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;

public final class Demo {
  private Demo() {}

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(250);
      App app = App.create("Crossterm Demo", true);
      runApp(terminal, app, tickRate, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, App app, Duration tickRate, CrosstermJni jni) {
    Instant[] lastTick = {Instant.now()};

    while (true) {
      terminal.draw(f -> Ui.draw(f, app));

      Duration elapsed = Duration.between(lastTick[0], Instant.now());
      Duration remaining = tickRate.minus(elapsed);
      tui.crossterm.Duration timeout =
          new tui.crossterm.Duration(remaining.toSeconds(), remaining.getNano());
      if (jni.poll(timeout)) {
        Event ev = jni.read();
        if (ev instanceof Event.Key key) {
          tui.crossterm.KeyCode code = key.keyEvent().code();
          if (code instanceof KeyCode.Char c) {
            app.onKey(c.c());
          } else if (code instanceof KeyCode.Left) {
            app.onLeft();
          } else if (code instanceof KeyCode.Up) {
            app.onUp();
          } else if (code instanceof KeyCode.Right) {
            app.onRight();
          } else if (code instanceof KeyCode.Down) {
            app.onDown();
          }
        }
      }
      Duration elapsed2 = Duration.between(lastTick[0], Instant.now());
      if (elapsed2.compareTo(tickRate) >= 0) {
        app.onTick();
        lastTick[0] = Instant.now();
      }
      if (app.shouldQuit) {
        return;
      }
    }
  }
}
