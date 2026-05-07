package jatatui.tests.widgets.input;

import static org.junit.jupiter.api.Assertions.*;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.widgets.input.TextInput;
import jatatui.widgets.input.TextInput.TextResult;
import org.junit.jupiter.api.Test;

class TextInputTest {

  // ---- Pure state transitions ----

  @Test
  void insert_at_appends_when_cursor_at_end() {
    TextResult r = TextInput.insertAt("hel", 3, 'l');
    assertEquals("hell", r.value());
    assertEquals(4, r.cursorPos());
  }

  @Test
  void insert_at_inserts_in_middle() {
    TextResult r = TextInput.insertAt("ho", 1, 'i');
    assertEquals("hio", r.value());
    assertEquals(2, r.cursorPos());
  }

  @Test
  void insert_string() {
    TextResult r = TextInput.insertStringAt("abc", 1, "xyz");
    assertEquals("axyzbc", r.value());
    assertEquals(4, r.cursorPos());
  }

  @Test
  void backspace_removes_char_before_cursor() {
    TextResult r = TextInput.backspaceAt("hello", 5);
    assertEquals("hell", r.value());
    assertEquals(4, r.cursorPos());
  }

  @Test
  void backspace_at_zero_is_noop() {
    TextResult r = TextInput.backspaceAt("hello", 0);
    assertEquals("hello", r.value());
    assertEquals(0, r.cursorPos());
  }

  @Test
  void delete_removes_char_at_cursor() {
    TextResult r = TextInput.deleteAt("hello", 1);
    assertEquals("hllo", r.value());
    assertEquals(1, r.cursorPos());
  }

  @Test
  void delete_at_end_is_noop() {
    TextResult r = TextInput.deleteAt("hello", 5);
    assertEquals("hello", r.value());
    assertEquals(5, r.cursorPos());
  }

  @Test
  void move_left_clamps_at_zero() {
    assertEquals(0, TextInput.moveLeft("abc", 0).cursorPos());
    assertEquals(0, TextInput.moveLeft("abc", 1).cursorPos());
    assertEquals(2, TextInput.moveLeft("abc", 3).cursorPos());
  }

  @Test
  void move_right_clamps_at_length() {
    assertEquals(3, TextInput.moveRight("abc", 3).cursorPos());
    assertEquals(3, TextInput.moveRight("abc", 2).cursorPos());
  }

  @Test
  void move_home_end() {
    assertEquals(0, TextInput.moveHome("hello", 3).cursorPos());
    assertEquals(5, TextInput.moveEnd("hello", 0).cursorPos());
  }

  // ---- Scroll ----

  @Test
  void scroll_keeps_cursor_in_view_when_typing_past_right_edge() {
    // width=5, cursor at 7 → scroll so cursor lands at rightmost cell
    int s = TextInput.scrollFor(7, 0, 5);
    assertEquals(3, s, "cursor at 7, width 5 → scroll 3 (cursor at screen col 4)");
  }

  @Test
  void scroll_pulls_back_when_cursor_moves_left_of_window() {
    // width=5, scroll=3, cursor at 1 → scroll left to 1
    int s = TextInput.scrollFor(1, 3, 5);
    assertEquals(1, s);
  }

  @Test
  void scroll_zero_when_cursor_in_view() {
    int s = TextInput.scrollFor(2, 0, 5);
    assertEquals(0, s);
  }

  // ---- Render ----

  @Test
  void render_shows_value() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 10, 1));
    TextInput.of("hello").render(buf.area(), buf);
    StringBuilder sb = new StringBuilder();
    for (int x = 0; x < 10; x++) sb.append(buf.cellAt(x, 0).symbol());
    assertEquals("hello     ", sb.toString());
  }

  @Test
  void render_placeholder_when_empty_and_unfocused() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 10, 1));
    TextInput.of("").withPlaceholder("name").render(buf.area(), buf);
    StringBuilder sb = new StringBuilder();
    for (int x = 0; x < 10; x++) sb.append(buf.cellAt(x, 0).symbol());
    assertEquals("name      ", sb.toString());
  }

  @Test
  void render_no_placeholder_when_focused() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 10, 1));
    TextInput.of("").withPlaceholder("name").withFocused(true).render(buf.area(), buf);
    StringBuilder sb = new StringBuilder();
    for (int x = 0; x < 10; x++) sb.append(buf.cellAt(x, 0).symbol());
    assertEquals("          ", sb.toString());
  }

  @Test
  void render_scrolls_to_keep_cursor_visible() {
    // value="0123456789" (len 10), cursor at 10 (past end), width 5 → scroll=6, visible="6789" +
    // 1 trailing blank cell which is where the cursor lands.
    Buffer buf = Buffer.empty(new Rect(0, 0, 5, 1));
    TextInput.of("0123456789").withCursorPos(10).withFocused(true).render(buf.area(), buf);
    StringBuilder sb = new StringBuilder();
    for (int x = 0; x < 5; x++) sb.append(buf.cellAt(x, 0).symbol());
    assertEquals("6789 ", sb.toString());
  }
}
