package jatatui.react.examples.gauge;

import static jatatui.react.Components.*;

import jatatui.components.gauge.GaugeProps;
import jatatui.components.gauge.Gauges;
import jatatui.core.layout.Margin;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.ReactApp;
import java.io.IOException;
import java.util.Optional;
import tui.crossterm.KeyCode;

/// Three controlled [jatatui.widgets.gauge.Gauge]s side-by-side. Tab cycles focus, Left/Right
/// adjusts the focused gauge by +/- 5%, Esc quits.
///
/// Demonstrates that the gauge component is purely a projection of its props record into a
/// memoized widget render — no internal state, no callbacks. The "current ratio" lives in app
/// state (a top-level [jatatui.react.Components#component] with a `useState` per gauge).
public final class GaugeExample {

  public static void main(String[] args) throws IOException {
    ReactApp.run(app());
  }

  private static final int GAUGE_COUNT = 3;
  private static final double STEP = 0.05;

  private static final Style[] BAR_STYLES = {
    Style.empty().withFg(Color.GREEN).withBg(Color.BLACK),
    Style.empty().withFg(Color.YELLOW).withBg(Color.BLACK),
    Style.empty().withFg(Color.MAGENTA).withBg(Color.BLACK)
  };

  static Element app() {
    return component(
        ctx -> {
          var ratios = ctx.useState(() -> new double[] {0.25, 0.50, 0.75});

          java.util.function.BiConsumer<Integer, Double> adjust =
              (idx, delta) -> {
                double[] arr = ratios.get();
                double[] next = arr.clone();
                double v = next[idx] + delta;
                if (v < 0.0) v = 0.0;
                if (v > 1.0) v = 1.0;
                next[idx] = v;
                ratios.set(next);
              };

          Element[] gaugeRow = new Element[GAUGE_COUNT];
          for (int i = 0; i < GAUGE_COUNT; i++) {
            gaugeRow[i] = fill(1, focusableGauge(i, ratios.get()[i], adjust));
          }

          return column(
                  length(7, row(gaugeRow).with(p -> p.withSpacing(2))),
                  length(
                      1,
                      text(
                          "Tab: cycle focus   <-/->: adjust focused gauge by 5%   Esc: quit",
                          Style.empty().withFg(Color.GRAY))))
              .with(p -> p.withSpacing(1).withMargin(new Margin(2, 1)));
        });
  }

  /// One focusable gauge tile. The gauge body itself is a [Gauges#gauge] (pure, memoized);
  /// focus and key handling live in this wrapper component so the gauge stays purely visual.
  static Element focusableGauge(
      int idx, double ratio, java.util.function.BiConsumer<Integer, Double> adjust) {
    return component(
        ctx -> {
          boolean focused = ctx.useFocus(Optional.of("gauge-" + idx), idx == 0);

          if (focused) {
            ctx.onKey(new KeyCode.Left(), () -> adjust.accept(idx, -STEP));
            ctx.onKey(new KeyCode.Right(), () -> adjust.accept(idx, +STEP));
          }

          String title = (focused ? " * Gauge " : "   Gauge ") + (idx + 1) + " ";
          String label = String.format("%d%%", Math.round(ratio * 100.0));
          Style barStyle = BAR_STYLES[idx % BAR_STYLES.length];

          GaugeProps props =
              GaugeProps.of(ratio)
                  .withTitle(title)
                  .withLabel(label)
                  .withGaugeStyle(barStyle)
                  .withUseUnicode(true);

          return Gauges.gauge(props);
        });
  }
}
