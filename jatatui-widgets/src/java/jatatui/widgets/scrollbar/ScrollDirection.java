package jatatui.widgets.scrollbar;

/// An enum representing a scrolling direction.
///
/// Mirrors `ratatui_widgets::scrollbar::ScrollDirection` (v0.30).
///
/// This is used with [ScrollbarState#scroll(ScrollDirection)]. It is useful for example when you
/// want to store in which direction to scroll.
public enum ScrollDirection {
  /// Forward scroll direction, usually corresponds to scrolling downwards or rightwards.
  Forward,
  /// Backward scroll direction, usually corresponds to scrolling upwards or leftwards.
  Backward
}
