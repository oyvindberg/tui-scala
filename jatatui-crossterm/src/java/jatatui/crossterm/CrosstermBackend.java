package jatatui.crossterm;

import jatatui.core.backend.Backend;
import jatatui.core.backend.ClearType;
import jatatui.core.backend.WindowSize;
import jatatui.core.buffer.BufferUpdate;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Position;
import jatatui.core.layout.Size;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.crossterm.Attribute;
import tui.crossterm.Command;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Xy;

/// A [Backend] implementation that uses the local [tui.crossterm] JNI binding to render to the
/// terminal.
///
/// Mirrors upstream `ratatui_crossterm::CrosstermBackend` (v0.30) — but where upstream is generic
/// over a `std::io::Write`, this implementation drives a [CrosstermJni] handle since all terminal
/// interactions go through the native binding rather than a Java `Writer`.
///
/// Most applications should not call methods on `CrosstermBackend` directly, but will instead use
/// the `Terminal` class, which provides a more ergonomic interface.
///
/// Usually applications will enable raw mode and switch to alternate screen mode after creating a
/// `CrosstermBackend`. This is done by calling [CrosstermJni#enableRawMode()] and enqueueing
/// [tui.crossterm.Command.EnterAlternateScreen] (and the corresponding disable/leave operations
/// when the application exits).
public final class CrosstermBackend implements Backend {

  /// The JNI handle used to send commands to the terminal.
  private final CrosstermJni writer;

  /// Creates a new `CrosstermBackend` with the given JNI handle.
  public CrosstermBackend(CrosstermJni writer) {
    this.writer = writer;
  }

  /// Returns the underlying JNI handle.
  ///
  /// Mirrors upstream's `writer()` accessor (gated behind the `backend-writer` unstable feature).
  /// Note: writing to the handle directly may cause incorrect output after the write — the
  /// `Terminal` implements diffing buffers and assumes exclusive control.
  public CrosstermJni writer() {
    return writer;
  }

  @Override
  public void draw(Iterable<BufferUpdate> content) throws IOException {
    Color fg = Color.RESET;
    Color bg = Color.RESET;
    Color underlineColor = Color.RESET;
    Modifier modifier = Modifier.EMPTY;
    Optional<Position> lastPos = Optional.empty();
    ArrayList<Command> commands = new ArrayList<>();

    for (BufferUpdate update : content) {
      int x = update.x();
      int y = update.y();
      Cell cell = update.cell();

      // Move the cursor if the previous location was not (x - 1, y).
      boolean adjacent =
          lastPos.isPresent() && x == lastPos.get().x() + 1 && y == lastPos.get().y();
      if (!adjacent) {
        commands.add(new Command.MoveTo(x, y));
      }
      lastPos = Optional.of(new Position(x, y));

      if (!cell.modifier.equals(modifier)) {
        ModifierDiff diff = new ModifierDiff(modifier, cell.modifier);
        diff.queue(commands);
        modifier = cell.modifier;
      }
      if (!cell.fg.equals(fg) || !cell.bg.equals(bg)) {
        commands.add(
            new Command.SetColors(
                Optional.of(CrosstermColorConv.toCrossterm(cell.fg)),
                Optional.of(CrosstermColorConv.toCrossterm(cell.bg))));
        fg = cell.fg;
        bg = cell.bg;
      }
      if (!cell.underlineColor.equals(underlineColor)) {
        commands.add(
            new Command.SetUnderlineColor(CrosstermColorConv.toCrossterm(cell.underlineColor)));
        underlineColor = cell.underlineColor;
      }

      commands.add(new Command.Print(cell.symbol()));
    }

    commands.add(new Command.SetForegroundColor(new tui.crossterm.Color.Reset()));
    commands.add(new Command.SetBackgroundColor(new tui.crossterm.Color.Reset()));
    commands.add(new Command.SetUnderlineColor(new tui.crossterm.Color.Reset()));
    commands.add(new Command.SetAttribute(Attribute.Reset));

    enqueue(commands);
  }

  @Override
  public void hideCursor() throws IOException {
    execute(new Command.Hide());
  }

  @Override
  public void showCursor() throws IOException {
    execute(new Command.Show());
  }

  @Override
  public Position getCursorPosition() throws IOException {
    Xy xy = jniCall(writer::cursorPosition);
    return new Position(xy.x(), xy.y());
  }

  @Override
  public void setCursorPosition(Position position) throws IOException {
    execute(new Command.MoveTo(position.x(), position.y()));
  }

  @Override
  public void clear() throws IOException {
    clearRegion(ClearType.All);
  }

  @Override
  public void clearRegion(ClearType clearType) throws IOException {
    tui.crossterm.ClearType native_ =
        switch (clearType) {
          case All -> tui.crossterm.ClearType.All;
          case AfterCursor -> tui.crossterm.ClearType.FromCursorDown;
          case BeforeCursor -> tui.crossterm.ClearType.FromCursorUp;
          case CurrentLine -> tui.crossterm.ClearType.CurrentLine;
          case UntilNewLine -> tui.crossterm.ClearType.UntilNewLine;
        };
    execute(new Command.Clear(native_));
  }

  @Override
  public void appendLines(int n) throws IOException {
    ArrayList<Command> commands = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      commands.add(new Command.Print("\n"));
    }
    enqueue(commands);
    flush();
  }

  @Override
  public Size size() throws IOException {
    Xy xy = jniCall(writer::terminalSize);
    return new Size(xy.x(), xy.y());
  }

  @Override
  public WindowSize windowSize() throws IOException {
    // The local JNI binding only exposes columns/rows via `terminalSize()`. Pixel size is not
    // available, so we report (0, 0) for pixels — matching the upstream behaviour on terminals
    // where `TIOCGWINSZ` returns zero pixel dimensions (see WindowSize Javadoc).
    Xy xy = jniCall(writer::terminalSize);
    return new WindowSize(new Size(xy.x(), xy.y()), new Size(0, 0));
  }

  @Override
  public void flush() throws IOException {
    try {
      writer.flush();
    } catch (RuntimeException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void scrollRegionUp(int regionStart, int regionEnd, int lineCount) throws IOException {
    scrollRegion(regionStart, regionEnd, lineCount, 'S');
  }

  @Override
  public void scrollRegionDown(int regionStart, int regionEnd, int lineCount) throws IOException {
    scrollRegion(regionStart, regionEnd, lineCount, 'T');
  }

  private void scrollRegion(int regionStart, int regionEnd, int lineCount, char op)
      throws IOException {
    if (lineCount == 0) {
      return;
    }
    int top = Math.min(regionStart + 1, 0xFFFF);
    int bottom = Math.min(regionEnd + 1, 0xFFFF);
    enqueue(
        List.of(
            new Command.Print("[" + top + ";" + bottom + "r"),
            new Command.Print("[" + lineCount + op),
            new Command.Print("[r")));
  }

  // ---- internal helpers ----

  /// Enqueue a batch of commands, mapping any native runtime failure to [IOException].
  private void enqueue(List<Command> commands) throws IOException {
    if (commands.isEmpty()) {
      return;
    }
    try {
      writer.enqueue(commands);
    } catch (RuntimeException e) {
      throw new IOException(e);
    }
  }

  /// Execute a single command immediately, mapping any native runtime failure to [IOException].
  private void execute(Command command) throws IOException {
    try {
      writer.execute(command);
    } catch (RuntimeException e) {
      throw new IOException(e);
    }
  }

  private interface JniSupplier<T> {
    T get();
  }

  private static <T> T jniCall(JniSupplier<T> supplier) throws IOException {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      throw new IOException(e);
    }
  }

  // ---- ModifierDiff ----

  /// Calculates the difference between two [Modifier] values, producing the minimal sequence of
  /// `SetAttribute` commands to transition from `from` to `to`.
  ///
  /// Mirrors upstream's private `ModifierDiff` struct.
  static final class ModifierDiff {
    final Modifier from;
    final Modifier to;

    ModifierDiff(Modifier from, Modifier to) {
      this.from = from;
      this.to = to;
    }

    void queue(List<Command> commands) {
      Modifier removed = from.minus(to);
      if (removed.contains(Modifier.REVERSED)) {
        commands.add(new Command.SetAttribute(Attribute.NoReverse));
      }
      if (removed.contains(Modifier.BOLD) || removed.contains(Modifier.DIM)) {
        // Bold and Dim are both reset by applying the Normal intensity.
        commands.add(new Command.SetAttribute(Attribute.NormalIntensity));

        // The remaining Bold and Dim attributes must be reapplied after the intensity reset.
        if (to.contains(Modifier.DIM)) {
          commands.add(new Command.SetAttribute(Attribute.Dim));
        }
        if (to.contains(Modifier.BOLD)) {
          commands.add(new Command.SetAttribute(Attribute.Bold));
        }
      }
      if (removed.contains(Modifier.ITALIC)) {
        commands.add(new Command.SetAttribute(Attribute.NoItalic));
      }
      if (removed.contains(Modifier.UNDERLINED)) {
        commands.add(new Command.SetAttribute(Attribute.NoUnderline));
      }
      if (removed.contains(Modifier.CROSSED_OUT)) {
        commands.add(new Command.SetAttribute(Attribute.NotCrossedOut));
      }
      if (removed.contains(Modifier.SLOW_BLINK) || removed.contains(Modifier.RAPID_BLINK)) {
        commands.add(new Command.SetAttribute(Attribute.NoBlink));
      }

      Modifier added = to.minus(from);
      if (added.contains(Modifier.REVERSED)) {
        commands.add(new Command.SetAttribute(Attribute.Reverse));
      }
      if (added.contains(Modifier.BOLD)) {
        commands.add(new Command.SetAttribute(Attribute.Bold));
      }
      if (added.contains(Modifier.ITALIC)) {
        commands.add(new Command.SetAttribute(Attribute.Italic));
      }
      if (added.contains(Modifier.UNDERLINED)) {
        commands.add(new Command.SetAttribute(Attribute.Underlined));
      }
      if (added.contains(Modifier.DIM)) {
        commands.add(new Command.SetAttribute(Attribute.Dim));
      }
      if (added.contains(Modifier.CROSSED_OUT)) {
        commands.add(new Command.SetAttribute(Attribute.CrossedOut));
      }
      if (added.contains(Modifier.SLOW_BLINK)) {
        commands.add(new Command.SetAttribute(Attribute.SlowBlink));
      }
      if (added.contains(Modifier.RAPID_BLINK)) {
        commands.add(new Command.SetAttribute(Attribute.RapidBlink));
      }
    }
  }
}
