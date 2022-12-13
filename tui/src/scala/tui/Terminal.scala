package tui

import scala.util.control.NonFatal

/// Interface to the terminal backed by Termion
case class Terminal private (
    backend: Backend,
    /// Holds the results of the current and previous draw calls. The two are compared at the end
    /// of each draw pass to output the necessary updates to the terminal
    buffers: Array[Buffer],
    /// Index of the current buffer in the previous array
    var current: Int,
    /// Whether the cursor is currently hidden
    var hidden_cursor: Boolean,
    /// Viewport
    viewport: Viewport
) {
  require(buffers.length == 2)

  def drop(): Unit =
    // Attempt to restore the cursor state
    if (hidden_cursor) {
      try show_cursor()
      catch {
        case NonFatal(e) => System.err.println(s"Failed to show the cursor: ${e.getMessage}")
      }
    }
  /// Get a Frame object which provides a consistent view into the terminal state for rendering.
  def get_frame(): Frame =
    Frame(
      buffer = current_buffer_mut(),
      size = viewport.area,
      cursor_position = None
    )

  def current_buffer_mut(): Buffer =
    buffers(current)

  /// Obtains a difference between the previous and the current buffer and passes it to the
  /// current backend for drawing.
  def flush(): Unit = {
    val previous_buffer = buffers(1 - current)
    val current_buffer = buffers(current)
    val updates = previous_buffer.diff(current_buffer)
    backend.draw(updates)
  }

  /// Updates the Terminal so that internal buffers match the requested size. Requested size will
  /// be saved so the size can remain consistent when rendering.
  /// This leads to a full clear of the screen.
  def resize(area: Rect): Unit = {
    buffers(current).resize(area)
    buffers(1 - current).resize(area)
    viewport.area = area
    clear()
  }

  /// Queries the backend for size and resizes if it doesn't match the previous size.
  def autoresize(): Unit =
    if (viewport.resize_behavior == ResizeBehavior.Auto) {
      val size_ = size()
      if (size_ != viewport.area) {
        resize(size_)
      }
    }

  /// Synchronizes terminal size, calls the rendering closure, flushes the current internal state
  /// and prepares for the next draw call.
  def draw(f: Frame => Unit): CompletedFrame = {
    // Autoresize - otherwise we get glitches if shrinking or potential desync between widgets
    // and the terminal (if growing), which may OOB.
    autoresize()

    val frame = get_frame()
    f(frame)
    // We can't change the cursor position right away because we have to flush the frame to
    // stdout first. But we also can't keep the frame around, since it holds a &mut to
    // Terminal. Thus, we're taking the important data out of the Frame and dropping it.
    val cursor_position = frame.cursor_position

    // Draw to stdout
    flush()

    cursor_position match {
      case None => hide_cursor()
      case Some((x, y)) =>
        show_cursor()
        set_cursor(x, y)
    }

    // Swap buffers
    buffers(1 - current).reset()
    current = 1 - current

    // Flush
    backend.flush()
    CompletedFrame(
      buffer = buffers(1 - current),
      area = viewport.area
    )
  }

  def hide_cursor(): Unit = {
    backend.hide_cursor()
    hidden_cursor = true
  }

  def show_cursor(): Unit = {
    backend.show_cursor()
    hidden_cursor = false
  }

  def get_cursor(): (Int, Int) =
    backend.get_cursor()

  def set_cursor(x: Int, y: Int): Unit =
    backend.set_cursor(x, y)

  /// Clear the terminal and force a full redraw on the next draw call.
  def clear(): Unit = {
    backend.clear()
    // Reset the back buffer to make sure the next update will redraw everything.
    buffers(1 - current).reset()
  }

  /// Queries the real size of the backend.
  def size(): Rect =
    backend.size()
}

object Terminal {
  /// Wrapper around Terminal initialization. Each buffer is initialized with a blank string and
  /// default colors for the foreground and the background
  def init(backend: Backend): Terminal = {

    val size = backend.size()

    Terminal.with_options(
      backend,
      TerminalOptions(
        viewport = Viewport(
          area = size,
          resize_behavior = ResizeBehavior.Auto
        )
      )
    )
  }

  /// UNSTABLE
  def with_options(backend: Backend, options: TerminalOptions): Terminal =
    Terminal(
      backend,
      buffers = Array(
        Buffer.empty(options.viewport.area),
        Buffer.empty(options.viewport.area)
      ),
      current = 0,
      hidden_cursor = false,
      viewport = options.viewport
    )
}
