package tui
package widgets
package canvas

/** Shape to draw a rectangle from a `Rect` with the given color
  */
case class Rectangle(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    color: Color
) extends Shape {
  override def draw(painter: Painter): Unit = {
    val lines = Array(
      Line(x1 = x, y1 = y, x2 = x, y2 = y + height, color = color),
      Line(x1 = x, y1 = y + height, x2 = x + width, y2 = y + height, color = color),
      Line(x1 = x + width, y1 = y, x2 = x + width, y2 = y + height, color = color),
      Line(x1 = x, y1 = y, x2 = x + width, y2 = y, color = color)
    )
    lines.foreach { line =>
      line.draw(painter)
    }
  }
}
