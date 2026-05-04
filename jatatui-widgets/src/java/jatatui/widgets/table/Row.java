package jatatui.widgets.table;

import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import java.util.ArrayList;
import java.util.List;

/// A single row of data to be displayed in a [Table] widget.
///
/// A `Row` is a collection of [TableCell]s.
///
/// By default, a row has a height of 1 but you can change this using [#withHeight(int)].
///
/// You can set the style of the entire row using [#withStyle(Style)]. This [Style] will be combined
/// with the [Style] of each individual [TableCell] by adding the [Style] of the cell to the
/// [Style] of the row.
///
/// Mirrors `ratatui_widgets::table::Row` (v0.30).
public record Row(List<TableCell> cells, int height, int topMargin, int bottomMargin, Style style)
    implements Stylize<Row> {

  /// Canonical constructor that defensively copies the cells list.
  public Row {
    cells = List.copyOf(cells);
  }

  /// Creates an empty [Row] with no cells, height `1`, no margins, and the default style.
  public static Row empty() {
    return new Row(List.of(), 1, 0, 0, Style.empty());
  }

  /// Creates a new [Row] from a list of [TableCell]s with height `1`, no margins, and the default
  /// style.
  public static Row of(List<TableCell> cells) {
    return new Row(new ArrayList<>(cells), 1, 0, 0, Style.empty());
  }

  /// Creates a new [Row] from varargs of [TableCell]s with height `1`, no margins, and the default
  /// style.
  public static Row of(TableCell... cells) {
    List<TableCell> list = new ArrayList<>(cells.length);
    for (TableCell c : cells) list.add(c);
    return new Row(list, 1, 0, 0, Style.empty());
  }

  /// Creates a new [Row] from a list of strings (each string becomes a [TableCell]) with height
  /// `1`, no margins, and the default style.
  public static Row ofStrings(List<String> values) {
    List<TableCell> list = new ArrayList<>(values.size());
    for (String v : values) list.add(TableCell.of(v));
    return new Row(list, 1, 0, 0, Style.empty());
  }

  /// Creates a new [Row] from varargs of strings.
  public static Row ofStrings(String... values) {
    List<TableCell> list = new ArrayList<>(values.length);
    for (String v : values) list.add(TableCell.of(v));
    return new Row(list, 1, 0, 0, Style.empty());
  }

  // ---- Fluent setters ----

  /// Returns a copy with the given cells set.
  public Row withCells(List<TableCell> cells) {
    return new Row(new ArrayList<>(cells), height, topMargin, bottomMargin, style);
  }

  /// Returns a copy with the fixed height set. Any cell whose content has more lines than this
  /// height will see its content truncated. By default, the height is `1`.
  public Row withHeight(int height) {
    return new Row(cells, height, topMargin, bottomMargin, style);
  }

  /// Returns a copy with the top margin set. The top margin is the number of blank lines to be
  /// displayed before the row. By default, the top margin is `0`.
  public Row withTopMargin(int margin) {
    return new Row(cells, height, margin, bottomMargin, style);
  }

  /// Returns a copy with the bottom margin set. The bottom margin is the number of blank lines to
  /// be displayed after the row. By default, the bottom margin is `0`.
  public Row withBottomMargin(int margin) {
    return new Row(cells, height, topMargin, margin, style);
  }

  /// Returns a copy with the style set, replacing the current style.
  public Row withStyle(Style style) {
    return new Row(cells, height, topMargin, bottomMargin, style);
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Row setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Internal helpers ----

  /// Returns the total height of the row including top and bottom margins.
  ///
  /// Uses saturating addition: if the sum would overflow [Integer#MAX_VALUE] the result is
  /// clamped instead of wrapping around.
  int heightWithMargin() {
    return saturatingAdd(saturatingAdd(height, topMargin), bottomMargin);
  }

  private static int saturatingAdd(int a, int b) {
    long sum = (long) a + (long) b;
    if (sum > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (sum < Integer.MIN_VALUE) return Integer.MIN_VALUE;
    return (int) sum;
  }
}
