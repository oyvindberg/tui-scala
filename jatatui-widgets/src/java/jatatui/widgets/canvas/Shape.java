package jatatui.widgets.canvas;

/// Something that can be drawn on a [Canvas].
///
/// You may implement your own canvas custom widgets by implementing this interface.
///
/// Mirrors `ratatui_widgets::canvas::Shape` (v0.30).
public interface Shape {

  /// Draws this [Shape] using the given [Painter].
  ///
  /// This is the only method required to implement a custom widget that can be drawn on a
  /// [Canvas].
  void draw(Painter painter);
}
