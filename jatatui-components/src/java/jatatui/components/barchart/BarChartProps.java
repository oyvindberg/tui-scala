package jatatui.components.barchart;

import jatatui.core.layout.Direction;
import jatatui.core.style.Style;
import java.util.List;

/// Immutable props for the React-style [BarChartComponent].
///
/// Stateless and controlled: callers pass an immutable `entries` list and the rest of the
/// presentation knobs. Records compare structurally, so memoization works:
///   - same `entries` instance across renders → fast path (reference equality on the list).
///   - new `entries` instance with structurally equal contents → still equal, body skipped.
///   - new `entries` instance with at least one different [BarEntry] → not equal, body runs.
public record BarChartProps(
    String title,
    List<BarEntry> entries,
    int barWidth,
    int barGap,
    Style barStyle,
    Direction direction) {

  public BarChartProps {
    entries = List.copyOf(entries);
  }
}
