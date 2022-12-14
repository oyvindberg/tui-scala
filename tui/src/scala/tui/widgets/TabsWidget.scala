package tui
package widgets
package tabs

import tui.internal.breakableForeach._
import tui.internal.saturating._

/// A widget to display available tabs in a multiple panels context.
case class TabsWidget(
    /// A block to wrap this widget in if necessary
    block: Option[BlockWidget] = None,
    /// One title for each tab
    titles: Array[Spans],
    /// The index of the selected tabs
    selected: Int = 0,
    /// The style used to draw the text
    style: Style = Style.DEFAULT,
    /// Style to apply to the selected item
    highlight_style: Style = Style.DEFAULT,
    /// Tab divider
    divider: Span = Span.nostyle(symbols.line.VERTICAL)
) extends Widget {

  def render(area: Rect, buf: Buffer): Unit = {
    val tabs_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.patchedStyle(style).render(area, buf)
        inner_area
      case None => area
    }

    if (tabs_area.height < 1) {
      return
    }

    var x = tabs_area.left
    val titles_length = titles.length
    titles.breakableForeach { (title, i) =>
      val last_title = titles_length - 1 == i
      x = x.saturating_add(1)
      val remaining_width = tabs_area.right.saturating_sub_unsigned(x)
      if (remaining_width == 0) {
        Break
      } else {
        val s = if (i == selected) Some(highlight_style) else None
        val pos = buf.set_spans(x, tabs_area.top, style / title / s, remaining_width)
        x = pos._1.saturating_add(1)
        val remaining_width1 = tabs_area.right.saturating_sub_unsigned(x)
        if (remaining_width1 == 0 || last_title) {
          ()
        } else {
          val pos = buf.set_span(x, tabs_area.top, style / divider, remaining_width1)
          x = pos._1
        }
        Continue
      }
    }
  }
}
