package jatatui.examples.demo;

import jatatui.core.terminal.Terminal;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import java.io.IOException;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// The original ratatui demo, ported to jatatui.
///
/// Mirrors `examples/apps/demo/src/main.rs` and `examples/apps/demo/src/crossterm.rs` (folded
/// into this entry-point class because [Jatatui#runIo] already handles the
/// raw-mode / alternate-screen lifecycle).
///
/// Controls:
/// - `q`: quit
/// - `t`: toggle the right-hand chart on the first tab
/// - left/right (or `h`/`l`): switch tabs
/// - up/down (or `k`/`j`): move the task list selection
///
/// Termion and termwiz alternate backends are N/A: jatatui only bundles a crossterm backend.
public final class DemoExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  /// Hard-coded tick rate (250 ms) mirroring upstream's `--tick-rate` clap default.
  private static final long TICK_RATE_MILLIS = 250L;

  /// Hard-coded `enhanced_graphics` flag (true) mirroring upstream's `--unicode` clap default.
  private static final boolean ENHANCED_GRAPHICS = true;

  private DemoExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> runApp(terminal, TICK_RATE_MILLIS, ENHANCED_GRAPHICS));
  }

  /// Mirrors upstream `crossterm::run` / `run_app`. Renders, reads events with a tick-aligned
  /// timeout, and exits when [App#shouldQuit] becomes true.
  private static void runApp(
      Terminal<CrosstermBackend> terminal, long tickRateMillis, boolean enhancedGraphics)
      throws IOException {
    App app = new App("Crossterm Demo", enhancedGraphics);
    long lastTick = System.nanoTime();
    long tickRateNanos = tickRateMillis * 1_000_000L;

    while (true) {
      terminal.draw(frame -> Ui.render(frame, app));

      long elapsed = System.nanoTime() - lastTick;
      long remaining = Math.max(0, tickRateNanos - elapsed);
      Duration timeout = toDuration(remaining);

      if (!JNI.poll(timeout)) {
        app.onTick();
        lastTick = System.nanoTime();
        continue;
      }
      Event event = JNI.read();
      if (event instanceof Event.Key keyEvt && keyEvt.keyEvent().kind() == KeyEventKind.Press) {
        KeyCode code = keyEvt.keyEvent().code();
        if (code instanceof KeyCode.Char ch) {
          char c = ch.c();
          switch (c) {
            case 'h' -> app.onLeft();
            case 'j' -> app.onDown();
            case 'k' -> app.onUp();
            case 'l' -> app.onRight();
            default -> app.onKey(c);
          }
        } else if (code instanceof KeyCode.Left) {
          app.onLeft();
        } else if (code instanceof KeyCode.Down) {
          app.onDown();
        } else if (code instanceof KeyCode.Up) {
          app.onUp();
        } else if (code instanceof KeyCode.Right) {
          app.onRight();
        }
      }
      if (app.shouldQuit) {
        return;
      }
    }
  }

  private static Duration toDuration(long nanos) {
    long secs = nanos / 1_000_000_000L;
    int rem = (int) (nanos - secs * 1_000_000_000L);
    return new Duration(secs, rem);
  }
}
