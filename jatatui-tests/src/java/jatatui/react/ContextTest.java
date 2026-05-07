package jatatui.react;

import static jatatui.react.Components.*;
import static org.junit.jupiter.api.Assertions.*;

import jatatui.core.backend.TestBackend;
import jatatui.core.terminal.Terminal;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ContextTest {

  static final Context<String> THEME = Context.create("light");
  static final Context<Integer> COUNT = Context.create(0);

  @Test
  void use_context_returns_default_when_no_provider() throws IOException {
    AtomicReference<String> seen = new AtomicReference<>();
    Element app =
        component(
            ctx -> {
              seen.set(ctx.useContext(THEME));
              return text("ok");
            });
    render(app);
    assertEquals("light", seen.get());
  }

  @Test
  void provide_overrides_default() throws IOException {
    AtomicReference<String> seen = new AtomicReference<>();
    Element child =
        component(
            ctx -> {
              seen.set(ctx.useContext(THEME));
              return text("ok");
            });
    Element app = provide(THEME, "dark", child);
    render(app);
    assertEquals("dark", seen.get());
  }

  @Test
  void nested_providers_inner_wins() throws IOException {
    AtomicReference<String> seen = new AtomicReference<>();
    Element leaf =
        component(
            ctx -> {
              seen.set(ctx.useContext(THEME));
              return text("ok");
            });
    Element app = provide(THEME, "dark", provide(THEME, "high-contrast", leaf));
    render(app);
    assertEquals("high-contrast", seen.get());
  }

  @Test
  void provider_pops_after_subtree() throws IOException {
    AtomicReference<String> seenInside = new AtomicReference<>();
    AtomicReference<String> seenOutside = new AtomicReference<>();
    Element inside =
        component(
            ctx -> {
              seenInside.set(ctx.useContext(THEME));
              return text("inside");
            });
    Element outside =
        component(
            ctx -> {
              seenOutside.set(ctx.useContext(THEME));
              return text("outside");
            });
    Element app = column(provide(THEME, "dark", inside), outside);
    render(app);
    assertEquals("dark", seenInside.get());
    assertEquals("light", seenOutside.get(), "after provider exits, default is restored");
  }

  @Test
  void multiple_independent_contexts() throws IOException {
    AtomicReference<String> theme = new AtomicReference<>();
    AtomicReference<Integer> count = new AtomicReference<>();
    Element leaf =
        component(
            ctx -> {
              theme.set(ctx.useContext(THEME));
              count.set(ctx.useContext(COUNT));
              return text("ok");
            });
    Element app = provide(THEME, "dark", provide(COUNT, 42, leaf));
    render(app);
    assertEquals("dark", theme.get());
    assertEquals(42, count.get());
  }

  static void render(Element root) throws IOException {
    EventRegistry events = new EventRegistry();
    HookStore hooks = new HookStore();
    FocusManager focus = new FocusManager();
    TestBackend backend = new TestBackend(40, 12);
    Terminal<TestBackend> terminal = Terminal.create(backend);
    terminal.draw(
        frame -> {
          events.clear();
          focus.clearFrame();
          RenderContext ctx = new RenderContext(frame, events, hooks, focus, () -> {});
          events.recordBounds(Fiber.root(), frame.area());
          root.render(ctx, frame.area());
          ctx.drainPortals();
        });
    hooks.sweep();
    focus.commit();
  }
}
