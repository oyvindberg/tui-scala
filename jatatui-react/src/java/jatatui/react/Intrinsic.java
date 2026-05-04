package jatatui.react;

import jatatui.core.layout.Rect;

/// A leaf renderer — paints directly to the buffer (or registers handlers / lays out children).
///
/// Wrapped by [Element.Host], this is the escape hatch from "Component returns Element" recursion.
/// Most components shouldn't need this — prefer composing existing factories. Use Intrinsic when:
///   - You need to write to the buffer directly (a custom paint primitive)
///   - You're authoring a layout primitive that calls [RenderContext#renderChild] for sub-areas
///   - You're wrapping an existing [jatatui.core.widgets.Widget] (or use `widget(w)` instead)
@FunctionalInterface
public interface Intrinsic {
  void render(RenderContext ctx, Rect area);
}
