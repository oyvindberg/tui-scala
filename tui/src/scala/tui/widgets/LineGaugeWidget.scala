package tui.widgets

import tui.internal.ranges
import tui._
import tui.internal.saturating.IntOps

/** A compact widget to display a task progress over a single line.
  */
case class LineGaugeWidget(
    block: Option[BlockWidget] = None,
    ratio: GaugeWidget.Ratio = GaugeWidget.Ratio.Zero,
    label: Option[Spans] = None,
    lineSet: symbols.line.Set = symbols.line.NORMAL,
    style: Style = Style.DEFAULT,
    gaugeStyle: Style = Style.DEFAULT
) extends Widget {
  override def render(area: Rect, buf: Buffer): Unit = {
    buf.setStyle(area, style)
    val gauge_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.render(area, buf)
        inner_area
      case None => area
    }

    if (gauge_area.height < 1) {
      return
    }

    val label = this.label.getOrElse(Spans.nostyle(s"${(ratio.value * 100.0).toInt}%"))
    val (col, row) = buf.setSpans(
      gauge_area.left,
      gauge_area.top,
      label,
      gauge_area.width
    )
    val start = col + 1
    if (start >= gauge_area.right) {
      return
    }

    val end = start + (gauge_area.right.saturating_sub_unsigned(start).toDouble * ratio.value).floor.toInt
    ranges.range(start, end) { col =>
      buf
        .get(col, row)
        .setSymbol(lineSet.horizontal)
        .setStyle(
          Style(
            fg = gaugeStyle.fg,
            bg = None,
            addModifier = gaugeStyle.addModifier,
            subModifier = gaugeStyle.subModifier
          )
        )
      ()
    }
    ranges.range(end, gauge_area.right) { col =>
      buf
        .get(col, row)
        .setSymbol(lineSet.horizontal)
        .setStyle(
          Style(
            fg = gaugeStyle.bg,
            bg = None,
            addModifier = gaugeStyle.addModifier,
            subModifier = gaugeStyle.subModifier
          )
        )
      ()
    }
  }
}
