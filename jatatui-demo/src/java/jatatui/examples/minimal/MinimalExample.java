package jatatui.examples.minimal;

import jatatui.core.terminal.Terminal;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import java.io.IOException;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;

/// A minimal example of a jatatui application.
///
/// Mirrors `examples/apps/minimal/src/main.rs` from the upstream ratatui repo.
///
/// This is a bare minimum example. There are many approaches to running an application loop, so
/// this is not meant to be prescriptive. See the other examples for more complete versions; in
/// particular, the [jatatui.examples.helloworld.HelloWorldExample] is a good starting point.
public final class MinimalExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private MinimalExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> run(terminal));
  }

  private static void run(Terminal<CrosstermBackend> terminal) throws IOException {
    while (true) {
      terminal.draw(frame -> Widget.renderString("Hello World!", frame.area(), frame.bufferMut()));
      Event event = JNI.read();
      if (isKeyPress(event)) {
        return;
      }
    }
  }

  private static boolean isKeyPress(Event event) {
    return event instanceof Event.Key key
        && key.keyEvent().kind() == tui.crossterm.KeyEventKind.Press;
  }
}
