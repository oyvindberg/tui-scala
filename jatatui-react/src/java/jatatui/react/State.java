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

  /// Read the LATEST stored value, ignoring the render-time snapshot. Useful when a State handle
  /// is captured outside the render that produced it (e.g. in a Context-provided API like
  /// [jatatui.components.router.RouterApi#current] or [jatatui.components.form.FormApi#values]):
  /// callers from later renders need to see fresh data, not a stale snapshot.
  ///
  /// Within a render, prefer [#get] — it gives you a stable snapshot consistent with the rest of
  /// that render pass.
  @SuppressWarnings("unchecked")
  public T latest() {
    return (T) ctx.hooks.values.getOrDefault(key, value);
  }

  /// Set the current value. Triggers a re-render iff the stored value differs from `next`.
  ///
  /// Reads the LATEST stored value from the hook store (not the render-time captured `value`)
  /// for the equality check — so multiple `set` calls within one event-dispatch cycle compose
  /// correctly instead of overwriting each other.
  @SuppressWarnings("unchecked")
  public void set(T next) {
    T current = (T) ctx.hooks.values.getOrDefault(key, value);
    if (!Objects.equals(current, next)) {
      ctx.hooks.values.put(key, next);
      ctx.requestRerender.run();
    }
  }

  /// Read-modify-write. `fn` is applied to the LATEST stored value (not the render-time captured
  /// `value`), so multiple `update` calls within one event-dispatch cycle compose correctly:
  ///
  /// ```java
  /// log.update(prev -> append(prev, "inner"));
  /// log.update(prev -> append(prev, "middle"));   // sees "inner" already appended
  /// log.update(prev -> append(prev, "outer"));    // sees both
  /// ```
  @SuppressWarnings("unchecked")
  public void update(UnaryOperator<T> fn) {
    T current = (T) ctx.hooks.values.getOrDefault(key, value);
    set(fn.apply(current));
  }
}
