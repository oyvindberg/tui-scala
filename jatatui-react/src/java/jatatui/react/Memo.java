package jatatui.react;

import jatatui.core.layout.Rect;
import java.util.function.Supplier;

/// `React.memo` equivalent. Caches the produced [Element] keyed by `deps` (an immutable values
/// array). When `deps` is reference-equal element-wise to the previous render, `body` is skipped
/// and the cached Element is rendered into the new area.
///
/// The cache lives in [RenderContext.HookStore] keyed by [Fiber], so it's per-instance and is
/// cleaned up automatically when the fiber unmounts (see `HookStore.sweep`).
///
/// Use [Components#memo(Object[], Supplier)].
public record Memo(Object[] deps, Supplier<Element> body) implements Element {
  @Override
  public void render(RenderContext ctx, Rect area) {
    Element produced = ctx.hooks.memoOrCompute(ctx.fiber, deps, body);
    ctx.renderChild(0, produced, area);
  }
}
