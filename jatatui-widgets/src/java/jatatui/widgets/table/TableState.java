package jatatui.widgets.table;

import java.util.Objects;
import java.util.Optional;

/// State of a [Table] widget.
///
/// This state can be used to scroll through the rows and select one of them. When the table is
/// rendered as a stateful widget, the selected row, column and cell will be highlighted and the
/// table will be shifted to ensure that the selected row is visible. This will modify the
/// [TableState] object passed to the render method.
///
/// The state consists of three fields:
/// - [#offset()]: the index of the first row to be displayed
/// - [#selected()]: the index of the selected row, which can be `Optional.empty()` if no row is
///   selected
/// - [#selectedColumn()]: the index of the selected column, which can be `Optional.empty()` if no
///   column is selected
///
/// Mirrors `ratatui_widgets::table::TableState` (v0.30). Mutable, like the upstream type.
public final class TableState {

  /// Sentinel "max" value used by `selectLast()` / `selectLastColumn()` and `selectPrevious()` from
  /// an unset selection. Mirrors Rust's `usize::MAX`.
  public static final int MAX_INDEX = Integer.MAX_VALUE;

  private int offset;
  private Optional<Integer> selected;
  private Optional<Integer> selectedColumn;

  /// Creates a new [TableState] with no selection and offset `0`.
  public TableState() {
    this.offset = 0;
    this.selected = Optional.empty();
    this.selectedColumn = Optional.empty();
  }

  // ---- Fluent setters (return `this` after mutation) ----

  /// Sets the index of the first row to be displayed and returns `this`.
  public TableState withOffset(int offset) {
    this.offset = offset;
    return this;
  }

  /// Sets the index of the selected row and returns `this`.
  public TableState withSelected(Optional<Integer> selected) {
    this.selected = selected;
    return this;
  }

  /// Convenience shortcut for [#withSelected(Optional)] taking a primitive index.
  public TableState withSelected(int selected) {
    return withSelected(Optional.of(selected));
  }

  /// Sets the index of the selected column and returns `this`.
  public TableState withSelectedColumn(Optional<Integer> selectedColumn) {
    this.selectedColumn = selectedColumn;
    return this;
  }

  /// Convenience shortcut for [#withSelectedColumn(Optional)] taking a primitive index.
  public TableState withSelectedColumn(int selectedColumn) {
    return withSelectedColumn(Optional.of(selectedColumn));
  }

  /// Sets the indexes of the selected cell and returns `this`. Passing [Optional#empty()] clears
  /// both row and column selection.
  public TableState withSelectedCell(Optional<RowAndColumn> selected) {
    if (selected.isPresent()) {
      RowAndColumn rc = selected.get();
      this.selected = Optional.of(rc.row());
      this.selectedColumn = Optional.of(rc.column());
    } else {
      this.selected = Optional.empty();
      this.selectedColumn = Optional.empty();
    }
    return this;
  }

  /// Convenience shortcut for [#withSelectedCell(Optional)] taking primitive indexes.
  public TableState withSelectedCell(int row, int column) {
    return withSelectedCell(Optional.of(new RowAndColumn(row, column)));
  }

  // ---- Getters ----

  /// Index of the first row to be displayed.
  public int offset() {
    return offset;
  }

  /// Index of the selected row. Returns [Optional#empty()] if no row is selected.
  public Optional<Integer> selected() {
    return selected;
  }

  /// Index of the selected column. Returns [Optional#empty()] if no column is selected.
  public Optional<Integer> selectedColumn() {
    return selectedColumn;
  }

  /// Indexes of the selected cell. Returns [Optional#empty()] if no cell is selected (i.e. either
  /// the row or the column is unset).
  public Optional<RowAndColumn> selectedCell() {
    if (selected.isPresent() && selectedColumn.isPresent()) {
      return Optional.of(new RowAndColumn(selected.get(), selectedColumn.get()));
    }
    return Optional.empty();
  }

  // ---- Mutators ----

  /// Sets the offset directly.
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /// Sets the index of the selected row directly.
  public void setSelected(Optional<Integer> selected) {
    this.selected = selected;
  }

  /// Sets the index of the selected column directly.
  public void setSelectedColumn(Optional<Integer> selectedColumn) {
    this.selectedColumn = selectedColumn;
  }

  /// Sets the index of the selected row.
  ///
  /// Setting to [Optional#empty()] also resets the offset to `0`.
  public void select(Optional<Integer> index) {
    this.selected = index;
    if (index.isEmpty()) {
      this.offset = 0;
    }
  }

  /// Convenience shortcut for [#select(Optional)] taking a primitive index.
  public void select(int index) {
    select(Optional.of(index));
  }

  /// Sets the index of the selected column.
  public void selectColumn(Optional<Integer> index) {
    this.selectedColumn = index;
  }

  /// Convenience shortcut for [#selectColumn(Optional)] taking a primitive index.
  public void selectColumn(int index) {
    selectColumn(Optional.of(index));
  }

  /// Sets the indexes of the selected cell. Passing [Optional#empty()] also resets the row offset
  /// to `0`.
  public void selectCell(Optional<RowAndColumn> indexes) {
    if (indexes.isPresent()) {
      RowAndColumn rc = indexes.get();
      this.selected = Optional.of(rc.row());
      this.selectedColumn = Optional.of(rc.column());
    } else {
      this.offset = 0;
      this.selected = Optional.empty();
      this.selectedColumn = Optional.empty();
    }
  }

  /// Selects the next row, or the first one if no row is selected.
  ///
  /// Until the table is rendered the number of rows is unknown, so the index is set to `0` (or
  /// incremented) and will be corrected when the table is rendered.
  public void selectNext() {
    int next = selected.map(TableState::saturatingInc).orElse(0);
    select(Optional.of(next));
  }

  /// Selects the next column, or the first one if no column is selected.
  public void selectNextColumn() {
    int next = selectedColumn.map(TableState::saturatingInc).orElse(0);
    selectColumn(Optional.of(next));
  }

  /// Selects the previous row, or the last one ([#MAX_INDEX]) if no item is selected.
  public void selectPrevious() {
    int prev = selected.map(TableState::saturatingDec).orElse(MAX_INDEX);
    select(Optional.of(prev));
  }

  /// Selects the previous column, or the last one ([#MAX_INDEX]) if no column is selected.
  public void selectPreviousColumn() {
    int prev = selectedColumn.map(TableState::saturatingDec).orElse(MAX_INDEX);
    selectColumn(Optional.of(prev));
  }

  /// Selects the first row.
  public void selectFirst() {
    select(Optional.of(0));
  }

  /// Selects the first column.
  public void selectFirstColumn() {
    selectColumn(Optional.of(0));
  }

  /// Selects the last row (sets index to [#MAX_INDEX]; will be clamped on render).
  public void selectLast() {
    select(Optional.of(MAX_INDEX));
  }

  /// Selects the last column (sets index to [#MAX_INDEX]; will be clamped on render).
  public void selectLastColumn() {
    selectColumn(Optional.of(MAX_INDEX));
  }

  /// Scrolls down by `amount`. Saturates at [#MAX_INDEX].
  public void scrollDownBy(int amount) {
    int s = selected.orElse(0);
    select(Optional.of(saturatingAdd(s, amount)));
  }

  /// Scrolls up by `amount`. Saturates at `0`.
  public void scrollUpBy(int amount) {
    int s = selected.orElse(0);
    select(Optional.of(saturatingSub(s, amount)));
  }

  /// Scrolls right by `amount`. Saturates at [#MAX_INDEX].
  public void scrollRightBy(int amount) {
    int s = selectedColumn.orElse(0);
    selectColumn(Optional.of(saturatingAdd(s, amount)));
  }

  /// Scrolls left by `amount`. Saturates at `0`.
  public void scrollLeftBy(int amount) {
    int s = selectedColumn.orElse(0);
    selectColumn(Optional.of(saturatingSub(s, amount)));
  }

  // ---- equals / hashCode / toString ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TableState other)) return false;
    return offset == other.offset
        && selected.equals(other.selected)
        && selectedColumn.equals(other.selectedColumn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset, selected, selectedColumn);
  }

  @Override
  public String toString() {
    return "TableState{offset="
        + offset
        + ", selected="
        + selected
        + ", selectedColumn="
        + selectedColumn
        + "}";
  }

  // ---- Helpers ----

  private static int saturatingInc(int v) {
    return v == Integer.MAX_VALUE ? Integer.MAX_VALUE : v + 1;
  }

  private static int saturatingDec(int v) {
    return v == 0 ? 0 : v - 1;
  }

  private static int saturatingAdd(int a, int b) {
    long sum = (long) a + (long) b;
    if (sum > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (sum < Integer.MIN_VALUE) return Integer.MIN_VALUE;
    return (int) sum;
  }

  private static int saturatingSub(int a, int b) {
    long diff = (long) a - (long) b;
    if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (diff < 0) return 0;
    return (int) diff;
  }

  /// A `(row, column)` pair used by [#selectedCell()] and [#withSelectedCell(int, int)].
  ///
  /// Mirrors upstream's `Option<(usize, usize)>` for selected cells with a domain-specific name.
  public record RowAndColumn(int row, int column) {}
}
