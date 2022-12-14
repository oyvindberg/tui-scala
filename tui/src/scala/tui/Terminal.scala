package tui

/// Interface to the terminal
case class Terminal(backend: Backend) {
  private var hidden_cursor = false
  private var lastArea: Rect = size()
  private var previousBuffer: Buffer = Buffer.empty(lastArea)

  /// Synchronizes terminal size, calls the rendering closure, flushes the current internal state
  /// and prepares for the next draw call.
  def draw(f: Frame => Unit): CompletedFrame = {
    val area = size()

    // on resize
    if (area != lastArea) {
      previousBuffer = Buffer.empty(area)
      backend.clear()
      lastArea = area
    }

    val currentBuffer = Buffer.empty(area)
    val frame = Frame(buffer = currentBuffer, size = area, cursor_position = None)
    f(frame)

    // compute updates from last frame
    val updates = previousBuffer.diff(currentBuffer)
    previousBuffer = currentBuffer
    backend.draw(updates)

    // We can't change the cursor position right away because we have to flush the frame to stdout first.
    frame.cursor_position match {
      case None => hide_cursor()
      case Some((x, y)) =>
        show_cursor()
        set_cursor(x, y)
    }

    // Flush
    backend.flush()

    CompletedFrame(currentBuffer, area)
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

  /// Queries the real size of the backend.
  def size(): Rect =
    backend.size()
}
