package tui.widgets.canvas

import tui.{Color, Point}

case class Painter(
    context: Context,
    resolution: Point
) {
  /// Convert the (x, y) coordinates to location of a point on the grid
  ///
  /// # Examples:
  /// ```
  /// use tui.{symbols, widgets.canvas.{Painter, Context}};
  ///
  /// var ctx = Context.new(2, 2, [1.0, 2.0], [0.0, 2.0], symbols.Marker.Braille);
  /// var painter = Painter.from(ctx);
  /// val point = painter.get_point(1.0, 0.0);
  /// assert_eq!(point, Some((0, 7)));
  /// val point = painter.get_point(1.5, 1.0);
  /// assert_eq!(point, Some((1, 3)));
  /// val point = painter.get_point(0.0, 0.0);
  /// assert_eq!(point, None);
  /// val point = painter.get_point(2.0, 2.0);
  /// assert_eq!(point, Some((3, 0)));
  /// val point = painter.get_point(1.0, 2.0);
  /// assert_eq!(point, Some((0, 0)));
  /// ```
  def get_point(x: Double, y: Double): Option[(Int, Int)] = {
    val left = this.context.x_bounds.x
    val right = this.context.x_bounds.y
    val top = this.context.y_bounds.y
    val bottom = this.context.y_bounds.x
    if (x < left || x > right || y < bottom || y > top) {
      return None
    }
    val width = math.abs(this.context.x_bounds.y - this.context.x_bounds.x)
    val height = math.abs(this.context.y_bounds.y - this.context.y_bounds.x)
    if (width == 0.0 || height == 0.0) {
      return None
    }
    val x0 = ((x - left) * this.resolution.x / width).toInt
    val y0 = ((top - y) * this.resolution.y / height).toInt
    Some((x0, y0))
  }

  /// Paint a point of the grid
  ///
  /// # Examples:
  /// ```
  /// use tui.{style.Color, symbols, widgets.canvas.{Painter, Context}};
  ///
  /// var ctx = Context.new(1, 1, [0.0, 2.0], [0.0, 2.0], symbols.Marker.Braille);
  /// var painter = Painter.from(ctx);
  /// val cell = painter.paint(1, 3, Color.Red);
  /// ```
  def paint(x: Int, y: Int, color: Color): Unit =
    this.context.grid.paint(x, y, color)
}

object Painter {
  def from(context: Context): Painter = {
    val resolution = context.grid.resolution
    Painter(context, resolution)
  }
}
