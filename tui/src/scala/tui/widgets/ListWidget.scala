package tui
package widgets

import tui.internal.ranges
import tui.internal.saturating._

/** A widget to display several items among which one can be selected (optional)
  *
  * @param block
  * @param items
  * @param style
  *   Style used as a base style for the widget
  * @param startCorner
  * @param highlightStyle
  *   Style used to render selected item
  * @param highlightSymbol
  *   Symbol in front of the selected item (Shift all items to the right)
  * @param repeatHighlightSymbol
  *   Whether to repeat the highlight symbol for each line of the selected item
  */
case class ListWidget(
    state: ListWidget.State = ListWidget.State(),
    items: Array[ListWidget.Item],
    style: Style = Style.DEFAULT,
    startCorner: Corner = Corner.TopLeft,
    highlightStyle: Style = Style.DEFAULT,
    highlightSymbol: Option[String] = None,
    repeatHightlightSymbol: Boolean = false
) extends Widget {

  def getItemsBounds(
      selected0: Option[Int],
      offset0: Int,
      maxHeight: Int
  ): (Int, Int) = {
    val offset = math.min(offset0, items.length.saturating_sub_unsigned(1))
    var start = offset
    var end = offset
    var height = 0
    val it = items.iterator.drop(offset)
    var continue = true
    while (continue && it.hasNext) {
      val item = it.next()
      if (height + item.height > maxHeight) {
        continue = false
      } else {
        height += item.height
        end += 1
      }
    }

    val selected = math.min(selected0.getOrElse(0), items.length - 1)
    while (selected >= end) {
      height = height.saturating_add(items(end).height)
      end += 1
      while (height > maxHeight) {
        height = height.saturating_sub_unsigned(items(start).height)
        start += 1
      }
    }
    while (selected < start) {
      start -= 1
      height = height.saturating_add(items(start).height)
      while (height > maxHeight) {
        end -= 1
        height = height.saturating_sub_unsigned(items(end).height)
      }
    }
    (start, end)
  }

  type State = ListWidget.State

  def render(area: Rect, buf: Buffer): Unit = {
    buf.setStyle(area, style)
    if (area.width < 1 || area.height < 1) {
      return
    }

    if (items.isEmpty) {
      return
    }
    val list_height = area.height

    val (start, end) = getItemsBounds(state.selected, state.offset, list_height)
    state.offset = start

    val highlight_symbol1 = highlightSymbol.getOrElse("")
    val blank_symbol = " ".repeat(Grapheme(highlight_symbol1).width)

    var current_height = 0
    val has_selection = state.selected.isDefined
    ranges.range(state.offset, state.offset + end - start) { i =>
      val item = items(i)
      val (x, y) = startCorner match {
        case Corner.BottomLeft =>
          current_height += item.height
          (area.left, area.bottom - current_height)
        case _ =>
          val pos = (area.left, area.top + current_height)
          current_height += item.height
          pos
      }
      val itemArea = Rect(x, y, width = area.width, height = item.height)

      val item_style = style.patch(item.style)
      buf.setStyle(itemArea, item_style)

      val is_selected = state.selected.contains(i)
      item.content.lines.zipWithIndex.foreach { case (line, j) =>
        // if the item is selected, we need to display the hightlight symbol:
        // - either for the first line of the item only,
        // - or for each line of the item if the appropriate option is set
        val symbol = if (is_selected && (j == 0 || repeatHightlightSymbol)) {
          highlight_symbol1
        } else {
          blank_symbol
        }
        val (elem_x, max_element_width) = if (has_selection) {
          val (elem_x, _) = buf.setStringn(
            x,
            y + j,
            symbol,
            area.width,
            item_style
          )
          (elem_x, area.width - (elem_x - x))
        } else {
          (x, area.width)
        }
        buf.setSpans(elem_x, y + j, line, max_element_width);
      }
      if (is_selected) {
        buf.setStyle(itemArea, highlightStyle)
      }
    }
  }
}

object ListWidget {
  case class State(
      var offset: Int = 0,
      var selected: Option[Int] = None
  ) {
    def select(index: Option[Int]): Unit = {
      selected = index
      if (index.isEmpty) {
        offset = 0
      }
    }
  }

  case class Item(content: Text, style: Style = Style.DEFAULT) {
    def height: Int = content.height
  }
}
