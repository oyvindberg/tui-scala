package tui.widgets.canvas

/** Interface for all shapes that may be drawn on a Canvas widget.
  */
trait Shape {
  def draw(painter: Painter): Unit
}
