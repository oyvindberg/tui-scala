package jatatui.react;

import java.util.Objects;
import java.util.function.UnaryOperator;

/// State handle returned by [RenderContext#useState].
///
///   - `get()` — current value
///   - `set(next)` — replace; if it's not equal to current, requests a re-render
///   - `update(fn)` — read-modify-write convenience
///
/// State is a value-type wrapper around a `(HookKey, currentValue)` pair; mutating it goes through
/// the owning [RenderContext]'s hook store and triggers re-renders via the runner's dirty flag.
public final class State<T> {
  private final RenderContext ctx;
  private final HookKey key;
  private final T value;

  State(RenderContext ctx, HookKey key, T value) {
    this.ctx = ctx;
    this.key = key;
    this.value = value;
  }

  public T get() {
    return value;
  }

  public void set(T next) {
    if (!Objects.equals(value, next)) {
      ctx.hooks.values.put(key, next);
      ctx.requestRerender.run();
    }
  }

  public void update(UnaryOperator<T> fn) {
    set(fn.apply(value));
  }
}
