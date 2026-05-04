package jatatui.examples.gauge;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.style.palette.Tailwind;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.Padding;
import jatatui.widgets.gauge.Gauge;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates different types of gauges.
///
/// Mirrors `examples/apps/gauge/src/main.rs` from ratatui v0.30.0.
public final class GaugeExample {

  private static final Color GAUGE1_COLOR = Tailwind.RED.c800();
  private static final Color GAUGE2_COLOR = Tailwind.GREEN.c800();
  private static final Color GAUGE3_COLOR = Tailwind.BLUE.c800();
  private static final Color GAUGE4_COLOR = Tailwind.ORANGE.c800();
  private static final Color CUSTOM_LABEL_COLOR = Tailwind.SLATE.c200();

  private static final CrosstermJni JNI = new CrosstermJni();

  private GaugeExample() {}

  /// Application state: current step in the gauge progression and an [AppState] tag.
  private static final class App {
    AppState state = AppState.Running;
    int progressColumns = 0;
    int progress1 = 0;
    double progress2 = 0.0;
    double progress3 = 40.0;
    double progress4 = 40.0;

    void update(int terminalWidth) {
      if (state != AppState.Started) {
        return;
      }
      int next = progressColumns + 1;
      if (next < 0) next = 0;
      if (next > terminalWidth) next = terminalWidth;
      progressColumns = next;
      int safeWidth = Math.max(1, terminalWidth);
      progress1 = (progressColumns * 100) / safeWidth;
      progress2 = ((double) progressColumns * 100.0) / (double) safeWidth;
      progress3 = clamp(progress3 + 0.1, 40.0, 100.0);
      progress4 = clamp(progress4 + 0.1, 40.0, 100.0);
    }

    void start() {
      state = AppState.Started;
    }

    void quit() {
      state = AppState.Quitting;
    }
  }

  private enum AppState {
    Running,
    Started,
    Quitting
  }

  private static double clamp(double v, double lo, double hi) {
    if (v < lo) return lo;
    if (v > hi) return hi;
    return v;
  }

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(
        terminal -> {
          App app = new App();
          while (app.state != AppState.Quitting) {
            terminal.draw(frame -> render(frame.bufferMut(), frame.area(), app));
            handleEvents(app);
            app.update(terminal.size().width());
          }
        });
  }

  private static void handleEvents(App app) throws IOException {
    // 1/20s = 50ms
    Duration timeout = new Duration(0, 50_000_000);
    if (!JNI.poll(timeout)) {
      return;
    }
    Event ev = JNI.read();
    if (!(ev instanceof Event.Key keyEv)) {
      return;
    }
    KeyEvent key = keyEv.keyEvent();
    if (key.kind() != KeyEventKind.Press) {
      return;
    }
    KeyCode code = key.code();
    if (code instanceof KeyCode.Char ch) {
      if (ch.c() == ' ') {
        app.start();
      } else if (ch.c() == 'q') {
        app.quit();
      }
    } else if (code instanceof KeyCode.Enter) {
      app.start();
    } else if (code instanceof KeyCode.Esc) {
      app.quit();
    }
  }

  private static void render(Buffer buf, Rect area, App app) {
    Layout vertical =
        Layout.vertical(new Constraint.Length(2), new Constraint.Min(0), new Constraint.Length(1));
    Rect[] outer = area.layout(vertical, 3);
    Rect headerArea = outer[0];
    Rect gaugeArea = outer[1];
    Rect footerArea = outer[2];

    Layout gaugesLayout =
        Layout.vertical(
            new Constraint.Ratio(1, 4),
            new Constraint.Ratio(1, 4),
            new Constraint.Ratio(1, 4),
            new Constraint.Ratio(1, 4));
    Rect[] gauges = gaugeArea.layout(gaugesLayout, 4);

    renderHeader(headerArea, buf);
    renderFooter(footerArea, buf);

    renderGauge1(gauges[0], buf, app);
    renderGauge2(gauges[1], buf, app);
    renderGauge3(gauges[2], buf, app);
    renderGauge4(gauges[3], buf, app);
  }

  private static void renderHeader(Rect area, Buffer buf) {
    Paragraph.of("Ratatui Gauge Example")
        .withStyle(Style.empty().withFg(CUSTOM_LABEL_COLOR).bold())
        .withAlignment(HorizontalAlignment.Center)
        .render(area, buf);
  }

  private static void renderFooter(Rect area, Buffer buf) {
    Paragraph.of("Press ENTER to start")
        .withStyle(Style.empty().withFg(CUSTOM_LABEL_COLOR).bold())
        .withAlignment(HorizontalAlignment.Center)
        .render(area, buf);
  }

  private static void renderGauge1(Rect area, Buffer buf, App app) {
    Block title = titleBlock("Gauge with percentage");
    Gauge.empty()
        .withBlock(title)
        .withGaugeStyle(Style.empty().withFg(GAUGE1_COLOR))
        .withPercent(app.progress1)
        .render(area, buf);
  }

  private static void renderGauge2(Rect area, Buffer buf, App app) {
    Block title = titleBlock("Gauge with ratio and custom label");
    Span label =
        Span.styled(
            String.format("%.1f/100", app.progress2),
            Style.empty().italic().bold().withFg(CUSTOM_LABEL_COLOR));
    Gauge.empty()
        .withBlock(title)
        .withGaugeStyle(Style.empty().withFg(GAUGE2_COLOR))
        .withRatio(app.progress2 / 100.0)
        .withLabel(label)
        .render(area, buf);
  }

  private static void renderGauge3(Rect area, Buffer buf, App app) {
    Block title = titleBlock("Gauge with ratio (no unicode)");
    String label = String.format("%.1f%%", app.progress3);
    Gauge.empty()
        .withBlock(title)
        .withGaugeStyle(Style.empty().withFg(GAUGE3_COLOR))
        .withRatio(app.progress3 / 100.0)
        .withLabel(label)
        .render(area, buf);
  }

  private static void renderGauge4(Rect area, Buffer buf, App app) {
    Block title = titleBlock("Gauge with ratio (unicode)");
    String label = String.format("%.1f%%", app.progress3);
    Gauge.empty()
        .withBlock(title)
        .withGaugeStyle(Style.empty().withFg(GAUGE4_COLOR))
        .withRatio(app.progress4 / 100.0)
        .withLabel(label)
        .withUseUnicode(true)
        .render(area, buf);
  }

  private static Block titleBlock(String title) {
    Line line = Line.from(title).centered();
    return Block.empty()
        .withBorders(Borders.NONE)
        .withPadding(Padding.vertical(1))
        .withTitle(line)
        .withStyle(Style.empty().withFg(CUSTOM_LABEL_COLOR));
  }
}
