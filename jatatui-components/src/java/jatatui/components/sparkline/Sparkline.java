package jatatui.components.sparkline;

import jatatui.react.Components;
import jatatui.react.Element;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.sparkline.SparklineBar;
import java.util.ArrayList;
import java.util.List;

/// React-style wrapper around [jatatui.widgets.sparkline.Sparkline].
///
/// The component is **stateless and controlled**: it has no hooks of its own and reads everything
/// it needs from [SparklineProps]. Parent components own the data and re-render with new
/// immutable lists to push values in.
///
/// Memoization is handled by [Components#pureComponent] — equal props (by `equals()`) skip the
/// build entirely. With `List<Long>`, reference identity is the cheapest fast-path: passing the
/// same list instance across renders short-circuits before even consulting `equals()`.
public final class Sparkline {
  private Sparkline() {}

  static Element sparkline(SparklineProps props) {
    return Components.pureComponent(props, Sparkline::build);
  }

  private static Element build(SparklineProps props) {
    List<SparklineBar> bars = new ArrayList<>(props.data().size());
    for (Long v : props.data()) {
      bars.add(SparklineBar.of(v));
    }
    Block block = Block.empty().withBorders(Borders.ALL).withTitle(props.title());
    jatatui.widgets.sparkline.Sparkline widget =
        jatatui.widgets.sparkline.Sparkline.empty()
            .withBlock(block)
            .withStyle(props.style())
            .withData(bars);
    if (props.max().isPresent()) {
      widget = widget.withMax(props.max().get());
    }
    return Components.widget(widget);
  }
}
