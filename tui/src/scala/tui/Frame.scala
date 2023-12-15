package tui

/** Represents a consistent terminal interface for rendering.
  * @param size
  *   Terminal size, guaranteed not to change when rendering.
  * @param cursorPosition
  *   Where should the cursor be after drawing this frame. If `None`, the cursor is hidden and its position is controlled by the backend. If `Some((x, /// y))`,
  *   the cursor is shown and placed at `(x, y)` after the call to `Terminal.draw()`.
  */
case class Frame(
    buffer: Buffer,
    size: Rect,
    var cursorPosition: Option[(Int, Int)]
) {

  /** Render a `Widget` to the current buffer using `Widget.render`.
    */
  def renderWidget(widget: Widget, area: Rect): Unit =
    widget.render(area, buffer)

  /** After drawing this frame, make the cursor visible and put it at the specified (x, y) coordinates. If this method is not called, the cursor will be hidden.
    *
    * Note that this will interfere with calls to `Terminal.hide_cursor()`, `Terminal.show_cursor()`, and `Terminal.set_cursor()`. Pick one of the APIs and
    * stick with it.
    */
  def setCursor(x: Int, y: Int): Unit =
    cursorPosition = Some((x, y))
}
