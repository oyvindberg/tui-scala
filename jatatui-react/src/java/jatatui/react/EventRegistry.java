package jatatui.react;

import jatatui.core.layout.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/// Per-frame collection of event handlers registered by elements during render.
///
/// Cleared at the start of each frame; populated during render via
/// [RenderContext#onClick] / [RenderContext#onKey] / [RenderContext#onHover];
/// queried by the [ReactApp] runner when a Crossterm `Event` arrives.
public final class EventRegistry {
  private final List<AreaHandler> areaHandlers = new ArrayList<>();
  private final List<HoverHandler> hoverHandlers = new ArrayList<>();
  private final List<KeyHandler> keyHandlers = new ArrayList<>();

  void clear() {
    areaHandlers.clear();
    hoverHandlers.clear();
    keyHandlers.clear();
  }

  void add(EventKind kind, Rect area, Runnable handler) {
    areaHandlers.add(new AreaHandler(kind, area, handler));
  }

  void addHover(Rect area, Consumer<Boolean> onChange) {
    hoverHandlers.add(new HoverHandler(area, onChange));
  }

  void addKey(Fiber fiber, Object matcher, Runnable handler) {
    keyHandlers.add(new KeyHandler(Optional.of(fiber), matcher, handler));
  }

  public boolean dispatchClick(int x, int y) {
    return dispatchAt(EventKind.CLICK, x, y);
  }

  public boolean dispatchScroll(int x, int y, boolean up) {
    return dispatchAt(up ? EventKind.SCROLL_UP : EventKind.SCROLL_DOWN, x, y);
  }

  public void deliverHover(int x, int y, Optional<Rect> previouslyHovered) {
    // sketch only
  }

  /// Dispatch a key. If `focusedFiber` is present, only handlers registered by that fiber
  /// (or globally with no fiber) are eligible. Returns true if a handler fired.
  public boolean dispatchKey(Object key, Optional<Fiber> focusedFiber) {
    for (KeyHandler h : keyHandlers) {
      boolean focusOk = h.fiber.isEmpty() || h.fiber.equals(focusedFiber);
      if (focusOk && h.matcher.equals(key)) {
        h.handler.run();
        return true;
      }
    }
    return false;
  }

  private boolean dispatchAt(EventKind kind, int x, int y) {
    for (int i = areaHandlers.size() - 1; i >= 0; i--) {
      AreaHandler h = areaHandlers.get(i);
      if (h.kind == kind && contains(h.area, x, y)) {
        h.handler.run();
        return true;
      }
    }
    return false;
  }

  private static boolean contains(Rect r, int x, int y) {
    return x >= r.x() && x < r.x() + r.width() && y >= r.y() && y < r.y() + r.height();
  }

  private record AreaHandler(EventKind kind, Rect area, Runnable handler) {}

  private record HoverHandler(Rect area, Consumer<Boolean> onChange) {}

  private record KeyHandler(Optional<Fiber> fiber, Object matcher, Runnable handler) {}
}
