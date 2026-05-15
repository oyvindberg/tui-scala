package jatatui.react;

import static jatatui.react.Components.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ImperativeFocusTest {

  /// `ctx.focus(id)` from inside a render body changes which focusable is focused on the next
  /// render. (Mirrors the real-world pattern: a click handler / validation callback / list-row
  /// selector imperatively focuses something specific.)
  @Test
  void focus_id_takes_effect_next_render() throws IOException {
    TestHarness h = new TestHarness(40, 12);

    AtomicReference<RenderContext> ctxRef = new AtomicReference<>();
    List<Boolean> flags = new ArrayList<>();

    Element app =
        component(
            ctx -> {
              ctxRef.set(ctx);
              flags.clear();
              flags.add(ctx.useFocus(Optional.of("a"), true));
              flags.add(ctx.useFocus(Optional.of("b"), false));
              flags.add(ctx.useFocus(Optional.of("c"), false));
              return text("3 focusables");
            });

    // Two renders: first registers, second sees autoFocus committed.
    h.render(app);
    h.render(app);
    assertEquals(List.of(true, false, false), flags);

    // Imperatively focus "c".
    ctxRef.get().focus("c");
    h.render(app);
    assertEquals(List.of(false, false, true), flags);

    // Imperatively focus "b".
    ctxRef.get().focus("b");
    h.render(app);
    assertEquals(List.of(false, true, false), flags);
  }

  @Test
  void blur_clears_focus() throws IOException {
    TestHarness h = new TestHarness(40, 12);
    AtomicReference<RenderContext> ctxRef = new AtomicReference<>();
    List<Boolean> flags = new ArrayList<>();

    Element app =
        component(
            ctx -> {
              ctxRef.set(ctx);
              flags.clear();
              flags.add(ctx.useFocus(Optional.of("a"), true));
              return text("one");
            });

    h.render(app);
    h.render(app);
    assertEquals(List.of(true), flags);

    ctxRef.get().blur();
    h.render(app);
    assertEquals(List.of(false), flags, "blur clears focus; nothing focused next render");
  }

  /// `ctx.focus(id)` from a click handler — the canonical "click to focus" pattern.
  @Test
  void click_handler_can_focus_other_field() throws IOException {
    TestHarness h = new TestHarness(40, 12);

    List<Boolean> flags = new ArrayList<>();

    Element app =
        component(
            ctx -> {
              flags.clear();
              flags.add(ctx.useFocus(Optional.of("a"), true));
              boolean bFocused = ctx.useFocus(Optional.of("b"), false);
              flags.add(bFocused);
              ctx.onClick(e -> ctx.focus("b"));
              return text("clickable-area");
            });

    h.render(app);
    h.render(app);
    assertEquals(List.of(true, false), flags);

    // Click anywhere — handler calls ctx.focus("b").
    h.events.dispatchMouse(
        new MouseEvent(2, 0, new tui.crossterm.KeyModifiers(0), MouseEvent.Kind.DOWN));
    h.render(app);
    assertEquals(List.of(false, true), flags);
  }
}
