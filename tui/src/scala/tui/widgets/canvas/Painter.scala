package tui.widgets.canvas

import tui.{Color, Point}

case class Painter(
    context: Context,
    resolution: Point
) {

  /** Convert the (x, y) coordinates to location of a point on the grid
    */
  def getPoint(x: Double, y: Double): Option[(Int, Int)] = {
    val left = this.context.xBounds.x
    val right = this.context.xBounds.y
    val top = this.context.yBounds.y
    val bottom = this.context.yBounds.x
    if (x < left || x > right || y < bottom || y > top) {
      return None
    }
    val width = math.abs(this.context.xBounds.y - this.context.xBounds.x)
    val height = math.abs(this.context.yBounds.y - this.context.yBounds.x)
    if (width == 0.0 || height == 0.0) {
      return None
    }
    val x0 = ((x - left) * this.resolution.x / width).toInt
    val y0 = ((top - y) * this.resolution.y / height).toInt
    Some((x0, y0))
  }

  /** Paint a point of the grid
    */
  def paint(x: Int, y: Int, color: Color): Unit =
    this.context.grid.paint(x, y, color)
}

object Painter {
  def from(context: Context): Painter = {
    val resolution = context.grid.resolution
    Painter(context, resolution)
  }
}
