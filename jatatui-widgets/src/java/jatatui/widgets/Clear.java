package jatatui.widgets;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.widgets.Widget;

/// A widget to clear/reset a certain area to allow overdrawing (e.g. for popups).
///
/// Mirrors `ratatui_widgets::clear::Clear` (v0.30).
///
/// This widget **cannot be used to clear the terminal on the first render** as `ratatui` assumes
/// the render area is empty. Use `Terminal::clear` instead.
///
/// # Example
///
/// ```java
/// Clear.INSTANCE.render(area, buf);
/// ```
public final class Clear implements Widget {

  /// The single shared `Clear` instance — the widget is stateless.
  public static final Clear INSTANCE = new Clear();

  /// Returns the singleton [Clear] instance.
  public static Clear instance() {
    return INSTANCE;
  }

  /// Public no-arg constructor for parity with upstream `Clear` (a unit struct). Prefer
  /// [#INSTANCE] / [#instance()] when reusing the same widget.
  public Clear() {}

  @Override
  public void render(Rect area, Buffer buf) {
    for (int x = area.left(); x < area.right(); x++) {
      for (int y = area.top(); y < area.bottom(); y++) {
        buf.cellAt(x, y).reset();
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Clear;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public String toString() {
    return "Clear";
  }
}
