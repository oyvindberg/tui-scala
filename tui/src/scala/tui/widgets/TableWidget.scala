package tui
package widgets

import tui.internal.breakableForeach
import tui.internal.breakableForeach.BreakableForeachArray
import tui.internal.saturating.IntOps
import tui.internal.stepBy.StepBySyntax

import scala.collection.mutable

/// A widget to display data in formatted columns.
///
/// It is a collection of [`Row`]s, themselves composed of [`Cell`]s:
/// ```rust
/// # use tui.widgets.{Block, Borders, Table, Row, Cell};
/// # use tui.layout.Constraint;
/// # use tui.style.{Style, Color, Modifier};
/// # use tui.text.{Text, Spans, Span};
/// Table.new(vec![
///     // Row can be created from simple strings.
///     Row.new(vec!["Row11", "Row12", "Row13"]),
///     // You can style the entire row.
///     Row.new(vec!["Row21", "Row22", "Row23"]).style(Style.DEFAULT.fg(Color.Blue)),
///     // If you need more control over the styling you may need to create Cells directly
///     Row.new(vec![
///         Cell.from("Row31"),
///         Cell.from("Row32").style(Style.DEFAULT.fg(Color.Yellow)),
///         Cell.from(Spans.from(vec![
///             Span.raw("Row"),
///             Span.styled("33", Style.DEFAULT.fg(Color.Green))
///         ])),
///     ]),
///     // If a Row need to display some content over multiple lines, you just have to change
///     // its height.
///     Row.new(vec![
///         Cell.from("Row\n41"),
///         Cell.from("Row\n42"),
///         Cell.from("Row\n43"),
///     ]).height(2),
/// ])
/// // You can set the style of the entire Table.
/// .style(Style.DEFAULT.fg(Color.White))
/// // It has an optional header, which is simply a Row always visible at the top.
/// .header(
///     Row.new(vec!["Col1", "Col2", "Col3"])
///         .style(Style.DEFAULT.fg(Color.Yellow))
///         // If you want some space between the header and the rest of the rows, you can always
///         // specify some margin at the bottom.
///         .bottom_margin(1)
/// )
/// // As any other widget, a Table can be wrapped in a Block.
/// .block(Block.default().title("Table"))
/// // Columns widths are constrained in the same way as Layout...
/// .widths(&[Constraint.Length(5), Constraint.Length(5), Constraint.Length(10)])
/// // ...and they can be separated by a fixed spacing.
/// .column_spacing(1)
/// // If you wish to highlight a row in any specific way when it is selected...
/// .highlight_style(Style.DEFAULT.add_modifier(Modifier.BOLD))
/// // ...and potentially show a symbol in front of the selection.
/// .highlight_symbol(">>");
/// ```
case class TableWidget(
    /// A block to wrap the widget in
    block: Option[BlockWidget] = None,
    /// Base style for the widget
    style: Style = Style.DEFAULT,
    /// Width constraints for each column
    widths: Array[Constraint] = Array.empty,
    /// Space between each column
    column_spacing: Int = 1,
    /// Style used to render the selected row
    highlight_style: Style = Style.DEFAULT,
    /// Symbol in front of the selected rom
    highlight_symbol: Option[String] = None,
    /// Optional header
    header: Option[TableWidget.Row] = None,
    /// Data to display in each row
    rows: Array[TableWidget.Row]
) extends StatefulWidget
    with Widget {
  def get_columns_widths(max_width: Int, has_selection: Boolean): Array[Int] = {
    val constraints = mutable.ArrayBuffer.empty[Constraint]

    constraints.sizeHint(widths.length * 2 + 1)

    if (has_selection) {
      val highlight_symbol_width = highlight_symbol.map(s => Grapheme(s).width).getOrElse(0)
      constraints += Constraint.Length(highlight_symbol_width)
    }
    widths.foreach { constraint =>
      constraints += constraint
      constraints += Constraint.Length(column_spacing);
    }
    if (widths.nonEmpty) {
      constraints.dropRightInPlace(1)
    }
    var chunks = Layout(direction = Direction.Horizontal, constraints = constraints.toArray, expand_to_fill = false)
      .split(Rect(x = 0, y = 0, width = max_width, height = 1))
    if (has_selection) {
      chunks = chunks.drop(1)
    }
    chunks.stepBy(2).map(_.width)
  }

  def get_row_bounds(
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
        height += item.total_height
        end += 1
        breakableForeach.Continue
      }
    }

    val selected = math.min(selected0.getOrElse(0), rows.length - 1)
    while (selected >= end) {
      height = height.saturating_add(rows(end).total_height)
      end += 1
      while (height > max_height) {
        height = height.saturating_sub_unsigned(rows(start).total_height)
        start += 1
      }
    }
    while (selected < start) {
      start -= 1
      height = height.saturating_add(rows(start).total_height)
      while (height > max_height) {
        end -= 1
        height = height.saturating_sub_unsigned(rows(end).total_height)
      }
    }
    (start, end)
  }
  type State = TableWidget.State

  override def render(area: Rect, buf: Buffer, state: State): Unit = {
    if (area.area == 0) {
      return
    }
    buf.set_style(area, style)
    val table_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.render(area, buf)
        inner_area

      case None => area
    }

    val has_selection = state.selected.isDefined
    val columns_widths = get_columns_widths(table_area.width, has_selection)
    val highlight_symbol = Grapheme(this.highlight_symbol.getOrElse(""))
    val blank_symbol = " ".repeat(highlight_symbol.width)
    var current_height = 0
    var rows_height = table_area.height

    // Draw header
    this.header.foreach { header =>
      val max_header_height = table_area.height.min(header.total_height)
      val header_rect = Rect(x = table_area.left, y = table_area.top, width = table_area.width, height = table_area.height.min(header.height))
      buf.set_style(header_rect, header.style)
      var col = table_area.left
      if (has_selection) {
        col += highlight_symbol.width.min(table_area.width)
      }
      columns_widths.zip(header.cells).foreach { case (width, cell) =>
        val cell_rect = Rect(x = col, y = table_area.top, width = width, height = max_header_height)
        render_cell(buf, cell, cell_rect)
        col += width + column_spacing;
      }
      current_height += max_header_height
      rows_height = rows_height.saturating_sub_unsigned(max_header_height);
    }

    // Draw rows
    if (rows.isEmpty) {
      return
    }
    val (start, end) = get_row_bounds(state.selected, state.offset, rows_height)
    state.offset = start
    rows.zipWithIndex.slice(state.offset, state.offset + end - start).foreach { case (table_row, i) =>
      val (row, col0) = (table_area.top + current_height, table_area.left)
      current_height += table_row.total_height
      val table_row_area = Rect(x = col0, y = row, width = table_area.width, height = table_row.height)
      buf.set_style(table_row_area, table_row.style)
      val is_selected = state.selected.contains(i)
      val table_row_start_col =
        if (has_selection) {
          val symbol = if (is_selected) highlight_symbol.str else blank_symbol
          val (col, _) = buf.set_stringn(col0, row, symbol, table_area.width, table_row.style)
          col
        } else col0

      var col1 = table_row_start_col
      columns_widths.zip(table_row.cells).foreach { case (width, cell) =>
        val rect = Rect(x = col1, y = row, width = width, height = table_row.height)
        render_cell(buf, cell, rect)
        col1 += width + column_spacing;
      }
      if (is_selected) {
        buf.set_style(table_row_area, highlight_style)
      }
    }
  }

  def render_cell(buf: Buffer, cell: TableWidget.Cell, area: Rect): Unit = {
    buf.set_style(area, cell.style)
    cell.content.lines.breakableForeach { case (spans, i) =>
      if (i >= area.height) {
        breakableForeach.Break
      } else {
        buf.set_spans(area.x, area.y + i, spans, area.width)
        breakableForeach.Continue
      }
    }
  }

  def render(area: Rect, buf: Buffer): Unit = {
    val state = TableWidget.State()
    render(area, buf, state)
  }
}

object TableWidget {

  /// A [`Cell`] contains the [`Text`] to be displayed in a [`Row`] of a [`Table`].
  ///
  /// It can be created from anything that can be converted to a [`Text`].
  /// ```rust
  /// # use tui.widgets.Cell;
  /// # use tui.style.{Style, Modifier};
  /// # use tui.text.{Span, Spans, Text};
  /// # use std.borrow.Cow;
  /// Cell.from("simple string");
  ///
  /// Cell.from(Span.from("span"));
  ///
  /// Cell.from(Spans.from(vec![
  ///     Span.raw("a vec of "),
  ///     Span.styled("spans", Style.DEFAULT.add_modifier(Modifier.BOLD))
  /// ]));
  ///
  /// Cell.from(Text.from("a text"));
  ///
  /// Cell.from(Text.from(Cow.Borrowed("hello")));
  /// ```
  ///
  /// You can apply a [`Style`] on the entire [`Cell`] using [`Cell.style`] or rely on the styling
  /// capabilities of [`Text`].
  case class Cell(
      content: Text,
      style: Style = Style.DEFAULT
  )

  object Cell {
    def from(str: String): Cell = from(Span.from(str))

    def from(span: Span): Cell = Cell(Text.from(span))
  }

  /// Holds data to be displayed in a [`Table`] widget.
  ///
  /// A [`Row`] is a collection of cells. It can be created from simple strings:
  /// ```rust
  /// # use tui.widgets.Row;
  /// Row.new(vec!["Cell1", "Cell2", "Cell3"]);
  /// ```
  ///
  /// But if you need a bit more control over individual cells, you can explicity create [`Cell`]s:
  /// ```rust
  /// # use tui.widgets.{Row, Cell};
  /// # use tui.style.{Style, Color};
  /// Row.new(vec![
  ///     Cell.from("Cell1"),
  ///     Cell.from("Cell2").style(Style.DEFAULT.fg(Color.Yellow)),
  /// ]);
  /// ```
  ///
  /// You can also construct a row from any type that can be converted into [`Text`]:
  /// ```rust
  /// # use std.borrow.Cow;
  /// # use tui.widgets.Row;
  /// Row.new(vec![
  ///     Cow.Borrowed("hello"),
  ///     Cow.Owned("world".to_uppercase()),
  /// ]);
  /// ```
  ///
  /// By default, a row has a height of 1 but you can change this using [`Row.height`].
  case class Row(
      cells: Array[TableWidget.Cell],
      height: Int = 1,
      style: Style = Style.DEFAULT,
      bottom_margin: Int = 0
  ) {
    /// Returns the total height of the row.
    val total_height: Int =
      height.saturating_add(bottom_margin)
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
