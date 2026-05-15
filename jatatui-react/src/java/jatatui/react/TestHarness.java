package jatatui.react;

import jatatui.core.backend.TestBackend;
import jatatui.core.terminal.Terminal;
import java.io.IOException;

/// Test-only convenience around [Renderer] + a [TestBackend] / [Terminal]. Lets tests in any
/// package drive renders without managing the terminal plumbing themselves. Tests that need
/// fine-grained control (a real Frame from a different source, a custom event sequence) should
/// build a [Renderer] directly.
///
/// Holds persistent state across `render` calls — useful for testing useState persistence,
/// keyed-list reordering, etc. Construct one per test method.
public final class TestHarness {

  /// The underlying renderer. Use this for `dispatchMouse` / `dispatchKey` / focus operations.
  public final Renderer renderer;

  // Same instances as the renderer, exposed for tests that interact directly.
  public final EventRegistry events;
  public final FocusManager focus;
  final HookStore hooks; // package-private — same as renderer.hooks()

  public final TestBackend backend;
  public final Terminal<TestBackend> terminal;

  public TestHarness(int width, int height) throws IOException {
    this.renderer = new Renderer();
    this.events = renderer.events();
    this.focus = renderer.focus();
    this.hooks = renderer.hooks();
    this.backend = new TestBackend(width, height);
    this.terminal = Terminal.create(backend);
  }

  /// Render `root` once via the renderer.
  public void render(Element root) throws IOException {
    terminal.draw(frame -> renderer.render(frame, root));
  }
}
