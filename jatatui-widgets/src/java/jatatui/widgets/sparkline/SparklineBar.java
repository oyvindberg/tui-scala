package jatatui.widgets.sparkline;

import jatatui.core.style.Style;
import java.util.Objects;
import java.util.Optional;

/// A bar in a [Sparkline].
///
/// Mirrors `ratatui_widgets::sparkline::SparklineBar` (v0.30).
///
/// The height of the bar is determined by the value, and a value of [Optional#empty()] is
/// interpreted as the _absence_ of a value, as distinct from a value of `Some(0)`.
public final class SparklineBar {

  /// The value of the bar. Empty when the bar is absent.
  public final Optional<Long> value;

  /// The style of the bar. Empty means use the sparkline's style.
  public final Optional<Style> style;

  private SparklineBar(Optional<Long> value, Optional<Style> style) {
    this.value = value;
    this.style = style;
  }

  /// Creates a [SparklineBar] from a `long` value (no per-bar style).
  public static SparklineBar of(long value) {
    return new SparklineBar(Optional.of(value), Optional.empty());
  }

  /// Creates a [SparklineBar] from an optional value (no per-bar style).
  public static SparklineBar of(Optional<Long> value) {
    return new SparklineBar(value, Optional.empty());
  }

  /// Creates an absent [SparklineBar] (no value, no per-bar style).
  public static SparklineBar absent() {
    return new SparklineBar(Optional.empty(), Optional.empty());
  }

  /// Returns a copy with the per-bar style set.
  public SparklineBar withStyle(Style style) {
    return new SparklineBar(value, Optional.of(style));
  }

  /// Returns a copy with the per-bar style optional replaced.
  public SparklineBar withStyle(Optional<Style> style) {
    return new SparklineBar(value, style);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SparklineBar other)) return false;
    return value.equals(other.value) && style.equals(other.style);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, style);
  }

  @Override
  public String toString() {
    return "SparklineBar[value=" + value + ", style=" + style + "]";
  }
}
