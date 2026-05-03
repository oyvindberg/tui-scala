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
import tui.Modifier;
import tui.Rect;
import tui.Span;
import tui.Spans;
import tui.Style;
import tui.Terminal;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.GaugeWidget;

public final class GaugeExample {
  private GaugeExample() {}

  public static final class App {
    public int progress1;
    public int progress2;
    public double progress3;
    public int progress4;

    public App(int progress1, int progress2, double progress3, int progress4) {
      this.progress1 = progress1;
      this.progress2 = progress2;
      this.progress3 = progress3;
      this.progress4 = progress4;
    }

    public static App empty() {
      return new App(0, 0, 0.45, 0);
    }

    public void onTick() {
      progress1 += 1;
      if (progress1 > 100) {
        progress1 = 0;
      }
      progress2 += 2;
      if (progress2 > 100) {
        progress2 = 0;
      }
      progress3 += 0.001;
      if (progress3 > 1.0) {
        progress3 = 0.0;
      }
      progress4 += 1;
      if (progress4 > 100) {
        progress4 = 0;
      }
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
    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(2),
            new Constraint[] {
              new Constraint.Percentage(25),
              new Constraint.Percentage(25),
              new Constraint.Percentage(25),
              new Constraint.Percentage(25)
            }, true);
    Rect[] chunks = layout.split(f.size);

    GaugeWidget gauge0 =
        GaugeWidget.empty()
            .withBlock(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Gauge1"))
                    .withBorders(Borders.ALL))
            .withGaugeStyle(Style.empty().withFg(Color.Yellow))
            .withRatio(GaugeWidget.Ratio.percent(app.progress1));
    f.renderWidget(gauge0, chunks[0]);

    GaugeWidget gauge1 =
        GaugeWidget.empty()
            .withBlock(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Gauge2"))
                    .withBorders(Borders.ALL))
            .withGaugeStyle(Style.empty().withFg(Color.Magenta).withBg(Color.Green))
            .withRatio(GaugeWidget.Ratio.percent(app.progress2))
            .withLabel(Span.nostyle(app.progress2 + "/100"));
    f.renderWidget(gauge1, chunks[1]);

    GaugeWidget gauge2 =
        GaugeWidget.empty()
            .withBlock(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Gauge3"))
                    .withBorders(Borders.ALL))
            .withGaugeStyle(Style.empty().withFg(Color.Yellow))
            .withRatio(new GaugeWidget.Ratio(app.progress3))
            .withLabel(
                Span.styled(
                    String.format("%.2f", app.progress3 * 100.0),
                    Style.empty()
                        .withFg(Color.Red)
                        .withAddModifier(Modifier.ITALIC.or(Modifier.BOLD))))
            .withUseUnicode(true);
    f.renderWidget(gauge2, chunks[2]);

    GaugeWidget gauge3 =
        GaugeWidget.empty()
            .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Gauge4")))
            .withGaugeStyle(Style.empty().withFg(Color.Cyan).withAddModifier(Modifier.ITALIC))
            .withRatio(GaugeWidget.Ratio.percent(app.progress4))
            .withLabel(Span.nostyle(app.progress4 + "/100"));
    f.renderWidget(gauge3, chunks[3]);
  }
}
