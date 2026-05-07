package jatatui.react;

import jatatui.core.backend.TestBackend;
import jatatui.core.terminal.Terminal;
import java.io.IOException;

/// Test-only helper for off-loop rendering of an Element tree against a [TestBackend]. Lets
/// tests in any package drive renders without depending on package-private internals
/// (`HookStore`, `EventRegistry`, `FocusManager`, `Fiber`, the package-private `RenderContext`
/// constructor).
///
/// Holds persistent state across `render` calls — useful for testing useState persistence,
/// keyed-list reordering, etc. Construct one per test method.
public final class TestHarness {

  public final EventRegistry events = new EventRegistry();
  public final HookStore hooks = new HookStore();
  public final FocusManager focus = new FocusManager();
  public final TestBackend backend;
  public final Terminal<TestBackend> terminal;

  public TestHarness(int width, int height) throws IOException {
    this.backend = new TestBackend(width, height);
    this.terminal = Terminal.create(backend);
  }

  /// Render `root` once, drain portals, sweep hooks, commit focus.
  public void render(Element root) throws IOException {
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
