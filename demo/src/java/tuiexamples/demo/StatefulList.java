package tuiexamples.demo;

import java.util.ArrayDeque;
import java.util.Optional;
import tui.widgets.ListWidget;

public final class StatefulList<T> {
  public final ListWidget.State state;
  public final ArrayDeque<T> items;

  public StatefulList(ListWidget.State state, ArrayDeque<T> items) {
    this.state = state;
    this.items = items;
  }

  public void next() {
    int i;
    if (state.selected.isPresent()) {
      int s = state.selected.get();
      i = s >= items.size() - 1 ? 0 : s + 1;
    } else {
      i = 0;
    }
    state.select(Optional.of(i));
  }

  public void previous() {
    int i;
    if (state.selected.isPresent()) {
      int s = state.selected.get();
      i = s == 0 ? items.size() - 1 : s - 1;
    } else {
      i = 0;
    }
    state.select(Optional.of(i));
  }

  public static <T> StatefulList<T> withItems(T[] items) {
    ArrayDeque<T> q = new ArrayDeque<>(items.length);
    for (T t : items) q.addLast(t);
    return new StatefulList<>(ListWidget.State.empty(), q);
  }
}
