package jatatui.components.selectablelist;

import jatatui.react.Element;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/// Props for [SelectableList].
///
/// Sibling of [jatatui.components.list.ListComponent] but with:
///   - **arbitrary [Element] rows** via [RowRenderer], not just labelled strings, and
///   - **heterogeneous activatability** via [isActivatable] — schema headers, dividers, and
///     other decorative rows live alongside selectable items; Up/Down skips the non-activatable
///     ones, Enter only fires on activatable ones.
///
/// Selection is **controlled** (parent owns `selected`, child notifies via `onSelectChange`) —
/// same pattern as [jatatui.components.list.ListComponent]. The widget's scroll offset is
/// internal, automatically clamped so the selected row stays visible.
///
/// **Type parameter `T`** is the row payload type. It threads through [isActivatable],
/// [RowRenderer], and [onActivate], so the compiler enforces consistency end to end.
///
/// Heterogeneous rendering is encoded in `T`:
///
/// ```
/// sealed interface Row permits Row.Header, Row.Item { }
/// record Header(String title) implements Row {}
/// record Item(MyData data)    implements Row {}
///
/// SelectableListProps.of(
///   rows,
///   row -> row instanceof Item,
///   (row, selected) -> switch (row) {
///     case Header h -> text(h.title(), magenta);
///     case Item it  -> renderItemRow(it, selected);
///   },
///   selectedIdx,
///   newIdx -> ...,
///   onActivate)
/// ```
///
/// **`showScrollbar`** controls whether a vertical-right scrollbar is rendered next to the rows
/// when content overflows the viewport. Defaults to `true` — the visible state of the scrollbar
/// disappears automatically when everything fits, so it costs nothing for short lists. Set it to
/// `false` for embedded lists where reserving a right column would be visually awkward (a
/// one-column-wide list, a list inside a tight modal, etc.).
public record SelectableListProps<T>(
    List<T> items,
    Predicate<T> isActivatable,
    RowRenderer<T> rowRenderer,
    int selected,
    IntConsumer onSelectChange,
    Optional<Consumer<T>> onActivate,
    Optional<String> focusId,
    boolean autoFocus,
    boolean showScrollbar) {

  public SelectableListProps {
    items = List.copyOf(items);
  }

  /// Minimal-args factory. Scrollbar is on by default.
  public static <T> SelectableListProps<T> of(
      List<T> items,
      Predicate<T> isActivatable,
      RowRenderer<T> rowRenderer,
      int selected,
      IntConsumer onSelectChange) {
    return new SelectableListProps<>(
        items,
        isActivatable,
        rowRenderer,
        selected,
        onSelectChange,
        Optional.empty(),
        Optional.empty(),
        true,
        true);
  }

  public SelectableListProps<T> withOnActivate(Consumer<T> onActivate) {
    return new SelectableListProps<>(
        items,
        isActivatable,
        rowRenderer,
        selected,
        onSelectChange,
        Optional.of(onActivate),
        focusId,
        autoFocus,
        showScrollbar);
  }

  public SelectableListProps<T> withFocusId(String id) {
    return new SelectableListProps<>(
        items,
        isActivatable,
        rowRenderer,
        selected,
        onSelectChange,
        onActivate,
        Optional.of(id),
        autoFocus,
        showScrollbar);
  }

  public SelectableListProps<T> withAutoFocus(boolean autoFocus) {
    return new SelectableListProps<>(
        items,
        isActivatable,
        rowRenderer,
        selected,
        onSelectChange,
        onActivate,
        focusId,
        autoFocus,
        showScrollbar);
  }

  public SelectableListProps<T> withShowScrollbar(boolean showScrollbar) {
    return new SelectableListProps<>(
        items,
        isActivatable,
        rowRenderer,
        selected,
        onSelectChange,
        onActivate,
        focusId,
        autoFocus,
        showScrollbar);
  }

  // ---- Type-safe lambda surrogates ----

  /// Paint one row. `selected=true` when this row is highlighted; the renderer chooses how to
  /// indicate selection (bg colour, caret, bold, etc.). Non-activatable rows still get called
  /// with `selected=false`.
  @FunctionalInterface
  public interface RowRenderer<T> {
    Element render(T item, boolean selected);
  }
}
