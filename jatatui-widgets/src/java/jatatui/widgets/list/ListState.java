package jatatui.widgets.list;

import java.util.Objects;
import java.util.Optional;

/// State of the [List] widget.
///
/// This state can be used to scroll through items and select one. When the list is rendered as a
/// stateful widget, the selected item will be highlighted and the list will be shifted to ensure
/// that the selected item is visible.
///
/// The state consists of two fields:
/// - [#offset()]: the index of the first item to be displayed.
/// - [#selected()]: the index of the selected item, which can be empty if no item is selected.
///
/// Mirrors `ratatui_widgets::list::ListState` (v0.30). Upstream uses `usize`; here we use `int`.
/// `usize::MAX` translates to [Integer#MAX_VALUE] (treated as "the last item" by the rendering
/// code).
///
/// `ListState` is **mutable**: select/scroll methods mutate `this` in place to mirror the Rust
/// `&mut self` API.
public final class ListState {

  private int offset;
  private Optional<Integer> selected;

  private ListState(int offset, Optional<Integer> selected) {
    this.offset = offset;
    this.selected = selected;
  }

  // ---- Construction ----

  /// Returns a [ListState] with offset 0 and no selection.
  public static ListState empty() {
    return new ListState(0, Optional.empty());
  }

  /// Returns a [ListState] with the given offset and selection.
  public static ListState of(int offset, Optional<Integer> selected) {
    return new ListState(offset, selected);
  }

  // ---- Fluent setters (return this for chaining; same instance) ----

  /// Sets the index of the first item to be displayed and returns this state.
  public ListState withOffset(int offset) {
    this.offset = offset;
    return this;
  }

  /// Sets the index of the selected item and returns this state.
  public ListState withSelected(Optional<Integer> selected) {
    this.selected = selected;
    return this;
  }

  // ---- Accessors ----

  /// Returns the index of the first item to be displayed.
  public int offset() {
    return offset;
  }

  /// Sets the index of the first item to be displayed.
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /// Returns the index of the selected item.
  ///
  /// Returns [Optional#empty()] if no item is selected.
  public Optional<Integer> selected() {
    return selected;
  }

  // ---- Mutation ----

  /// Sets the index of the selected item.
  ///
  /// Pass [Optional#empty()] if no item is selected. This will also reset the offset to `0`.
  public void select(Optional<Integer> index) {
    this.selected = index;
    if (index.isEmpty()) {
      this.offset = 0;
    }
  }

  /// Selects the next item or the first one if no item is selected.
  ///
  /// Note: until the list is rendered, the number of items is not known, so the index is set to
  /// `0` and will be corrected when the list is rendered.
  public void selectNext() {
    int next = selected.map(i -> saturatingAdd(i, 1)).orElse(0);
    select(Optional.of(next));
  }

  /// Selects the previous item or the last one if no item is selected.
  ///
  /// Note: until the list is rendered, the number of items is not known, so the index is set to
  /// [Integer#MAX_VALUE] and will be corrected when the list is rendered.
  public void selectPrevious() {
    int previous = selected.map(i -> saturatingSub(i, 1)).orElse(Integer.MAX_VALUE);
    select(Optional.of(previous));
  }

  /// Selects the first item.
  ///
  /// Note: until the list is rendered, the number of items is not known, so the index is set to
  /// `0` and will be corrected when the list is rendered.
  public void selectFirst() {
    select(Optional.of(0));
  }

  /// Selects the last item.
  ///
  /// Note: until the list is rendered, the number of items is not known, so the index is set to
  /// [Integer#MAX_VALUE] and will be corrected when the list is rendered.
  public void selectLast() {
    select(Optional.of(Integer.MAX_VALUE));
  }

  /// Scrolls down by a specified `amount` in the list.
  ///
  /// This method updates the selected index by moving it down by the given `amount`.
  /// If the `amount` causes the index to go out of bounds (i.e., greater than the length of the
  /// list), the last item in the list will be selected.
  ///
  /// Upstream takes a `u16`. Here `amount` is a non-negative `int`.
  public void scrollDownBy(int amount) {
    int sel = selected.orElse(0);
    select(Optional.of(saturatingAdd(sel, amount)));
  }

  /// Scrolls up by a specified `amount` in the list.
  ///
  /// This method updates the selected index by moving it up by the given `amount`.
  /// If the `amount` causes the index to go out of bounds (i.e., less than zero),
  /// the first item in the list will be selected.
  ///
  /// Upstream takes a `u16`. Here `amount` is a non-negative `int`.
  public void scrollUpBy(int amount) {
    int sel = selected.orElse(0);
    select(Optional.of(saturatingSub(sel, amount)));
  }

  // ---- Helpers ----

  private static int saturatingAdd(int a, int b) {
    long r = (long) a + (long) b;
    if (r > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (r < 0) return 0;
    return (int) r;
  }

  private static int saturatingSub(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    return (int) r;
  }

  // ---- Equality / hash / debug ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ListState other)) return false;
    return offset == other.offset && selected.equals(other.selected);
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset, selected);
  }

  @Override
  public String toString() {
    return "ListState{offset=" + offset + ", selected=" + selected + "}";
  }
}
