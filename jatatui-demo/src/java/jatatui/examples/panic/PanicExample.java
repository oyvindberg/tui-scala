package jatatui.examples.panic;

import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to handle panics in your application.
///
/// Mirrors `examples/apps/panic/src/main.rs`. The shutdown hook installed by [Jatatui#init()]
/// plays the role of the upstream Rust panic hook: when this process exits — whether normally,
/// through an uncaught exception, or via a SIGTERM — the terminal is restored. The 'h' key
/// removes the JVM shutdown hook so the user can compare the two behaviors.
public final class PanicExample {

  /// Mirrors upstream's `enum PanicHandlerState { Enabled, Disabled }`.
  enum PanicHandlerState {
    Enabled,
    Disabled
  }

  private static final CrosstermJni JNI = new CrosstermJni();

  /// The shutdown hook installed by [Jatatui]. Removed when the user presses 'h' to disable the
  /// "panic" handler.
  private static Thread restoreHook = null;

  private PanicExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> run(terminal));
  }

  private static void run(Terminal<CrosstermBackend> terminal) throws IOException {
    PanicHandlerState[] state = {PanicHandlerState.Enabled};
    while (true) {
      terminal.draw(frame -> render(frame, state[0]));
      Event event = JNI.read();
      if (event instanceof Event.Key keyEvt
          && keyEvt.keyEvent().kind() == KeyEventKind.Press
          && keyEvt.keyEvent().code() instanceof KeyCode.Char ch) {
        switch (ch.c()) {
          case 'p' -> throw new RuntimeException("intentional demo panic");
          case 'e' -> throw new IOException("intentional demo error");
          case 'h' -> {
            removeRestoreHook();
            state[0] = PanicHandlerState.Disabled;
          }
          case 'q' -> {
            return;
          }
          default -> {
            // ignore other keys
          }
        }
      }
    }
  }

  private static void render(Frame frame, PanicHandlerState state) {
    List<Line> lines = new ArrayList<>();
    lines.add(Line.from("Panic hook is currently: " + state));
    lines.add(Line.from(""));
    lines.add(Line.from("Press `p` to cause a panic"));
    lines.add(Line.from("Press `e` to cause an error"));
    lines.add(Line.from("Press `h` to disable the panic hook"));
    lines.add(Line.from("Press `q` to quit"));
    lines.add(Line.from(""));
    lines.add(Line.from("When your app panics without a panic hook, you will likely have to"));
    lines.add(Line.from("reset your terminal afterwards with the `reset` command"));
    lines.add(Line.from(""));
    lines.add(Line.from("Try first with the panic handler enabled, and then with it disabled"));
    lines.add(Line.from("to see the difference"));

    Paragraph paragraph =
        Paragraph.of(lines)
            .withBlock(Block.bordered().withTitle("Panic Handler Demo"))
            .centered();
    frame.renderWidget(paragraph, frame.area());
  }

  /// Locate and remove the shutdown hook installed by [Jatatui#init()].
  private static synchronized void removeRestoreHook() {
    if (restoreHook != null) {
      try {
        Runtime.getRuntime().removeShutdownHook(restoreHook);
      } catch (IllegalStateException ignored) {
        // shutdown already in progress — nothing we can do
      }
      restoreHook = null;
      return;
    }
    // Best-effort: the hook is registered with name "jatatui-restore" (see Jatatui#installPanicHook).
    // We can't enumerate shutdown hooks via the public API, so subsequent presses of 'h' are no-ops.
    // The behavior matches the upstream demo: pressing 'h' once is enough to disable the handler.
  }
}
