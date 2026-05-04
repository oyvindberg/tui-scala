package jatatui.react;

import jatatui.core.layout.Rect;
import java.util.function.Function;

/// A "function component" — a function that takes [RenderContext] and returns an [Element].
///
/// Hooks declared inside `body` (useState/useRef/useEffect/useFocus) live in this Component's
/// fiber slot. The produced Element is rendered in a child fiber so its own hooks/children don't
/// clash with body's hooks. Mirrors React's fiber tree.
///
/// Construct via [Components#component(Function)]; rarely worth `new Component(...)` directly.
public record Component(Function<RenderContext, Element> body) implements Element {
  @Override
  public void render(RenderContext ctx, Rect area) {
    Element produced = body.apply(ctx);
    // Push a child fiber for the produced Element so its hook/child slots are namespaced
    // separately from this Component's hooks (React's invariant).
    ctx.renderChild(0, produced, area);
  }
}
