package jatatui.core.widgets;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import java.util.Optional;

/// A `Widget` is a type that can be drawn on a [Buffer] in a given [Rect].
///
/// Mirrors `ratatui_core::widgets::Widget` (v0.30).
///
/// Prior to Ratatui 0.26.0, widgets generally were created for each frame as they were consumed
/// during rendering. This meant that they were not meant to be stored but used as *commands* to
/// draw common figures in the UI.
///
/// Starting with Ratatui 0.26.0, all the internal widgets implement Widget for a reference to
/// themselves. This allows you to store a reference to a widget and render it later. In Java this
/// distinction does not exist: a [Widget] is a regular object that can be re-used freely between
/// draws.
///
/// In general, where you expect a widget to immutably work on its data, simply implement
/// [#render(Rect, Buffer)] without mutating `this`. If you need to keep state between draw calls,
/// implement [StatefulWidget] instead, or mutate `this` from inside [#render(Rect, Buffer)] if you
/// want a mutable widget.
///
/// **Blanket impls — N/A in Java**: upstream Rust provides
/// `impl<W: Widget> Widget for &W` and `impl<W: Widget> Widget for &mut W`. Both are blanket
/// implementations over reference types. Java has no concept of "reference type" distinct from
/// the value type, so these are not needed.
///
/// **`WidgetRef` / `StatefulWidgetRef` — N/A**: the upstream `widget.rs` doc-comments mention
/// these as historical unstable additions that allowed rendering boxed widgets. They do not exist
/// as separate traits in `ratatui-core` v0.30. Boxed widgets are trivially supported in Java by
/// holding any `Widget` reference.
///
/// **`impl Widget for &str` / `String` / `Option<W>`**: see the static helpers
/// [#renderString(String, Rect, Buffer)] and [#renderOptional(Optional, Rect, Buffer)].
/// `String` is `final` in Java so we cannot graft an interface onto it.
///
/// # Example
///
/// ```java
/// class MyWidget implements Widget {
///   public void render(Rect area, Buffer buf) {
///     Widget.renderString("Hello", area, buf);
///   }
/// }
/// ```
@FunctionalInterface
public interface Widget {

  /// Draws the current state of the widget in the given buffer. That is the only method required
  /// to implement a custom widget.
  ///
  /// Upstream signature is `fn render(self, area: Rect, buf: &mut Buffer)` — `self` is consumed.
  /// Java has no consume semantics; callers should treat the widget as logically consumed and not
  /// expect any particular state afterwards (mutable widgets may have mutated `this`).
  void render(Rect area, Buffer buf);

  /// Renders a [String] as a widget — the equivalent of upstream's `impl Widget for &str` and
  /// `impl Widget for String`.
  ///
  /// Writes the string starting at `(area.x(), area.y())` and truncates at `area.width()`
  /// columns.
  static void renderString(String s, Rect area, Buffer buf) {
    buf.setStringn(area.x(), area.y(), s, area.width(), Style.empty());
  }

  /// Renders an [Optional] widget — the equivalent of upstream's `impl<W: Widget> Widget for
  /// Option<W>`. Empty optionals render nothing.
  static void renderOptional(Optional<? extends Widget> widget, Rect area, Buffer buf) {
    widget.ifPresent(w -> w.render(area, buf));
  }
}
