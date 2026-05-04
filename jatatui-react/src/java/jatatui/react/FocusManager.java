package jatatui.react;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Ink-style focus model.
///
///   - Components register focusables during render via [RenderContext#useFocus].
///   - Tab / Shift-Tab cycles through registered focusables in render order.
///   - The first focusable with `autoFocus=true` claims focus on first frame, if nothing else has
// it.
///   - Explicit `id` lets you preserve focus across reorders, and lets app code [#focus(String)]
///     a specific element imperatively.
///
/// Mirrors the API surface of [Ink's `useFocus`](https://github.com/vadimdemedes/ink#usefocus).
public final class FocusManager {

  /// Per-frame: every focusable that registered itself this render, in order.
  private final List<String> registered = new ArrayList<>();
  private final Map<String, Boolean> autoFocusFlags = new HashMap<>();
  /// Per-frame: focus id → the Fiber that registered it. Used so the runner can route key
  /// handlers to handlers registered by the focused fiber only.
  private final Map<String, Fiber> idToFiber = new HashMap<>();

  private Optional<String> focused = Optional.empty();
  private boolean focusClaimedThisFrame = false;

  /// Called by [RenderContext#useFocus] during render.
  void register(String id, Fiber fiber, boolean autoFocus) {
    registered.add(id);
    autoFocusFlags.put(id, autoFocus);
    idToFiber.put(id, fiber);
  }

  /// The Fiber that owns `id`, if it registered this frame.
  public Optional<Fiber> fiberFor(String id) {
    return Optional.ofNullable(idToFiber.get(id));
  }

  /// The Fiber that currently holds focus, if any.
  public Optional<Fiber> focusedFiber() {
    return focused.flatMap(this::fiberFor);
  }

  /// Called once per frame after render, before event dispatch.
  void commit() {
    if (focused.isEmpty() || !registered.contains(focused.get())) {
      // Either no focus yet, or the previously-focused element unmounted.
      // Pick the first autoFocus, else the first registered.
      Optional<String> auto =
          registered.stream().filter(id -> autoFocusFlags.getOrDefault(id, false)).findFirst();
      focused =
          auto.or(() -> registered.isEmpty() ? Optional.empty() : Optional.of(registered.get(0)));
    }
    focusClaimedThisFrame = true;
  }

  /// Called by ReactApp before the next render.
  void clearFrame() {
    registered.clear();
    autoFocusFlags.clear();
    idToFiber.clear();
    focusClaimedThisFrame = false;
  }

  public boolean isFocused(String id) {
    return focused.map(f -> f.equals(id)).orElse(false);
  }

  public Optional<String> currentlyFocused() {
    return focused;
  }

  public void focus(String id) {
    focused = Optional.of(id);
  }

  public void blur() {
    focused = Optional.empty();
  }

  public void tab() {
    cycle(+1);
  }

  public void shiftTab() {
    cycle(-1);
  }

  private void cycle(int direction) {
    if (registered.isEmpty()) return;
    int currentIdx =
        focused.map(registered::indexOf).filter(i -> i >= 0).orElse(direction > 0 ? -1 : 0);
    int n = registered.size();
    int nextIdx = ((currentIdx + direction) % n + n) % n;
    focused = Optional.of(registered.get(nextIdx));
  }
}
