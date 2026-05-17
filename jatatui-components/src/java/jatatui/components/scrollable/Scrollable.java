package jatatui.components.scrollable;

import static jatatui.react.Components.*;

import jatatui.core.layout.Rect;
import jatatui.react.Element;
import jatatui.react.MouseEvent;
import jatatui.react.State;
import java.util.List;

/// Wrap a list of fixed-height children so the mouse wheel scrolls them. Three rows per wheel
/// tick. Max offset is clamped against the assigned area's height — wheel-down stops at the
/// point where the last child is exactly aligned to the bottom of the visible window, so you
/// can't over-scroll past the content (the typr.cli.app.components.Scrollable original noted
/// this as a TODO; we do the right thing here).
///
/// Focus is intentionally NOT adjusted on scroll — Tab order stays consistent with render
/// order, so a scrolled-out focused item is still reachable via keyboard. Click-to-focus on a
/// visible child works as usual.
///
/// For lists of selectable items where Up/Down should move the selection and the viewport
/// should follow, use [jatatui.components.selectablelist.SelectableList] instead — it owns
/// the selection logic and auto-scrolls when the cursor leaves the viewport.
public final class Scrollable {
  private Scrollable() {}

  /// Cells scrolled per wheel tick.
  public static final int STEP = 3;

  public static Element column(List<Element> children) {
    return component(
        ctx -> {
          State<Integer> offset = ctx.useState(() -> 0);
          int areaHeight = ctx.area().map(Rect::height).orElse(children.size());
          int maxOffset = Math.max(0, children.size() - Math.max(1, areaHeight));

          int liveOffset = Math.min(offset.get(), maxOffset);
          ctx.onScroll(
              (MouseEvent e) -> {
                switch (e.kind()) {
                  case SCROLL_UP -> offset.set(Math.max(0, liveOffset - STEP));
                  case SCROLL_DOWN -> offset.set(Math.min(maxOffset, liveOffset + STEP));
                  default -> {}
                }
              });

          List<Element> visible = children.subList(liveOffset, children.size());
          if (visible.isEmpty()) return empty();
          return jatatui.react.Components.column(visible.toArray(new Element[0]));
        });
  }
}
