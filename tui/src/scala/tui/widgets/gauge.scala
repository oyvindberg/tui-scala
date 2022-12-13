package tui
package widgets

import tui.Style
import tui.buffer.Buffer
import tui.internal.ranges
import tui.internal.saturating._
import tui.layout.Rect
import tui.text.{Span, Spans}

case class Ratio(value: Double) {
  require(value >= 0 && value <= 1, s"$value is not between 0 and 1")
}

object Ratio {
  val Zero = new Ratio(0.0)
  def percent(value: Double): Ratio =
    new Ratio(value / 100)
}

/// A widget to display a task progress.
///
/// # Examples:
///
/// ```
/// # use tui.widgets.{Widget, Gauge, Block, Borders};
/// # use tui.style.{Style, Color, Modifier};
/// Gauge.default()
///     .block(Block.default().borders(Borders.ALL).title("Progress"))
///     .gauge_style(Style.DEFAULT.fg(Color.White).bg(Color.Black).add_modifier(Modifier.ITALIC))
///     .percent(20);
/// ```
case class Gauge(
    block: Option[Block] = None,
    ratio: Ratio = Ratio.Zero,
    label: Option[Span] = None,
    use_unicode: Boolean = false,
    style: Style = Style.DEFAULT,
    gauge_style: Style = Style.DEFAULT
) extends Widget {

  override def render(area: Rect, buf: Buffer): Unit = {
    buf.set_style(area, style)
    val gauge_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.render(area, buf)
        inner_area

      case None => area
    }
    buf.set_style(gauge_area, gauge_style)
    if (gauge_area.height < 1) {
      return
    }

    // compute label value and its position
    // label is put at the center of the gauge_area
    val label = {
      val pct = math.round(ratio.value * 100.0)
      this.label.getOrElse(Span.from(pct.toString + "%"))
    }
    val clamped_label_width = gauge_area.width.min(label.width)
    val label_col = gauge_area.left + (gauge_area.width - clamped_label_width) / 2
    val label_row = gauge_area.top + gauge_area.height / 2

    // the gauge will be filled proportionally to the ratio
    val filled_width = gauge_area.width.toDouble * this.ratio.value
    val end: Int = if (use_unicode) {
      gauge_area.left + math.floor(filled_width).toInt
    } else {
      gauge_area.left + math.round(filled_width).toInt
    }
    ranges.range(gauge_area.top, gauge_area.bottom) { y =>
      // render the filled area (left to end)
      ranges.range(gauge_area.left, end) { x =>
        // spaces are needed to apply the background styling
        buf
          .get(x, y)
          .set_symbol(" ")
          .set_fg(gauge_style.bg.getOrElse(Color.Reset))
          .set_bg(gauge_style.fg.getOrElse(Color.Reset));
        ()
      }
      if (use_unicode && ratio.value < 1.0) {
        buf
          .get(end, y)
          .set_symbol(get_unicode_block(filled_width % 1.0))
        ()
      }
    }
    // set the span
    buf.set_span(label_col, label_row, label, clamped_label_width)
    ()
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

/// A compact widget to display a task progress over a single line.
///
/// # Examples:
///
/// ```
/// # use tui.widgets.{Widget, LineGauge, Block, Borders};
/// # use tui.style.{Style, Color, Modifier};
/// # use tui.symbols;
/// LineGauge.default()
///     .block(Block.default().borders(Borders.ALL).title("Progress"))
///     .gauge_style(Style.DEFAULT.fg(Color.White).bg(Color.Black).add_modifier(Modifier.BOLD))
///     .line_set(symbols.line.THICK)
///     .ratio(0.4);
/// ```
case class LineGauge(
    block: Option[Block] = None,
    ratio: Ratio = Ratio.Zero,
    label: Option[Spans] = None,
    line_set: symbols.line.Set = symbols.line.NORMAL,
    style: Style = Style.DEFAULT,
    gauge_style: Style = Style.DEFAULT
) extends Widget {
  override def render(area: Rect, buf: Buffer): Unit = {
    buf.set_style(area, style)
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

    val label = this.label.getOrElse(Spans.from(s"${(ratio.value * 100.0).toInt}%"))
    val (col, row) = buf.set_spans(
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
        .set_symbol(line_set.horizontal)
        .set_style(
          Style(
            fg = gauge_style.fg,
            bg = None,
            add_modifier = gauge_style.add_modifier,
            sub_modifier = gauge_style.sub_modifier
          )
        );
      ()
    }
    ranges.range(end, gauge_area.right) { col =>
      buf
        .get(col, row)
        .set_symbol(line_set.horizontal)
        .set_style(
          Style(
            fg = gauge_style.bg,
            bg = None,
            add_modifier = gauge_style.add_modifier,
            sub_modifier = gauge_style.sub_modifier
          )
        );
      ()
    }
  }
}
