package tui.widgets.canvas

import tui.internal.ranges
import tui.{symbols, Color, Point}

case class BrailleGrid(
    width: Int,
    height: Int,
    cells: Array[Int],
    colors: Array[Color]
) extends Grid {
  override def resolution: Point =
    Point(
      this.width.toDouble * 2.0 - 1.0,
      this.height.toDouble * 4.0 - 1.0
    )

  override def save(): Layer =
    Layer(
      string = new String(this.cells, 0, cells.length),
      colors = this.colors.clone()
    )

  override def reset(): Unit = {
    ranges.range(0, this.cells.length) { i =>
      this.cells(i) = symbols.braille.BLANK
    }
    ranges.range(0, this.colors.length) { i =>
      this.colors(i) = Color.Reset
    }
  }

  override def paint(x: Int, y: Int, color: Color): Unit = {
    val index = y / 4 * this.width + x / 2
    if (index < this.cells.length) {
      val c = this.cells(index)
      val chosenDots = symbols.braille.DOTS(y % 4)
      val chosenDot = x % 2 match {
        case 0 => chosenDots._1
        case 1 => chosenDots._2
      }
      val newC = c | chosenDot
      this.cells(index) = newC
    }
    if (index < this.colors.length) {
      this.colors(index) = color
    }
  }
}

object BrailleGrid {
  def apply(width: Int, height: Int): BrailleGrid = {
    val length = width * height
    BrailleGrid(
      width,
      height,
      cells = Array.fill(length)(symbols.braille.BLANK),
      colors = Array.fill(length)(Color.Reset)
    )
  }
}
