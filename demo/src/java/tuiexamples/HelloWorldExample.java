package tuiexamples;

import tui.Terminal;
import tui.Text;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.ParagraphWidget;

/// A bare-minimum example that draws a greeting and exits when the user presses 'q'.
public final class HelloWorldExample {
  private HelloWorldExample() {}

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      runApp(terminal, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, CrosstermJni jni) {
    while (true) {
      terminal.draw(
          f -> {
            ParagraphWidget greeting =
                ParagraphWidget.empty(Text.nostyle("Hello World! (press 'q' to quit)"));
            f.renderWidget(greeting, f.size);
          });
      // Poll with a 250ms timeout so the loop is responsive without busy-spinning.
      Duration timeout = new Duration(0, 250_000_000);
      if (jni.poll(timeout)) {
        Event ev = jni.read();
        if (ev instanceof Event.Key key
            && key.keyEvent().code() instanceof KeyCode.Char c
            && c.c() == 'q') {
          return;
        }
      }
    }
  }
}
