package jatatui.react;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.terminal.Frame;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/// Threaded through every Element's render call. Owns:
///   - the current [Frame] (and via it, the buffer)
///   - the per-frame [EventRegistry] for click / key / focus handlers
///   - the persistent [HookStore] across frames
///   - the current [Fiber] (component identity for hook lookup)
///   - the [FocusManager] so [#useFocus] can register focusables in render order
public final class RenderContext {
  final Frame frame;
  final EventRegistry events;
  final HookStore hooks;
  final FocusManager focus;
  final Runnable requestRerender;
  Fiber fiber;
  int hookIndex;

  RenderContext(
      Frame frame,
      EventRegistry events,
      HookStore hooks,
      FocusManager focus,
      Runnable requestRerender) {
    this.frame = frame;
    this.events = events;
    this.hooks = hooks;
    this.focus = focus;
    this.requestRerender = requestRerender;
    this.fiber = Fiber.root();
    this.hookIndex = 0;
  }

  public Frame frame() {
    return frame;
  }

  public Buffer buffer() {
    return frame.bufferMut();
  }

  // ---- Composition ----

  /// Render a child Element with a fresh fiber id derived from the parent's. Use the indexed
  /// overload for stable-order children (most cases); use the keyed overload to preserve hook
  /// state when children can reorder (analogue of React's `key="..."`).
  public void renderChild(int index, Element child, Rect area) {
    Fiber prev = fiber;
    int prevHook = hookIndex;
    fiber = prev.child(index);
    hookIndex = 0;
    hooks.touched.add(fiber);
    try {
      child.render(this, area);
    } finally {
      fiber = prev;
      hookIndex = prevHook;
    }
  }

  public void renderChild(String key, Element child, Rect area) {
    Fiber prev = fiber;
    int prevHook = hookIndex;
    fiber = prev.child(key);
    hookIndex = 0;
    hooks.touched.add(fiber);
    try {
      child.render(this, area);
    } finally {
      fiber = prev;
      hookIndex = prevHook;
    }
  }

  // ---- Hooks ----

  public <T> State<T> useState(Supplier<T> initial) {
    HookKey key = new HookKey(fiber, hookIndex++);
    @SuppressWarnings("unchecked")
    T current = (T) hooks.values.computeIfAbsent(key, k -> initial.get());
    return new State<>(this, key, current);
  }

  public <T> Ref<T> useRef(Supplier<T> initial) {
    HookKey key = new HookKey(fiber, hookIndex++);
    @SuppressWarnings("unchecked")
    T current = (T) hooks.values.computeIfAbsent(key, k -> initial.get());
    return new Ref<>(hooks, key, current);
  }

  /// `useEffect` — runs `effect` after render if the dependency array changed (or on first render).
  /// Cleanup runs before the next effect, AND on unmount (when this fiber isn't touched in the
  /// next render — see [HookStore#sweep]).
  public void useEffect(Runnable effect, Object... deps) {
    HookKey key = new HookKey(fiber, hookIndex++);
    Object[] prev = hooks.deps.get(key);
    boolean changed = prev == null || !arraysEqual(prev, deps);
    if (changed) {
      Runnable cleanup = hooks.cleanups.remove(key);
      if (cleanup != null) cleanup.run();
      effect.run();
      hooks.deps.put(key, deps);
    }
  }

  /// Ink-style focus. Returns true when this fiber currently holds focus.
  ///
  ///   - `id` (optional): explicit focus identity. Used to preserve focus across reorders, and to
  ///     let other code call [FocusManager#focus(String)] to jump here. Without `id`, the
  ///     focusable's identity is its fiber.
  ///   - `autoFocus`: claim focus on first render if nothing else has it.
  ///
  /// FocusManager.tab() / shiftTab() cycle through the registered focusables in render order.
  public boolean useFocus(Optional<String> id, boolean autoFocus) {
    String fid = id.orElse("fiber:" + fiber.hashCode());
    focus.register(fid, fiber, autoFocus);
    return focus.isFocused(fid);
  }

  // ---- Event registration ----

  public void onClick(Rect area, Runnable handler) {
    events.add(EventKind.CLICK, area, handler);
  }

  public void onHover(Rect area, Consumer<Boolean> onChange) {
    events.addHover(area, onChange);
  }

  /// Register a key handler. Fires when this fiber is focused AND the matcher accepts the key.
  /// (Use [FocusManager#registerGlobalKey] for a global, focus-independent shortcut.)
  public void onKey(Object keyMatcher, Runnable handler) {
    events.addKey(fiber, keyMatcher, handler);
  }

  // ---- Internals ----

  private static boolean arraysEqual(Object[] a, Object[] b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (!java.util.Objects.equals(a[i], b[i])) return false;
    }
    return true;
  }

  // ---- Hook store ----

  static final class HookStore {
    final Map<HookKey, Object> values = new HashMap<>();
    final Map<HookKey, Object[]> deps = new HashMap<>();
    final Map<HookKey, Runnable> cleanups = new HashMap<>();
    /// Memo cache keyed by Fiber — stores `(deps, producedElement)` for `Memo` / `PureComponent`.
    final Map<Fiber, MemoEntry> memoCache = new HashMap<>();
    final java.util.Set<Fiber> touched = new java.util.HashSet<>();

    /// Look up a memoized Element by Fiber + deps. If the cached deps are reference-equal element-
    /// wise to `newDeps`, return the cached Element. Otherwise call `compute`, cache, return.
    Element memoOrCompute(Fiber fiber, Object[] newDeps, java.util.function.Supplier<Element> compute) {
      MemoEntry cached = memoCache.get(fiber);
      if (cached != null && depsRefEqual(cached.deps, newDeps)) {
        return cached.element;
      }
      Element produced = compute.get();
      memoCache.put(fiber, new MemoEntry(newDeps, produced));
      return produced;
    }

    private static boolean depsRefEqual(Object[] a, Object[] b) {
      if (a.length != b.length) return false;
      for (int i = 0; i < a.length; i++) {
        // Allow value-equality for records (immutable by construction). Reference identity
        // would be sufficient for `Memo` (caller controls identity) but PureComponent passes a
        // freshly-constructed props record each render, so we need .equals().
        if (!java.util.Objects.equals(a[i], b[i])) return false;
      }
      return true;
    }

    /// Drop hook state and memo cache for fibers that weren't touched in the just-finished render
    /// — they're "unmounted". Run their pending cleanups. Mirrors React's commit-phase unmount sweep.
    void sweep() {
      java.util.Iterator<Map.Entry<HookKey, Object>> it = values.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<HookKey, Object> e = it.next();
        if (!touched.contains(e.getKey().fiber())) {
          Runnable cleanup = cleanups.remove(e.getKey());
          if (cleanup != null) cleanup.run();
          deps.remove(e.getKey());
          it.remove();
        }
      }
      memoCache.keySet().removeIf(f -> !touched.contains(f));
      touched.clear();
    }
  }

  record MemoEntry(Object[] deps, Element element) {}

  record HookKey(Fiber fiber, int index) {}

  public static final class State<T> {
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
      if (!java.util.Objects.equals(value, next)) {
        ctx.hooks.values.put(key, next);
        ctx.requestRerender.run();
      }
    }

    public void update(java.util.function.UnaryOperator<T> fn) {
      set(fn.apply(value));
    }
  }

  public static final class Ref<T> {
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
}
