package jatatui.crossterm;

import jatatui.core.terminal.Terminal;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;
import tui.crossterm.Command;
import tui.crossterm.CrosstermJni;

/// Convenience entry point mirroring the upstream `ratatui` umbrella crate's
/// `init.rs` (the `ratatui::run`, `ratatui::init`, `ratatui::restore` family).
///
/// The umbrella `ratatui` crate is not ported as a Java module; its only
/// substantive content is the init/restore helpers which are bundled here.
public final class Jatatui {

  private static final CrosstermJni JNI = new CrosstermJni();

  private Jatatui() {}

  /// Initialize a terminal with reasonable defaults: raw mode + alternate screen,
  /// returning a `Terminal<CrosstermBackend>` ready to draw.
  public static Terminal<CrosstermBackend> init() throws IOException {
    JNI.enableRawMode();
    JNI.execute(new Command.EnterAlternateScreen());
    installPanicHook();
    return Terminal.create(new CrosstermBackend(JNI));
  }

  /// Restore the terminal to a sane state: leave the alternate screen and
  /// disable raw mode. Errors are logged and swallowed so this can be safely
  /// called from finally blocks and shutdown hooks.
  public static void restore() {
    try {
      JNI.execute(new Command.LeaveAlternateScreen());
    } catch (RuntimeException e) {
      System.err.println("Failed to leave alternate screen: " + e.getMessage());
    }
    try {
      JNI.disableRawMode();
    } catch (RuntimeException e) {
      System.err.println("Failed to disable raw mode: " + e.getMessage());
    }
  }

  /// Run a function with an initialized terminal, restoring the terminal
  /// afterwards regardless of whether the function returned normally or threw.
  public static <R> R run(Function<Terminal<CrosstermBackend>, R> body) throws IOException {
    Terminal<CrosstermBackend> terminal = init();
    try {
      return body.apply(terminal);
    } finally {
      try {
        terminal.close();
      } finally {
        restore();
      }
    }
  }

  /// Run a function that may throw IOException. The IOException is rethrown verbatim;
  /// other checked exceptions must be wrapped by the caller.
  public static void runIo(IoConsumer body) throws IOException {
    Terminal<CrosstermBackend> terminal = init();
    try {
      body.accept(terminal);
    } finally {
      try {
        terminal.close();
      } finally {
        restore();
      }
    }
  }

  @FunctionalInterface
  public interface IoConsumer {
    void accept(Terminal<CrosstermBackend> terminal) throws IOException;
  }

  private static volatile boolean panicHookInstalled = false;

  private static synchronized void installPanicHook() {
    if (panicHookInstalled) {
      return;
    }
    Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(
        (t, e) -> {
          restore();
          if (previous != null) {
            previous.uncaughtException(t, e);
          } else {
            e.printStackTrace(System.err);
          }
        });
    Runtime.getRuntime().addShutdownHook(new Thread(Jatatui::restore, "jatatui-restore"));
    panicHookInstalled = true;
  }

  /// Sneaky-throw helper for code paths that want a `Function` but need to
  /// surface `IOException`. Wraps `IOException` as `UncheckedIOException`.
  public static <R> Function<Terminal<CrosstermBackend>, R> ioFn(IoFunction<R> f) {
    return terminal -> {
      try {
        return f.apply(terminal);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    };
  }

  @FunctionalInterface
  public interface IoFunction<R> {
    R apply(Terminal<CrosstermBackend> terminal) throws IOException;
  }
}
