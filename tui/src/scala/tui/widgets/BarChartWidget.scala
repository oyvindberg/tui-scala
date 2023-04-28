package tui
package widgets

import tui.internal.ranges._
import tui.{Grapheme, Style}

/** Display multiple bars in a single widgets
  *
  * @param block
  *   Block to wrap the widget in
  * @param bar_width
  *   The width of each bar
  * @param bar_gap
  *   The gap between each bar
  * @param bar_set
  *   Set of symbols used to display the data
  * @param bar_style
  *   Style of the bars
  * @param value_style
  *   Style of the values printed at the bottom of each bar
  * @param label_style
  *   Style of the labels printed under each bar
  * @param style
  *   Style for the widget
  * @param data
  *   Slice of (label, value) pair to plot on the chart
  * @param max
  *   Value necessary for a bar to reach the maximum height (if no value is specified, the maximum value in the data is taken as reference)
  */
case class BarChartWidget(
    block: Option[BlockWidget] = None,
    bar_width: Int = 1,
    bar_gap: Int = 1,
    bar_set: symbols.bar.Set = symbols.bar.NINE_LEVELS,
    bar_style: Style = Style.DEFAULT,
    value_style: Style = Style.DEFAULT,
    label_style: Style = Style.DEFAULT,
    style: Style = Style.DEFAULT,
    data: Array[(String, Int)] = Array.empty,
    max: Option[Int] = None
) extends Widget {

  /** Values to display on the bar (computed when the data is passed to the widget)
    */
  private lazy val values: Array[Grapheme] = data.collect { case (_, v) => Grapheme(v.toString) }

  override def render(area: Rect, buf: Buffer): Unit = {
    buf.set_style(area, style)

    val chart_area: Rect = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.render(area, buf)
        inner_area
      case None => area
    }

    if (chart_area.height < 2) {
      return
    }

    val max = this.max.getOrElse(data.maxByOption { case (_, value) => value }.fold(0) { case (_, value) => value })

    val max_index = math.min(
      chart_area.width / (bar_width + bar_gap),
      data.length
    )

    case class Data(label: String, var value: Int)
    val data2 = this.data.take(max_index).map { case (l, v) => Data(l, v * (chart_area.height - 1) * 8 / math.max(max, 1)) }.zipWithIndex

    revRange(0, chart_area.height - 1) { j =>
      data2.foreach { case (d, i) =>
        val symbol = d.value match {
          case 0 => bar_set.empty
          case 1 => bar_set.one_eighth
          case 2 => bar_set.one_quarter
          case 3 => bar_set.three_eighths
          case 4 => bar_set.half
          case 5 => bar_set.five_eighths
          case 6 => bar_set.three_quarters
          case 7 => bar_set.seven_eighths
          case _ => bar_set.full
        }
        range(0, bar_width) { x =>
          buf
            .get(
              chart_area.left + i * (bar_width + bar_gap) + x,
              chart_area.top + j
            )
            .set_symbol(symbol)
            .set_style(bar_style)
          ()
        }

        if (d.value > 8) {
          d.value = d.value - 8
        } else {
          d.value = 0
        }
      }
    }
    data.take(max_index).zipWithIndex.foreach { case ((label, value), i) =>
      if (value != 0) {
        val value_label = values(i)
        val width = value_label.width
        if (width < bar_width) {
          buf.set_string(
            chart_area.left
              + i * (bar_width + bar_gap)
              + (bar_width - width) / 2,
            chart_area.bottom - 2,
            value_label.str,
            value_style
          )
        }
      }
      buf.set_stringn(
        chart_area.left + i * (bar_width + bar_gap),
        chart_area.bottom - 1,
        label,
        bar_width,
        label_style
      );

    }
  }
}
