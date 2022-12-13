package tui
package widgets

import tui.Style
import tui.internal.ranges

/// A widget to display a task progress.
case class GaugeWidget(
    block: Option[BlockWidget] = None,
    ratio: GaugeWidget.Ratio = GaugeWidget.Ratio.Zero,
    label: Option[Span] = None,
    use_unicode: Boolean = false,
    style: Style = Style.DEFAULT,
    gauge_style: Style = Style.DEFAULT
) extends Widget {

  override def render(area: Rect, buf: Buffer): Unit = {
    val gauge_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.patchedStyle(style).render(area, buf)
        inner_area

      case None => area
    }
    if (gauge_area.height < 1) {
      return
    }

    // compute label value and its position
    // label is put at the center of the gauge_area
    val label = {
      val pct = math.round(ratio.value * 100.0)
      this.label.getOrElse(Span.nostyle(pct.toString + "%"))
    }
    val clamped_label_width = gauge_area.width.min(label.width)
    val label_col = gauge_area.left + (gauge_area.width - clamped_label_width) / 2
    val label_row = gauge_area.top + gauge_area.height / 2

    // the gauge will be filled proportionally to the ratio
    val filled_width = gauge_area.width.toDouble * this.ratio.value
    val filled_width_int = math.floor(filled_width).toInt

    val row = {
      val filled_style = style / gauge_style.copy(fg = gauge_style.bg, bg = gauge_style.fg)
      val filled_span = Span(" ", filled_style)
      val filled = Array.fill(filled_width_int)(filled_span)
      val maybePartial = if (use_unicode && ratio.value < 1.0) Some(Span(get_unicode_block(filled_width % 1.0), style / gauge_style)) else None
      val unfilled_span = Span(" ", style / gauge_style)
      Spans((filled ++ maybePartial).padTo(gauge_area.width, unfilled_span))
    }

    ranges.range(gauge_area.top, gauge_area.bottom) { y =>
      buf.set_spans(gauge_area.left, y, row, gauge_area.width)
      ()
    }

    // render label
    label.content.take(clamped_label_width).zipWithIndex.foreach { case (c, i) =>
      buf.update(label_col + i, label_row)(cell => cell.withSymbol(c).withStyle(label.style))
    }
  }

  def get_unicode_block(frac: Double): String =
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
