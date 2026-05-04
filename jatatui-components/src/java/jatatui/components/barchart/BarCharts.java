package jatatui.components.barchart;

import jatatui.core.layout.Direction;
import jatatui.core.style.Style;
import jatatui.react.Element;
import java.util.List;

/// Static factories for [BarChartComponent]. Mirrors the convention used by
/// `jatatui.react.Components` and [jatatui.components.list.ListComponents] — import statically:
///
/// ```java
/// import static jatatui.components.barchart.BarCharts.*;
/// ```
public final class BarCharts {
  private BarCharts() {}

  /// Full-control factory: pass a [BarChartProps] you've built yourself.
  public static Element barChart(BarChartProps props) {
    return BarChartComponent.of(props);
  }

  /// Vertical bar chart with explicit bar width / gap / style.
  public static Element barChart(
      String title, List<BarEntry> entries, int barWidth, int barGap, Style barStyle) {
    return BarChartComponent.of(
        new BarChartProps(title, entries, barWidth, barGap, barStyle, Direction.Vertical));
  }

  /// Bar chart with all knobs explicit (vertical or horizontal).
  public static Element barChart(
      String title,
      List<BarEntry> entries,
      int barWidth,
      int barGap,
      Style barStyle,
      Direction direction) {
    return BarChartComponent.of(
        new BarChartProps(title, entries, barWidth, barGap, barStyle, direction));
  }

  /// Vertical bar chart with reasonable defaults (barWidth=5, barGap=1).
  public static Element verticalBarChart(String title, List<BarEntry> entries, Style barStyle) {
    return BarChartComponent.vertical(title, entries, barStyle);
  }

  /// Horizontal bar chart with reasonable defaults (barWidth=1, barGap=0).
  public static Element horizontalBarChart(String title, List<BarEntry> entries, Style barStyle) {
    return BarChartComponent.horizontal(title, entries, barStyle);
  }
}
