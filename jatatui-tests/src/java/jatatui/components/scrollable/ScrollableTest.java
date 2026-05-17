package jatatui.components.scrollable;

import static jatatui.react.Components.*;
import static org.junit.jupiter.api.Assertions.*;

import jatatui.react.Element;
import jatatui.react.MouseEvent;
import jatatui.react.TestHarness;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import tui.crossterm.KeyModifiers;

class ScrollableTest {

  static List<Element> rows(int count) {
    List<Element> rs = new ArrayList<>();
    for (int i = 0; i < count; i++) rs.add(length(1, text("row-" + i)));
    return rs;
  }

  static String topLine(TestHarness h, int width) {
    var buf = h.backend.buffer();
    StringBuilder sb = new StringBuilder();
    for (int x = 0; x < width; x++) sb.append(buf.cellAt(x, 0).symbol());
    return sb.toString().trim();
  }

  @Test
  void wheel_down_scrolls_content_by_step() throws IOException {
    Element app = Scrollable.column(rows(30));
    TestHarness h = new TestHarness(20, 10);
    h.render(app);
    assertTrue(topLine(h, 20).startsWith("row-0"));

    h.renderer.dispatchMouse(
        new MouseEvent(2, 2, new KeyModifiers(0), MouseEvent.Kind.SCROLL_DOWN));
    h.render(app);
    assertTrue(topLine(h, 20).startsWith("row-" + Scrollable.STEP));
  }

  @Test
  void wheel_up_clamps_at_zero() throws IOException {
    Element app = Scrollable.column(rows(30));
    TestHarness h = new TestHarness(20, 10);
    h.render(app);

    for (int i = 0; i < 5; i++) {
      h.renderer.dispatchMouse(
          new MouseEvent(2, 2, new KeyModifiers(0), MouseEvent.Kind.SCROLL_UP));
      h.render(app);
    }
    assertTrue(topLine(h, 20).startsWith("row-0"), "wheel-up at offset 0 stays at 0");
  }

  /// Improved behavior over the typr original: max offset is clamped against the assigned
  /// area's height. With 30 children and a 10-row viewport, max offset = 20 — wheel-down
  /// can't push beyond row-20 at the top (which would mean row-29 at the bottom, exactly
  /// aligned).
  @Test
  void wheel_down_clamps_at_max_so_last_child_stays_visible() throws IOException {
    Element app = Scrollable.column(rows(30));
    TestHarness h = new TestHarness(20, 10);
    h.render(app);

    // Scroll way down (10 ticks × STEP = 30, way past max=20).
    for (int i = 0; i < 10; i++) {
      h.renderer.dispatchMouse(
          new MouseEvent(2, 2, new KeyModifiers(0), MouseEvent.Kind.SCROLL_DOWN));
      h.render(app);
    }

    // Top should be row-20 (max offset for n=30, visibleH=10 = 30-10).
    assertTrue(
        topLine(h, 20).startsWith("row-20"),
        "clamped at max offset; got top: '" + topLine(h, 20) + "'");
  }

  @Test
  void no_overflow_no_scroll() throws IOException {
    Element app = Scrollable.column(rows(5));
    TestHarness h = new TestHarness(20, 10);
    h.render(app);
    // 5 children fit in 10-row viewport. max offset = max(0, 5-10) = 0. Wheel-down does nothing.
    h.renderer.dispatchMouse(
        new MouseEvent(2, 2, new KeyModifiers(0), MouseEvent.Kind.SCROLL_DOWN));
    h.render(app);
    assertTrue(topLine(h, 20).startsWith("row-0"));
  }
}
