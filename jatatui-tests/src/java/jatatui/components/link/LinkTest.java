package jatatui.components.link;

import static jatatui.react.Components.*;
import static org.junit.jupiter.api.Assertions.*;

import jatatui.react.Element;
import jatatui.react.KeyEvent;
import jatatui.react.MouseEvent;
import jatatui.react.TestHarness;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyModifiers;

class LinkTest {

  @Test
  void click_invokes_onactivate() throws IOException {
    AtomicInteger n = new AtomicInteger();
    Element app = Link.focusable(true, n::incrementAndGet, focused -> text("link"));

    TestHarness h = new TestHarness(40, 5);
    h.render(app);
    h.renderer.dispatchMouse(new MouseEvent(2, 0, new KeyModifiers(0), MouseEvent.Kind.DOWN));
    assertEquals(1, n.get());
  }

  @Test
  void enter_when_focused_invokes_onactivate() throws IOException {
    AtomicInteger n = new AtomicInteger();
    Element app = Link.focusable(true, n::incrementAndGet, focused -> text("link"));

    TestHarness h = new TestHarness(40, 5);
    h.render(app);
    h.render(app); // eager-claim already focused; render once more to settle
    h.renderer.dispatchKey(new KeyEvent(new KeyCode.Enter(), new KeyModifiers(0)));
    assertEquals(1, n.get());
  }

  @Test
  void content_receives_live_focused_flag() throws IOException {
    java.util.List<Boolean> seen = new java.util.ArrayList<>();
    Element app =
        Link.focusable(
            true,
            () -> {},
            focused -> {
              seen.add(focused);
              return text("link");
            });

    TestHarness h = new TestHarness(40, 5);
    h.render(app);
    assertEquals(java.util.List.of(true), seen, "autoFocus=true → first render sees focused=true");
  }

  @Test
  void click_variant_not_focusable_but_clickable() throws IOException {
    AtomicInteger n = new AtomicInteger();
    Element app = Link.click(n::incrementAndGet, text("plain"));

    TestHarness h = new TestHarness(40, 5);
    h.render(app);
    // No useFocus → nothing focused on the link. Click still fires.
    h.renderer.dispatchMouse(new MouseEvent(2, 0, new KeyModifiers(0), MouseEvent.Kind.DOWN));
    assertEquals(1, n.get());
    // Enter does nothing (no key handler registered).
    h.renderer.dispatchKey(new KeyEvent(new KeyCode.Enter(), new KeyModifiers(0)));
    assertEquals(1, n.get());
  }
}
