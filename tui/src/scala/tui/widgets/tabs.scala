package tui
package widgets
package tabs

import tui.buffer.Buffer
import tui.internal.ranges
import tui.internal.saturating._
import tui.layout.Rect
import tui.text.{Span, Spans}

/// A widget to display available tabs in a multiple panels context.
///
/// # Examples
///
/// ```
/// # use tui::widgets::{Block, Borders, Tabs};
/// # use tui::style::{Style, Color};
/// # use tui::text::{Spans};
/// # use tui::symbols::{DOT};
/// let titles = ["Tab1", "Tab2", "Tab3", "Tab4"].iter().cloned().map(Spans::from).collect();
/// Tabs::new(titles)
///     .block(Block::default().title("Tabs").borders(Borders::ALL))
///     .style(Style::DEFAULT.fg(Color::White))
///     .highlight_style(Style::DEFAULT.fg(Color::Yellow))
///     .divider(DOT);
/// ```

case class Tabs(
    /// A block to wrap this widget in if necessary
    block: Option[Block] = None,
    /// One title for each tab
    titles: Array[Spans],
    /// The index of the selected tabs
    selected: Int = 0,
    /// The style used to draw the text
    style: Style = Style.DEFAULT,
    /// Style to apply to the selected item
    highlight_style: Style = Style.DEFAULT,
    /// Tab divider
    divider: Span = Span.raw(symbols.line.VERTICAL)
) extends Widget {

  def render(area: Rect, buf: Buffer): Unit = {
    buf.set_style(area, style);
    val tabs_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area);
        b.render(area, buf);
        inner_area
      case None => area
    };

    if (tabs_area.height < 1) {
      return;
    }

    var x = tabs_area.left;
    val titles_length = titles.length;
    ranges.range(0, titles_length) { i =>
      val title = titles(i)
      val last_title = titles_length - 1 == i;
      x = x.saturating_add(1);
      val remaining_width = tabs_area.right.saturating_sub_unsigned(x);
      if (remaining_width == 0) {
        ()
      } else {
        val pos = buf.set_spans(x, tabs_area.top, title, remaining_width);
        if (i == selected) {
          buf.set_style(
            Rect(
              x,
              y = tabs_area.top,
              width = pos._1.saturating_sub_unsigned(x),
              height = 1
            ),
            highlight_style
          );
        }
        x = pos._1.saturating_add(1);
        val remaining_width1 = tabs_area.right.saturating_sub_unsigned(x);
        if (remaining_width1 == 0 || last_title) {
          ()
        } else {
          val pos = buf.set_span(x, tabs_area.top, divider, remaining_width1);
          x = pos._1;
        }
      }
    }
  }
}
