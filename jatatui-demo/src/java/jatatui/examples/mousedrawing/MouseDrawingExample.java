package jatatui.examples.mousedrawing;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.symbols.Block;
import jatatui.core.terminal.Frame;
import jatatui.core.text.Line;
import jatatui.crossterm.Jatatui;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import tui.crossterm.Command;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;
import tui.crossterm.MouseEvent;
import tui.crossterm.MouseEventKind;

/// A jatatui example that demonstrates how to handle mouse events.
///
/// Mirrors `examples/apps/mouse-drawing/src/main.rs` from ratatui v0.30.0.
public final class MouseDrawingExample {

  private static final CrosstermJni JNI = new CrosstermJni();
  private static final Random RNG = new Random();

  private MouseDrawingExample() {}

  /// A drawn point: terminal position and the color it was drawn with.
  ///
  /// Replaces upstream's `(Position, Color)` tuple.
  private record ColoredPoint(Position position, Color color) {}

  private static final class App {
    boolean shouldExit = false;
    Optional<Position> mousePosition = Optional.empty();
    final ArrayList<ColoredPoint> points = new ArrayList<>();
    Color currentColor = Color.RESET;

    void onKeyEvent(KeyEvent key) {
      if (key.kind() != KeyEventKind.Press) {
        return;
      }
      KeyCode code = key.code();
      if (code instanceof KeyCode.Char ch) {
        if (ch.c() == ' ') {
          currentColor =
              new Color.Rgb(RNG.nextInt(256), RNG.nextInt(256), RNG.nextInt(256));
        } else if (ch.c() == 'q') {
          shouldExit = true;
        }
      } else if (code instanceof KeyCode.Esc) {
        shouldExit = true;
      }
    }

    void onMouseEvent(MouseEvent ev) {
      Position position = new Position(ev.column(), ev.row());
      MouseEventKind kind = ev.kind();
      if (kind instanceof MouseEventKind.Down) {
        points.add(new ColoredPoint(position, currentColor));
      } else if (kind instanceof MouseEventKind.Drag) {
        drawLine(position);
      }
      mousePosition = Optional.of(position);
    }

    /// Draw a line between the last point and the given position using Bresenham's algorithm.
    void drawLine(Position position) {
      if (points.isEmpty()) {
        return;
      }
      ColoredPoint start = points.get(points.size() - 1);
      int x0 = start.position().x();
      int y0 = start.position().y();
      int x1 = position.x();
      int y1 = position.y();
      bresenham(x0, y0, x1, y1, currentColor, points);
    }
  }

  /// Bresenham's line algorithm — yields each integer point on the segment from (x0, y0) to
  /// (x1, y1), appending each as a `ColoredPoint` to `out`. Mirrors the `line_drawing::Bresenham`
  /// crate used upstream.
  private static void bresenham(
      int x0, int y0, int x1, int y1, Color color, ArrayList<ColoredPoint> out) {
    int dx = Math.abs(x1 - x0);
    int dy = -Math.abs(y1 - y0);
    int sx = x0 < x1 ? 1 : -1;
    int sy = y0 < y1 ? 1 : -1;
    int err = dx + dy;
    int x = x0;
    int y = y0;
    while (true) {
      out.add(new ColoredPoint(new Position(clampU16(x), clampU16(y)), color));
      if (x == x1 && y == y1) {
        return;
      }
      int e2 = 2 * err;
      if (e2 >= dy) {
        if (x == x1) return;
        err += dy;
        x += sx;
      }
      if (e2 <= dx) {
        if (y == y1) return;
        err += dx;
        y += sy;
      }
    }
  }

  private static int clampU16(int v) {
    if (v < 0) return 0;
    if (v > Position.U16_MAX) return Position.U16_MAX;
    return v;
  }

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(
        terminal -> {
          JNI.execute(new Command.EnableMouseCapture());
          try {
            App app = new App();
            while (!app.shouldExit) {
              terminal.draw(frame -> render(frame, app));
              handleEvents(app);
            }
          } finally {
            JNI.execute(new Command.DisableMouseCapture());
          }
        });
  }

  private static void handleEvents(App app) {
    Event ev = JNI.read();
    if (ev instanceof Event.Key keyEv) {
      app.onKeyEvent(keyEv.keyEvent());
    } else if (ev instanceof Event.Mouse mouseEv) {
      app.onMouseEvent(mouseEv.mouseEvent());
    }
  }

  private static void render(Frame frame, App app) {
    // call order is important here as later elements are drawn on top of earlier elements
    renderPoints(frame, app);
    renderMouseCursor(frame, app);
    String value = "Mouse Example ('Esc' to quit. Click / drag to draw. 'Space' to change color)";
    Line title = Line.from(value).centered();
    frame.renderWidget(jatatui.widgets.paragraph.Paragraph.of(title), frame.area());
  }

  private static void renderPoints(Frame frame, App app) {
    Buffer buf = frame.bufferMut();
    Rect frameArea = frame.area();
    for (ColoredPoint cp : app.points) {
      Rect raw = Rect.fromPositionAndSize(cp.position(), new Size(1, 1));
      Rect area = raw.clamp(frameArea);
      if (area.isEmpty()) continue;
      buf.cellAt(area.x(), area.y())
          .setSymbol(Block.FULL)
          .setStyle(Style.empty().withFg(cp.color()));
    }
  }

  private static void renderMouseCursor(Frame frame, App app) {
    if (app.mousePosition.isEmpty()) {
      return;
    }
    Position position = app.mousePosition.get();
    Rect raw = Rect.fromPositionAndSize(position, new Size(1, 1));
    Rect area = raw.clamp(frame.area());
    if (area.isEmpty()) {
      return;
    }
    frame.bufferMut()
        .cellAt(area.x(), area.y())
        .setSymbol("╳")
        .setStyle(Style.empty().withBg(app.currentColor));
  }
}
