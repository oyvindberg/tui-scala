package tui

/// A simple rectangle used in the computation of the layout and to give widgets a hint about the
/// area they are supposed to render to.
//  #[derive(Debug, Clone, Copy, Hash, PartialEq, Eq, Default)]
case class Rect(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) {
  def area: Int =
    width * height

  def left: Int =
    x

  def right: Int =
    x + width

  def top: Int =
    y

  def bottom: Int =
    y + height

  def inner(margin: Margin): Rect =
    if (width < 2 * margin.horizontal || height < 2 * margin.vertical) {
      Rect.default
    } else {
      Rect(
        x = x + margin.horizontal,
        y = y + margin.vertical,
        width = width - 2 * margin.horizontal,
        height = height - 2 * margin.vertical
      )
    }

  def union(other: Rect): Rect = {
    val x1 = math.min(x, other.x);
    val y1 = math.min(y, other.y);
    val x2 = math.max(x + width, other.x + other.width);
    val y2 = math.max(y + height, other.y + other.height);
    Rect(
      x = x1,
      y = y1,
      width = x2 - x1,
      height = y2 - y1
    )
  }

  def intersection(other: Rect): Rect = {
    val x1 = math.max(x, other.x);
    val y1 = math.max(y, other.y);
    val x2 = math.min(x + width, other.x + other.width);
    val y2 = math.min(y + height, other.y + other.height);
    Rect(
      x = x1,
      y = y1,
      width = x2 - x1,
      height = y2 - y1
    )
  }

  def intersects(other: Rect): Boolean =
    x < (other.x + other.width) && (x + width) > other.x && y < (other.y + other.height) && (y + height) > other.y
}

object Rect {
  val default = Rect(x = 0, y = 0, width = 0, height = 0)
}
