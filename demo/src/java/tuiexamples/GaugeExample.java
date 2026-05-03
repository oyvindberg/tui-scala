package tuiexamples;

import java.time.Duration;
import java.time.Instant;
import tui.Alignment;
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
      return new App(0, 0, 0.0, 0);
    }

    public void update() {
      progress1 = Math.min(progress1 + 4, 100);
      progress2 = Math.min(progress2 + 3, 100);
      progress3 = Math.min(progress3 + 0.02, 1.0);
      progress4 = Math.min(progress4 + 1, 100);
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(100);
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
        app.update();
        lastTick[0] = Instant.now();
      }
    }
  }

  public static void ui(Frame f, App app) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {
              new Constraint.Percentage(25),
              new Constraint.Percentage(25),
              new Constraint.Percentage(25),
              new Constraint.Percentage(25)
            },
            true);
    Rect[] chunks = layout.split(f.size);

    renderGauge1(f, app.progress1, chunks[0]);
    renderGauge2(f, app.progress2, chunks[1]);
    renderGauge3(f, app.progress3, chunks[2]);
    renderGauge4(f, app.progress4, chunks[3]);
  }

  private static BlockWidget titleBlock(String title) {
    return BlockWidget.empty()
        .withTitle(Spans.nostyle(title))
        .withTitleAlignment(Alignment.Center)
        .withBorders(Borders.TOP);
  }

  private static void renderGauge1(Frame f, int progress, Rect area) {
    GaugeWidget gauge =
        GaugeWidget.empty()
            .withBlock(titleBlock("Gauge with percentage progress"))
            .withGaugeStyle(Style.empty().withFg(Color.LightRed))
            .withRatio(GaugeWidget.Ratio.percent(progress));
    f.renderWidget(gauge, area);
  }

  private static void renderGauge2(Frame f, int progress, Rect area) {
    GaugeWidget gauge =
        GaugeWidget.empty()
            .withBlock(titleBlock("Gauge with percentage progress and custom label"))
            .withGaugeStyle(Style.empty().withFg(Color.Blue).withBg(Color.LightBlue))
            .withRatio(GaugeWidget.Ratio.percent(progress))
            .withLabel(Span.nostyle(progress + "/100"));
    f.renderWidget(gauge, area);
  }

  private static void renderGauge3(Frame f, double progress, Rect area) {
    Span label =
        Span.styled(
            String.format("%.2f%%", progress * 100.0),
            Style.empty()
                .withFg(Color.Red)
                .withAddModifier(Modifier.ITALIC.or(Modifier.BOLD)));
    GaugeWidget gauge =
        GaugeWidget.empty()
            .withBlock(
                titleBlock("Gauge with ratio progress, custom label with style, and unicode"))
            .withGaugeStyle(Style.empty().withFg(Color.Yellow))
            .withRatio(new GaugeWidget.Ratio(progress))
            .withLabel(label)
            .withUseUnicode(true);
    f.renderWidget(gauge, area);
  }

  private static void renderGauge4(Frame f, int progress, Rect area) {
    GaugeWidget gauge =
        GaugeWidget.empty()
            .withBlock(titleBlock("Gauge with percentage progress and label"))
            .withGaugeStyle(Style.empty().withFg(Color.Green).withAddModifier(Modifier.ITALIC))
            .withRatio(GaugeWidget.Ratio.percent(progress))
            .withLabel(Span.nostyle(progress + "/100"));
    f.renderWidget(gauge, area);
  }
}
