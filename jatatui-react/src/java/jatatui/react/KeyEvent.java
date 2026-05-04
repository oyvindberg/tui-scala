package jatatui.react;

import tui.crossterm.KeyCode;
import tui.crossterm.KeyModifiers;

/// A key event delivered to a handler.
public final class KeyEvent {
  public final KeyCode code;
  public final KeyModifiers modifiers;
  private boolean stopped;

  public KeyEvent(KeyCode code, KeyModifiers modifiers) {
    this.code = code;
    this.modifiers = modifiers;
  }

  public KeyCode code() {
    return code;
  }

  public KeyModifiers modifiers() {
    return modifiers;
  }

  /// Prevents the event from bubbling further up the fiber tree (and from reaching global key
  /// handlers).
  public void stopPropagation() {
    stopped = true;
  }

  public boolean isPropagationStopped() {
    return stopped;
  }
}
