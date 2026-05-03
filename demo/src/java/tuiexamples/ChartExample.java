package tuiexamples;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import tui.Borders;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Modifier;
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
import tui.widgets.ChartWidget;

public final class ChartExample {
  private ChartExample() {}

  public static final Point[] DATA = {
    Point.Zero,
    new Point(1.0, 1.0),
    new Point(2.0, 2.0),
    new Point(3.0, 3.0),
    new Point(4.0, 4.0)
  };

  public static final Point[] DATA2 = {
    Point.Zero,
    new Point(10.0, 1.0),
    new Point(20.0, 0.5),
    new Point(30.0, 1.5),
    new Point(40.0, 1.0),
    new Point(50.0, 2.5),
    new Point(60.0, 3.0)
  };

  public static final class SinSignal implements Iterator<Point> {
    public final double interval;
    public final double period;
    public final double scale;
    public double x;

    public SinSignal(double interval, double period, double scale) {
      this.interval = interval;
      this.period = period;
      this.scale = scale;
      this.x = 0.0;
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public Point next() {
      Point p = new Point(x, Math.sin(x * 1.0 / period) * scale);
      x += interval;
      return p;
    }

    public Point[] take(int n) {
      Point[] arr = new Point[n];
      for (int i = 0; i < n; i++) arr[i] = next();
      return arr;
    }
  }

  public static final class App {
    public final SinSignal signal1;
    public Point[] data1;
    public final SinSignal signal2;
    public Point[] data2;
    public Point window;

    public App(
        SinSignal signal1, Point[] data1, SinSignal signal2, Point[] data2, Point window) {
      this.signal1 = signal1;
      this.data1 = data1;
      this.signal2 = signal2;
      this.data2 = data2;
      this.window = window;
    }

    public static App create() {
      SinSignal signal1 = new SinSignal(0.2, 3.0, 18.0);
      SinSignal signal2 = new SinSignal(0.1, 2.0, 10.0);
      Point[] data1 = signal1.take(200);
      Point[] data2 = signal2.take(200);
      return new App(signal1, data1, signal2, data2, new Point(0.0, 20.0));
    }

    public void onTick() {
      data1 = drop(data1, 5, signal1);
      data2 = drop(data2, 10, signal2);
      window = new Point(window.x() + 1.0, window.y() + 1.0);
    }

    private static Point[] drop(Point[] data, int n, SinSignal signal) {
      Point[] next = new Point[data.length];
      System.arraycopy(data, n, next, 0, data.length - n);
      for (int i = 0; i < n; i++) {
        next[data.length - n + i] = signal.next();
      }
      return next;
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(250);
      App app = App.create();
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
          if (key.keyEvent().code() instanceof KeyCode.Char c && c.c() == 'q') {
            return;
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
    Rect size = f.size;
    Layout layout =
        new Layout(
            Direction.Vertical,
            new Margin(0, 0),
            new Constraint[] {
              new Constraint.Ratio(1, 3), new Constraint.Ratio(1, 3), new Constraint.Ratio(1, 3)
            }, true);
    Rect[] chunks = layout.split(size);

    Style bold = Style.empty().withAddModifier(Modifier.BOLD);

    Span[] xLabels = {
      Span.styled(Double.toString(app.window.x()), bold),
      Span.nostyle(Double.toString((app.window.x() + app.window.y()) / 2.0)),
      Span.styled(Double.toString(app.window.y()), bold)
    };

    {
      ChartWidget.Dataset[] datasets = {
        new ChartWidget.Dataset(
            "data2",
            app.data1,
            Symbols.Marker.Dot,
            ChartWidget.GraphType.Scatter,
            Style.empty().withFg(Color.Cyan)),
        new ChartWidget.Dataset(
            "data3",
            app.data2,
            Symbols.Marker.Braille,
            ChartWidget.GraphType.Scatter,
            Style.empty().withFg(Color.Yellow))
      };

      ChartWidget chart =
          ChartWidget.empty(datasets)
              .withBlock(
                  BlockWidget.empty()
                      .withTitle(
                          Spans.from(
                              Span.styled(
                                  "Chart 1",
                                  Style.empty()
                                      .withFg(Color.Cyan)
                                      .withAddModifier(Modifier.BOLD))))
                      .withBorders(Borders.ALL))
              .withXAxis(
                  ChartWidget.Axis.empty()
                      .withTitle(Spans.nostyle("X Axis"))
                      .withStyle(Style.empty().withFg(Color.Gray))
                      .withLabels(xLabels)
                      .withBounds(app.window))
              .withYAxis(
                  ChartWidget.Axis.empty()
                      .withTitle(Spans.nostyle("Y Axis"))
                      .withStyle(Style.empty().withFg(Color.Gray))
                      .withLabels(
                          new Span[] {
                            Span.styled("-20", bold),
                            Span.nostyle("0"),
                            Span.styled("20", bold)
                          })
                      .withBounds(new Point(-20.0, 20.0)));
      f.renderWidget(chart, chunks[0]);
    }

    {
      ChartWidget.Dataset[] datasets = {
        new ChartWidget.Dataset(
            "data",
            DATA,
            Symbols.Marker.Braille,
            ChartWidget.GraphType.Line,
            Style.empty().withFg(Color.Yellow))
      };
      ChartWidget chart =
          ChartWidget.empty(datasets)
              .withBlock(
                  BlockWidget.empty()
                      .withTitle(
                          Spans.from(
                              Span.styled(
                                  "Chart 2",
                                  Style.empty()
                                      .withFg(Color.Cyan)
                                      .withAddModifier(Modifier.BOLD))))
                      .withBorders(Borders.ALL))
              .withXAxis(
                  ChartWidget.Axis.empty()
                      .withTitle(Spans.nostyle("X Axis"))
                      .withStyle(Style.empty().withFg(Color.Gray))
                      .withBounds(new Point(0.0, 5.0))
                      .withLabels(
                          new Span[] {
                            Span.styled("0", bold),
                            Span.nostyle("2.5"),
                            Span.styled("5.0", bold)
                          }))
              .withYAxis(
                  ChartWidget.Axis.empty()
                      .withTitle(Spans.nostyle("Y Axis"))
                      .withStyle(Style.empty().withFg(Color.Gray))
                      .withBounds(new Point(0.0, 5.0))
                      .withLabels(
                          new Span[] {
                            Span.styled("0", bold),
                            Span.nostyle("2.5"),
                            Span.styled("5.0", bold)
                          }));
      f.renderWidget(chart, chunks[1]);
    }

    {
      ChartWidget.Dataset[] datasets = {
        new ChartWidget.Dataset(
            "data",
            DATA2,
            Symbols.Marker.Braille,
            ChartWidget.GraphType.Line,
            Style.empty().withFg(Color.Yellow))
      };
      ChartWidget chart =
          ChartWidget.empty(datasets)
              .withBlock(
                  BlockWidget.empty()
                      .withTitle(
                          Spans.from(
                              Span.styled(
                                  "Chart 3",
                                  Style.empty()
                                      .withFg(Color.Cyan)
                                      .withAddModifier(Modifier.BOLD))))
                      .withBorders(Borders.ALL))
              .withXAxis(
                  ChartWidget.Axis.empty()
                      .withTitle(Spans.nostyle("X Axis"))
                      .withBounds(new Point(0.0, 50.0))
                      .withLabels(
                          new Span[] {
                            Span.styled("0", bold),
                            Span.nostyle("25"),
                            Span.styled("50", bold)
                          })
                      .withStyle(Style.empty().withFg(Color.Gray)))
              .withYAxis(
                  ChartWidget.Axis.empty()
                      .withTitle(Spans.nostyle("Y Axis"))
                      .withStyle(Style.empty().withFg(Color.Gray))
                      .withBounds(new Point(0.0, 5.0))
                      .withLabels(
                          new Span[] {
                            Span.styled("0", bold),
                            Span.nostyle("2.5"),
                            Span.styled("5", bold)
                          }));
      f.renderWidget(chart, chunks[2]);
    }
  }
}
