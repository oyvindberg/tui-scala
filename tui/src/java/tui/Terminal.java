package tui;

import java.util.Optional;

/// Interface to the terminal backed by Termion
public final class Terminal {
  public final Backend backend;
  public final Buffer[] buffers;
  public int current;
  public boolean hiddenCursor;
  public final Viewport viewport;

  private Terminal(
      Backend backend,
      Buffer[] buffers,
      int current,
      boolean hiddenCursor,
      Viewport viewport) {
    if (buffers.length != 2) {
      throw new IllegalArgumentException("Terminal needs exactly 2 buffers");
    }
    this.backend = backend;
    this.buffers = buffers;
    this.current = current;
    this.hiddenCursor = hiddenCursor;
    this.viewport = viewport;
  }

  public void drop() {
    // Attempt to restore the cursor state
    if (hiddenCursor) {
      try {
        showCursor();
      } catch (Exception e) {
        System.err.println("Failed to show the cursor: " + e.getMessage());
      }
    }
  }

  /// Get a Frame object which provides a consistent view into the terminal state for rendering.
  public Frame getFrame() {
    return new Frame(currentBufferMut(), viewport.area, Optional.empty());
  }

  public Buffer currentBufferMut() {
    return buffers[current];
  }

  /// Obtains a difference between the previous and the current buffer and passes it to the current backend for drawing.
  public void flush() {
    Buffer previousBuffer = buffers[1 - current];
    Buffer currentBuffer = buffers[current];
    BufferUpdate[] updates = previousBuffer.diff(currentBuffer);
    backend.draw(updates);
  }

  /// Updates the Terminal so that internal buffers match the requested size.
  public void resize(Rect area) {
    buffers[current].resize(area);
    buffers[1 - current].resize(area);
    viewport.area = area;
    clear();
  }

  /// Queries the backend for size and resizes if it doesn't match the previous size.
  public void autoresize() {
    if (viewport.resizeBehavior == ResizeBehavior.Auto) {
      Rect sz = size();
      if (!sz.equals(viewport.area)) {
        resize(sz);
      }
    }
  }

  /// Synchronizes terminal size, calls the rendering closure, flushes the current internal state and prepares for the next draw call.
  public CompletedFrame draw(java.util.function.Consumer<Frame> f) {
    autoresize();

    Frame frame = getFrame();
    f.accept(frame);
    Optional<Position> cursorPosition = frame.cursorPosition;

    flush();

    if (cursorPosition.isEmpty()) {
      hideCursor();
    } else {
      Position p = cursorPosition.get();
      showCursor();
      setCursor(p.x(), p.y());
    }

    // Swap buffers
    buffers[1 - current].reset();
    current = 1 - current;

    backend.flush();
    return new CompletedFrame(buffers[1 - current], viewport.area);
  }

  public void hideCursor() {
    backend.hideCursor();
    hiddenCursor = true;
  }

  public void showCursor() {
    backend.showCursor();
    hiddenCursor = false;
  }

  public Position getCursor() {
    return backend.getCursor();
  }

  public void setCursor(int x, int y) {
    backend.setCursor(x, y);
  }

  /// Clear the terminal and force a full redraw on the next draw call.
  public void clear() {
    backend.clear();
    buffers[1 - current].reset();
  }

  /// Queries the real size of the backend.
  public Rect size() {
    return backend.size();
  }

  /// Wrapper around Terminal initialization.
  public static Terminal init(Backend backend) {
    Rect size = backend.size();
    return Terminal.withOptions(
        backend, new TerminalOptions(new Viewport(size, ResizeBehavior.Auto)));
  }

  // UNSTABLE
  public static Terminal withOptions(Backend backend, TerminalOptions options) {
    return new Terminal(
        backend,
        new Buffer[] {Buffer.empty(options.viewport().area), Buffer.empty(options.viewport().area)},
        0,
        false,
        options.viewport());
  }
}
