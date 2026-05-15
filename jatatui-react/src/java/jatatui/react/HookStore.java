package jatatui.react;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/// Persistent state across renders — `useState` values, `useEffect` deps + cleanups, memoization
/// caches. Owned by [ReactApp]; threaded through [RenderContext].
final class HookStore {
  final Map<HookKey, Object> values = new HashMap<>();
  final Map<HookKey, Object[]> deps = new HashMap<>();
  final Map<HookKey, Runnable> cleanups = new HashMap<>();

  /// Cache for `memo(deps, body)` and `pureComponent(props, body)`: keyed by Fiber, stores
  /// `(deps[], producedElement)`.
  final Map<Fiber, MemoEntry> memoCache = new HashMap<>();

  /// What "type" was last rendered at each fiber position. Used by [RenderContext]'s
  /// reconciliation step — when the type at a fiber changes between frames (e.g. a stack-based
  /// router swaps SourceEditor for OutputEditor at the same fiber slot), the previous subtree
  /// is unmounted (state dropped, cleanups run) before the new one renders. Mirrors React's
  /// "different element type at same fiber → unmount + remount" rule.
  final Map<Fiber, Object> elementTypes = new HashMap<>();

  // Note: there used to be an `applyCache` for Element.Of auto-memoization, but it interacted
  // badly with side-effect handlers (onClick/onKey/useFocus registrations would be dropped on
  // cache hit). Removed — memoization is opt-in only via memo() / pureComponent(), which use
  // memoCache above.

  /// Per-frame: which fibers were touched. Used by [#sweep] to drop unmounted state + run cleanups.
  final Set<Fiber> touched = new java.util.HashSet<>();

  // -------------------- memoOrCompute (used by Memo / PureComponent) --------------------

  Element memoOrCompute(Fiber fiber, Object[] newDeps, Supplier<Element> compute) {
    MemoEntry cached = memoCache.get(fiber);
    if (cached != null && depsRefEqual(cached.deps(), newDeps)) {
      return cached.element();
    }
    Element produced = compute.get();
    memoCache.put(fiber, new MemoEntry(newDeps, produced));
    return produced;
  }

  private static boolean depsRefEqual(Object[] a, Object[] b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (!Objects.equals(a[i], b[i])) return false;
    }
    return true;
  }

  // -------------------- unmount (reconciliation) --------------------

  /// Drop all hook state for `root` and every descendant fiber, running cleanups. Used by
  /// [RenderContext]'s reconciliation when the Element type at a fiber position changes between
  /// frames — the old subtree is "unmounted" before the new one renders.
  void unmount(Fiber root) {
    Iterator<Map.Entry<HookKey, Runnable>> ci = cleanups.entrySet().iterator();
    while (ci.hasNext()) {
      Map.Entry<HookKey, Runnable> e = ci.next();
      if (isInSubtree(root, e.getKey().fiber())) {
        e.getValue().run();
        ci.remove();
      }
    }
    values.keySet().removeIf(k -> isInSubtree(root, k.fiber()));
    deps.keySet().removeIf(k -> isInSubtree(root, k.fiber()));
    memoCache.keySet().removeIf(f -> isInSubtree(root, f));
    elementTypes.keySet().removeIf(f -> isInSubtree(root, f));
  }

  /// True if `f` is `root` or descended from it.
  private static boolean isInSubtree(Fiber root, Fiber f) {
    Fiber cur = f;
    while (cur != null) {
      if (cur.equals(root)) return true;
      cur = cur.parent().orElse(null);
    }
    return false;
  }

  // -------------------- sweep --------------------

  /// Drop hook state, memo cache, apply cache for fibers that weren't touched in the just-finished
  /// render — they're "unmounted". Run their pending cleanups. Mirrors React's commit-phase
  /// unmount sweep.
  void sweep() {
    Iterator<Map.Entry<HookKey, Object>> it = values.entrySet().iterator();
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
    elementTypes.keySet().removeIf(f -> !touched.contains(f));
    touched.clear();
  }

  record MemoEntry(Object[] deps, Element element) {}
}
