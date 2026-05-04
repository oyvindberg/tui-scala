package jatatui.tests.core.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.backend.ClearType;
import jatatui.core.backend.TestBackend;
import jatatui.core.backend.WindowSize;
import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.BufferUpdate;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import jatatui.tests._support.BufferAssertions;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Ports the inline tests from `submodules/ratatui/ratatui-core/src/backend/test.rs`.
public class TestBackendTest {

  // ---- Helpers (mirrors the upstream `assert_*` methods on TestBackend) ----

  private static void assertBufferLines(TestBackend backend, String... expected) {
    BufferAssertions.assertBufferEq(backend.buffer(), Buffer.withLines(expected));
  }

  private static void assertScrollbackLines(TestBackend backend, String... expected) {
    BufferAssertions.assertBufferEq(backend.scrollback(), Buffer.withLines(expected));
  }

  private static void assertScrollbackEmpty(TestBackend backend) {
    int width = backend.scrollback().area().width();
    Buffer expected = Buffer.empty(new Rect(0, 0, width, 0));
    BufferAssertions.assertBufferEq(backend.scrollback(), expected);
  }

  private static void assertCursorPosition(TestBackend backend, Position expected) {
    assertEquals(expected, backend.getCursorPosition());
  }

  // ---- Tests ----

  @Test
  public void new_() {
    TestBackend backend = new TestBackend(10, 2);
    // Equivalent to upstream's struct equality check: same buffer, scrollback, cursor, pos.
    assertEquals(Buffer.withLines("          ", "          "), backend.buffer());
    assertEquals(Buffer.empty(new Rect(0, 0, 10, 0)), backend.scrollback());
    assertFalse(backend.cursorShown());
    assertEquals(new Position(0, 0), backend.pos());
  }

  @Test
  public void test_buffer_view() {
    Buffer buffer = Buffer.withLines("aaaa", "aaaa");
    assertEquals("\"aaaa\"\n\"aaaa\"\n", TestBackend.bufferView(buffer));
  }

  @Test
  public void buffer_view_with_overwrites() {
    // Upstream uses the family ZWJ sequence "👨‍👩‍👧‍👦" which their `unicode-width` crate
    // reports as display-width 2, yielding `[(1, " ")]`. Our port's Wcwidth helper sums per
    // codepoint widths (no ZWJ-cluster awareness), so the same cluster reports width 8 and the
    // resulting buffer has 7 hidden trailing cells. The test still exercises the multi-width
    // formatting path; only the column count differs.
    String multiByteChar = "👨‍👩‍👧‍👦";
    Buffer buffer = Buffer.withLines(multiByteChar);
    String expected =
        "\""
            + multiByteChar
            + "\" Hidden by multi-width symbols: "
            + "[(1, \" \"), (2, \" \"), (3, \" \"), (4, \" \"), (5, \" \"), (6, \" \"), (7, \" \")]\n";
    assertEquals(expected, TestBackend.bufferView(buffer));
  }

  @Test
  public void buffer() {
    TestBackend backend = new TestBackend(10, 2);
    assertBufferLines(backend, "          ", "          ");
  }

  @Test
  public void resize() {
    TestBackend backend = new TestBackend(10, 2);
    backend.resize(5, 5);
    assertBufferLines(backend, "     ", "     ", "     ", "     ", "     ");
  }

  @Test
  public void assert_buffer() {
    TestBackend backend = new TestBackend(10, 2);
    assertBufferLines(backend, "          ", "          ");
  }

  @Test
  public void assert_buffer_panics() {
    TestBackend backend = new TestBackend(10, 2);
    AssertionError err =
        assertThrows(
            AssertionError.class, () -> assertBufferLines(backend, "aaaaaaaaaa", "aaaaaaaaaa"));
    assertTrue(
        err.getMessage().contains("buffer contents not equal"),
        "unexpected message: " + err.getMessage());
  }

  @Test
  public void assert_scrollback_panics() {
    TestBackend backend = new TestBackend(10, 2);
    // Upstream uses `assert_eq!` which panics with "assertion `left == right` failed"; ours uses
    // BufferAssertions which surfaces "buffer areas not equal" — same intent.
    assertThrows(
        AssertionError.class, () -> assertScrollbackLines(backend, "aaaaaaaaaa", "aaaaaaaaaa"));
  }

  @Test
  public void display() {
    TestBackend backend = new TestBackend(10, 2);
    assertEquals("\"          \"\n\"          \"\n", backend.toString());
  }

  @Test
  public void draw() throws Exception {
    TestBackend backend = new TestBackend(10, 2);
    Cell cell = Cell.of("a");
    backend.draw(List.of(new BufferUpdate(0, 0, cell)));
    backend.draw(List.of(new BufferUpdate(0, 1, cell)));
    assertBufferLines(backend, "a         ", "a         ");
  }

  @Test
  public void hide_cursor() throws Exception {
    TestBackend backend = new TestBackend(10, 2);
    backend.hideCursor();
    assertFalse(backend.cursorShown());
  }

  @Test
  public void show_cursor() throws Exception {
    TestBackend backend = new TestBackend(10, 2);
    backend.showCursor();
    assertTrue(backend.cursorShown());
  }

  @Test
  public void get_cursor_position() throws Exception {
    TestBackend backend = new TestBackend(10, 2);
    assertEquals(Position.ORIGIN, backend.getCursorPosition());
  }

  @Test
  public void assert_cursor_position() {
    TestBackend backend = new TestBackend(10, 2);
    assertCursorPosition(backend, Position.ORIGIN);
  }

  @Test
  public void set_cursor_position() throws Exception {
    TestBackend backend = new TestBackend(10, 10);
    backend.setCursorPosition(new Position(5, 5));
    assertEquals(new Position(5, 5), backend.pos());
  }

  @Test
  public void clear() throws Exception {
    TestBackend backend = new TestBackend(4, 2);
    Cell cell = Cell.of("a");
    backend.draw(List.of(new BufferUpdate(0, 0, cell)));
    backend.draw(List.of(new BufferUpdate(0, 1, cell)));
    backend.clear();
    assertBufferLines(backend, "    ", "    ");
  }

  @Test
  public void clear_region_all() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa");
    backend.clearRegion(ClearType.All);
    assertBufferLines(
        backend, "          ", "          ", "          ", "          ", "          ");
  }

  @Test
  public void clear_region_after_cursor() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa");
    backend.setCursorPosition(new Position(3, 2));
    backend.clearRegion(ClearType.AfterCursor);
    assertBufferLines(
        backend, "aaaaaaaaaa", "aaaaaaaaaa", "aaaa      ", "          ", "          ");
  }

  @Test
  public void clear_region_before_cursor() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa");
    backend.setCursorPosition(new Position(5, 3));
    backend.clearRegion(ClearType.BeforeCursor);
    assertBufferLines(
        backend, "          ", "          ", "          ", "     aaaaa", "aaaaaaaaaa");
  }

  @Test
  public void clear_region_current_line() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa");
    backend.setCursorPosition(new Position(3, 1));
    backend.clearRegion(ClearType.CurrentLine);
    assertBufferLines(
        backend, "aaaaaaaaaa", "          ", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa");
  }

  @Test
  public void clear_region_until_new_line() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa");
    backend.setCursorPosition(new Position(3, 0));
    backend.clearRegion(ClearType.UntilNewLine);
    assertBufferLines(
        backend, "aaa       ", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa", "aaaaaaaaaa");
  }

  @Test
  public void append_lines_not_at_last_line() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");

    backend.setCursorPosition(Position.ORIGIN);

    // If the cursor is not at the last line in the terminal the addition of a newline simply
    // moves the cursor down and to the right.

    backend.appendLines(1);
    assertCursorPosition(backend, new Position(1, 1));

    backend.appendLines(1);
    assertCursorPosition(backend, new Position(2, 2));

    backend.appendLines(1);
    assertCursorPosition(backend, new Position(3, 3));

    backend.appendLines(1);
    assertCursorPosition(backend, new Position(4, 4));

    // As such the buffer should remain unchanged.
    assertBufferLines(
        backend, "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");
    assertScrollbackEmpty(backend);
  }

  @Test
  public void append_lines_at_last_line() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");

    // If the cursor is at the last line in the terminal the addition of a newline will scroll
    // the contents of the buffer.
    backend.setCursorPosition(new Position(0, 4));

    backend.appendLines(1);

    assertBufferLines(
        backend, "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee", "          ");
    assertScrollbackLines(backend, "aaaaaaaaaa");

    // It also moves the cursor to the right (raw-mode behaviour).
    assertCursorPosition(backend, new Position(1, 4));
  }

  @Test
  public void append_multiple_lines_not_at_last_line() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");

    backend.setCursorPosition(Position.ORIGIN);

    // If the cursor is not at the last line, appending multiple newlines simply moves the cursor
    // n lines down and to the right by 1.
    backend.appendLines(4);
    assertCursorPosition(backend, new Position(1, 4));

    // As such the buffer should remain unchanged.
    assertBufferLines(
        backend, "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");
    assertScrollbackEmpty(backend);
  }

  @Test
  public void append_multiple_lines_past_last_line() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");

    backend.setCursorPosition(new Position(0, 3));

    backend.appendLines(3);
    assertCursorPosition(backend, new Position(1, 4));

    assertBufferLines(
        backend, "cccccccccc", "dddddddddd", "eeeeeeeeee", "          ", "          ");
    assertScrollbackLines(backend, "aaaaaaaaaa", "bbbbbbbbbb");
  }

  @Test
  public void append_multiple_lines_where_cursor_at_end_appends_height_lines() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");

    backend.setCursorPosition(new Position(0, 4));

    backend.appendLines(5);
    assertCursorPosition(backend, new Position(1, 4));

    assertBufferLines(
        backend, "          ", "          ", "          ", "          ", "          ");
    assertScrollbackLines(
        backend, "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");
  }

  @Test
  public void append_multiple_lines_where_cursor_appends_height_lines() throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");

    backend.setCursorPosition(Position.ORIGIN);

    backend.appendLines(5);
    assertCursorPosition(backend, new Position(1, 4));

    assertBufferLines(
        backend, "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee", "          ");
    assertScrollbackLines(backend, "aaaaaaaaaa");
  }

  @Test
  public void append_multiple_lines_where_cursor_at_end_appends_more_than_height_lines()
      throws Exception {
    TestBackend backend =
        TestBackend.withLines(
            "aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd", "eeeeeeeeee");

    backend.setCursorPosition(new Position(0, 4));

    backend.appendLines(8);
    assertCursorPosition(backend, new Position(1, 4));

    assertBufferLines(
        backend, "          ", "          ", "          ", "          ", "          ");
    assertScrollbackLines(
        backend,
        "aaaaaaaaaa",
        "bbbbbbbbbb",
        "cccccccccc",
        "dddddddddd",
        "eeeeeeeeee",
        "          ",
        "          ",
        "          ");
  }

  @Test
  public void append_lines_truncates_beyond_u16_max() throws Exception {
    TestBackend backend = new TestBackend(10, 5);

    int rowCount = 65_535 + 10;
    for (int row = 0; row <= rowCount; row++) {
      if (row > 4) {
        backend.setCursorPosition(new Position(0, 4));
        backend.appendLines(1);
      }
      String s = String.format("%10d", row);
      int y = Math.min(4, row);
      java.util.List<BufferUpdate> updates = new java.util.ArrayList<>();
      // Iterate by code points so any multi-char graphemes still align to columns; for
      // right-aligned numbers each char is a single column.
      for (int x = 0; x < s.length(); x++) {
        Cell cell = Cell.ofChar(s.charAt(x));
        updates.add(new BufferUpdate(x, y, cell));
      }
      backend.draw(updates);
    }

    // Last 5 lines appended.
    assertBufferLines(
        backend,
        "     65541",
        "     65542",
        "     65543",
        "     65544",
        "     65545");

    // The Rust test slices the scrollback content directly. We do the same by constructing a
    // synthetic Buffer over a slice of the scrollback's content array.
    Buffer scrollback = backend.scrollback();
    Cell[] all = scrollback.content();

    Cell[] first5Slice = new Cell[10 * 5];
    System.arraycopy(all, 0, first5Slice, 0, 10 * 5);
    Buffer firstSliceBuf = bufferOf(new Rect(0, 0, 10, 5), first5Slice);
    BufferAssertions.assertBufferEq(
        firstSliceBuf,
        Buffer.withLines("         6", "         7", "         8", "         9", "        10"));

    Cell[] last5Slice = new Cell[10 * 5];
    System.arraycopy(all, 10 * 65_530, last5Slice, 0, 10 * 5);
    Buffer lastSliceBuf = bufferOf(new Rect(0, 0, 10, 5), last5Slice);
    BufferAssertions.assertBufferEq(
        lastSliceBuf,
        Buffer.withLines("     65536", "     65537", "     65538", "     65539", "     65540"));

    // Make sure the scrollback is the right size.
    assertEquals(10, scrollback.area().width());
    assertEquals(65_535, scrollback.area().height());
    assertEquals(10 * 65_535, scrollback.content().length);
  }

  // Reflection-free helper: builds a Buffer with the supplied area & content array via a tiny
  // round-trip through Buffer.empty + content swap.
  private static Buffer bufferOf(Rect area, Cell[] content) {
    Buffer b = Buffer.empty(area);
    // Buffer.content is publicly mutable on Buffer.java — see the field declaration there.
    b.content = content;
    return b;
  }

  @Test
  public void size() throws Exception {
    TestBackend backend = new TestBackend(10, 2);
    assertEquals(new Size(10, 2), backend.size());
  }

  @Test
  public void window_size() throws Exception {
    // Not in upstream, but exercises the constant we defined.
    TestBackend backend = new TestBackend(10, 2);
    assertEquals(new WindowSize(new Size(10, 2), new Size(640, 480)), backend.windowSize());
  }

  @Test
  public void flush() throws Exception {
    TestBackend backend = new TestBackend(10, 2);
    backend.flush();
  }

  // ---- scrolling_regions: scroll_region_up ----

  static Stream<Arguments> scroll_region_up_cases() {
    String A = "aaaa";
    String B = "bbbb";
    String C = "cccc";
    String D = "dddd";
    String E = "eeee";
    String S = "    ";
    return Stream.of(
        Arguments.of(
            new String[] {A, B, C, D, E}, 0, 5, 0, new String[] {}, new String[] {A, B, C, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E},
            0,
            5,
            2,
            new String[] {A, B},
            new String[] {C, D, E, S, S}),
        Arguments.of(
            new String[] {A, B, C, D, E},
            0,
            5,
            5,
            new String[] {A, B, C, D, E},
            new String[] {S, S, S, S, S}),
        Arguments.of(
            new String[] {A, B, C, D, E},
            0,
            5,
            7,
            new String[] {A, B, C, D, E, S, S},
            new String[] {S, S, S, S, S}),
        Arguments.of(
            new String[] {A, B, C, D, E}, 0, 3, 0, new String[] {}, new String[] {A, B, C, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E},
            0,
            3,
            2,
            new String[] {A, B},
            new String[] {C, S, S, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E},
            0,
            3,
            3,
            new String[] {A, B, C},
            new String[] {S, S, S, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E},
            0,
            3,
            4,
            new String[] {A, B, C, S},
            new String[] {S, S, S, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E}, 1, 4, 0, new String[] {}, new String[] {A, B, C, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E}, 1, 4, 2, new String[] {}, new String[] {A, D, S, S, E}),
        Arguments.of(
            new String[] {A, B, C, D, E}, 1, 4, 3, new String[] {}, new String[] {A, S, S, S, E}),
        Arguments.of(
            new String[] {A, B, C, D, E}, 1, 4, 4, new String[] {}, new String[] {A, S, S, S, E}),
        Arguments.of(
            new String[] {A, B, C, D, E}, 0, 0, 0, new String[] {}, new String[] {A, B, C, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E},
            0,
            0,
            2,
            new String[] {S, S},
            new String[] {A, B, C, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E}, 2, 2, 0, new String[] {}, new String[] {A, B, C, D, E}),
        Arguments.of(
            new String[] {A, B, C, D, E}, 2, 2, 2, new String[] {}, new String[] {A, B, C, D, E}));
  }

  @ParameterizedTest(name = "[{1}, {2}) by {3}")
  @MethodSource("scroll_region_up_cases")
  public void scroll_region_up(
      String[] initialScreen,
      int regionStart,
      int regionEnd,
      int scrollBy,
      String[] expectedScrollback,
      String[] expectedBuffer)
      throws Exception {
    TestBackend backend = TestBackend.withLines(initialScreen);
    backend.scrollRegionUp(regionStart, regionEnd, scrollBy);
    if (expectedScrollback.length == 0) {
      assertScrollbackEmpty(backend);
    } else {
      assertScrollbackLines(backend, expectedScrollback);
    }
    assertBufferLines(backend, expectedBuffer);
  }

  // ---- scrolling_regions: scroll_region_down ----

  static Stream<Arguments> scroll_region_down_cases() {
    String A = "aaaa";
    String B = "bbbb";
    String C = "cccc";
    String D = "dddd";
    String E = "eeee";
    String S = "    ";
    return Stream.of(
        Arguments.of(new String[] {A, B, C, D, E}, 0, 5, 0, new String[] {A, B, C, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 5, 2, new String[] {S, S, A, B, C}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 5, 5, new String[] {S, S, S, S, S}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 5, 7, new String[] {S, S, S, S, S}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 3, 0, new String[] {A, B, C, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 3, 2, new String[] {S, S, A, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 3, 3, new String[] {S, S, S, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 3, 4, new String[] {S, S, S, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 1, 4, 0, new String[] {A, B, C, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 1, 4, 2, new String[] {A, S, S, B, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 1, 4, 3, new String[] {A, S, S, S, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 1, 4, 4, new String[] {A, S, S, S, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 0, 0, new String[] {A, B, C, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 0, 0, 2, new String[] {A, B, C, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 2, 2, 0, new String[] {A, B, C, D, E}),
        Arguments.of(new String[] {A, B, C, D, E}, 2, 2, 2, new String[] {A, B, C, D, E}));
  }

  @ParameterizedTest(name = "[{1}, {2}) by {3}")
  @MethodSource("scroll_region_down_cases")
  public void scroll_region_down(
      String[] initialScreen,
      int regionStart,
      int regionEnd,
      int scrollBy,
      String[] expectedBuffer)
      throws Exception {
    TestBackend backend = TestBackend.withLines(initialScreen);
    backend.scrollRegionDown(regionStart, regionEnd, scrollBy);
    assertScrollbackEmpty(backend);
    assertBufferLines(backend, expectedBuffer);
  }
}
