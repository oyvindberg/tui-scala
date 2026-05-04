package jatatui.react.examples.sparkline;

import static jatatui.react.Components.*;

import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.ReactApp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import tui.crossterm.KeyCode;

/// Demo for the React-style sparkline component.
///
/// Two stacked sparklines:
///   - **Manual**: starts empty. Spacebar appends one fresh random sample (and drops the oldest
///     when the buffer is full) by replacing the state with a brand-new immutable list. The
///     [jatatui.components.sparkline.SparklineProps] record's `equals()` then sees a different
///     `data` list and lets the pure component re-render.
///   - **Preset**: a fixed pretty curve, never changes — its props are reference-equal across
///     renders, so the pure component short-circuits immediately.
///
/// Esc / Ctrl-C quits (handled globally by [ReactApp]).
public final class SparklineExample {

  private static final int CAPACITY = 60;
  private static final long PRESET_MAX = 100L;

  /// A pleasant, deterministic curve so the preset chart is recognisable across runs.
  private static final List<Long> PRESET = buildPreset();

  public static void main(String[] args) throws IOException {
    ReactApp.run(app());
  }

  static Element app() {
    return component(
        ctx -> {
          var data = ctx.<List<Long>>useState(List::of);
          var rng = ctx.useRef(() -> new Random(0xC0FFEEL));

          // Spacebar: append a fresh random sample, drop the oldest if at capacity.
          // Build a NEW immutable list so SparklineProps.equals() detects the change.
          ctx.onKey(
              new KeyCode.Char(' '),
              () -> data.update(prev -> appendSample(prev, nextSample(rng.get()))));

          return column(
                  length(
                      3,
                      text(
                          " Press SPACE to push a sample.  Esc or Ctrl-C to quit.",
                          Style.empty().withFg(Color.GRAY))),
                  fill(
                      1,
                      jatatui.components.sparkline.Components.sparkline(
                          new jatatui.components.sparkline.SparklineProps(
                              " Manual ("
                                  + data.get().size()
                                  + "/"
                                  + CAPACITY
                                  + ") — SPACE to add ",
                              data.get(),
                              Optional.of(PRESET_MAX),
                              Style.empty().withFg(Color.CYAN)))),
                  fill(
                      1,
                      jatatui.components.sparkline.Components.sparkline(
                          new jatatui.components.sparkline.SparklineProps(
                              " Preset (memoized; never changes) ",
                              PRESET,
                              Optional.of(PRESET_MAX),
                              Style.empty().withFg(Color.MAGENTA)))))
              .with(p -> p.withSpacing(1).withMargin(new jatatui.core.layout.Margin(2, 1)));
        });
  }

  private static long nextSample(Random rng) {
    // Random walk-ish in [5, 95]
    return 5 + rng.nextInt(91);
  }

  private static List<Long> appendSample(List<Long> prev, long sample) {
    if (prev.size() < CAPACITY) {
      return Stream.concat(prev.stream(), Stream.of(sample)).toList();
    }
    return Stream.concat(prev.stream().skip(1), Stream.of(sample)).toList();
  }

  private static List<Long> buildPreset() {
    List<Long> out = new ArrayList<>(CAPACITY);
    for (int i = 0; i < CAPACITY; i++) {
      // Smooth-ish wave between 10 and 95.
      double a = Math.sin(i * 0.18) * 35.0;
      double b = Math.sin(i * 0.05 + 1.2) * 18.0;
      double v = 55.0 + a + b;
      long clamped = Math.max(5L, Math.min(95L, (long) Math.round(v)));
      out.add(clamped);
    }
    return List.copyOf(out);
  }
}
