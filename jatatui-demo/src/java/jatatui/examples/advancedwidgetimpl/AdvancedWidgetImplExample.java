package jatatui.examples.advancedwidgetimpl;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.terminal.Terminal;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to implement the [Widget] interface.
///
/// Java port of `examples/apps/advanced-widget-impl/src/main.rs` from ratatui v0.30.
///
/// The Rust example demonstrates three reference flavours of the `Widget` trait:
/// `impl Widget for App`, `impl Widget for &Timer`, `impl Widget for &mut RightAlignedSquare`,
/// plus boxed `WidgetRef` widgets in `BoxedSquares`. In Java these collapse to a single
/// [Widget] interface — every widget is a regular object that may mutate `this` from inside
/// `render`. The example still shows three distinct use-cases:
///
/// - [Greeting] — built fresh on every render (cheap, single-use).
/// - [Timer] — long-lived widget that keeps state across draws.
/// - [BoxedSquares] — heterogeneous list of widgets stored as `List<Widget>`.
/// - [RightAlignedSquare] — mutates `this` in `render` to remember its last screen position.
public final class AdvancedWidgetImplExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private AdvancedWidgetImplExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  /// Top-level application state.
  static final class App implements Widget {
    boolean shouldQuit;
    final Timer timer = new Timer();
    final BoxedSquares boxedSquares = new BoxedSquares();
    final RightAlignedSquare greenSquare = new RightAlignedSquare();

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      while (!shouldQuit) {
        terminal.draw(frame -> frame.renderWidget(this, frame.area()));
        handleEvents();
      }
    }

    private void handleEvents() throws IOException {
      // Handle events at least 50 frames per second (gifs are usually 50fps).
      Duration timeout = new Duration(0, 20_000_000);
      if (!JNI.poll(timeout)) {
        return;
      }
      Event event = JNI.read();
      if (event instanceof Event.Key key && key.keyEvent().kind() == KeyEventKind.Press) {
        shouldQuit = true;
      }
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Rect[] split =
          area.layout(Layout.vertical(Constraint.fromLengths(1, 1, 2, 1)), 4);
      Rect greeting = split[0];
      Rect timerArea = split[1];
      Rect squares = split[2];
      Rect position = split[3];

      // Ephemeral greeting widget.
      new Greeting("Ratatui!").render(greeting, buf);

      // Reusable timer widget.
      timer.render(timerArea, buf);

      // Boxed widget containing red and blue squares.
      boxedSquares.render(squares, buf);

      // Mutable green-square widget.
      greenSquare.render(squares, buf);

      // Display the dynamically updated position of the green square.
      Widget.renderString(
          "Green square is at " + greenSquare.lastPosition, position, buf);
    }
  }

  /// An ephemeral greeting widget — built fresh per render and consumed.
  static final class Greeting implements Widget {
    private final String name;

    Greeting(String name) {
      this.name = name;
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Widget.renderString("Hello, " + name + "!", area, buf);
    }
  }

  /// A timer widget that displays the elapsed time since it was created.
  static final class Timer implements Widget {
    private final long start = System.nanoTime();

    @Override
    public void render(Rect area, Buffer buf) {
      double elapsed = (System.nanoTime() - start) / 1_000_000_000.0;
      Widget.renderString(String.format("Elapsed: %.1fs", elapsed), area, buf);
    }
  }

  /// A widget that contains a list of several different widgets, rendered side by side.
  static final class BoxedSquares implements Widget {
    private final List<Widget> squares;

    BoxedSquares() {
      this.squares = new ArrayList<>();
      this.squares.add(new RedSquare());
      this.squares.add(new BlueSquare());
    }

    @Override
    public void render(Rect area, Buffer buf) {
      List<Constraint> constraints = new ArrayList<>(squares.size());
      for (int i = 0; i < squares.size(); i++) {
        constraints.add(new Constraint.Length(4));
      }
      List<Rect> areas = area.layoutVec(Layout.horizontal(constraints));
      for (int i = 0; i < squares.size() && i < areas.size(); i++) {
        squares.get(i).render(areas.get(i), buf);
      }
    }
  }

  /// A widget that renders a red square.
  static final class RedSquare implements Widget {
    @Override
    public void render(Rect area, Buffer buf) {
      fill(area, buf, "█", Style.empty().withFg(Color.RED));
    }
  }

  /// A widget that renders a blue square.
  static final class BlueSquare implements Widget {
    @Override
    public void render(Rect area, Buffer buf) {
      fill(area, buf, "█", Style.empty().withFg(Color.BLUE));
    }
  }

  /// A widget that renders a green square aligned to the right of the area, and stores its last
  /// rendered position in a public field so the rest of the app can read it.
  static final class RightAlignedSquare implements Widget {
    Position lastPosition = Position.ORIGIN;

    @Override
    public void render(Rect area, Buffer buf) {
      final int width = 4;
      int x = area.right() - width;
      lastPosition = new Position(x, area.y());
      Size size = new Size(width, area.height());
      Rect target = Rect.fromPositionAndSize(lastPosition, size);
      fill(target, buf, "█", Style.empty().withFg(Color.GREEN));
    }
  }

  /// Fill the area with the given symbol and style.
  private static void fill(Rect area, Buffer buf, String symbol, Style style) {
    for (int y = area.top(); y < area.bottom(); y++) {
      for (int x = area.left(); x < area.right(); x++) {
        buf.cellAt(x, y).setSymbol(symbol).setStyle(style);
      }
    }
  }
}
