package jatatui.core.terminal;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.widgets.StatefulWidget;
import jatatui.core.widgets.Widget;
import java.util.Optional;

/// A consistent view into the terminal state for rendering a single frame.
///
/// This is obtained via the closure argument of [Terminal#draw(java.util.function.Consumer)]. It is
/// used to render widgets to the terminal and control the cursor position.
///
/// The changes drawn to the frame are applied only to the current [Buffer]. After the closure
/// returns, the current buffer is compared to the previous buffer and only the changes are applied
/// to the terminal. This avoids drawing redundant cells.
///
/// Mirrors `ratatui_core::terminal::Frame` (v0.30). Upstream models the buffer with a `&mut Buffer`
/// borrow. Java has no borrow types — the field is a plain reference and the [Terminal] is
/// responsible for ensuring no two frames hold a reference to the same buffer simultaneously.
public final class Frame {

  /// Where should the cursor be after drawing this frame? [Optional#empty()] hides the cursor.
  Optional<Position> cursorPosition;

  /// The area of the viewport.
  final Rect viewportArea;

  /// The buffer that is used to draw the current frame.
  final Buffer buffer;

  /// The frame count indicating the sequence number of this frame.
  final int count;

  Frame(Optional<Position> cursorPosition, Rect viewportArea, Buffer buffer, int count) {
    this.cursorPosition = cursorPosition;
    this.viewportArea = viewportArea;
    this.buffer = buffer;
    this.count = count;
  }

  /// The area of the current frame.
  ///
  /// This is guaranteed not to change during rendering, so may be called multiple times.
  ///
  /// If your app listens for a resize event from the backend, it should ignore the values from the
  /// event for any calculations that are used to render the current frame and use this value
  /// instead as this is the area of the buffer that is used to render the current frame.
  public Rect area() {
    return viewportArea;
  }

  /// Renders a [Widget] to the current buffer using [Widget#render(Rect, Buffer)].
  ///
  /// Usually the area argument is the size of the current frame or a sub-area of the current frame
  /// (which can be obtained using [jatatui.core.layout.Layout] to split the total area).
  public void renderWidget(Widget widget, Rect area) {
    widget.render(area, buffer);
  }

  /// Renders a [StatefulWidget] to the current buffer using
  /// [StatefulWidget#render(Rect, Buffer, Object)].
  ///
  /// Usually the area argument is the size of the current frame or a sub-area of the current frame
  /// (which can be obtained using [jatatui.core.layout.Layout] to split the total area).
  ///
  /// The last argument should be an instance of the state type associated with the given
  /// [StatefulWidget].
  public <S> void renderStatefulWidget(StatefulWidget<S> widget, Rect area, S state) {
    widget.render(area, buffer, state);
  }

  /// After drawing this frame, makes the cursor visible and puts it at the specified position. If
  /// this method is not called, the cursor will be hidden.
  ///
  /// Note that this will interfere with calls to [Terminal#hideCursor()],
  /// [Terminal#showCursor()] and [Terminal#setCursorPosition(Position)]. Pick one of the APIs and
  /// stick with it.
  public void setCursorPosition(Position position) {
    this.cursorPosition = Optional.of(position);
  }

  /// Convenience overload — equivalent to `setCursorPosition(new Position(x, y))`.
  public void setCursorPosition(int x, int y) {
    setCursorPosition(new Position(x, y));
  }

  /// Returns the buffer that this `Frame` draws into.
  ///
  /// Java has no `&mut`; mutation is done through this returned reference. Callers must not retain
  /// it past the lifetime of this frame.
  public Buffer bufferMut() {
    return buffer;
  }

  /// Returns the current frame count.
  ///
  /// This count is a sequence number indicating how many frames have been rendered up to (but not
  /// including) this one. It can be used for purposes such as animation, performance tracking, or
  /// debugging.
  ///
  /// Each time a frame has been rendered, this count is incremented. When count reaches its maximum
  /// value (`Integer.MAX_VALUE`), it wraps around to zero (mirroring upstream's
  /// `wrapping_add(1)`).
  public int count() {
    return count;
  }
}
