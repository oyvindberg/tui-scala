package tui
package widgets

import tui.internal.ranges

/// Widget to render a sparkline over one or more lines.

case class SparklineWidget(
    /// A block to wrap the widget in
    block: Option[BlockWidget] = None,
    /// Widget style
    style: Style = Style.DEFAULT,
    /// A slice of the data to display
    data: collection.Seq[Int] = Nil,
    /// The maximum value to take to compute the maximum bar height (if nothing is specified, the
    /// widget uses the max of the dataset)
    max: Option[Int] = None,
    /// A set of bar symbols used to represent the give data
    bar_set: symbols.bar.Set = symbols.bar.NINE_LEVELS
) extends Widget {
  def render(area: Rect, buf: Buffer): Unit = {
    val spark_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area);
        b.render(area, buf);
        inner_area
      case None => area
    };

    if (spark_area.height < 1) {
      return;
    }

    val max = this.max match {
      case Some(v) => v
      case None    => this.data.maxOption.getOrElse(1)
    };
    val max_index = math.min(spark_area.width, this.data.length);
    val data = this.data.take(max_index).map { e =>
      if (max != 0) {
        e * spark_area.height * 8 / max
      } else {
        0
      }
    }

    ranges.revRange(0, spark_area.height) { j =>
      ranges.range(0, data.length) { i =>
        var d = data(i)
        val symbol = d match {
          case 0 => bar_set.empty
          case 1 => bar_set.one_eighth
          case 2 => bar_set.one_quarter
          case 3 => bar_set.three_eighths
          case 4 => bar_set.half
          case 5 => bar_set.five_eighths
          case 6 => bar_set.three_quarters
          case 7 => bar_set.seven_eighths
          case _ => bar_set.full
        };
        buf
          .get(spark_area.left + i, spark_area.top + j)
          .set_symbol(symbol)
          .set_style(style);

        if (d > 8) {
          d -= 8;
        } else {
          d = 0;
        }
      }
    }
  }
}
