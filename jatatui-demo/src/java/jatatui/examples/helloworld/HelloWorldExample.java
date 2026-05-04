package jatatui.examples.helloworld;

import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates a basic hello world application.
///
/// Mirrors `examples/apps/hello-world/src/main.rs`. This example exits when the user presses 'q'.
/// There is a 250ms timeout on the event poll to ensure that the terminal is rendered at least
/// once every 250ms.
public final class HelloWorldExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private HelloWorldExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> run(terminal));
  }

  /// Run the application loop.
  private static void run(Terminal<CrosstermBackend> terminal) throws IOException {
    while (true) {
      terminal.draw(HelloWorldExample::render);
      if (shouldQuit()) {
        return;
      }
    }
  }

  /// Render the application UI.
  private static void render(Frame frame) {
    Paragraph greeting = Paragraph.of("Hello World! (press 'q' to quit)");
    frame.renderWidget(greeting, frame.area());
  }

  /// Returns true once the user has pressed 'q'. Polls events with a 250ms timeout so the draw
  /// loop runs at least four times per second even when the user is idle.
  private static boolean shouldQuit() throws IOException {
    if (JNI.poll(new Duration(0, 250_000_000))) {
      Event event = JNI.read();
      if (event instanceof Event.Key key
          && key.keyEvent().kind() == KeyEventKind.Press
          && key.keyEvent().code() instanceof KeyCode.Char ch
          && ch.c() == 'q') {
        return true;
      }
    }
    return false;
  }
}
