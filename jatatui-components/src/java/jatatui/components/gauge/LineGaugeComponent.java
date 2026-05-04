package jatatui.components.gauge;

import jatatui.core.text.Line;
import jatatui.react.Components;
import jatatui.react.Element;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.gauge.LineGauge;

/// React-style wrapper around [jatatui.widgets.gauge.LineGauge].
///
/// Same stateless / controlled / pure semantics as [GaugeComponent].
public final class LineGaugeComponent {
  private LineGaugeComponent() {}

  /// Build the React Element from the props record.
  public static Element of(LineGaugeProps props) {
    return Components.pureComponent(props, LineGaugeComponent::build);
  }

  private static Element build(LineGaugeProps p) {
    LineGauge g =
        LineGauge.empty()
            .withRatio(p.ratio())
            .withStyle(p.style())
            .withFilledStyle(p.filledStyle())
            .withUnfilledStyle(p.unfilledStyle());
    if (p.label().isPresent()) {
      g = g.withLabel(Line.from(p.label().get()));
    }
    if (p.title().isPresent()) {
      g = g.withBlock(Block.empty().withTitle(Line.from(p.title().get())).withBorders(Borders.ALL));
    }
    return Components.widget(g);
  }
}
