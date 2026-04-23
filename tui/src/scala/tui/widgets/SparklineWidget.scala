package tui
package widgets

import tui.internal.ranges

/** Widget to render a sparkline over one or more lines.
  *
  * @param block
  *   A block to wrap the widget in
  * @param style
  *   Widget style
  * @param data
  *   A slice of the data to display
  * @param max
  *   The maximum value to take to compute the maximum bar height (if nothing is specified, the widget uses the max of the dataset)
  * @param barSet
  *   A set of bar symbols used to represent the give data
  */
case class SparklineWidget(
    style: Style = Style.DEFAULT,
    data: collection.Seq[Int] = Nil,
    max: Option[Int] = None,
    barSet: symbols.bar.Set = symbols.bar.NINE_LEVELS
) extends Widget {
  def render(area: Rect, buf: Buffer): Unit = {
    if (area.height < 1) {
      return
    }

    val max = this.max match {
      case Some(v) => v
      case None    => this.data.maxOption.getOrElse(1)
    }
    val max_index = math.min(area.width, this.data.length)
    val data = this.data.take(max_index).toArray.map { e =>
      if (max != 0) {
        e * area.height * 8 / max
      } else {
        0
      }
    }

    ranges.revRange(0, area.height) { j =>
      ranges.range(0, data.length) { i =>
        val d = data(i)
        val symbol = d match {
          case 0 => barSet.empty
          case 1 => barSet.oneEighth
          case 2 => barSet.oneQuarter
          case 3 => barSet.threeEighths
          case 4 => barSet.half
          case 5 => barSet.fiveEighths
          case 6 => barSet.threeQuarters
          case 7 => barSet.sevenEighths
          case _ => barSet.full
        }
        buf
          .get(area.left + i, area.top + j)
          .setSymbol(symbol)
          .setStyle(style)

        if (d > 8) {
          data(i) -= 8
        } else {
          data(i) = 0
        }
      }
    }
  }
}
