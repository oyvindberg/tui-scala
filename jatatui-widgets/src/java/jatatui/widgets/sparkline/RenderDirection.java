package jatatui.widgets.sparkline;

/// Defines the direction in which a [Sparkline] will be rendered.
///
/// Mirrors `ratatui_widgets::sparkline::RenderDirection` (v0.30).
public enum RenderDirection {
  /// The first value is on the left, going to the right.
  LeftToRight,
  /// The first value is on the right, going to the left.
  RightToLeft
}
