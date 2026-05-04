package jatatui.react;

/// Ref handle returned by [RenderContext#useRef]. Like [State] but mutating the value does NOT
/// trigger a re-render. Use for: holding mutable widget state across renders, prior-value tracking
/// for change detection, anything imperative.
public final class Ref<T> {
  private final HookStore hooks;
  private final HookKey key;
  private final T value;

  Ref(HookStore hooks, HookKey key, T value) {
    this.hooks = hooks;
    this.key = key;
    this.value = value;
  }

  public T get() {
    return value;
  }

  public void set(T next) {
    hooks.values.put(key, next);
  }
}
