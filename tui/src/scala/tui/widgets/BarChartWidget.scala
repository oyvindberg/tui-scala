package tui
package widgets

import tui.internal.ranges._
import tui.{Grapheme, Style}

/** Display multiple bars in a single widgets
  *
  * @param block
  *   Block to wrap the widget in
  * @param barWidth
  *   The width of each bar
  * @param barGap
  *   The gap between each bar
  * @param barSet
  *   Set of symbols used to display the data
  * @param barStyle
  *   Style of the bars
  * @param valueStyle
  *   Style of the values printed at the bottom of each bar
  * @param labelStyle
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
    barWidth: Int = 1,
    barGap: Int = 1,
    barSet: symbols.bar.Set = symbols.bar.NINE_LEVELS,
    barStyle: Style = Style.DEFAULT,
    valueStyle: Style = Style.DEFAULT,
    labelStyle: Style = Style.DEFAULT,
    style: Style = Style.DEFAULT,
    data: Array[(String, Int)] = Array.empty,
    max: Option[Int] = None
) extends Widget {

  /** Values to display on the bar (computed when the data is passed to the widget)
    */
  private lazy val values: Array[Grapheme] = data.collect { case (_, v) => Grapheme(v.toString) }

  override def render(area: Rect, buf: Buffer): Unit = {
    buf.setStyle(area, style)

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
      chart_area.width / (barWidth + barGap),
      data.length
    )

    case class Data(label: String, var value: Int)
    val data2 = this.data.take(max_index).map { case (l, v) => Data(l, v * (chart_area.height - 1) * 8 / math.max(max, 1)) }.zipWithIndex

    revRange(0, chart_area.height - 1) { j =>
      data2.foreach { case (d, i) =>
        val symbol = d.value match {
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
        range(0, barWidth) { x =>
          buf
            .get(
              chart_area.left + i * (barWidth + barGap) + x,
              chart_area.top + j
            )
            .setSymbol(symbol)
            .setStyle(barStyle)
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
        if (width < barWidth) {
          buf.setString(
            chart_area.left
              + i * (barWidth + barGap)
              + (barWidth - width) / 2,
            chart_area.bottom - 2,
            value_label.str,
            valueStyle
          )
        }
      }
      buf.setStringn(
        chart_area.left + i * (barWidth + barGap),
        chart_area.bottom - 1,
        label,
        barWidth,
        labelStyle
      )

    }
  }
}
