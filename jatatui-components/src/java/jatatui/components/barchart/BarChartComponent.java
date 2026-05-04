package jatatui.components.barchart;

import static jatatui.react.Components.pureComponent;
import static jatatui.react.Components.widget;

import jatatui.core.layout.Direction;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.widgets.Borders;
import jatatui.widgets.barchart.Bar;
import jatatui.widgets.barchart.BarChart;
import jatatui.widgets.barchart.BarGroup;
import jatatui.widgets.block.Block;
import java.util.ArrayList;
import java.util.List;

/// React-style component wrapping [jatatui.widgets.barchart.BarChart].
///
/// Stateless and controlled: callers pass [BarChartProps] (record → structural equality), and
/// the body is memoized through [jatatui.react.Components#pureComponent] — the underlying
/// [BarChart] widget is rebuilt only when the props record actually changes.
///
/// Build the props directly via [BarChartComponent#of(BarChartProps)] or use the static
/// factories on [BarCharts].
public final class BarChartComponent {

  private BarChartComponent() {}

  /// Constructs the memoized React Element for the given props.
  public static Element of(BarChartProps props) {
    return pureComponent(props, BarChartComponent::build);
  }

  // ---- Internal: assemble the underlying widget tree ----

  private static Element build(BarChartProps props) {
    Style titleStyle = Style.empty().withFg(Color.GRAY);
    Block block =
        Block.empty()
            .withTitle(jatatui.core.text.Line.from(props.title()))
            .withTitleStyle(titleStyle)
            .withBorders(Borders.ALL)
            .withBorderStyle(titleStyle);

    List<Bar> bars = new ArrayList<>(props.entries().size());
    for (BarEntry e : props.entries()) {
      Bar bar = Bar.withLabel(e.label(), e.value());
      if (e.style().isPresent()) {
        bar = bar.withStyle(e.style().get());
      }
      bars.add(bar);
    }

    BarChart chart =
        BarChart.empty()
            .withGroup(BarGroup.of(bars))
            .withBlock(block)
            .withBarStyle(props.barStyle())
            .withBarWidth(props.barWidth())
            .withBarGap(props.barGap())
            .withDirection(props.direction());

    return widget(chart);
  }

  // ---- Convenience for the common "vertical chart with default bar width" call site ----

  /// Vertical bar chart with default bar width 5, gap 1, and the given barStyle.
  public static Element vertical(String title, List<BarEntry> entries, Style barStyle) {
    return of(new BarChartProps(title, entries, 5, 1, barStyle, Direction.Vertical));
  }

  /// Horizontal bar chart with default bar height 1, gap 0, and the given barStyle.
  public static Element horizontal(String title, List<BarEntry> entries, Style barStyle) {
    return of(new BarChartProps(title, entries, 1, 0, barStyle, Direction.Horizontal));
  }
}
