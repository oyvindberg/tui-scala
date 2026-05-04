package jatatui.widgets.list;

/// Defines the direction in which the [List] will be rendered.
///
/// If there are too few items to fill the screen, the list will stick to the starting edge.
///
/// Mirrors `ratatui_widgets::list::ListDirection` (v0.30).
public enum ListDirection {
  /// The first value is on the top, going to the bottom.
  ///
  /// This is the default.
  TopToBottom,

  /// The first value is on the bottom, going to the top.
  BottomToTop;

  /// Returns the default direction ([#TopToBottom]).
  public static ListDirection defaultValue() {
    return TopToBottom;
  }
}
