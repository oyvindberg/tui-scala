package jatatui.components.gauge;

import jatatui.core.style.Style;
import jatatui.react.Element;

/// Static factories for [GaugeComponent] / [LineGaugeComponent]. Mirrors the convention used by
/// [jatatui.react.Components] and [jatatui.components.list.ListComponents] — import statically:
///
/// ```java
/// import static jatatui.components.gauge.Gauges.*;
/// ```
public final class Gauges {
  private Gauges() {}

  // ---- Block (vertical) gauge ----

  /// Full-control factory: pass a [GaugeProps] you've built yourself.
  public static Element gauge(GaugeProps props) {
    return GaugeComponent.of(props);
  }

  /// Convenience: just a ratio.
  public static Element gauge(double ratio) {
    return GaugeComponent.of(GaugeProps.of(ratio));
  }

  /// Convenience: title + ratio + label + bar style. Borders default to all sides.
  public static Element gauge(String title, double ratio, String label, Style gaugeStyle) {
    return GaugeComponent.of(
        GaugeProps.of(ratio).withTitle(title).withLabel(label).withGaugeStyle(gaugeStyle));
  }

  // ---- Single-line gauge ----

  /// Full-control factory: pass a [LineGaugeProps] you've built yourself.
  public static Element lineGauge(LineGaugeProps props) {
    return LineGaugeComponent.of(props);
  }

  /// Convenience: just a ratio.
  public static Element lineGauge(double ratio) {
    return LineGaugeComponent.of(LineGaugeProps.of(ratio));
  }

  /// Convenience: title + ratio + filled-bar style.
  public static Element lineGauge(String title, double ratio, Style filledStyle) {
    return LineGaugeComponent.of(
        LineGaugeProps.of(ratio).withTitle(title).withFilledStyle(filledStyle));
  }
}
