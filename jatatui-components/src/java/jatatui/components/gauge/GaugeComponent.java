package jatatui.components.gauge;

import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.react.Components;
import jatatui.react.Element;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.gauge.Gauge;

/// React-style wrapper around [jatatui.widgets.gauge.Gauge].
///
/// The component is **stateless and controlled**: it has no hooks of its own and reads
/// everything it needs from [GaugeProps]. Parent components own `ratio` and re-render with new
/// props to push values in.
///
/// Memoization is handled by [Components#pureComponent] — equal props (by `equals()`) skip the
/// build entirely. Because [GaugeProps] is a record over primitive / value-equal fields,
/// reuse-on-identity is automatic — two `GaugeProps` with the same field values compare equal.
public final class GaugeComponent {
  private GaugeComponent() {}

  /// Build the React Element from the props record.
  public static Element of(GaugeProps props) {
    return Components.pureComponent(props, GaugeComponent::build);
  }

  private static Element build(GaugeProps p) {
    Gauge g =
        Gauge.empty()
            .withRatio(p.ratio())
            .withStyle(p.style())
            .withGaugeStyle(p.gaugeStyle())
            .withUseUnicode(p.useUnicode());
    if (p.label().isPresent()) {
      g = g.withLabel(Span.raw(p.label().get()));
    }
    if (p.title().isPresent()) {
      g =
          g.withBlock(
              Block.empty().withTitle(Line.from(p.title().get())).withBorders(Borders.ALL));
    }
    return Components.widget(g);
  }
}
