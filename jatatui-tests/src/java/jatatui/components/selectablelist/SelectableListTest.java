package jatatui.components.selectablelist;

import static jatatui.react.Components.*;
import static org.junit.jupiter.api.Assertions.*;

import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.KeyEvent;
import jatatui.react.MouseEvent;
import jatatui.react.TestHarness;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyModifiers;

class SelectableListTest {

  /// Heterogeneous-row payload: Header for non-activatable decoration, Item for activatable rows.
  sealed interface Row {
    record Header(String title) implements Row {}
    record Item(String name) implements Row {}
  }

  static SelectableListProps.RowRenderer<Row> renderer() {
    return (row, sel) ->
        switch (row) {
          case Row.Header h -> text("== " + h.title() + " ==", Style.empty());
          case Row.Item it -> text((sel ? "> " : "  ") + it.name(), Style.empty());
        };
  }

  @Test
  void down_skips_non_activatable_rows() throws IOException {
    AtomicInteger selectedIdx = new AtomicInteger(1); // start on first Item
    List<Row> rows =
        List.of(
            new Row.Header("Group A"),
            new Row.Item("a1"),
            new Row.Item("a2"),
            new Row.Header("Group B"),
            new Row.Item("b1"));

    Element appBound =
        component(
            ctx ->
                jatatui.components.Components.selectableList(
                    SelectableListProps.of(
                            rows,
                            r -> r instanceof Row.Item,
                            renderer(),
                            selectedIdx.get(),
                            selectedIdx::set)
                        .withFocusId("list")
                        .withAutoFocus(true)));

    TestHarness h = new TestHarness(40, 20);
    h.render(appBound);
    h.render(appBound);

    // Down from idx 1 (Item "a1") → idx 2 ("a2"), then skip header (idx 3) to idx 4 ("b1").
    h.renderer.dispatchKey(new KeyEvent(new KeyCode.Down(), new KeyModifiers(0)));
    h.render(appBound);
    assertEquals(2, selectedIdx.get(), "down moves to a2");

    h.renderer.dispatchKey(new KeyEvent(new KeyCode.Down(), new KeyModifiers(0)));
    h.render(appBound);
    assertEquals(4, selectedIdx.get(), "down skips header at idx 3, lands on b1");
  }

  @Test
  void up_skips_non_activatable_rows() throws IOException {
    AtomicInteger selectedIdx = new AtomicInteger(4); // last Item
    List<Row> rows =
        List.of(
            new Row.Header("Group A"),
            new Row.Item("a1"),
            new Row.Item("a2"),
            new Row.Header("Group B"),
            new Row.Item("b1"));

    Element app =
        component(
            ctx ->
                jatatui.components.Components.selectableList(
                    SelectableListProps.of(
                            rows,
                            r -> r instanceof Row.Item,
                            renderer(),
                            selectedIdx.get(),
                            selectedIdx::set)
                        .withFocusId("list")
                        .withAutoFocus(true)));

    TestHarness h = new TestHarness(40, 20);
    h.render(app);
    h.render(app);

    h.renderer.dispatchKey(new KeyEvent(new KeyCode.Up(), new KeyModifiers(0)));
    h.render(app);
    assertEquals(2, selectedIdx.get(), "up from b1 (idx 4) skips header and lands on a2 (idx 2)");
  }

  @Test
  void enter_activates_only_when_selected_is_activatable() throws IOException {
    AtomicReference<String> activated = new AtomicReference<>();
    AtomicInteger selectedIdx = new AtomicInteger(1); // Item
    List<Row> rows =
        List.of(
            new Row.Header("Group"),
            new Row.Item("a1"),
            new Row.Item("a2"));

    Element app =
        component(
            ctx ->
                jatatui.components.Components.selectableList(
                    SelectableListProps.of(
                            rows,
                            r -> r instanceof Row.Item,
                            renderer(),
                            selectedIdx.get(),
                            selectedIdx::set)
                        .withOnActivate(
                            r -> activated.set(((Row.Item) r).name()))
                        .withFocusId("list")
                        .withAutoFocus(true)));

    TestHarness h = new TestHarness(40, 20);
    h.render(app);
    h.render(app);

    h.renderer.dispatchKey(new KeyEvent(new KeyCode.Enter(), new KeyModifiers(0)));
    assertEquals("a1", activated.get());
  }

  @Test
  void click_on_unselected_row_selects_it_first() throws IOException {
    AtomicInteger activations = new AtomicInteger();
    AtomicInteger selectedIdx = new AtomicInteger(0);
    List<Row> rows = List.of(new Row.Item("a"), new Row.Item("b"), new Row.Item("c"));

    Element app =
        component(
            ctx ->
                jatatui.components.Components.selectableList(
                    SelectableListProps.of(
                            rows,
                            r -> true,
                            renderer(),
                            selectedIdx.get(),
                            selectedIdx::set)
                        .withOnActivate(r -> activations.incrementAndGet())
                        .withFocusId("list")
                        .withAutoFocus(true)));

    TestHarness h = new TestHarness(40, 20);
    h.render(app);
    h.render(app);

    // Click on row 2 (idx 2, "c"). Not currently selected → selects, no activation.
    h.renderer.dispatchMouse(
        new MouseEvent(2, 2, new KeyModifiers(0), MouseEvent.Kind.DOWN));
    h.render(app);
    assertEquals(2, selectedIdx.get());
    assertEquals(0, activations.get(), "click on unselected row selects, doesn't activate");

    // Click again on the now-selected row (still y=2) → activates.
    h.renderer.dispatchMouse(
        new MouseEvent(2, 2, new KeyModifiers(0), MouseEvent.Kind.DOWN));
    h.render(app);
    assertEquals(1, activations.get(), "click on already-selected row activates");
  }

  @Test
  void click_on_non_activatable_row_is_noop() throws IOException {
    AtomicInteger activations = new AtomicInteger();
    AtomicInteger selectedIdx = new AtomicInteger(1); // start on the Item
    List<Row> rows = List.of(new Row.Header("hdr"), new Row.Item("a"));

    Element app =
        component(
            ctx ->
                jatatui.components.Components.selectableList(
                    SelectableListProps.of(
                            rows,
                            r -> r instanceof Row.Item,
                            renderer(),
                            selectedIdx.get(),
                            selectedIdx::set)
                        .withOnActivate(r -> activations.incrementAndGet())
                        .withFocusId("list")
                        .withAutoFocus(true)));

    TestHarness h = new TestHarness(40, 20);
    h.render(app);
    h.render(app);

    // Click on the header row (y=0).
    h.renderer.dispatchMouse(
        new MouseEvent(2, 0, new KeyModifiers(0), MouseEvent.Kind.DOWN));
    h.render(app);
    assertEquals(1, selectedIdx.get(), "selection unchanged");
    assertEquals(0, activations.get(), "no activation");
  }

  @Test
  void wheel_scrolls_without_moving_selection() throws IOException {
    AtomicInteger selectedIdx = new AtomicInteger(0);
    List<Row> rows = new java.util.ArrayList<>();
    for (int i = 0; i < 30; i++) rows.add(new Row.Item("item-" + i));

    Element app =
        component(
            ctx ->
                jatatui.components.Components.selectableList(
                    SelectableListProps.of(
                            rows,
                            r -> true,
                            renderer(),
                            selectedIdx.get(),
                            selectedIdx::set)
                        .withFocusId("list")
                        .withAutoFocus(true)));

    TestHarness h = new TestHarness(40, 10);
    h.render(app);
    h.render(app);

    // Verify item-0 is currently visible at the top.
    var buf = h.backend.buffer();
    StringBuilder topRowBefore = new StringBuilder();
    for (int x = 0; x < 12; x++) topRowBefore.append(buf.cellAt(x, 0).symbol());
    assertTrue(topRowBefore.toString().contains("item-0"));

    // Scroll wheel down a few times.
    for (int i = 0; i < 3; i++) {
      h.renderer.dispatchMouse(
          new MouseEvent(2, 5, new KeyModifiers(0), MouseEvent.Kind.SCROLL_DOWN));
      h.render(app);
    }

    // Selection should still be 0 (no movement on wheel).
    assertEquals(0, selectedIdx.get(), "wheel doesn't move selection");

    // The visible window has scrolled — item-0 no longer at top.
    StringBuilder topRowAfter = new StringBuilder();
    for (int x = 0; x < 12; x++) topRowAfter.append(buf.cellAt(x, 0).symbol());
    assertFalse(
        topRowAfter.toString().startsWith("> item-0") || topRowAfter.toString().startsWith("  item-0"),
        "viewport scrolled past item-0; got '" + topRowAfter + "'");
  }

  @Test
  void selection_change_auto_scrolls_into_view() throws IOException {
    AtomicInteger selectedIdx = new AtomicInteger(0);
    List<Row> rows = new java.util.ArrayList<>();
    for (int i = 0; i < 30; i++) rows.add(new Row.Item("i" + i));

    Element app =
        component(
            ctx ->
                jatatui.components.Components.selectableList(
                    SelectableListProps.of(
                            rows,
                            r -> true,
                            renderer(),
                            selectedIdx.get(),
                            selectedIdx::set)
                        .withFocusId("list")
                        .withAutoFocus(true)));

    TestHarness h = new TestHarness(40, 10);
    h.render(app);
    h.render(app);

    // Jump selection programmatically to row 25 (way out of the 0..9 viewport).
    selectedIdx.set(25);
    h.render(app);

    // Auto-scroll: selected (25) should be visible. Viewport size = 10 → offset = 25-10+1 = 16.
    // Last visible row = 25 (selected). Top visible = 16.
    var buf = h.backend.buffer();
    StringBuilder topRow = new StringBuilder();
    for (int x = 0; x < 6; x++) topRow.append(buf.cellAt(x, 0).symbol());
    assertTrue(
        topRow.toString().contains("i16"),
        "viewport auto-scrolled so sel 25 is visible; top row should be i16, got '" + topRow + "'");
  }
}
