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
  private final EventRegistry events = new EventRegistry();
  private final HookStore hooks = new HookStore();
  private final FocusManager focus = new FocusManager();
  private final AtomicBoolean dirty = new AtomicBoolean(true);
  private final CrosstermJni jni;

  private ReactApp(Element root, CrosstermJni jni) {
    this.root = root;
    this.jni = jni;
  }

  public static void run(Element root) throws IOException {
    Jatatui.runIo(
        terminal -> {
          ReactApp app = new ReactApp(root, new CrosstermJni());
          app.loop(terminal);
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
      // Block on input — pure event-driven.
      if (jni.poll(new Duration(60L, 0))) {
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
  }

  /// Returns false to quit the loop.
  private boolean handle(Event ev) {
    if (ev instanceof Event.Key keyEv && keyEv.keyEvent().kind() == KeyEventKind.Press) {
      KeyCode code = keyEv.keyEvent().code();
      KeyModifiers mods = keyEv.keyEvent().modifiers();
      if (isQuit(code, mods)) return false;
      if (code instanceof KeyCode.Tab) {
        if ((mods.bits() & KeyModifiers.SHIFT) != 0) focus.shiftTab();
        else focus.tab();
        dirty.set(true);
        return true;
      }
      KeyEvent kev = new KeyEvent(code, mods);
      if (events.dispatchKey(kev, focus.focusedFiber())) dirty.set(true);
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

  private static boolean isQuit(KeyCode code, KeyModifiers mods) {
    if (code instanceof KeyCode.Esc) return true;
    if (code instanceof KeyCode.Char ch
        && ch.c() == 'c'
        && (mods.bits() & KeyModifiers.CONTROL) != 0) return true;
    return false;
  }
}
