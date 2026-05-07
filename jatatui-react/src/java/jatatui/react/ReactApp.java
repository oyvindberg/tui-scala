package jatatui.react;

import jatatui.core.terminal.Terminal;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;
import tui.crossterm.KeyModifiers;

/// Runs an [Element] tree on the terminal.
///
/// Re-render is **strictly event-driven**: the loop only redraws when:
///   - a `useState.set(...)` reports a change → flips dirty
///   - a Crossterm event arrives → may dispatch a handler that flips dirty
///   - the terminal is resized
///
/// (No frame timer for now — animations would need a `useFrame`-style hook, see DESIGN.md.)
public final class ReactApp {

  private final Element root;
  // Package-private so tests in this package can drive ReactApp without going through the JNI.
  final EventRegistry events = new EventRegistry();
  final HookStore hooks = new HookStore();
  final FocusManager focus = new FocusManager();
  final AtomicBoolean dirty = new AtomicBoolean(true);
  private final CrosstermJni jni;

  /// Package-private — tests construct ReactApp directly to drive `handle(...)` without going
  /// through the JNI poll loop.
  ReactApp(Element root, CrosstermJni jni) {
    this.root = root;
    this.jni = jni;
  }

  public static void run(Element root) throws IOException {
    Jatatui.runIo(
        terminal -> {
          CrosstermJni jni = new CrosstermJni();
          // Enable mouse capture so onClick / onScroll handlers receive events. Without this the
          // terminal never sends mouse events to the app at all. Disabled in finally so the
          // terminal is restored cleanly on exit (or panic, via Jatatui.runIo's cleanup).
          jni.execute(new tui.crossterm.Command.EnableMouseCapture());
          try {
            ReactApp app = new ReactApp(root, jni);
            app.loop(terminal);
          } finally {
            try {
              jni.execute(new tui.crossterm.Command.DisableMouseCapture());
            } catch (RuntimeException e) {
              // best-effort cleanup; don't mask the original failure
            }
          }
        });
  }

  private void loop(Terminal<CrosstermBackend> terminal) throws IOException {
    boolean running = true;
    while (running) {
      if (dirty.getAndSet(false)) {
        terminal.draw(this::renderFrame);
        hooks.sweep();
        focus.commit();
      }
      // Poll with 100ms timeout so timer-driven re-renders (toasts, animations) wake the loop
      // promptly. Pure event-driven for app correctness — the loop body just re-checks `dirty`.
      if (jni.poll(new Duration(0L, 100_000_000))) {
        Event ev = jni.read();
        running = handle(ev);
      }
    }
  }

  private void renderFrame(jatatui.core.terminal.Frame frame) {
    events.clear();
    focus.clearFrame();
    RenderContext ctx = new RenderContext(frame, events, hooks, focus, () -> dirty.set(true));
    // Record the root fiber's bounds so click hit-tests bubble all the way up to root handlers.
    events.recordBounds(Fiber.root(), frame.area());
    root.render(ctx, frame.area());
    // Portals render last so they paint over the main UI; drainPortals also re-runs to handle
    // portals queued by other portals.
    ctx.drainPortals();
  }

  /// Returns false to quit the loop.
  /// Package-private so tests can drive ReactApp without going through the JNI poll loop.
  boolean handle(Event ev) {
    if (ev instanceof Event.Key keyEv && keyEv.keyEvent().kind() == KeyEventKind.Press) {
      KeyCode code = keyEv.keyEvent().code();
      KeyModifiers mods = keyEv.keyEvent().modifiers();
      // Ctrl-C is unconditional (OS-style abort).
      if (code instanceof KeyCode.Char ch
          && ch.c() == 'c'
          && (mods.bits() & KeyModifiers.CONTROL) != 0) return false;
      if (code instanceof KeyCode.Tab) {
        if ((mods.bits() & KeyModifiers.SHIFT) != 0) focus.shiftTab();
        else focus.tab();
        dirty.set(true);
        return true;
      }
      // Most terminals send Shift-Tab as KeyCode.BackTab rather than Tab+SHIFT modifier.
      if (code instanceof KeyCode.BackTab) {
        focus.shiftTab();
        dirty.set(true);
        return true;
      }
      KeyEvent kev = new KeyEvent(code, mods);
      boolean handled = events.dispatchKey(kev, focus.focusedFiber());
      if (handled) dirty.set(true);
      // Esc-to-quit is a debug fallback: if nothing in the app handled Esc, quit. Apps that want
      // Esc to mean something (modal dismiss, form cancel) just register an Esc handler.
      if (!handled && code instanceof KeyCode.Esc) return false;
    } else if (ev instanceof Event.Mouse mouseEv) {
      var me = mouseEv.mouseEvent();
      MouseEvent.Kind kind =
          switch (me.kind()) {
            case tui.crossterm.MouseEventKind.Down ignored -> MouseEvent.Kind.DOWN;
            case tui.crossterm.MouseEventKind.Up ignored -> MouseEvent.Kind.UP;
            case tui.crossterm.MouseEventKind.Drag ignored -> MouseEvent.Kind.DRAG;
            case tui.crossterm.MouseEventKind.Moved ignored -> MouseEvent.Kind.MOVE;
            case tui.crossterm.MouseEventKind.ScrollUp ignored -> MouseEvent.Kind.SCROLL_UP;
            case tui.crossterm.MouseEventKind.ScrollDown ignored -> MouseEvent.Kind.SCROLL_DOWN;
            default -> null;
          };
      if (kind != null) {
        MouseEvent mev = new MouseEvent(me.column(), me.row(), me.modifiers(), kind);
        if (events.dispatchMouse(mev)) dirty.set(true);
      }
    } else if (ev instanceof Event.Resize) {
      dirty.set(true);
    }
    return true;
  }

}
