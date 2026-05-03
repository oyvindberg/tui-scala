package tui;

/// Base requirements for a Widget
public interface Widget {

  /// Draws the current state of the widget in the given buffer. That is the only method required to implement a custom widget.
  void render(Rect area, Buffer buf);
}
