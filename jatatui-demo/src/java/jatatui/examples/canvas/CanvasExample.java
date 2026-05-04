package jatatui.examples.canvas;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.symbols.Marker;
import jatatui.core.text.Line;
import jatatui.core.text.Text;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Circle;
import jatatui.widgets.canvas.Coord;
import jatatui.widgets.canvas.Map;
import jatatui.widgets.canvas.MapResolution;
import jatatui.widgets.canvas.Points;
import jatatui.widgets.canvas.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import tui.crossterm.Command;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;
import tui.crossterm.MouseEvent;
import tui.crossterm.MouseEventKind;

/// A jatatui example that demonstrates how to draw on a canvas.
///
/// Mirrors `examples/apps/canvas/src/main.rs` from ratatui v0.30.0.
public final class CanvasExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private CanvasExample() {}

  /// Mutable application state — bouncing ball, free-draw points, and the active marker.
  private static final class App {
    boolean exit = false;
    double x = 0.0;
    double y = 0.0;
    Circle ball = new Circle(20.0, 40.0, 10.0, Color.YELLOW);
    final Rect playground = new Rect(10, 10, 200, 100);
    double vx = 1.0;
    double vy = 1.0;
    Marker marker = Marker.Dot;
    final ArrayList<Position> points = new ArrayList<>();
    boolean isDrawing = false;

    void handleKeyEvent(KeyEvent key) {
      if (key.kind() != KeyEventKind.Press) {
        return;
      }
      KeyCode code = key.code();
      if (code instanceof KeyCode.Char ch) {
        switch (ch.c()) {
          case 'q' -> exit = true;
          case 'j' -> y += 1.0;
          case 'k' -> y -= 1.0;
          case 'l' -> x += 1.0;
          case 'h' -> x -= 1.0;
          default -> {
            // ignored
          }
        }
      } else if (code instanceof KeyCode.Esc) {
        exit = true;
      } else if (code instanceof KeyCode.Down) {
        y += 1.0;
      } else if (code instanceof KeyCode.Up) {
        y -= 1.0;
      } else if (code instanceof KeyCode.Right) {
        x += 1.0;
      } else if (code instanceof KeyCode.Left) {
        x -= 1.0;
      } else if (code instanceof KeyCode.Enter) {
        cycleMarker();
      }
    }

    void handleMouseEvent(MouseEvent ev) {
      MouseEventKind kind = ev.kind();
      if (kind instanceof MouseEventKind.Down) {
        isDrawing = true;
      } else if (kind instanceof MouseEventKind.Up) {
        isDrawing = false;
      } else if (kind instanceof MouseEventKind.Drag) {
        points.add(new Position(ev.column(), ev.row()));
      }
    }

    void cycleMarker() {
      marker =
          switch (marker) {
            case Dot -> Marker.Braille;
            case Braille -> Marker.Block;
            case Block -> Marker.HalfBlock;
            case HalfBlock -> Marker.Quadrant;
            case Quadrant -> Marker.Sextant;
            case Sextant -> Marker.Octant;
            case Octant -> Marker.Bar;
            case Bar -> Marker.Dot;
          };
    }

    void onTick() {
      // bounce the ball by flipping the velocity vector
      if (ball.x() - ball.radius() < (double) playground.left()
          || ball.x() + ball.radius() > (double) playground.right()) {
        vx = -vx;
      }
      if (ball.y() - ball.radius() < (double) playground.top()
          || ball.y() + ball.radius() > (double) playground.bottom()) {
        vy = -vy;
      }
      ball = new Circle(ball.x() + vx, ball.y() + vy, ball.radius(), ball.color());
    }
  }

  public static void main(String[] args) throws IOException {
    JNI.execute(new Command.EnableMouseCapture());
    try {
      Jatatui.runIo(
          terminal -> {
            App app = new App();
            // 16 ms tick rate
            long tickRateNanos = 16_000_000L;
            long lastTickNanos = System.nanoTime();
            while (!app.exit) {
              terminal.draw(frame -> render(frame, app));
              long elapsedNanos = System.nanoTime() - lastTickNanos;
              long remainingNanos = Math.max(0L, tickRateNanos - elapsedNanos);
              Duration timeout =
                  new Duration(
                      remainingNanos / 1_000_000_000L, (int) (remainingNanos % 1_000_000_000L));
              if (!JNI.poll(timeout)) {
                app.onTick();
                lastTickNanos = System.nanoTime();
                continue;
              }
              Event ev = JNI.read();
              if (ev instanceof Event.Key keyEv) {
                app.handleKeyEvent(keyEv.keyEvent());
              } else if (ev instanceof Event.Mouse mouseEv) {
                app.handleMouseEvent(mouseEv.mouseEvent());
              }
            }
          });
    } finally {
      JNI.execute(new Command.DisableMouseCapture());
    }
  }

  private static void render(jatatui.core.terminal.Frame frame, App app) {
    Text header =
        Text.from(
            Line.styled("Canvas Example", Style.empty().bold()),
            Line.from("<q> Quit | <enter> Change Marker | <hjkl> Move"));

    Layout vertical =
        Layout.vertical(
            new Constraint.Length(header.height()), new Constraint.Fill(1), new Constraint.Fill(1));
    Rect[] rows = frame.area().layout(vertical, 3);
    Rect textArea = rows[0];
    Rect up = rows[1];
    Rect down = rows[2];

    frame.renderWidget(
        jatatui.widgets.paragraph.Paragraph.of(header.centered())
            .withAlignment(HorizontalAlignment.Center),
        textArea);

    Layout horizontal = Layout.horizontal(new Constraint.Fill(1), new Constraint.Fill(1));
    Rect[] upHalves = up.layout(horizontal, 2);
    Rect[] downHalves = down.layout(horizontal, 2);
    Rect drawArea = upHalves[0];
    Rect pongArea = upHalves[1];
    Rect mapArea = downHalves[0];
    Rect boxesArea = downHalves[1];

    frame.renderWidget(mapCanvas(app), mapArea);
    frame.renderWidget(drawCanvas(app, drawArea), drawArea);
    frame.renderWidget(pongCanvas(app), pongArea);
    frame.renderWidget(boxesCanvas(app, boxesArea), boxesArea);
  }

  private static Canvas mapCanvas(App app) {
    final double appX = app.x;
    final double appY = app.y;
    return Canvas.empty()
        .withBlock(Block.bordered().withTitle("World"))
        .withMarker(app.marker)
        .withPaintFn(
            ctx -> {
              ctx.draw(new Map(MapResolution.High, Color.GREEN));
              ctx.print(appX, -appY, Line.styled("You are here", Style.empty().yellow()));
            })
        .withXBounds(new double[] {-180.0, 180.0})
        .withYBounds(new double[] {-90.0, 90.0});
  }

  private static Canvas drawCanvas(App app, Rect area) {
    final ArrayList<Position> snapshot = new ArrayList<>(app.points);
    final int areaLeft = area.left();
    final int areaBottom = area.bottom();
    return Canvas.empty()
        .withBlock(Block.bordered().withTitle("Draw here"))
        .withMarker(app.marker)
        .withXBounds(new double[] {0.0, (double) area.width()})
        .withYBounds(new double[] {0.0, (double) area.height()})
        .withPaintFn(
            ctx -> {
              Coord[] coords = new Coord[snapshot.size()];
              for (int i = 0; i < snapshot.size(); i++) {
                Position p = snapshot.get(i);
                coords[i] =
                    new Coord(
                        (double) p.x() - (double) areaLeft, (double) areaBottom - (double) p.y());
              }
              ctx.draw(new Points(coords, Color.WHITE));
            });
  }

  private static Canvas pongCanvas(App app) {
    final Circle ball = app.ball;
    return Canvas.empty()
        .withBlock(Block.bordered().withTitle("Pong"))
        .withMarker(app.marker)
        .withPaintFn(ctx -> ctx.draw(ball))
        .withXBounds(new double[] {10.0, 210.0})
        .withYBounds(new double[] {10.0, 110.0});
  }

  private static Canvas boxesCanvas(App app, Rect area) {
    double left = 0.0;
    double right = (double) area.width();
    double bottom = 0.0;
    double top = (double) area.height() * 2.0 - 4.0;
    return Canvas.empty()
        .withBlock(Block.bordered().withTitle("Rects"))
        .withMarker(app.marker)
        .withXBounds(new double[] {left, right})
        .withYBounds(new double[] {bottom, top})
        .withPaintFn(
            ctx -> {
              for (int i = 0; i <= 11; i++) {
                double x = (double) (i * i + 3 * i) / 2.0 + 2.0;
                double size = (double) i;
                ctx.draw(new Rectangle(x, 2.0, size, size, Color.RED));
                ctx.draw(new Rectangle(x, 21.0, size, size, Color.BLUE));
              }
              for (int i = 0; i < 100; i++) {
                if (i % 10 != 0) {
                  ctx.print((double) i + 1.0, 0.0, Integer.toString(i % 10));
                }
                if (i % 2 == 0 && i % 10 != 0) {
                  ctx.print(0.0, (double) i, Integer.toString(i % 10));
                }
              }
            });
  }
}
