package tui
package widgets
package tabs

import tui.internal.ranges
import tui.internal.saturating._

/** A widget to display available tabs in a multiple panels context.
  *
  * @param block
  *   A block to wrap this widget in if necessary
  * @param titles
  *   One title for each tab
  * @param selected
  *   The index of the selected tabs
  * @param style
  *   The style used to draw the text
  * @param highlightStyle
  *   Style to apply to the selected item
  * @param divider
  *   Tab divider
  */
case class TabsWidget(
    titles: Array[Spans],
    selected: Int = 0,
    style: Style = Style.DEFAULT,
    highlightStyle: Style = Style.DEFAULT,
    divider: Span = Span.nostyle(symbols.line.VERTICAL)
) extends Widget {

  def render(area: Rect, buf: Buffer): Unit = {
    buf.setStyle(area, style)
    if (area.height < 1) {
      return
    }

    var x = area.left
    val titles_length = titles.length
    ranges.range(0, titles_length) { i =>
      val title = titles(i)
      val last_title = titles_length - 1 == i
      x = x.saturating_add(1)
      val remaining_width = area.right.saturating_sub_unsigned(x)
      if (remaining_width == 0) {
        ()
      } else {
        val pos = buf.setSpans(x, area.top, title, remaining_width)
        if (i == selected) {
          buf.setStyle(
            Rect(
              x,
              y = area.top,
              width = pos._1.saturating_sub_unsigned(x),
              height = 1
            ),
            highlightStyle
          )
        }
        x = pos._1.saturating_add(1)
        val remaining_width1 = area.right.saturating_sub_unsigned(x)
        if (remaining_width1 == 0 || last_title) {
          ()
        } else {
          val pos = buf.setSpan(x, area.top, divider, remaining_width1)
          x = pos._1
        }
      }
    }
  }
}
