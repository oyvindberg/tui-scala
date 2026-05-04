package jatatui.components.gauge;

import jatatui.core.style.Style;
import java.util.Optional;

/// Immutable props for the React-style block-style gauge component.
///
/// **Stateless and controlled** — the parent owns `ratio` and (optionally) the label / styling
/// and pushes new immutable instances each render to update the bar. Memoization works on
/// `equals()`: passing a value-equal `GaugeProps` across renders skips the body entirely.
///
///   - `title`       — optional surrounding [jatatui.widgets.block.Block] title. Empty → no
///                     border, just the bar.
///   - `ratio`       — bar fill in `[0.0, 1.0]`. Outside this range throws on render.
///   - `label`       — text drawn centered over the bar. Empty → falls back to the percentage
///                     formatted as e.g. `"42%"`.
///   - `style`       — overall widget style (background of the unfilled portion).
///   - `gaugeStyle`  — style of the filled portion.
///   - `useUnicode`  — when `true`, partial cells are drawn with eighth-block characters for
///                     finer-grained progress.
public record GaugeProps(
    Optional<String> title,
    double ratio,
    Optional<String> label,
    Style style,
    Style gaugeStyle,
    boolean useUnicode) {

  /// Minimal props: just a ratio. No title, no custom label, default styles, ASCII bar.
  public static GaugeProps of(double ratio) {
    return new GaugeProps(
        Optional.empty(), ratio, Optional.empty(), Style.empty(), Style.empty(), false);
  }

  public GaugeProps withTitle(String title) {
    return new GaugeProps(Optional.of(title), ratio, label, style, gaugeStyle, useUnicode);
  }

  public GaugeProps withLabel(String label) {
    return new GaugeProps(title, ratio, Optional.of(label), style, gaugeStyle, useUnicode);
  }

  public GaugeProps withStyle(Style style) {
    return new GaugeProps(title, ratio, label, style, gaugeStyle, useUnicode);
  }

  public GaugeProps withGaugeStyle(Style gaugeStyle) {
    return new GaugeProps(title, ratio, label, style, gaugeStyle, useUnicode);
  }

  public GaugeProps withUseUnicode(boolean useUnicode) {
    return new GaugeProps(title, ratio, label, style, gaugeStyle, useUnicode);
  }
}
