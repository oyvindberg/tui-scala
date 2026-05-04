package jatatui.components.list;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntConsumer;

/// Props for the React-style [ListComponent].
///
/// Generic over the item type `T`. Each item is rendered as a single line via `labelFn`.
///
/// Selection is **controlled**: the parent owns `selected` (an `int` index) and is notified
/// of requested changes through `onSelectChange`. The component itself holds no selection
/// state — only the underlying widget's scroll offset (a [jatatui.widgets.list.ListState])
/// is kept across renders via `useRef`. This mirrors React's controlled-input pattern
/// (`<input value={...} onChange={...}>`).
///
/// `onActivate` (optional) fires when Enter is pressed while focused, or when the
/// already-selected row is clicked.
///
/// `selected = -1` means "no selection".
public record ListProps<T>(
    String title,
    List<T> items,
    Function<T, String> labelFn,
    int selected,
    IntConsumer onSelectChange,
    Optional<Runnable> onActivate,
    Optional<String> focusId,
    boolean autoFocus) {

  public ListProps {
    items = List.copyOf(items);
  }
}
