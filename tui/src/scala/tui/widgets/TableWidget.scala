package tui
package widgets

import tui.internal.breakableForeach
import tui.internal.breakableForeach.BreakableForeachArray
import tui.internal.saturating.IntOps
import tui.internal.stepBy.StepBySyntax

import scala.collection.mutable

/** A widget to display data in formatted columns.
  *
  * It is a collection of `Row`s, themselves composed of `Cell`s:
  *
  * @param block
  *   A block to wrap the widget in
  * @param style
  *   Base style for the widget
  * @param widths
  *   Width constraints for each column
  * @param columnSpacing
  *   Space between each column
  * @param highlightStyle
  *   Style used to render the selected row
  * @param highlightSymbol
  *   Symbol in front of the selected rom
  * @param header
  *   Optional header
  * @param rows
  *   Data to display in each row
  */
case class TableWidget(
    state: TableWidget.State = TableWidget.State(),
    style: Style = Style.DEFAULT,
    widths: Array[Constraint] = Array.empty,
    columnSpacing: Int = 1,
    highlightStyle: Style = Style.DEFAULT,
    highlightSymbol: Option[String] = None,
    header: Option[TableWidget.Row] = None,
    rows: Array[TableWidget.Row]
) extends Widget {
  def getColumnsWidths(max_width: Int, has_selection: Boolean): Array[Int] = {
    val constraints = mutable.ArrayBuffer.empty[Constraint]

    constraints.sizeHint(widths.length * 2 + 1)

    if (has_selection) {
      val highlight_symbol_width = highlightSymbol.map(s => Grapheme(s).width).getOrElse(0)
      constraints += Constraint.Length(highlight_symbol_width)
    }
    widths.foreach { constraint =>
      constraints += constraint
      constraints += Constraint.Length(columnSpacing);
    }
    if (widths.nonEmpty) {
      constraints.dropRightInPlace(1)
    }
    var chunks = Layout
      .cached(
        area = Rect(x = 0, y = 0, width = max_width, height = 1),
        direction = Direction.Horizontal,
        constraints = constraints.toArray,
        expandToFill = false,
        margin = Margin.None
      )

    if (has_selection) {
      chunks = chunks.drop(1)
    }
    chunks.stepBy(2).map(_.width)
  }

  def getRowBounds(
      selected0: Option[Int],
      offset0: Int,
      max_height: Int
  ): (Int, Int) = {
    val offset = math.min(offset0, rows.length.saturating_sub_unsigned(1))
    var start = offset
    var end = offset
    var height = 0
    rows.drop(offset).breakableForeach { case (item, _) =>
      if (height + item.height > max_height) {
        breakableForeach.Break
      } else {
        height += item.totalHeight
        end += 1
        breakableForeach.Continue
      }
    }

    val selected = math.min(selected0.getOrElse(0), rows.length - 1)
    while (selected >= end) {
      height = height.saturating_add(rows(end).totalHeight)
      end += 1
      while (height > max_height) {
        height = height.saturating_sub_unsigned(rows(start).totalHeight)
        start += 1
      }
    }
    while (selected < start) {
      start -= 1
      height = height.saturating_add(rows(start).totalHeight)
      while (height > max_height) {
        end -= 1
        height = height.saturating_sub_unsigned(rows(end).totalHeight)
      }
    }
    (start, end)
  }
  type State = TableWidget.State

  override def render(area: Rect, buf: Buffer): Unit = {
    if (area.area == 0) {
      return
    }
    buf.setStyle(area, style)
    val has_selection = state.selected.isDefined
    val columns_widths = getColumnsWidths(area.width, has_selection)
    val highlight_symbol = Grapheme(this.highlightSymbol.getOrElse(""))
    val blank_symbol = " ".repeat(highlight_symbol.width)
    var current_height = 0
    var rows_height = area.height

    // Draw header
    this.header.foreach { header =>
      val max_header_height = area.height.min(header.totalHeight)
      val header_rect = Rect(x = area.left, y = area.top, width = area.width, height = area.height.min(header.height))
      buf.setStyle(header_rect, header.style)
      var col = area.left
      if (has_selection) {
        col += highlight_symbol.width.min(area.width)
      }
      columns_widths.zip(header.cells).foreach { case (width, cell) =>
        val cell_rect = Rect(x = col, y = area.top, width = width, height = max_header_height)
        renderCell(buf, cell, cell_rect)
        col += width + columnSpacing;
      }
      current_height += max_header_height
      rows_height = rows_height.saturating_sub_unsigned(max_header_height);
    }

    // Draw rows
    if (rows.isEmpty) {
      return
    }
    val (start, end) = getRowBounds(state.selected, state.offset, rows_height)
    state.offset = start
    rows.zipWithIndex.slice(state.offset, state.offset + end - start).foreach { case (table_row, i) =>
      val (row, col0) = (area.top + current_height, area.left)
      current_height += table_row.totalHeight
      val table_row_area = Rect(x = col0, y = row, width = area.width, height = table_row.height)
      buf.setStyle(table_row_area, table_row.style)
      val is_selected = state.selected.contains(i)
      val table_row_start_col =
        if (has_selection) {
          val symbol = if (is_selected) highlight_symbol.str else blank_symbol
          val (col, _) = buf.setStringn(col0, row, symbol, area.width, table_row.style)
          col
        } else col0

      var col1 = table_row_start_col
      columns_widths.zip(table_row.cells).foreach { case (width, cell) =>
        val rect = Rect(x = col1, y = row, width = width, height = table_row.height)
        renderCell(buf, cell, rect)
        col1 += width + columnSpacing;
      }
      if (is_selected) {
        buf.setStyle(table_row_area, highlightStyle)
      }
    }
  }

  def renderCell(buf: Buffer, cell: TableWidget.Cell, area: Rect): Unit = {
    buf.setStyle(area, cell.style)
    cell.content.lines.breakableForeach { case (spans, i) =>
      if (i >= area.height) {
        breakableForeach.Break
      } else {
        buf.setSpans(area.x, area.y + i, spans, area.width)
        breakableForeach.Continue
      }
    }
  }
}

object TableWidget {

  /** A `Cell` contains the `Text` to be displayed in a `Row` of a `Table`.
    *
    * It can be created from anything that can be converted to a `Text`.
    */
  case class Cell(
      content: Text,
      style: Style = Style.DEFAULT
  )

  /** Holds data to be displayed in a `Table` widget.
    *
    * A `Row` is a collection of cells. It can be created from simple strings: You can also construct a row from any type that can be converted into `Text`: By
    * default, a row has a height of 1 but you can change this using `Row.height`.
    */
  case class Row(
      cells: Array[TableWidget.Cell],
      height: Int = 1,
      style: Style = Style.DEFAULT,
      bottomMargin: Int = 0
  ) {

    /** Returns the total height of the row.
      */
    val totalHeight: Int =
      height.saturating_add(bottomMargin)
  }

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
}
