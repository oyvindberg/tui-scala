package tui
package widgets
package canvas

import tui.internal.ranges
import tui.internal.saturating._

/// Shape to draw a line from (x1, y1) to (x2, y2) with the given color
case class Line(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
    color: Color
) extends Shape {
  override def draw(painter: Painter): Unit = {
    val (x1, y1) = painter.get_point(this.x1, this.y1) match {
      case Some(c) => c
      case None    => return
    }
    val (x2, y2) = painter.get_point(this.x2, this.y2) match {
      case Some(c) => c
      case None    => return
    }

    val (dx, x_range) = if (x2 >= x1) {
      (x2 - x1, Range.inclusive(x1, x2))
    } else {
      (x1 - x2, Range.inclusive(x2, x1))
    }
    val (dy, y_range) = if (y2 >= y1) {
      (y2 - y1, Range.inclusive(y1, y2))
    } else {
      (y1 - y2, Range.inclusive(y2, y1))
    }

    if (dx == 0) {
      y_range.foreach { y =>
        painter.paint(x1, y, this.color);
      }
    } else if (dy == 0) {
      x_range.foreach { x =>
        painter.paint(x, y1, this.color);
      }
    } else if (dy < dx) {
      if (x1 > x2) {
        Line.draw_line_low(painter, x2, y2, x1, y1, this.color)
      } else {
        Line.draw_line_low(painter, x1, y1, x2, y2, this.color)
      }
    } else if (y1 > y2) {
      Line.draw_line_high(painter, x2, y2, x1, y1, this.color)
    } else {
      Line.draw_line_high(painter, x1, y1, x2, y2, this.color)
    }
  }
}

object Line {
  def draw_line_low(painter: Painter, x1: Int, y1: Int, x2: Int, y2: Int, color: Color): Unit = {
    val dx = x2 - x1
    val dy = math.abs(y2 - y1)
    var d = 2 * dy - dx
    var y = y1
    ranges.range(x1, x2 + 1) { x =>
      painter.paint(x, y, color)
      if (d > 0) {
        y = if (y1 > y2) {
          y.saturating_sub_unsigned(1)
        } else {
          y.saturating_add(1)
        }
        d -= 2 * dx
      }
      d += 2 * dy;
    }
  }

  def draw_line_high(painter: Painter, x1: Int, y1: Int, x2: Int, y2: Int, color: Color): Unit = {
    val dx = math.abs(x2 - x1)
    val dy = y2 - y1
    var d = 2 * dx - dy
    var x = x1
    ranges.range(y1, y2 + 1) { y =>
      painter.paint(x, y, color)
      if (d > 0) {
        x = if (x1 > x2) {
          x.saturating_sub_unsigned(1)
        } else {
          x.saturating_add(1)
        }
        d -= 2 * dy
      }
      d += 2 * dx;
    }
  }
}
