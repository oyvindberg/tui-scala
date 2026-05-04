package jatatui.widgets.scrollbar;

/// The position of a [Scrollbar] around a given area.
///
/// Mirrors `ratatui_widgets::scrollbar::ScrollbarOrientation` (v0.30).
///
/// ```text
///           HorizontalTop
///             ┌───────┐
/// VerticalLeft│       │VerticalRight
///             └───────┘
///          HorizontalBottom
/// ```
public enum ScrollbarOrientation {
  /// Positions the scrollbar on the right, scrolling vertically.
  VerticalRight,
  /// Positions the scrollbar on the left, scrolling vertically.
  VerticalLeft,
  /// Positions the scrollbar on the bottom, scrolling horizontally.
  HorizontalBottom,
  /// Positions the scrollbar on the top, scrolling horizontally.
  HorizontalTop;

  /// Returns `true` if the scrollbar is vertical.
  public boolean isVertical() {
    return this == VerticalRight || this == VerticalLeft;
  }

  /// Returns `true` if the scrollbar is horizontal.
  public boolean isHorizontal() {
    return this == HorizontalTop || this == HorizontalBottom;
  }
}
