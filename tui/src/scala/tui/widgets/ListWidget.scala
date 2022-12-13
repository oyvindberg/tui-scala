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
    val list_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.patchedStyle(style).render(area, buf)
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

    val highlight_symbol = this.highlight_symbol.getOrElse("")
    val blank_symbol = " ".repeat(Grapheme(highlight_symbol).width)

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
      val is_selected = state.selected.contains(i)
      val s = if (is_selected) Some(highlight_style) else None
      val item_style = style / item.style

      item.content.lines.zipWithIndex.foreach { case (line, j) =>
        val newLine = if (has_selection) {
          if (is_selected && (j == 0 || repeat_highlight_symbol)) {
            Spans(Span.nostyle(highlight_symbol) +: line.spans)
          } else {
            Spans(Span.nostyle(blank_symbol) +: line.spans)
          }
        } else line

        val paddedLine = Spans(newLine.spans.padTo(list_area.width - newLine.width, Span.nostyle(" ")))

        buf.set_spans(x, y + j, item_style / paddedLine / s, list_area.right - x);
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
