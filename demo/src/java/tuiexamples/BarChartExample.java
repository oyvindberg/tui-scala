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
import tui.Modifier;
import tui.Rect;
import tui.Spans;
import tui.Style;
import tui.Symbols;
import tui.Terminal;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BarChartWidget;
import tui.widgets.BlockWidget;

public final class BarChartExample {
  private BarChartExample() {}

  public static final class App {
    public BarChartWidget.LabelValue[] data;

    public App(BarChartWidget.LabelValue[] data) {
      this.data = data;
    }

    public void onTick() {
      BarChartWidget.LabelValue[] next = new BarChartWidget.LabelValue[data.length];
      next[0] = data[data.length - 1];
      System.arraycopy(data, 0, next, 1, data.length - 1);
      data = next;
    }

    public static BarChartWidget.LabelValue[] initialData() {
      return new BarChartWidget.LabelValue[] {
        new BarChartWidget.LabelValue("B1", 9),
        new BarChartWidget.LabelValue("B2", 12),
        new BarChartWidget.LabelValue("B3", 5),
        new BarChartWidget.LabelValue("B4", 8),
        new BarChartWidget.LabelValue("B5", 2),
        new BarChartWidget.LabelValue("B6", 4),
        new BarChartWidget.LabelValue("B7", 5),
        new BarChartWidget.LabelValue("B8", 9),
        new BarChartWidget.LabelValue("B9", 14),
        new BarChartWidget.LabelValue("B10", 15),
        new BarChartWidget.LabelValue("B11", 1),
        new BarChartWidget.LabelValue("B12", 0),
        new BarChartWidget.LabelValue("B13", 4),
        new BarChartWidget.LabelValue("B14", 6),
        new BarChartWidget.LabelValue("B15", 4),
        new BarChartWidget.LabelValue("B16", 6),
        new BarChartWidget.LabelValue("B17", 4),
        new BarChartWidget.LabelValue("B18", 7),
        new BarChartWidget.LabelValue("B19", 13),
        new BarChartWidget.LabelValue("B20", 8),
        new BarChartWidget.LabelValue("B21", 11),
        new BarChartWidget.LabelValue("B22", 9),
        new BarChartWidget.LabelValue("B23", 3),
        new BarChartWidget.LabelValue("B24", 5)
      };
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(250);
      App app = new App(App.initialData());
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
    Layout vertical =
        new Layout(
            Direction.Vertical,
            new Margin(2, 2),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)}, true);
    Rect[] verticalChunks = vertical.split(f.size);

    BarChartWidget barchart1 =
        new BarChartWidget(
            Optional.of(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Data1"))
                    .withBorders(Borders.ALL)),
            9,
            1,
            Symbols.bar.NINE_LEVELS,
            Style.empty().withFg(Color.Yellow),
            Style.empty().withFg(Color.Black).withBg(Color.Yellow),
            Style.DEFAULT,
            Style.DEFAULT,
            app.data,
            Optional.empty());
    f.renderWidget(barchart1, verticalChunks[0]);

    Layout horizontal =
        new Layout(
            Direction.Horizontal,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)}, true);
    Rect[] horizontalChunks = horizontal.split(verticalChunks[1]);

    BarChartWidget barchart2 =
        new BarChartWidget(
            Optional.of(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Data2"))
                    .withBorders(Borders.ALL)),
            5,
            3,
            Symbols.bar.NINE_LEVELS,
            Style.empty().withFg(Color.Green),
            Style.empty().withBg(Color.Green).withAddModifier(Modifier.BOLD),
            Style.DEFAULT,
            Style.DEFAULT,
            app.data,
            Optional.empty());
    f.renderWidget(barchart2, horizontalChunks[0]);

    BarChartWidget barchart3 =
        new BarChartWidget(
            Optional.of(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Data3"))
                    .withBorders(Borders.ALL)),
            7,
            0,
            Symbols.bar.NINE_LEVELS,
            Style.empty().withFg(Color.Red),
            Style.empty().withBg(Color.Red),
            Style.empty().withFg(Color.Cyan).withAddModifier(Modifier.ITALIC),
            Style.DEFAULT,
            app.data,
            Optional.empty());
    f.renderWidget(barchart3, horizontalChunks[1]);
  }
}
