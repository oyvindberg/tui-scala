package tuiexamples;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
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
    public boolean dirX;
    public boolean dirY;

    public App(
        double x,
        double y,
        Rectangle ball,
        Rect playground,
        double vx,
        double vy,
        boolean dirX,
        boolean dirY) {
      this.x = x;
      this.y = y;
      this.ball = ball;
      this.playground = playground;
      this.vx = vx;
      this.vy = vy;
      this.dirX = dirX;
      this.dirY = dirY;
    }

    public static App empty() {
      return new App(
          0.0,
          0.0,
          new Rectangle(10.0, 30.0, 10.0, 10.0, Color.Yellow),
          new Rect(10, 10, 100, 100),
          1.0,
          1.0,
          true,
          true);
    }

    public void onTick() {
      if (ball.x < (double) playground.left()
          || ball.x + ball.width > (double) playground.right()) {
        dirX = !dirX;
      }
      if (ball.y < (double) playground.top()
          || ball.y + ball.height > (double) playground.bottom()) {
        dirY = !dirY;
      }

      double newX = dirX ? ball.x + vx : ball.x - vx;
      double newY = dirY ? ball.x + vy : ball.x - vy;
      ball = new Rectangle(newX, newY, ball.width, ball.height, ball.color);
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(250);
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
    Layout layout =
        new Layout(
            Direction.Horizontal,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)}, true);
    Rect[] chunks = layout.split(f.size);

    CanvasWidget canvas0 =
        new CanvasWidget(
            Optional.of(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle("World"))),
            new Point(-180.0, 180.0),
            new Point(-90.0, 90.0),
            Color.Reset,
            Symbols.Marker.Braille,
            ctx -> {
              ctx.draw(new WorldMap(MapResolution.High, Color.White));
              ctx.print(
                  app.x,
                  -app.y,
                  Spans.from(
                      Span.styled("You are here", Style.empty().withFg(Color.Yellow))));
            });
    f.renderWidget(canvas0, chunks[0]);

    CanvasWidget canvas1 =
        new CanvasWidget(
            Optional.of(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle("Pong"))),
            new Point(10.0, 110.0),
            new Point(10.0, 110.0),
            Color.Reset,
            Symbols.Marker.Braille,
            ctx -> ctx.draw(app.ball));
    f.renderWidget(canvas1, chunks[1]);
  }
}
