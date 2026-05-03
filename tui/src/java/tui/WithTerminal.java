package tui;

import java.util.function.BiFunction;
import tui.crossterm.Command;
import tui.crossterm.CrosstermJni;

public final class WithTerminal {
  private WithTerminal() {}

  public static <T> T apply(BiFunction<CrosstermJni, Terminal, T> f) {
    CrosstermJni jni = new CrosstermJni();
    // setup terminal
    jni.enableRawMode();
    jni.execute(new Command.EnterAlternateScreen(), new Command.EnableMouseCapture());

    CrosstermBackend backend = new CrosstermBackend(jni);
    Terminal terminal = Terminal.init(backend);

    try {
      return f.apply(jni, terminal);
    } finally {
      // restore terminal
      jni.disableRawMode();
      jni.execute(new Command.LeaveAlternateScreen(), new Command.DisableMouseCapture());
      backend.showCursor();
    }
  }
}
