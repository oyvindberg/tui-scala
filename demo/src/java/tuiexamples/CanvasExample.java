package tuiexamples;

import java.time.Duration;
import java.time.Instant;
import tui.Borders;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Point;
import tui.Rect;
import tui.Span;
import tui.Spans;
import tui.Style;
import tui.Symbols;
import tui.Terminal;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.canvas.CanvasWidget;
import tui.widgets.canvas.MapResolution;
import tui.widgets.canvas.Rectangle;
import tui.widgets.canvas.WorldMap;

public final class CanvasExample {
  private CanvasExample() {}

  public static final class App {
    public double x;
    public double y;
    public Rectangle ball;
    public Rect playground;
    public double vx;
    public double vy;
    public long tickCount;
    public Symbols.Marker marker;

    public App(
        double x,
        double y,
        Rectangle ball,
        Rect playground,
        double vx,
        double vy,
        long tickCount,
        Symbols.Marker marker) {
      this.x = x;
      this.y = y;
      this.ball = ball;
      this.playground = playground;
      this.vx = vx;
      this.vy = vy;
      this.tickCount = tickCount;
      this.marker = marker;
    }

    public static App empty() {
      return new App(
          0.0,
          0.0,
          new Rectangle(20.0, 40.0, 10.0, 10.0, Color.Yellow),
          new Rect(10, 10, 200, 100),
          1.0,
          1.0,
          0,
          Symbols.Marker.Dot);
    }

    public void onTick() {
      tickCount += 1;
      // Cycle the marker every 180 ticks (~3s) to avoid stroboscopic effect.
      if (tickCount % 180 == 0) {
        marker =
            switch (marker) {
              case Dot -> Symbols.Marker.Braille;
              case Braille -> Symbols.Marker.Block;
              case Block -> Symbols.Marker.Bar;
              case Bar -> Symbols.Marker.Dot;
            };
      }

      if (ball.x < (double) playground.left()
          || ball.x + ball.width > (double) playground.right()) {
        vx = -vx;
      }
      if (ball.y < (double) playground.top()
          || ball.y + ball.height > (double) playground.bottom()) {
        vy = -vy;
      }
      ball = new Rectangle(ball.x + vx, ball.y + vy, ball.width, ball.height, ball.color);
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(16);
      App app = App.empty();
      runApp(terminal, app, tickRate, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, App app, Duration tickRate, CrosstermJni jni) {
    Instant[] lastTick = {Instant.now()};

    while (true) {
      terminal.draw(f -> ui(f, app));

      Duration elapsed = Duration.between(lastTick[0], Instant.now());
      Duration remaining = tickRate.minus(elapsed);
      tui.crossterm.Duration timeout =
          new tui.crossterm.Duration(remaining.toSeconds(), remaining.getNano());
      if (jni.poll(timeout)) {
        Event ev = jni.read();
        if (ev instanceof Event.Key key) {
          tui.crossterm.KeyCode code = key.keyEvent().code();
          if (code instanceof KeyCode.Char c && c.c() == 'q') {
            return;
          } else if (code instanceof KeyCode.Down) {
            app.y += 1.0;
          } else if (code instanceof KeyCode.Up) {
            app.y -= 1.0;
          } else if (code instanceof KeyCode.Right) {
            app.x += 1.0;
          } else if (code instanceof KeyCode.Left) {
            app.x -= 1.0;
          }
        }
      }
      Duration elapsed2 = Duration.between(lastTick[0], Instant.now());
      if (elapsed2.compareTo(tickRate) >= 0) {
        app.onTick();
        lastTick[0] = Instant.now();
      }
    }
  }

  public static void ui(Frame f, App app) {
    Layout main =
        new Layout(
            Direction.Horizontal,
            Margin.of(0),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)},
            true);
    Rect[] mainChunks = main.split(f.size);

    Layout right =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)},
            true);
    Rect[] rightChunks = right.split(mainChunks[1]);

    f.renderWidget(mapCanvas(app), mainChunks[0]);
    f.renderWidget(pongCanvas(app), rightChunks[0]);
    f.renderWidget(boxesCanvas(app, rightChunks[1]), rightChunks[1]);
  }

  private static CanvasWidget mapCanvas(App app) {
    return CanvasWidget.empty(
            ctx -> {
              ctx.draw(new WorldMap(MapResolution.High, Color.Green));
              ctx.print(
                  app.x,
                  -app.y,
                  Spans.from(
                      Span.styled("You are here", Style.empty().withFg(Color.Yellow))));
            })
        .withBlock(
            BlockWidget.empty()
                .withBorders(Borders.ALL)
                .withTitle(Spans.nostyle("World")))
        .withMarker(app.marker)
        .withXBounds(new Point(-180.0, 180.0))
        .withYBounds(new Point(-90.0, 90.0));
  }

  private static CanvasWidget pongCanvas(App app) {
    return CanvasWidget.empty(ctx -> ctx.draw(app.ball))
        .withBlock(
            BlockWidget.empty()
                .withBorders(Borders.ALL)
                .withTitle(Spans.nostyle("Pong")))
        .withMarker(app.marker)
        .withXBounds(new Point(10.0, 210.0))
        .withYBounds(new Point(10.0, 110.0));
  }

  private static CanvasWidget boxesCanvas(App app, Rect area) {
    double left = 0.0;
    double right = (double) area.width();
    double bottom = 0.0;
    double top = (double) area.height() * 2.0 - 4.0;
    return CanvasWidget.empty(
            ctx -> {
              for (int i = 0; i <= 11; i++) {
                double x = (double) (i * i + 3 * i) / 2.0 + 2.0;
                double size = (double) i;
                ctx.draw(new Rectangle(x, 2.0, size, size, Color.Red));
                ctx.draw(new Rectangle(x, 21.0, size, size, Color.Blue));
              }
              for (int i = 0; i < 100; i++) {
                if (i % 10 != 0) {
                  ctx.print((double) i + 1.0, 0.0, Spans.nostyle(Integer.toString(i % 10)));
                }
                if (i % 2 == 0 && i % 10 != 0) {
                  ctx.print(0.0, (double) i, Spans.nostyle(Integer.toString(i % 10)));
                }
              }
            })
        .withBlock(
            BlockWidget.empty()
                .withBorders(Borders.ALL)
                .withTitle(Spans.nostyle("Rects")))
        .withMarker(app.marker)
        .withXBounds(new Point(left, right))
        .withYBounds(new Point(bottom, top));
  }
}
