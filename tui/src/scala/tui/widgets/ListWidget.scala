package tui
package widgets

import tui.internal.ranges
import tui.internal.saturating._

/// A widget to display several items among which one can be selected (optional)
case class ListWidget(
    block: Option[BlockWidget] = None,
    items: Array[ListWidget.Item],
    /// Style used as a base style for the widget
    style: Style = Style.DEFAULT,
    start_corner: Corner = Corner.TopLeft,
    /// Style used to render selected item
    highlight_style: Style = Style.DEFAULT,
    /// Symbol in front of the selected item (Shift all items to the right)
    highlight_symbol: Option[String] = None,
    /// Whether to repeat the highlight symbol for each line of the selected item
    repeat_highlight_symbol: Boolean = false
) extends Widget
    with StatefulWidget {

  def get_items_bounds(
      selected0: Option[Int],
      offset0: Int,
      max_height: Int
  ): (Int, Int) = {
    val offset = math.min(offset0, items.length.saturating_sub_unsigned(1))
    var start = offset
    var end = offset
    var height = 0
    val it = items.iterator.drop(offset)
    var continue = true
    while (continue && it.hasNext) {
      val item = it.next()
      if (height + item.height > max_height) {
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
      while (height > max_height) {
        height = height.saturating_sub_unsigned(items(start).height)
        start += 1
      }
    }
    while (selected < start) {
      start -= 1
      height = height.saturating_add(items(start).height)
      while (height > max_height) {
        end -= 1
        height = height.saturating_sub_unsigned(items(end).height)
      }
    }
    (start, end)
  }

  type State = ListWidget.State

  def render(area: Rect, buf: Buffer, state: State): Unit = {
    buf.set_style(area, style)
    val list_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.render(area, buf)
        inner_area
      case None => area
    }

    if (list_area.width < 1 || list_area.height < 1) {
      return
    }

    if (items.isEmpty) {
      return
    }
    val list_height = list_area.height

    val (start, end) = get_items_bounds(state.selected, state.offset, list_height)
    state.offset = start

    val highlight_symbol1 = highlight_symbol.getOrElse("")
    val blank_symbol = " ".repeat(Grapheme(highlight_symbol1).width)

    var current_height = 0
    val has_selection = state.selected.isDefined
    ranges.range(state.offset, state.offset + end - start) { i =>
      val item = items(i)
      val (x, y) = start_corner match {
        case Corner.BottomLeft =>
          current_height += item.height
          (list_area.left, list_area.bottom - current_height)
        case _ =>
          val pos = (list_area.left, list_area.top + current_height)
          current_height += item.height
          pos
      }
      val area = Rect(x, y, width = list_area.width, height = item.height)

      val item_style = style.patch(item.style)
      buf.set_style(area, item_style)

      val is_selected = state.selected.contains(i)
      item.content.lines.zipWithIndex.foreach { case (line, j) =>
        // if the item is selected, we need to display the hightlight symbol:
        // - either for the first line of the item only,
        // - or for each line of the item if the appropriate option is set
        val symbol = if (is_selected && (j == 0 || repeat_highlight_symbol)) {
          highlight_symbol1
        } else {
          blank_symbol
        }
        val (elem_x, max_element_width) = if (has_selection) {
          val (elem_x, _) = buf.set_stringn(
            x,
            y + j,
            symbol,
            list_area.width,
            item_style
          )
          (elem_x, list_area.width - (elem_x - x))
        } else {
          (x, list_area.width)
        }
        buf.set_spans(elem_x, y + j, line, max_element_width);
      }
      if (is_selected) {
        buf.set_style(area, highlight_style)
      }
    }
  }

  def render(area: Rect, buf: Buffer): Unit = {
    val state = ListWidget.State()
    render(area, buf, state)
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
