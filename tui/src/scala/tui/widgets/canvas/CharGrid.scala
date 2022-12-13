package tui.widgets.canvas

import tui.internal.ranges
import tui.{Color, Point}

case class CharGrid(
    width: Int,
    height: Int,
    cells: Array[Char],
    colors: Array[Color],
    cell_char: Char
) extends Grid {

  override def resolution: Point =
    Point(this.width.toDouble - 1.0, this.height.toDouble - 1.0)

  override def save(): Layer =
    Layer(
      string = new String(this.cells),
      colors = this.colors.map(identity)
    )

  override def reset(): Unit = {
    ranges.range(0, this.cells.length) { i =>
      this.cells(i) = ' '
    }
    ranges.range(0, this.colors.length) { i =>
      this.colors(i) = Color.Reset
    }
  }

  override def paint(x: Int, y: Int, color: Color): Unit = {
    val index = y * this.width + x
    if (index < this.cells.length) {
      this.cells(index) = this.cell_char
    }
    if (index < this.colors.length) {
      this.colors(index) = color
    }
  }
}
object CharGrid {
  def apply(width: Int, height: Int, cell_char: Char): CharGrid = {
    val length = width * height
    CharGrid(
      width,
      height,
      cells = Array.fill(length)(' '),
      colors = Array.fill(length)(Color.Reset),
      cell_char
    )
  }
}
