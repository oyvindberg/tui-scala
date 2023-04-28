package tui

import scala.util.control.NonFatal

/** Interface to the terminal backed by Termion
  *
  * @param backend
  * @param buffers
  *   Holds the results of the current and previous draw calls. The two are compared at the end of each draw pass to output the necessary updates to the
  *   terminal
  * @param current
  *   Index of the current buffer in the previous array
  * @param hiddenCursor
  *   Whether the cursor is currently hidden
  * @param viewport
  */
case class Terminal private (
    backend: Backend,
    buffers: Array[Buffer],
    var current: Int,
    var hiddenCursor: Boolean,
    viewport: Viewport
) {
  require(buffers.length == 2)

  def drop(): Unit =
    // Attempt to restore the cursor state
    if (hiddenCursor) {
      try showCursor()
      catch {
        case NonFatal(e) => System.err.println(s"Failed to show the cursor: ${e.getMessage}")
      }
    }

  /** Get a Frame object which provides a consistent view into the terminal state for rendering.
    */
  def getFrame(): Frame =
    Frame(
      buffer = currentBufferMut(),
      size = viewport.area,
      cursorPosition = None
    )

  def currentBufferMut(): Buffer =
    buffers(current)

  /** Obtains a difference between the previous and the current buffer and passes it to the current backend for drawing.
    */
  def flush(): Unit = {
    val previous_buffer = buffers(1 - current)
    val current_buffer = buffers(current)
    val updates = previous_buffer.diff(current_buffer)
    backend.draw(updates)
  }

  /** Updates the Terminal so that internal buffers match the requested size. Requested size will be saved so the size can remain consistent when rendering.
    * This leads to a full clear of the screen.
    */
  def resize(area: Rect): Unit = {
    buffers(current).resize(area)
    buffers(1 - current).resize(area)
    viewport.area = area
    clear()
  }

  /** Queries the backend for size and resizes if it doesn't match the previous size.
    */
  def autoresize(): Unit =
    if (viewport.resizeBehavior == ResizeBehavior.Auto) {
      val size_ = size()
      if (size_ != viewport.area) {
        resize(size_)
      }
    }

  /** Synchronizes terminal size, calls the rendering closure, flushes the current internal state and prepares for the next draw call.
    */
  def draw(f: Frame => Unit): CompletedFrame = {
    // Autoresize - otherwise we get glitches if shrinking or potential desync between widgets
    // and the terminal (if growing), which may OOB.
    autoresize()

    val frame = getFrame()
    f(frame)
    // We can't change the cursor position right away because we have to flush the frame to
    // stdout first. But we also can't keep the frame around, since it holds a &mut to
    // Terminal. Thus, we're taking the important data out of the Frame and dropping it.
    val cursor_position = frame.cursorPosition

    // Draw to stdout
    flush()

    cursor_position match {
      case None => hideCursor()
      case Some((x, y)) =>
        showCursor()
        setCursor(x, y)
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

  def hideCursor(): Unit = {
    backend.hideCursor()
    hiddenCursor = true
  }

  def showCursor(): Unit = {
    backend.showCursor()
    hiddenCursor = false
  }

  def getCursor(): (Int, Int) =
    backend.getCursor()

  def setCursor(x: Int, y: Int): Unit =
    backend.setCursor(x, y)

  /** Clear the terminal and force a full redraw on the next draw call.
    */
  def clear(): Unit = {
    backend.clear()
    // Reset the back buffer to make sure the next update will redraw everything.
    buffers(1 - current).reset()
  }

  /** Queries the real size of the backend.
    */
  def size(): Rect =
    backend.size()
}

object Terminal {

  /** Wrapper around Terminal initialization. Each buffer is initialized with a blank string and default colors for the foreground and the background
    */
  def init(backend: Backend): Terminal = {

    val size = backend.size()

    Terminal.withOptions(
      backend,
      TerminalOptions(
        viewport = Viewport(
          area = size,
          resizeBehavior = ResizeBehavior.Auto
        )
      )
    )
  }

  // UNSTABLE
  def withOptions(backend: Backend, options: TerminalOptions): Terminal =
    Terminal(
      backend,
      buffers = Array(
        Buffer.empty(options.viewport.area),
        Buffer.empty(options.viewport.area)
      ),
      current = 0,
      hiddenCursor = false,
      viewport = options.viewport
    )
}
