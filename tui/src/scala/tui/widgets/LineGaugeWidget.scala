package tui.widgets

import tui.internal.ranges
import tui._
import tui.internal.saturating.IntOps

/** A compact widget to display a task progress over a single line.
  */
case class LineGaugeWidget(
    ratio: GaugeWidget.Ratio = GaugeWidget.Ratio.Zero,
    label: Option[Spans] = None,
    lineSet: symbols.line.Set = symbols.line.NORMAL,
    style: Style = Style.DEFAULT,
    gaugeStyle: Style = Style.DEFAULT
) extends Widget {
  override def render(area: Rect, buf: Buffer): Unit = {
    buf.setStyle(area, style)
    if (area.height < 1) {
      return
    }

    val label = this.label.getOrElse(Spans.nostyle(s"${(ratio.value * 100.0).toInt}%"))
    val (col, row) = buf.setSpans(
      area.left,
      area.top,
      label,
      area.width
    )
    val start = col + 1
    if (start >= area.right) {
      return
    }

    val end = start + (area.right.saturating_sub_unsigned(start).toDouble * ratio.value).floor.toInt
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
    ranges.range(end, area.right) { col =>
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
