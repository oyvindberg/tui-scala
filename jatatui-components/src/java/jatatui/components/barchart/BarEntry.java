package jatatui.components.barchart;

import jatatui.core.style.Style;
import java.util.Optional;

/// One labelled entry in a [BarChartProps] dataset.
///
/// `style` is per-entry; when present it's layered on top of the chart-wide `barStyle`.
/// Records get value-based equality for free, so a `List<BarEntry>` participates in
/// `pureComponent` memoization without further work — replace one entry to "highlight" it,
/// and the new list is `equals`-different from the old list, triggering a re-render.
public record BarEntry(String label, long value, Optional<Style> style) {

  public static BarEntry of(String label, long value) {
    return new BarEntry(label, value, Optional.empty());
  }

  public static BarEntry of(String label, long value, Style style) {
    return new BarEntry(label, value, Optional.of(style));
  }

  public BarEntry withStyle(Style style) {
    return new BarEntry(label, value, Optional.of(style));
  }

  public BarEntry withoutStyle() {
    return new BarEntry(label, value, Optional.empty());
  }
}
