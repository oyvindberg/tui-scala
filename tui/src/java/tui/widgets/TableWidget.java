package tui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.Buffer;
import tui.Constraint;
import tui.Direction;
import tui.Grapheme;
import tui.Layout;
import tui.Margin;
import tui.Position;
import tui.Rect;
import tui.Spans;
import tui.StatefulWidget;
import tui.Style;
import tui.Text;
import tui.Widget;
import tui.internal.Saturating;

/// A widget to display data in formatted columns.
public final class TableWidget implements Widget, StatefulWidget<TableWidget.State> {
  public final Optional<BlockWidget> block;
  public final Style style;
  public final Constraint[] widths;
  public final int columnSpacing;
  public final Style highlightStyle;
  public final Optional<String> highlightSymbol;
  public final Optional<Row> header;
  public final Row[] rows;

  public TableWidget(
      Optional<BlockWidget> block,
      Style style,
      Constraint[] widths,
      int columnSpacing,
      Style highlightStyle,
      Optional<String> highlightSymbol,
      Optional<Row> header,
      Row[] rows) {
    this.block = block;
    this.style = style;
    this.widths = widths;
    this.columnSpacing = columnSpacing;
    this.highlightStyle = highlightStyle;
    this.highlightSymbol = highlightSymbol;
    this.header = header;
    this.rows = rows;
  }

  public static TableWidget empty(Row[] rows) {
    return new TableWidget(
        Optional.empty(),
        Style.DEFAULT,
        new Constraint[0],
        1,
        Style.DEFAULT,
        Optional.empty(),
        Optional.empty(),
        rows);
  }

  public TableWidget withBlock(BlockWidget b) {
    return new TableWidget(
        Optional.of(b), style, widths, columnSpacing, highlightStyle, highlightSymbol, header, rows);
  }

  public TableWidget withStyle(Style s) {
    return new TableWidget(
        block, s, widths, columnSpacing, highlightStyle, highlightSymbol, header, rows);
  }

  public TableWidget withWidths(Constraint[] w) {
    return new TableWidget(
        block, style, w, columnSpacing, highlightStyle, highlightSymbol, header, rows);
  }

  public TableWidget withColumnSpacing(int c) {
    return new TableWidget(
        block, style, widths, c, highlightStyle, highlightSymbol, header, rows);
  }

  public TableWidget withHighlightStyle(Style s) {
    return new TableWidget(block, style, widths, columnSpacing, s, highlightSymbol, header, rows);
  }

  public TableWidget withHighlightSymbol(String s) {
    return new TableWidget(
        block, style, widths, columnSpacing, highlightStyle, Optional.of(s), header, rows);
  }

  public TableWidget withHeader(Row h) {
    return new TableWidget(
        block, style, widths, columnSpacing, highlightStyle, highlightSymbol, Optional.of(h), rows);
  }

  public TableWidget withRows(Row[] r) {
    return new TableWidget(
        block, style, widths, columnSpacing, highlightStyle, highlightSymbol, header, r);
  }

  public int[] getColumnsWidths(int maxWidth, boolean hasSelection) {
    List<Constraint> constraints = new ArrayList<>(widths.length * 2 + 1);

    if (hasSelection) {
      int highlightSymbolWidth =
          highlightSymbol.map(s -> new Grapheme(s).width()).orElse(0);
      constraints.add(new Constraint.Length(highlightSymbolWidth));
    }
    for (Constraint c : widths) {
      constraints.add(c);
      constraints.add(new Constraint.Length(columnSpacing));
    }
    if (widths.length > 0) {
      constraints.remove(constraints.size() - 1);
    }
    Layout layout =
        new Layout(
            Direction.Horizontal,
            new Margin(0, 0),
            constraints.toArray(new Constraint[0]), false);
    Rect[] chunks = layout.split(new Rect(0, 0, maxWidth, 1));
    int startIdx = hasSelection ? 1 : 0;
    List<Integer> result = new ArrayList<>();
    for (int i = startIdx; i < chunks.length; i += 2) {
      result.add(chunks[i].width());
    }
    int[] out = new int[result.size()];
    for (int i = 0; i < result.size(); i++) out[i] = result.get(i);
    return out;
  }

  public Bounds getRowBounds(Optional<Integer> selected0, int offset0, int maxHeight) {
    int offset = Math.min(offset0, Saturating.saturatingSubUnsigned(rows.length, 1));
    int start = offset;
    int end = offset;
    int height = 0;
    int i = offset;
    boolean cont = true;
    while (cont && i < rows.length) {
      Row item = rows[i];
      if (height + item.height() > maxHeight) {
        cont = false;
      } else {
        height += item.totalHeight();
        end += 1;
        i += 1;
      }
    }

    int selected = Math.min(selected0.orElse(0), rows.length - 1);
    while (selected >= end) {
      height = Saturating.saturatingAdd(height, rows[end].totalHeight());
      end += 1;
      while (height > maxHeight) {
        height = Saturating.saturatingSubUnsigned(height, rows[start].totalHeight());
        start += 1;
      }
    }
    while (selected < start) {
      start -= 1;
      height = Saturating.saturatingAdd(height, rows[start].totalHeight());
      while (height > maxHeight) {
        end -= 1;
        height = Saturating.saturatingSubUnsigned(height, rows[end].totalHeight());
      }
    }
    return new Bounds(start, end);
  }

  @Override
  public void render(Rect area, Buffer buf, State state) {
    if (area.area() == 0) {
      return;
    }
    buf.setStyle(area, style);
    Rect tableArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      tableArea = innerArea;
    } else {
      tableArea = area;
    }

    boolean hasSelection = state.selected.isPresent();
    int[] columnsWidths = getColumnsWidths(tableArea.width(), hasSelection);
    Grapheme highlightSymbolG = new Grapheme(highlightSymbol.orElse(""));
    String blankSymbol = " ".repeat(highlightSymbolG.width());
    int currentHeight = 0;
    int rowsHeight = tableArea.height();

    if (header.isPresent()) {
      Row hdr = header.get();
      int maxHeaderHeight = Math.min(tableArea.height(), hdr.totalHeight());
      Rect headerRect =
          new Rect(
              tableArea.left(),
              tableArea.top(),
              tableArea.width(),
              Math.min(tableArea.height(), hdr.height()));
      buf.setStyle(headerRect, hdr.style());
      int col = tableArea.left();
      if (hasSelection) {
        col += Math.min(highlightSymbolG.width(), tableArea.width());
      }
      int n = Math.min(columnsWidths.length, hdr.cells().length);
      for (int i = 0; i < n; i++) {
        int width = columnsWidths[i];
        Cell cell = hdr.cells()[i];
        Rect cellRect = new Rect(col, tableArea.top(), width, maxHeaderHeight);
        renderCell(buf, cell, cellRect);
        col += width + columnSpacing;
      }
      currentHeight += maxHeaderHeight;
      rowsHeight = Saturating.saturatingSubUnsigned(rowsHeight, maxHeaderHeight);
    }

    if (rows.length == 0) {
      return;
    }
    Bounds bounds = getRowBounds(state.selected, state.offset, rowsHeight);
    int start = bounds.start();
    int end = bounds.end();
    state.offset = start;
    for (int idx = state.offset; idx < state.offset + end - start; idx++) {
      Row tableRow = rows[idx];
      int row = tableArea.top() + currentHeight;
      int col0 = tableArea.left();
      currentHeight += tableRow.totalHeight();
      Rect tableRowArea = new Rect(col0, row, tableArea.width(), tableRow.height());
      buf.setStyle(tableRowArea, tableRow.style());
      boolean isSelected = state.selected.isPresent() && state.selected.get() == idx;
      int tableRowStartCol;
      if (hasSelection) {
        String symbol = isSelected ? highlightSymbolG.str : blankSymbol;
        Position p = buf.setStringn(col0, row, symbol, tableArea.width(), tableRow.style());
        tableRowStartCol = p.x();
      } else {
        tableRowStartCol = col0;
      }

      int col1 = tableRowStartCol;
      int n = Math.min(columnsWidths.length, tableRow.cells().length);
      for (int i = 0; i < n; i++) {
        int width = columnsWidths[i];
        Cell cell = tableRow.cells()[i];
        Rect rect = new Rect(col1, row, width, tableRow.height());
        renderCell(buf, cell, rect);
        col1 += width + columnSpacing;
      }
      if (isSelected) {
        buf.setStyle(tableRowArea, highlightStyle);
      }
    }
  }

  public void renderCell(Buffer buf, Cell cell, Rect area) {
    buf.setStyle(area, cell.style());
    Spans[] lines = cell.content().lines();
    int n = Math.min(lines.length, area.height());
    for (int i = 0; i < n; i++) {
      buf.setSpans(area.x(), area.y() + i, lines[i], area.width());
    }
  }

  @Override
  public void render(Rect area, Buffer buf) {
    State state = new State(0, Optional.empty());
    render(area, buf, state);
  }

  public record Bounds(int start, int end) {}

  /// A `Cell` contains the `Text` to be displayed in a `Row` of a `Table`.
  public record Cell(Text content, Style style) {}

  /// Holds data to be displayed in a `Table` widget.
  public record Row(Cell[] cells, int height, Style style, int bottomMargin) {
    /// Returns the total height of the row.
    public int totalHeight() {
      return Saturating.saturatingAdd(height, bottomMargin);
    }
  }

  public static final class State {
    public int offset;
    public Optional<Integer> selected;

    public State(int offset, Optional<Integer> selected) {
      this.offset = offset;
      this.selected = selected;
    }

    public static State empty() {
      return new State(0, Optional.empty());
    }

    public void select(Optional<Integer> index) {
      selected = index;
      if (index.isEmpty()) {
        offset = 0;
      }
    }

    /// Returns the receiver's scroll offset.
    ///
    /// Useful, for example, if you need to "synchronize" the scrolling of a `Table` and a `Paragraph`.
    public int offset() {
      return offset;
    }
  }
}
