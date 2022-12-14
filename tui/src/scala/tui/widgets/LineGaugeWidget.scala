package tui.widgets

import tui.internal.ranges
import tui._
import tui.internal.saturating.IntOps

/// A compact widget to display a task progress over a single line.
case class LineGaugeWidget(
    block: Option[BlockWidget] = None,
    ratio: GaugeWidget.Ratio = GaugeWidget.Ratio.Zero,
    label: Option[Spans] = None,
    line_set: symbols.line.Set = symbols.line.NORMAL,
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

    val label = this.label.getOrElse(Spans.nostyle(s"${(ratio.value * 100.0).toInt}%"))
    val (col, row) = buf.set_spans(gauge_area.left, gauge_area.top, style / label, gauge_area.width)
    val start = col + 1
    if (start >= gauge_area.right) {
      return
    }

    val end = start + (gauge_area.right.saturating_sub_unsigned(start).toDouble * ratio.value).floor.toInt
    val non_empty_cell = Cell(
      line_set.horizontal,
      style / Style(
        fg = gauge_style.fg,
        bg = None,
        add_modifier = gauge_style.add_modifier,
        sub_modifier = gauge_style.sub_modifier
      )
    )
    ranges.range(start, end)(col => buf.set(col, row, non_empty_cell))

    val empty_cell = Cell(
      line_set.horizontal,
      style / Style(
        fg = gauge_style.bg,
        bg = None,
        add_modifier = gauge_style.add_modifier,
        sub_modifier = gauge_style.sub_modifier
      )
    )
    ranges.range(end, gauge_area.right)(col => buf.set(col, row, empty_cell))
  }
}
