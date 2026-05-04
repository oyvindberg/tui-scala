package jatatui.examples.demo;

import jatatui.widgets.list.ListState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/// Wraps a [ListState] alongside the items currently displayed.
///
/// Mirrors `examples/apps/demo/src/app.rs` — `StatefulList&lt;T&gt;`. The state and items are
// mutable
/// so the demo can move items around (logs cycling) and update selection in place.
public final class StatefulList<T> {

  /// The selection / scroll state of the underlying [jatatui.widgets.list.List] widget.
  public final ListState state;

  /// The items shown by the list. Mutable in place to mirror the upstream `Vec&lt;T&gt;`.
  public final List<T> items;

  private StatefulList(ListState state, List<T> items) {
    this.state = state;
    this.items = items;
  }

  /// Creates a new [StatefulList] with the given items and a fresh empty [ListState].
  public static <T> StatefulList<T> withItems(Collection<T> items) {
    return new StatefulList<>(ListState.empty(), new ArrayList<>(items));
  }

  /// Selects the next item, wrapping to the first when at the end. If nothing is selected, the
  /// first item is selected.
  public void next() {
    Optional<Integer> selected = state.selected();
    int i;
    if (selected.isPresent()) {
      int cur = selected.get();
      if (cur >= items.size() - 1) {
        i = 0;
      } else {
        i = cur + 1;
      }
    } else {
      i = 0;
    }
    state.select(Optional.of(i));
  }

  /// Selects the previous item, wrapping to the last when at the beginning. If nothing is
  /// selected, the first item is selected.
  public void previous() {
    Optional<Integer> selected = state.selected();
    int i;
    if (selected.isPresent()) {
      int cur = selected.get();
      if (cur == 0) {
        i = items.size() - 1;
      } else {
        i = cur - 1;
      }
    } else {
      i = 0;
    }
    state.select(Optional.of(i));
  }
}
