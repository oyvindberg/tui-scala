package jatatui.react;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.terminal.Frame;
import java.util.ArrayDeque;
import java.util.Deque;
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
  /// Per-frame context stacks. A [Context] maps to a stack of values; the top of the stack is
  /// what [#useContext] returns. Push/pop is balanced by the Provider intrinsic — see
  /// [Intrinsics#PROVIDER].
  final Map<Context<?>, Deque<Object>> contextStacks = new HashMap<>();
  /// Per-frame portal queue. Each entry captures the declaring fiber + child Element + absolute
  /// area; entries are drained AFTER the main render pass so portals paint on top of everything
  /// else. See [#queuePortal] and [#drainPortals].
  final java.util.List<PortalEntry> portals = new java.util.ArrayList<>();
  Fiber fiber;
  int hookIndex;

  record PortalEntry(Fiber declaringFiber, int seq, Element child, Rect area) {}

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
    // Mark root as touched so any hooks declared by the top-level component survive sweep.
    // Symmetric with `renderChild`, which adds each child fiber to touched.
    hooks.touched.add(this.fiber);
  }

  public Frame frame() {
    return frame;
  }

  public Buffer buffer() {
    return frame.bufferMut();
  }

  /// Trigger a re-render on the next loop tick. Threadsafe — can be called from background
  /// timers / callbacks. The actual re-render runs on the main loop thread, so component code
  /// remains single-threaded. Components like [jatatui.components.toast.ToastsProvider] use this
  /// to dismiss expired entries even when the user isn't interacting.
  public Runnable requestRerender() {
    return requestRerender;
  }

  // ---- Composition ----

  /// Render a child Element with a fresh fiber id derived from the parent's. Use the indexed
  /// overload for stable-order children; use the keyed overload to preserve hook state when
  /// children can reorder (analogue of React's `key="..."`).
  public void renderChild(int index, Element child, Rect area) {
    Fiber prev = fiber;
    int prevHook = hookIndex;
    fiber = prev.child(index);
    hookIndex = 0;
    hooks.touched.add(fiber);
    events.recordBounds(fiber, area);
    reconcile(fiber, child);
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
    events.recordBounds(fiber, area);
    reconcile(fiber, child);
    try {
      child.render(this, area);
    } finally {
      fiber = prev;
      hookIndex = prevHook;
    }
  }

  /// Reconciliation: when the type at `fiber` changes between frames (e.g. a Router swaps
  /// SourceEditor for OutputEditor at the same fiber slot), the old subtree is unmounted —
  /// state dropped, cleanups run — before the new one renders. Mirrors React's "different
  /// element type at same fiber → unmount + remount" rule.
  ///
  /// "Type" means the [Component] reference for [Element.Of], the `Element.Host` class for
  /// host leaves, and recursively the child's type for the transparent [Element.Sized].
  /// `component(...)`-wrapped functions all share the FUNCTION Component reference, so we
  /// special-case them and use the body lambda's class identity (different `component(...)`
  /// call sites produce different anonymous-lambda classes; same call site produces the same
  /// class even across renders).
  private void reconcile(Fiber f, Element child) {
    Object newType = typeOf(child);
    Object oldType = hooks.elementTypes.get(f);
    if (oldType != null && !oldType.equals(newType)) {
      hooks.unmount(f);
    }
    hooks.elementTypes.put(f, newType);
  }

  private static Object typeOf(Element e) {
    if (e instanceof Element.Of<?> of) {
      if (of.type() == Intrinsics.FUNCTION) {
        Intrinsics.FunctionProps fp = (Intrinsics.FunctionProps) of.props();
        return fp.body().getClass();
      }
      return of.type();
    }
    if (e instanceof Element.Sized s) {
      return typeOf(s.child());
    }
    return e.getClass();
  }

  /// Apply an [Element.Of] — runs the component body unconditionally. Memoization is opt-in via
  /// [Components#memo] / [Components#pureComponent], which use a separate per-Fiber memo cache.
  ///
  /// Auto-memoizing every Element.Of would skip the body, but handler registrations
  /// (`onClick` / `onKey` / `useFocus`) are SIDE EFFECTS of body execution rather than data in
  /// the returned tree — skipping the body silently drops them. For now, body always runs.
  /// Intrinsics like text/box/column are cheap; user components opt into memo explicitly.
  @SuppressWarnings({"unchecked", "rawtypes"})
  Element applyAndMemo(Element.Of<?> of) {
    return ((Component) of.type()).apply(of.props(), this);
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

  // ---- Portal ----

  /// Queue a portal for deferred rendering after the main pass. Called by the Portal intrinsic;
  /// user code uses [Components#portal]. The portal will render as a child of the declaring
  /// fiber (so events bubble through that parent chain), at the given absolute area.
  public void queuePortal(Element child, Rect area) {
    portals.add(new PortalEntry(fiber, portals.size(), child, area));
  }

  /// Render all queued portals in declaration order. Each portal renders as a `renderChild` of
  /// its declaring fiber, with a stable sequential key so its hooks survive across renders.
  /// Recursively drains portals queued by portal children (last-wins z-order). Called by
  /// [ReactApp] after the main render pass.
  void drainPortals() {
    while (!portals.isEmpty()) {
      java.util.List<PortalEntry> batch = new java.util.ArrayList<>(portals);
      portals.clear();
      for (PortalEntry pe : batch) {
        Fiber prev = fiber;
        int prevHook = hookIndex;
        fiber = pe.declaringFiber();
        try {
          renderChild("portal:" + pe.seq(), pe.child(), pe.area());
        } finally {
          fiber = prev;
          hookIndex = prevHook;
        }
      }
    }
  }

  /// Read the value of `context` from the nearest enclosing provider in the Element tree, or
  /// [Context#defaultValue] if no provider is in scope. See [Components#provide].
  @SuppressWarnings("unchecked")
  public <T> T useContext(Context<T> context) {
    Deque<Object> stack = contextStacks.get(context);
    if (stack == null || stack.isEmpty()) return context.defaultValue;
    return (T) stack.peek();
  }

  /// Push a context value. Called by the Provider intrinsic — user code should use
  /// [Components#provide] instead.
  <T> void pushContext(Context<T> context, T value) {
    contextStacks.computeIfAbsent(context, c -> new ArrayDeque<>()).push(value);
  }

  /// Pop the top context value. Called by the Provider intrinsic on its render-end. Must be
  /// balanced with [#pushContext].
  void popContext(Context<?> context) {
    Deque<Object> stack = contextStacks.get(context);
    if (stack != null && !stack.isEmpty()) stack.pop();
  }

  /// Ink-style focus. Returns true when this fiber currently holds focus.
  public boolean useFocus(Optional<String> id, boolean autoFocus) {
    String fid = id.orElse("fiber:" + fiber.hashCode());
    focus.register(fid, fiber, autoFocus);
    return focus.isFocused(fid);
  }

  /// Imperatively move focus to the focusable with this `id`. Use from event handlers (click,
  /// validation-failed callbacks, list-row selection — anywhere you need to focus a specific
  /// component without going through Tab cycling). Pairs with [#useFocus]'s explicit `id`.
  ///
  /// Effective on the next render: focus state is read by `useFocus` during render, and this
  /// just stages the new focused id.
  public void focus(String id) {
    focus.focus(id);
    requestRerender.run();
  }

  /// Imperatively clear focus. Next render's `useFocus` calls all return false until something
  /// claims focus again (Tab cycling, autoFocus, or [#focus(String)]).
  public void blur() {
    focus.blur();
    requestRerender.run();
  }

  // ---- Event registration ----

  /// The bounds for this Component's fiber, recorded by the parent's `renderChild` call. Returns
  /// empty when called before render (which shouldn't happen — every Component runs inside a
  /// fiber with bounds set).
  public Optional<Rect> area() {
    return events.boundsOf(fiber);
  }

  /// Register a click handler. Fires on mouse-down inside `area`. Bubbles up the fiber tree
  /// (deepest registering fiber gets it first, then ancestors). Use [MouseEvent#stopPropagation]
  /// to prevent ancestors from also receiving it.
  public void onClick(Rect area, Consumer<MouseEvent> handler) {
    events.addClick(fiber, area, handler);
  }

  /// Convenience: register a click handler for the whole area of this Component. Equivalent to
  /// `area().ifPresent(r -> onClick(r, handler))`.
  public void onClick(Consumer<MouseEvent> handler) {
    events.boundsOf(fiber).ifPresent(r -> events.addClick(fiber, r, handler));
  }

  /// Convenience overload: handler ignores the [MouseEvent].
  public void onClick(Rect area, Runnable handler) {
    events.addClick(fiber, area, e -> handler.run());
  }

  public void onClick(Runnable handler) {
    events.boundsOf(fiber).ifPresent(r -> events.addClick(fiber, r, e -> handler.run()));
  }

  /// Register a scroll handler. Fires on mouse-wheel events inside `area`. Distinguish direction
  /// via the [MouseEvent#kind] (`SCROLL_UP` / `SCROLL_DOWN`). Bubbles like click.
  public void onScroll(Rect area, Consumer<MouseEvent> handler) {
    events.addScroll(fiber, area, handler);
  }

  public void onScroll(Consumer<MouseEvent> handler) {
    events.boundsOf(fiber).ifPresent(r -> events.addScroll(fiber, r, handler));
  }

  /// Register a key handler. Fires when this fiber (or one of its descendants) is focused AND the
  /// matcher accepts the key. Bubbles up the focus chain to root.
  public void onKey(Object keyMatcher, Consumer<KeyEvent> handler) {
    events.addKey(fiber, keyMatcher, handler);
  }

  /// Convenience overload: handler ignores the [KeyEvent].
  public void onKey(Object keyMatcher, Runnable handler) {
    events.addKey(fiber, keyMatcher, e -> handler.run());
  }

  /// Register a global key handler — fires AFTER the focus-chain bubble, regardless of focus.
  /// Use for app-wide shortcuts like Ctrl-Q. To intercept first, register at the root component
  /// (which is at the top of every focus chain).
  public void onGlobalKey(Object keyMatcher, Consumer<KeyEvent> handler) {
    events.addGlobalKey(keyMatcher, handler);
  }

  public void onGlobalKey(Object keyMatcher, Runnable handler) {
    events.addGlobalKey(keyMatcher, e -> handler.run());
  }

  // ---- Internals ----

  private static boolean arraysEqual(Object[] a, Object[] b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (!java.util.Objects.equals(a[i], b[i])) return false;
    }
    return true;
  }
}
