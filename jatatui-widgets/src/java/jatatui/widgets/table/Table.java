package jatatui.widgets.table;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.text.Text;
import jatatui.core.widgets.StatefulWidget;
import jatatui.core.widgets.Widget;
import jatatui.widgets.block.Block;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// A widget to display data in formatted columns.
///
/// A `Table` is a collection of [Row]s, each composed of [TableCell]s.
///
/// You can construct a [Table] using [Table#of(List, List)] / [Table#empty()] and then chain
/// `withFoo` builder-style methods to set the desired properties.
///
/// Make sure to call [#withWidths(List)], otherwise the columns will all have a width derived from
/// the table area divided by the number of columns. Note: if `widths` is empty, the table will be
/// rendered with equal widths.
///
/// [Table] implements [Widget] (renders without state) and [StatefulWidget] (renders with a
/// [TableState] that allows scrolling and selection). When rendered with a [TableState], the
/// selected row, column and cell are highlighted; if the selected row is not visible (based on the
/// offset), the table is scrolled to make it visible.
///
/// Highlight styles are applied in the following order: Row, Column, Cell.
///
/// Mirrors `ratatui_widgets::table::Table` (v0.30).
public record Table(
    List<Row> rows,
    Optional<Row> header,
    Optional<Row> footer,
    List<Constraint> widths,
    int columnSpacing,
    Optional<Block> block,
    Style style,
    Style rowHighlightStyle,
    Style columnHighlightStyle,
    Style cellHighlightStyle,
    Text highlightSymbol,
    HighlightSpacing highlightSpacing,
    Flex flex)
    implements Widget, StatefulWidget<TableState>, Stylize<Table> {

  /// Canonical constructor: defensively copies lists and validates percentages.
  public Table {
    rows = List.copyOf(rows);
    widths = List.copyOf(widths);
    ensurePercentagesLessThan100(widths);
  }

  /// Creates an empty [Table] (no rows, no widths) with the default style.
  public static Table empty() {
    return new Table(
        List.of(),
        Optional.empty(),
        Optional.empty(),
        List.of(),
        1,
        Optional.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty(),
        Text.empty(),
        HighlightSpacing.defaultValue(),
        Flex.Start);
  }

  /// Creates a new [Table] widget with the given rows and column widths.
  ///
  /// If the widths contain a [Constraint.Percentage] greater than 100 the constructor throws
  /// [IllegalArgumentException].
  public static Table of(List<Row> rows, List<Constraint> widths) {
    return empty().withRows(rows).withWidths(widths);
  }

  // ---- Fluent setters ----

  /// Returns a copy with the given rows. This does **not** alter the column widths; call
  /// [#withWidths(List)] separately if needed.
  public Table withRows(List<Row> rows) {
    return new Table(
        new ArrayList<>(rows),
        header,
        footer,
        widths,
        columnSpacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the given header row.
  public Table withHeader(Row header) {
    return new Table(
        rows,
        Optional.of(header),
        footer,
        widths,
        columnSpacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the given footer row.
  public Table withFooter(Row footer) {
    return new Table(
        rows,
        header,
        Optional.of(footer),
        widths,
        columnSpacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the column width constraints set. Throws [IllegalArgumentException] if
  /// any constraint is a [Constraint.Percentage] greater than 100.
  public Table withWidths(List<Constraint> widths) {
    return new Table(
        rows,
        header,
        footer,
        new ArrayList<>(widths),
        columnSpacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the spacing between columns set.
  public Table withColumnSpacing(int spacing) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        spacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy wrapped in the given [Block].
  public Table withBlock(Block block) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        columnSpacing,
        Optional.of(block),
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the base style set.
  public Table withStyle(Style style) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        columnSpacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the selected-row highlight style set.
  public Table withRowHighlightStyle(Style style) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        columnSpacing,
        block,
        this.style,
        style,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the selected-column highlight style set.
  public Table withColumnHighlightStyle(Style style) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        columnSpacing,
        block,
        this.style,
        rowHighlightStyle,
        style,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the selected-cell highlight style set.
  public Table withCellHighlightStyle(Style style) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        columnSpacing,
        block,
        this.style,
        rowHighlightStyle,
        columnHighlightStyle,
        style,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Returns a copy with the highlight symbol set (rendered in front of the selected row).
  public Table withHighlightSymbol(Text highlightSymbol) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        columnSpacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  /// Convenience overload: builds a [Text] from the given string and uses it as the highlight
  /// symbol.
  public Table withHighlightSymbol(String highlightSymbol) {
    return withHighlightSymbol(Text.from(highlightSymbol));
  }

  /// Returns a copy with the [HighlightSpacing] set.
  public Table withHighlightSpacing(HighlightSpacing value) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        columnSpacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        value,
        flex);
  }

  /// Returns a copy with how extra horizontal space is distributed amongst columns set.
  public Table withFlex(Flex flex) {
    return new Table(
        rows,
        header,
        footer,
        widths,
        columnSpacing,
        block,
        style,
        rowHighlightStyle,
        columnHighlightStyle,
        cellHighlightStyle,
        highlightSymbol,
        highlightSpacing,
        flex);
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Table setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Widget / StatefulWidget rendering ----

  /// Renders the table without any selection state (a fresh [TableState] is allocated internally).
  @Override
  public void render(Rect area, Buffer buf) {
    TableState state = new TableState();
    render(area, buf, state);
  }

  /// Renders the table using the given [TableState]; the state is mutated to reflect the offset
  /// and clamped selection after rendering.
  @Override
  public void render(Rect area, Buffer buf, TableState state) {
    buf.setStyle(area, style);
    block.ifPresent(b -> b.render(area, buf));
    Rect tableArea = block.map(b -> b.inner(area)).orElse(area);
    if (tableArea.isEmpty()) return;

    if (state.selected().isPresent() && state.selected().get() >= rows.size()) {
      state.select(Optional.of(Math.max(0, rows.size() - 1)));
    }
    if (rows.isEmpty()) {
      state.select(Optional.empty());
    }

    int columnCount = columnCount();
    if (state.selectedColumn().isPresent() && state.selectedColumn().get() >= columnCount) {
      state.selectColumn(Optional.of(Math.max(0, columnCount - 1)));
    }
    if (columnCount == 0) {
      state.selectColumn(Optional.empty());
    }

    int selectionWidth = selectionWidth(state);
    List<XAndWidth> columnWidths = getColumnWidths(tableArea.width(), selectionWidth, columnCount);
    HeaderRowsFooter areas = layout(tableArea);

    renderHeader(areas.headerArea(), buf, columnWidths);
    renderRows(areas.rowsArea(), buf, state, selectionWidth, columnWidths);
    renderFooter(areas.footerArea(), buf, columnWidths);
  }

  // ---- Internal helpers ----

  /// Splits the table area into a header, rows area and a footer.
  private HeaderRowsFooter layout(Rect area) {
    int headerTopMargin = header.map(Row::topMargin).orElse(0);
    int headerHeight = header.map(Row::height).orElse(0);
    int headerBottomMargin = header.map(Row::bottomMargin).orElse(0);
    int footerTopMargin = footer.map(Row::topMargin).orElse(0);
    int footerHeight = footer.map(Row::height).orElse(0);
    int footerBottomMargin = footer.map(Row::bottomMargin).orElse(0);
    Layout layout =
        Layout.vertical(
            new Constraint.Length(headerTopMargin),
            new Constraint.Length(headerHeight),
            new Constraint.Length(headerBottomMargin),
            new Constraint.Min(0),
            new Constraint.Length(footerTopMargin),
            new Constraint.Length(footerHeight),
            new Constraint.Length(footerBottomMargin));
    Rect[] segs = layout.split(area);
    return new HeaderRowsFooter(segs[1], segs[3], segs[5]);
  }

  private void renderHeader(Rect area, Buffer buf, List<XAndWidth> columnWidths) {
    if (header.isEmpty()) return;
    Row h = header.get();
    buf.setStyle(area, h.style());
    int n = Math.min(columnWidths.size(), h.cells().size());
    for (int i = 0; i < n; i++) {
      XAndWidth xw = columnWidths.get(i);
      h.cells().get(i).render(new Rect(area.x() + xw.x(), area.y(), xw.width(), area.height()), buf);
    }
  }

  private void renderFooter(Rect area, Buffer buf, List<XAndWidth> columnWidths) {
    if (footer.isEmpty()) return;
    Row f = footer.get();
    buf.setStyle(area, f.style());
    int n = Math.min(columnWidths.size(), f.cells().size());
    for (int i = 0; i < n; i++) {
      XAndWidth xw = columnWidths.get(i);
      f.cells().get(i).render(new Rect(area.x() + xw.x(), area.y(), xw.width(), area.height()), buf);
    }
  }

  private void renderRows(
      Rect area,
      Buffer buf,
      TableState state,
      int selectionWidth,
      List<XAndWidth> columnsWidths) {
    if (rows.isEmpty()) return;

    StartAndEnd visible = visibleRows(state, area);
    int startIndex = visible.start();
    int endIndex = visible.end();
    state.setOffset(startIndex);

    int yOffset = 0;
    Optional<Rect> selectedRowArea = Optional.empty();

    for (int i = startIndex; i < endIndex; i++) {
      Row row = rows.get(i);
      int y = area.y() + yOffset + row.topMargin();
      int height = Math.max(0, Math.min(y + row.height(), area.bottom()) - y);
      Rect rowArea = new Rect(area.x(), y, area.width(), height);
      buf.setStyle(rowArea, row.style());

      boolean isSelected = state.selected().isPresent() && state.selected().get() == i;
      if (selectionWidth > 0 && isSelected) {
        Rect selectionArea = new Rect(rowArea.x(), rowArea.y(), selectionWidth, rowArea.height());
        buf.setStyle(selectionArea, row.style());
        TextRenderer.renderText(highlightSymbol, selectionArea, buf);
      }
      int n = Math.min(columnsWidths.size(), row.cells().size());
      for (int c = 0; c < n; c++) {
        XAndWidth xw = columnsWidths.get(c);
        row.cells()
            .get(c)
            .render(
                new Rect(rowArea.x() + xw.x(), rowArea.y(), xw.width(), rowArea.height()), buf);
      }
      if (isSelected) {
        selectedRowArea = Optional.of(rowArea);
      }
      yOffset += row.heightWithMargin();
    }

    Optional<Rect> selectedColumnArea =
        state
            .selectedColumn()
            .flatMap(
                s -> {
                  if (s < 0 || s >= columnsWidths.size()) return Optional.empty();
                  XAndWidth xw = columnsWidths.get(s);
                  return Optional.of(new Rect(xw.x() + area.x(), area.y(), xw.width(), area.height()));
                });

    if (selectedRowArea.isPresent() && selectedColumnArea.isPresent()) {
      Rect rowA = selectedRowArea.get();
      Rect colA = selectedColumnArea.get();
      buf.setStyle(rowA, rowHighlightStyle);
      buf.setStyle(colA, columnHighlightStyle);
      buf.setStyle(rowA.intersection(colA), cellHighlightStyle);
    } else if (selectedRowArea.isPresent()) {
      buf.setStyle(selectedRowArea.get(), rowHighlightStyle);
    } else if (selectedColumnArea.isPresent()) {
      buf.setStyle(selectedColumnArea.get(), columnHighlightStyle);
    }
  }

  /// Return the indexes of the visible rows.
  ///
  /// The algorithm works as follows:
  /// - start at the offset and calculate the height of the rows that can be displayed within the
  ///   area.
  /// - if the selected row is not visible, scroll the table to ensure it is visible.
  /// - if there is still space to fill then there's a partial row at the end which should be
  ///   included in the view.
  private StartAndEnd visibleRows(TableState state, Rect area) {
    int lastRow = Math.max(0, rows.size() - 1);
    int start = Math.min(state.offset(), lastRow);
    if (state.selected().isPresent()) {
      start = Math.min(start, state.selected().get());
    }

    int end = start;
    int height = 0;
    int areaHeight = area.height();

    for (int i = start; i < rows.size(); i++) {
      Row item = rows.get(i);
      if (height + item.height() > areaHeight) {
        break;
      }
      height += item.heightWithMargin();
      end += 1;
    }

    if (state.selected().isPresent()) {
      int selected = Math.min(state.selected().get(), lastRow);
      while (selected >= end) {
        height = saturatingAdd(height, rows.get(end).heightWithMargin());
        end += 1;
        while (height > areaHeight) {
          height = Math.max(0, height - rows.get(start).heightWithMargin());
          start += 1;
        }
      }
    }

    if (height < areaHeight && end < rows.size()) {
      end += 1;
    }
    return new StartAndEnd(start, end);
  }

  /// Get all offsets and widths of all user specified columns.
  ///
  /// Returns `(x, width)` per column. When `widths` is empty, `.withWidths(...)` is assumed not to
  /// have been called and a default of equal widths is returned.
  ///
  /// Public to allow direct testing of column layout (mirrors upstream's `column_widths` tests).
  public List<XAndWidth> getColumnWidths(int maxWidth, int selectionWidth, int colCount) {
    List<Constraint> effectiveWidths;
    if (widths.isEmpty()) {
      int per = maxWidth / Math.max(colCount, 1);
      effectiveWidths = new ArrayList<>(colCount);
      for (int i = 0; i < colCount; i++) effectiveWidths.add(new Constraint.Length(per));
    } else {
      effectiveWidths = widths;
    }

    Rect[] selectionAndColumns =
        Layout.horizontal(new Constraint.Length(selectionWidth), new Constraint.Fill(0))
            .areas(new Rect(0, 0, maxWidth, 1), 2);
    Rect columnsArea = selectionAndColumns[1];
    Rect[] rects =
        Layout.horizontal(effectiveWidths).withFlex(flex).withSpacing(columnSpacing).split(columnsArea);
    List<XAndWidth> out = new ArrayList<>(rects.length);
    for (Rect r : rects) {
      out.add(new XAndWidth(r.x(), r.width()));
    }
    return out;
  }

  /// Returns the number of columns: the maximum cell-count across the body rows, header, and
  /// footer.
  public int columnCount() {
    int max = 0;
    for (Row r : rows) max = Math.max(max, r.cells().size());
    if (header.isPresent()) max = Math.max(max, header.get().cells().size());
    if (footer.isPresent()) max = Math.max(max, footer.get().cells().size());
    return max;
  }

  /// Returns the width of the selection column if a row is selected, or the [#highlightSpacing] is
  /// set to show the column always; otherwise `0`.
  private int selectionWidth(TableState state) {
    boolean hasSelection = state.selected().isPresent();
    if (highlightSpacing.shouldAdd(hasSelection)) {
      return highlightSymbol.width();
    }
    return 0;
  }

  private static void ensurePercentagesLessThan100(List<Constraint> widths) {
    for (Constraint c : widths) {
      if (c instanceof Constraint.Percentage(int p)) {
        if (p > 100) {
          throw new IllegalArgumentException(
              "Percentages should be between 0 and 100 inclusively.");
        }
      }
    }
  }

  private static int saturatingAdd(int a, int b) {
    long sum = (long) a + (long) b;
    if (sum > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (sum < Integer.MIN_VALUE) return Integer.MIN_VALUE;
    return (int) sum;
  }

  /// `(x, width)` pair returned by [#getColumnWidths(int, int, int)] for each column.
  ///
  /// Mirrors upstream's `Vec<(u16, u16)>` with a domain-specific name.
  public record XAndWidth(int x, int width) {}

  /// Header / rows / footer area triple returned by [#layout(Rect)].
  private record HeaderRowsFooter(Rect headerArea, Rect rowsArea, Rect footerArea) {}

  /// Start and end indexes of the visible row range returned by [#visibleRows(TableState, Rect)].
  private record StartAndEnd(int start, int end) {}
}
