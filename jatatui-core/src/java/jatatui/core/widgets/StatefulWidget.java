package jatatui.core.widgets;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;

/// A `StatefulWidget` is a widget that can take advantage of some local state to remember things
/// between two draw calls.
///
/// Mirrors `ratatui_core::widgets::StatefulWidget` (v0.30).
///
/// Most widgets can be drawn directly based on the input parameters. However, some features
/// require associated state to be implemented.
///
/// For example, the `List` widget can highlight the item currently selected. This is translated
/// to an offset (number of elements to skip so the selected item is within the viewport allocated
/// to the widget). Without state, the widget can only scroll to a predefined position whenever
/// the selected item leaves the viewport. With state — the last computed offset stored across
/// draws — the widget can implement natural scrolling: the last offset is reused until the
/// selected item leaves the viewport.
///
/// In Rust this is a trait with an associated type `State`. In Java we model the state as a
/// regular type parameter on the interface.
///
/// **`StatefulWidgetRef` — N/A**: the upstream documentation mentions a historical
/// `StatefulWidgetRef` trait, but it does not exist in `ratatui-core` v0.30.
///
/// **Unsized state types** (`type State = [u8]`): Rust permits `?Sized` associated types so the
/// state can be a slice. Java has no unsized types; pass the array (or any other reference) as
/// the type parameter and mutate it in place.
///
/// # Example
///
/// ```java
/// class PersonalGreeting implements StatefulWidget<StringBuilder> {
///   public void render(Rect area, Buffer buf, StringBuilder state) {
///     Widget.renderString("Hello " + state, area, buf);
///   }
/// }
/// ```
@FunctionalInterface
public interface StatefulWidget<State> {

  /// Draws the current state of the widget in the given buffer. That is the only method required
  /// to implement a custom stateful widget.
  ///
  /// Upstream signature is `fn render(self, area: Rect, buf: &mut Buffer, state: &mut Self::State)`
  /// — `self` is consumed and `state` is mutably borrowed. Java has no consume semantics; the
  /// widget should be treated as logically consumed after this call. The `state` parameter is a
  /// regular reference and is expected to be mutated in place.
  void render(Rect area, Buffer buf, State state);
}
