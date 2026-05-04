package jatatui.examples.demo;

import java.util.List;

/// State for the tabs at the top of the demo.
///
/// Mirrors `examples/apps/demo/src/app.rs` — `TabsState`. Holds a list of `titles` and the current
/// selection `index`. Mutates in place via [#next()] and [#previous()] to mirror the Rust
/// `&amp;mut self` API.
public final class TabsState {

  /// The tab titles. Immutable; the demo never replaces them after construction.
  public final List<String> titles;

  /// Index of the currently selected tab.
  public int index;

  /// Creates a new [TabsState] from the given titles, selecting the first tab.
  public TabsState(List<String> titles) {
    this.titles = List.copyOf(titles);
    this.index = 0;
  }

  /// Selects the next tab, wrapping to the first when at the end.
  public void next() {
    this.index = (this.index + 1) % titles.size();
  }

  /// Selects the previous tab, wrapping to the last when at the beginning.
  public void previous() {
    if (this.index > 0) {
      this.index -= 1;
    } else {
      this.index = titles.size() - 1;
    }
  }
}
