package jatatui.components.gauge;

import jatatui.core.style.Style;
import java.util.Optional;

/// Immutable props for the React-style single-line gauge component.
///
/// Same controlled / pure semantics as [GaugeProps], but for [jatatui.widgets.gauge.LineGauge].
/// The widget always occupies a single row (the bar plus optional label, left-aligned).
///
///   - `title`         — optional surrounding [jatatui.widgets.block.Block] title.
///   - `ratio`         — bar fill in `[0.0, 1.0]`. Outside this range throws on render.
///   - `label`         — text drawn at the start of the line. Empty → falls back to the
///                       percentage formatted as e.g. `" 42%"`.
///   - `style`         — overall widget style.
///   - `filledStyle`   — style of the filled portion of the bar.
///   - `unfilledStyle` — style of the unfilled portion of the bar.
public record LineGaugeProps(
    Optional<String> title,
    double ratio,
    Optional<String> label,
    Style style,
    Style filledStyle,
    Style unfilledStyle) {

  /// Minimal props: just a ratio. No title, no custom label, default styles.
  public static LineGaugeProps of(double ratio) {
    return new LineGaugeProps(
        Optional.empty(), ratio, Optional.empty(), Style.empty(), Style.empty(), Style.empty());
  }

  public LineGaugeProps withTitle(String title) {
    return new LineGaugeProps(Optional.of(title), ratio, label, style, filledStyle, unfilledStyle);
  }

  public LineGaugeProps withLabel(String label) {
    return new LineGaugeProps(title, ratio, Optional.of(label), style, filledStyle, unfilledStyle);
  }

  public LineGaugeProps withStyle(Style style) {
    return new LineGaugeProps(title, ratio, label, style, filledStyle, unfilledStyle);
  }

  public LineGaugeProps withFilledStyle(Style filledStyle) {
    return new LineGaugeProps(title, ratio, label, style, filledStyle, unfilledStyle);
  }

  public LineGaugeProps withUnfilledStyle(Style unfilledStyle) {
    return new LineGaugeProps(title, ratio, label, style, filledStyle, unfilledStyle);
  }
}
