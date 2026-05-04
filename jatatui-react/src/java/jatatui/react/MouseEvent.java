package jatatui.react;

import tui.crossterm.KeyModifiers;

/// A mouse event delivered to a handler. Mutable only via [#stopPropagation()] — the rest is
/// read-only.
public final class MouseEvent {
  public final int x;
  public final int y;
  public final KeyModifiers modifiers;
  public final Kind kind;
  private boolean stopped;

  public MouseEvent(int x, int y, KeyModifiers modifiers, Kind kind) {
    this.x = x;
    this.y = y;
    this.modifiers = modifiers;
    this.kind = kind;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }

  public KeyModifiers modifiers() {
    return modifiers;
  }

  public Kind kind() {
    return kind;
  }

  /// Prevents the event from bubbling further up the fiber tree. Already-fired handlers on this
  /// level still get called; subsequent fibers up the chain are skipped.
  public void stopPropagation() {
    stopped = true;
  }

  public boolean isPropagationStopped() {
    return stopped;
  }

  public enum Kind {
    DOWN,
    UP,
    DRAG,
    MOVE,
    SCROLL_UP,
    SCROLL_DOWN
  }
}
