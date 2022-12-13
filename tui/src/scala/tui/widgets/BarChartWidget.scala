package tui
package widgets

import tui.internal.ranges._
import tui.{Grapheme, Style}

/// Display multiple bars in a single widgets
///
/// # Examples
///
/// ```
/// # use tui::widgets::{Block, Borders, BarChart};
/// # use tui::style::{Style, Color, Modifier};
/// BarChart::default()
///     .block(Block::default().title("BarChart").borders(Borders::ALL))
///     .bar_width(3)
///     .bar_gap(1)
///     .bar_style(Style::DEFAULT.fg(Color::Yellow).bg(Color::Red))
///     .value_style(Style::DEFAULT.fg(Color::Red).add_modifier(Modifier::BOLD))
///     .label_style(Style::DEFAULT.fg(Color::White))
///     .data(&[("B0", 0), ("B1", 2), ("B2", 4), ("B3", 3)])
///     .max(4);
/// ```
//#[derive(Debug, Clone)]
case class BarChartWidget(
    /// Block to wrap the widget in
    block: Option[BlockWidget] = None,
    /// The width of each bar
    bar_width: Int = 1,
    /// The gap between each bar
    bar_gap: Int = 1,
    /// Set of symbols used to display the data
    bar_set: symbols.bar.Set = symbols.bar.NINE_LEVELS,
    /// Style of the bars
    bar_style: Style = Style.DEFAULT,
    /// Style of the values printed at the bottom of each bar
    value_style: Style = Style.DEFAULT,
    /// Style of the labels printed under each bar
    label_style: Style = Style.DEFAULT,
    /// Style for the widget
    style: Style = Style.DEFAULT,
    /// Slice of (label, value) pair to plot on the chart
    data: Array[(String, Int)] = Array.empty,
    /// Value necessary for a bar to reach the maximum height (if no value is specified,
    /// the maximum value in the data is taken as reference)
    max: Option[Int] = None
) extends Widget {
  /// Values to display on the bar (computed when the data is passed to the widget)
  private lazy val values: Array[Grapheme] = data.collect { case (_, v) => Grapheme(v.toString) }

  override def render(area: Rect, buf: Buffer): Unit = {
    buf.set_style(area, style);

    val chart_area: Rect = block match {
      case Some(b) =>
        val inner_area = b.inner(area);
        b.render(area, buf);
        inner_area
      case None => area
    };

    if (chart_area.height < 2) {
      return;
    }

    val max = this.max.getOrElse(data.maxByOption { case (_, value) => value }.fold(0) { case (_, value) => value })

    val max_index = math.min(
      chart_area.width / (bar_width + bar_gap),
      data.length
    );

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
            .set_style(bar_style);
          ()
        }

        if (d.value > 8) {
          d.value = d.value - 8;
        } else {
          d.value = 0
        }
      }
    }
    data.take(max_index).zipWithIndex.foreach { case ((label, value), i) =>
      if (value != 0) {
        val value_label = values(i);
        val width = value_label.width;
        if (width < bar_width) {
          buf.set_string(
            chart_area.left
              + i * (bar_width + bar_gap)
              + (bar_width - width) / 2,
            chart_area.bottom - 2,
            value_label.str,
            value_style
          );
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
