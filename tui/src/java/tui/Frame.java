package tui;

import java.util.Optional;

/// Represents a consistent terminal interface for rendering.
public final class Frame {
  public final Buffer buffer;
  public final Rect size;
  public Optional<Position> cursorPosition;

  public Frame(Buffer buffer, Rect size, Optional<Position> cursorPosition) {
    this.buffer = buffer;
    this.size = size;
    this.cursorPosition = cursorPosition;
  }

  /// Render a `Widget` to the current buffer using `Widget.render`.
  public void renderWidget(Widget widget, Rect area) {
    widget.render(area, buffer);
  }

  /// Render a `StatefulWidget` to the current buffer using `StatefulWidget.render`.
  ///
  /// The last argument should be an instance of the `StatefulWidget.State` associated to the given `StatefulWidget`.
  public <State> void renderStatefulWidget(StatefulWidget<State> widget, Rect area, State state) {
    widget.render(area, buffer, state);
  }

  /// After drawing this frame, make the cursor visible and put it at the specified (x, y) coordinates. If this method is not called, the cursor will be hidden.
  public void setCursor(int x, int y) {
    cursorPosition = Optional.of(new Position(x, y));
  }
}
