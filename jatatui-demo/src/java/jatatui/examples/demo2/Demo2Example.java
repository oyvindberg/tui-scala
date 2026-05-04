package jatatui.examples.demo2;

import jatatui.core.layout.Rect;
import jatatui.core.terminal.Terminal;
import jatatui.core.terminal.TerminalOptions;
import jatatui.core.terminal.Viewport;
import jatatui.crossterm.CrosstermBackend;
import java.io.IOException;
import tui.crossterm.Command;
import tui.crossterm.CrosstermJni;

/// Mirrors `apps/demo2/src/main.rs`.
///
/// Entry point for the demo2 example. Initializes the terminal with a fixed 81x18 viewport (to
/// match the GitHub social-preview window upstream uses with VHS), enters the alternate screen,
/// runs the [App] event loop, and restores the terminal on exit.
public final class Demo2Example {

  private Demo2Example() {}

  public static void main(String[] args) throws IOException {
    run();
  }

  /// Run the demo. Public so callers (e.g. a launcher) can invoke it without going through `main`.
  public static void run() throws IOException {
    CrosstermJni jni = new CrosstermJni();
    jni.enableRawMode();
    jni.execute(new Command.EnterAlternateScreen());
    Terminal<CrosstermBackend> terminal = null;
    try {
      // Match the upstream demo's fixed viewport (81x18 — the GitHub social-preview size used
      // when generating the demo GIF with VHS).
      Viewport viewport = new Viewport.Fixed(new Rect(0, 0, 81, 18));
      terminal = Terminal.withOptions(new CrosstermBackend(jni), new TerminalOptions(viewport));
      App app = App.create(jni);
      app.run(terminal);
    } finally {
      if (terminal != null) {
        try {
          terminal.close();
        } catch (RuntimeException ignored) {
          // continue restoring the terminal
        }
      }
      try {
        jni.execute(new Command.LeaveAlternateScreen());
      } catch (RuntimeException ignored) {
        // continue restoring the terminal
      }
      try {
        jni.disableRawMode();
      } catch (RuntimeException ignored) {
        // best effort
      }
    }
  }
}
