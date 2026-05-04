package jatatui.react;

import static jatatui.react.Components.*;
import static org.junit.jupiter.api.Assertions.*;

import jatatui.core.backend.TestBackend;
import jatatui.core.terminal.Terminal;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/// Reproduces the user-reported bug: "log empties for each thing I do".
///
/// Root cause: hooks.touched is only populated by `renderChild` — never for the root fiber
/// itself. After every render, hooks.sweep() drops every hook whose fiber wasn't touched, which
/// includes the root component's useState. Each re-render → fresh initial state → log empties.
class RootStatePersistenceTest {

  @Test
  void useState_at_root_persists_across_re_renders() throws IOException {
    EventRegistry events = new EventRegistry();
    HookStore hooks = new HookStore();
    FocusManager focus = new FocusManager();
    AtomicReference<State<Integer>> ref = new AtomicReference<>();

    Element app =
        component(
            ctx -> {
              State<Integer> count = ctx.useState(() -> 0);
              ref.set(count);
              return text("count = " + count.get());
            });

    rerender(events, hooks, focus, app);
    ref.get().set(42);

    rerender(events, hooks, focus, app);

    // The State should still hold 42 — it's just at the root fiber, not unmounted.
    Object stored =
        hooks.values.entrySet().stream()
            .filter(e -> e.getValue() instanceof Integer)
            .findFirst()
            .orElseThrow()
            .getValue();
    assertEquals(42, stored, "useState at root must survive sweep across renders");
  }

  @Test
  void useState_in_nested_component_already_persists() throws IOException {
    // Sanity check — nested components were never affected because their fiber gets touched
    // by renderChild.
    EventRegistry events = new EventRegistry();
    HookStore hooks = new HookStore();
    FocusManager focus = new FocusManager();
    AtomicReference<State<Integer>> ref = new AtomicReference<>();

    Element app =
        component(
            ctx ->
                component(
                    inner -> {
                      State<Integer> count = inner.useState(() -> 0);
                      ref.set(count);
                      return text("inner = " + count.get());
                    }));

    rerender(events, hooks, focus, app);
    ref.get().set(99);
    rerender(events, hooks, focus, app);

    Object stored =
        hooks.values.entrySet().stream()
            .filter(e -> e.getValue() instanceof Integer)
            .findFirst()
            .orElseThrow()
            .getValue();
    assertEquals(99, stored);
  }

  static void rerender(EventRegistry events, HookStore hooks, FocusManager focus, Element root)
      throws IOException {
    TestBackend backend = new TestBackend(80, 24);
    Terminal<TestBackend> terminal = Terminal.create(backend);
    terminal.draw(
        frame -> {
          events.clear();
          focus.clearFrame();
          RenderContext ctx = new RenderContext(frame, events, hooks, focus, () -> {});
          events.recordBounds(Fiber.root(), frame.area());
          root.render(ctx, frame.area());
        });
    hooks.sweep();
    focus.commit();
  }
}
