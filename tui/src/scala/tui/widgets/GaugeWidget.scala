package tui
package widgets

import tui.Style
import tui.internal.ranges

/** A widget to display a task progress.
  */
case class GaugeWidget(
    ratio: GaugeWidget.Ratio = GaugeWidget.Ratio.Zero,
    label: Option[Span] = None,
    useUnicode: Boolean = false,
    style: Style = Style.DEFAULT
) extends Widget {

  override def render(area: Rect, buf: Buffer): Unit = {
    buf.setStyle(area, style)
    if (area.height < 1) {
      return
    }

    // compute label value and its position
    // label is put at the center of the gauge_area
    val label = {
      val pct = math.round(ratio.value * 100.0)
      this.label.getOrElse(Span.nostyle(pct.toString + "%"))
    }
    val clamped_label_width = area.width.min(label.width)
    val label_col = area.left + (area.width - clamped_label_width) / 2
    val label_row = area.top + area.height / 2

    // the gauge will be filled proportionally to the ratio
    val filled_width = area.width.toDouble * this.ratio.value
    val end: Int = if (useUnicode) {
      area.left + math.floor(filled_width).toInt
    } else {
      area.left + math.round(filled_width).toInt
    }
    ranges.range(area.top, area.bottom) { y =>
      // render the filled area (left to end)
      ranges.range(area.left, end) { x =>
        // spaces are needed to apply the background styling
        buf
          .get(x, y)
          .setSymbol(" ")
          .setFg(style.bg.getOrElse(Color.Reset))
          .setBg(style.fg.getOrElse(Color.Reset))
        ()
      }
      if (useUnicode && ratio.value < 1.0) {
        buf
          .get(end, y)
          .setSymbol(getUnicodeBlock(filled_width % 1.0))
        ()
      }
    }
    // set the span
    buf.setSpan(label_col, label_row, label, clamped_label_width)
    ()
  }

  def getUnicodeBlock(frac: Double): String =
    math.round(frac * 8.0) match {
      case 1 => symbols.block.ONE_EIGHTH
      case 2 => symbols.block.ONE_QUARTER
      case 3 => symbols.block.THREE_EIGHTHS
      case 4 => symbols.block.HALF
      case 5 => symbols.block.FIVE_EIGHTHS
      case 6 => symbols.block.THREE_QUARTERS
      case 7 => symbols.block.SEVEN_EIGHTHS
      case 8 => symbols.block.FULL
      case _ => " "
    }
}

object GaugeWidget {
  case class Ratio(value: Double) {
    require(value >= 0 && value <= 1, s"$value is not between 0 and 1")
  }

  object Ratio {
    val Zero = new Ratio(0.0)

    def percent(value: Double): Ratio =
      new Ratio(value / 100)
  }
}
