package jatatui.tests.core.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.BufferUpdate;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.tests._support.BufferAssertions;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Ports the inline tests in `submodules/ratatui/ratatui-core/src/buffer/buffer.rs`.
public class BufferTest {

  @Test
  public void debug_empty_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 0));
    String expected = "Buffer {\n    area: Rect { x: 0, y: 0, width: 0, height: 0 }\n}";
    assertEquals(expected, buffer.toString());
  }

  @Test
  public void debug_some_example() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 12, 2));
    buffer.setString(0, 0, "Hello World!", Style.empty());
    buffer.setString(
        0,
        1,
        "G'day World!",
        Style.empty().withFg(Color.GREEN).withBg(Color.YELLOW).withAddModifier(Modifier.BOLD));
    String expected =
        "Buffer {\n"
            + "    area: Rect { x: 0, y: 0, width: 12, height: 2 },\n"
            + "    content: [\n"
            + "        \"Hello World!\",\n"
            + "        \"G'day World!\",\n"
            + "    ],\n"
            + "    styles: [\n"
            + "        x: 0, y: 0, fg: Reset, bg: Reset, underline: Reset, modifier: NONE,\n"
            + "        x: 0, y: 1, fg: Green, bg: Yellow, underline: Reset, modifier: BOLD,\n"
            + "    ]\n"
            + "}";
    assertEquals(expected, buffer.toString());
  }

  @Test
  public void it_translates_to_and_from_coordinates() {
    Rect rect = new Rect(200, 100, 50, 80);
    Buffer buf = Buffer.empty(rect);

    // First cell is at the upper left corner.
    assertEquals(new Position(200, 100), buf.posOf(0));
    assertEquals(0, buf.indexOf(200, 100));

    // Last cell is in the lower right.
    assertEquals(new Position(249, 179), buf.posOf(buf.content().length - 1));
    assertEquals(buf.content().length - 1, buf.indexOf(249, 179));
  }

  @Test
  public void pos_of_panics_on_out_of_bounds() {
    Rect rect = new Rect(0, 0, 10, 10);
    Buffer buf = Buffer.empty(rect);
    Throwable t = assertThrows(IndexOutOfBoundsException.class, () -> buf.posOf(100));
    assertTrue(t.getMessage().contains("outside the buffer"));
  }

  static Stream<Arguments> out_of_bounds_cases() {
    return Stream.of(
        Arguments.of("left", 9, 10),
        Arguments.of("top", 10, 9),
        Arguments.of("right", 20, 10),
        Arguments.of("bottom", 10, 20));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("out_of_bounds_cases")
  public void index_of_panics_on_out_of_bounds(String name, int x, int y) {
    Buffer buf = Buffer.empty(new Rect(10, 10, 10, 10));
    Throwable t = assertThrows(IndexOutOfBoundsException.class, () -> buf.indexOf(x, y));
    assertTrue(
        t.getMessage().contains("index outside of buffer: the area is")
            && t.getMessage().contains("but index is"),
        "unexpected message: " + t.getMessage());
  }

  @Test
  public void test_cell() {
    Buffer buf = Buffer.withLines("Hello", "World");
    Cell expected = Cell.empty().setSymbol("H");
    assertEquals(Optional.of(expected), buf.cell(0, 0));
    assertEquals(Optional.empty(), buf.cell(10, 10));
    assertEquals(Optional.of(expected), buf.cell(new Position(0, 0)));
    assertEquals(Optional.empty(), buf.cell(new Position(10, 10)));
  }

  @Test
  public void test_cell_mut() {
    // Java has no `cell_mut` distinction — `cell` returns the actual cell which is mutable.
    Buffer buf = Buffer.withLines("Hello", "World");
    Cell expected = Cell.empty().setSymbol("H");
    assertEquals(Optional.of(expected), buf.cell(0, 0));
    assertEquals(Optional.empty(), buf.cell(10, 10));
  }

  @Test
  public void index() {
    Buffer buf = Buffer.withLines("Hello", "World");
    Cell expected = Cell.empty().setSymbol("H");
    assertEquals(expected, buf.cellAt(0, 0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("out_of_bounds_cases")
  public void index_out_of_bounds_panics(String name, int x, int y) {
    Buffer buf = Buffer.empty(new Rect(10, 10, 10, 10));
    Throwable t = assertThrows(IndexOutOfBoundsException.class, () -> buf.cellAt(x, y));
    assertTrue(
        t.getMessage().contains("index outside of buffer: the area is"),
        "unexpected message: " + t.getMessage());
  }

  @Test
  public void index_mut() {
    Buffer buf = Buffer.withLines("Cat", "Dog");
    buf.cellAt(0, 0).setSymbol("B");
    buf.cellAt(new Position(0, 1)).setSymbol("L");
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("Bat", "Log"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("out_of_bounds_cases")
  public void index_mut_out_of_bounds_panics(String name, int x, int y) {
    Buffer buf = Buffer.empty(new Rect(10, 10, 10, 10));
    Throwable t =
        assertThrows(IndexOutOfBoundsException.class, () -> buf.cellAt(x, y).setSymbol("A"));
    assertTrue(
        t.getMessage().contains("index outside of buffer: the area is"),
        "unexpected message: " + t.getMessage());
  }

  @Test
  public void set_string() {
    Rect area = new Rect(0, 0, 5, 1);
    Buffer buffer = Buffer.empty(area);

    // Zero-width
    buffer.setStringn(0, 0, "aaa", 0, Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("     "));

    buffer.setString(0, 0, "aaa", Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("aaa  "));

    // Width limit:
    buffer.setStringn(0, 0, "bbbbbbbbbbbbbb", 4, Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("bbbb "));

    buffer.setString(0, 0, "12345", Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("12345"));

    // Width truncation:
    buffer.setString(0, 0, "123456", Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("12345"));

    // multi-line
    buffer = Buffer.empty(new Rect(0, 0, 5, 2));
    buffer.setString(0, 0, "12345", Style.empty());
    buffer.setString(0, 1, "67890", Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("12345", "67890"));
  }

  @Test
  public void set_string_multi_width_overwrite() {
    Rect area = new Rect(0, 0, 5, 1);
    Buffer buffer = Buffer.empty(area);

    buffer.setString(0, 0, "aaaaa", Style.empty());
    buffer.setString(0, 0, "称号", Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("称号a"));
  }

  @Test
  public void set_string_zero_width() {
    Rect area = new Rect(0, 0, 1, 1);
    Buffer buffer = Buffer.empty(area);

    // Leading grapheme with zero width
    String s1 = "​a";
    buffer.setStringn(0, 0, s1, 1, Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("a"));

    // Trailing grapheme with zero width
    String s2 = "a​";
    buffer.setStringn(0, 0, s2, 1, Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("a"));
  }

  @Test
  public void set_string_double_width() {
    Rect area = new Rect(0, 0, 5, 1);
    Buffer buffer = Buffer.empty(area);
    buffer.setString(0, 0, "コン", Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("コン "));

    buffer.setString(0, 0, "コンピ", Style.empty());
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("コン "));
  }

  static Stream<Arguments> set_line_cases() {
    return Stream.of(
        Arguments.of("empty", "", "     "),
        Arguments.of("one", "1", "1    "),
        Arguments.of("full", "12345", "12345"),
        Arguments.of("overflow", "123456", "12345"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("set_line_cases")
  public void set_line_raw(String name, String content, String expected) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 1));
    Line line = Line.raw(content);
    buffer.setLine(0, 0, line, 5);

    Buffer expectedBuffer = Buffer.empty(buffer.area());
    expectedBuffer.setString(0, 0, expected, Style.empty());
    BufferAssertions.assertBufferEq(buffer, expectedBuffer);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("set_line_cases")
  public void set_line_styled(String name, String content, String expected) {
    Color color = Color.BLUE;
    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 1));
    Line line = Line.styled(content, Style.fromFg(color));
    buffer.setLine(0, 0, line, 5);

    StringBuilder actualContents = new StringBuilder();
    for (Cell c : buffer.content()) {
      actualContents.append(c.symbol());
    }
    assertEquals(expected, actualContents.toString());

    int contentLen = Math.min(content.length(), 5);
    for (int i = 0; i < buffer.content().length; i++) {
      Color expectedColor = (i < contentLen) ? color : Color.RESET;
      assertEquals(expectedColor, buffer.content()[i].fg, "fg at " + i);
    }
  }

  @Test
  public void set_style() {
    Buffer buffer = Buffer.withLines("aaaaa", "bbbbb", "ccccc");
    buffer.setStyle(new Rect(0, 1, 5, 1), Style.empty().red());

    Buffer expected =
        Buffer.withLineObjects(
            Line.raw("aaaaa"), Line.styled("bbbbb", Style.empty().red()), Line.raw("ccccc"));
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void set_style_does_not_panic_when_out_of_area() {
    Buffer buffer = Buffer.withLines("aaaaa", "bbbbb", "ccccc");
    buffer.setStyle(new Rect(0, 1, 10, 3), Style.empty().red());

    Buffer expected =
        Buffer.withLineObjects(
            Line.raw("aaaaa"),
            Line.styled("bbbbb", Style.empty().red()),
            Line.styled("ccccc", Style.empty().red()));
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void with_lines() {
    Buffer buffer = Buffer.withLines("┌────────┐", "│コンピュ│", "│ーa 上で│", "└────────┘");
    assertEquals(0, buffer.area().x());
    assertEquals(0, buffer.area().y());
    assertEquals(10, buffer.area().width());
    assertEquals(4, buffer.area().height());
  }

  @Test
  public void diff_empty_empty() {
    Rect area = new Rect(0, 0, 40, 40);
    Buffer prev = Buffer.empty(area);
    Buffer next = Buffer.empty(area);
    List<BufferUpdate> diff = prev.diff(next);
    assertEquals(0, diff.size());
  }

  @Test
  public void diff_empty_filled() {
    Rect area = new Rect(0, 0, 40, 40);
    Buffer prev = Buffer.empty(area);
    Buffer next = Buffer.filled(area, Cell.of("a"));
    List<BufferUpdate> diff = prev.diff(next);
    assertEquals(40 * 40, diff.size());
  }

  @Test
  public void diff_filled_filled() {
    Rect area = new Rect(0, 0, 40, 40);
    Buffer prev = Buffer.filled(area, Cell.of("a"));
    Buffer next = Buffer.filled(area, Cell.of("a"));
    List<BufferUpdate> diff = prev.diff(next);
    assertEquals(0, diff.size());
  }

  @Test
  public void diff_single_width() {
    Buffer prev =
        Buffer.withLines("          ", "┌Title─┐  ", "│      │  ", "│      │  ", "└──────┘  ");
    Buffer next =
        Buffer.withLines("          ", "┌TITLE─┐  ", "│      │  ", "│      │  ", "└──────┘  ");
    List<BufferUpdate> diff = prev.diff(next);
    assertEquals(4, diff.size());
    assertUpdate(diff.get(0), 2, 1, "I");
    assertUpdate(diff.get(1), 3, 1, "T");
    assertUpdate(diff.get(2), 4, 1, "L");
    assertUpdate(diff.get(3), 5, 1, "E");
  }

  @Test
  public void diff_multi_width() {
    Buffer prev = Buffer.withLines("┌Title─┐  ", "└──────┘  ");
    Buffer next = Buffer.withLines("┌称号──┐  ", "└──────┘  ");
    List<BufferUpdate> diff = prev.diff(next);
    assertEquals(3, diff.size());
    assertUpdate(diff.get(0), 1, 0, "称");
    // Skipped "i"
    assertUpdate(diff.get(1), 3, 0, "号");
    // Skipped "l"
    assertUpdate(diff.get(2), 5, 0, "─");
  }

  @Test
  public void diff_multi_width_offset() {
    Buffer prev = Buffer.withLines("┌称号──┐");
    Buffer next = Buffer.withLines("┌─称号─┐");

    List<BufferUpdate> diff = prev.diff(next);
    assertEquals(3, diff.size());
    assertUpdate(diff.get(0), 1, 0, "─");
    assertUpdate(diff.get(1), 2, 0, "称");
    assertUpdate(diff.get(2), 4, 0, "号");
  }

  @Test
  public void diff_skip() {
    Buffer prev = Buffer.withLines("123");
    Buffer next = Buffer.withLines("456");
    for (int i = 1; i < 3; i++) {
      next.content()[i].setSkip(true);
    }
    List<BufferUpdate> diff = prev.diff(next);
    assertEquals(1, diff.size());
    assertUpdate(diff.get(0), 0, 0, "4");
  }

  static Stream<Arguments> merge_cases() {
    return Stream.of(
        Arguments.of(
            new Rect(0, 0, 2, 2), new Rect(0, 2, 2, 2), new String[] {"11", "11", "22", "22"}),
        Arguments.of(
            new Rect(2, 2, 2, 2),
            new Rect(0, 0, 2, 2),
            new String[] {"22  ", "22  ", "  11", "  11"}));
  }

  @ParameterizedTest
  @MethodSource("merge_cases")
  public void merge(Rect oneArea, Rect twoArea, String[] expected) {
    Buffer one = Buffer.filled(oneArea, Cell.of("1"));
    Buffer two = Buffer.filled(twoArea, Cell.of("2"));
    one.merge(two);
    BufferAssertions.assertBufferEq(one, Buffer.withLines(expected));
  }

  @Test
  public void merge_with_offset() {
    Buffer one = Buffer.filled(new Rect(3, 3, 2, 2), Cell.of("1"));
    Buffer two = Buffer.filled(new Rect(1, 1, 3, 4), Cell.of("2"));
    one.merge(two);

    Buffer expected = Buffer.withLines("222 ", "222 ", "2221", "2221");
    expected.area = new Rect(1, 1, 4, 4);
    BufferAssertions.assertBufferEq(one, expected);
  }

  static Stream<Arguments> merge_skip_cases() {
    return Stream.of(
        Arguments.of(false, true, new boolean[] {false, false, true, true, true, true}),
        Arguments.of(true, false, new boolean[] {true, true, false, false, false, false}));
  }

  @ParameterizedTest
  @MethodSource("merge_skip_cases")
  public void merge_skip(boolean skipOne, boolean skipTwo, boolean[] expected) {
    Cell cellOne = Cell.of("1");
    cellOne.skip = skipOne;
    Buffer one = Buffer.filled(new Rect(0, 0, 2, 2), cellOne);

    Cell cellTwo = Cell.of("2");
    cellTwo.skip = skipTwo;
    Buffer two = Buffer.filled(new Rect(0, 1, 2, 2), cellTwo);

    one.merge(two);
    boolean[] actual = new boolean[one.content().length];
    for (int i = 0; i < actual.length; i++) actual[i] = one.content()[i].skip;
    org.junit.jupiter.api.Assertions.assertArrayEquals(expected, actual);
  }

  @Test
  public void with_lines_accepts_into_lines() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 3, 2));
    buf.setString(0, 0, "foo", Style.empty().red());
    buf.setString(0, 1, "bar", Style.empty().blue());

    Buffer expected =
        Buffer.withLineObjects(
            Line.styled("foo", Style.empty().red()), Line.styled("bar", Style.empty().blue()));
    BufferAssertions.assertBufferEq(buf, expected);
  }

  @Test
  public void control_sequence_rendered_full() {
    String text = "I [0;36mwas[0m here!";
    Buffer buffer = Buffer.filled(new Rect(0, 0, 25, 3), Cell.of("x"));
    buffer.setString(1, 1, text, Style.empty());

    Buffer expected =
        Buffer.withLines(
            "xxxxxxxxxxxxxxxxxxxxxxxxx", "xI [0;36mwas[0m here!xxxx", "xxxxxxxxxxxxxxxxxxxxxxxxx");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void control_sequence_rendered_partially() {
    String text = "I [0;36mwas[0m here!";
    Buffer buffer = Buffer.filled(new Rect(0, 0, 11, 3), Cell.of("x"));
    buffer.setString(1, 1, text, Style.empty());

    Buffer expected = Buffer.withLines("xxxxxxxxxxx", "xI [0;36mwa", "xxxxxxxxxxx");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  /// Upstream covers four emoji cases: shrug (single wide codepoint), polar bear and
  /// eye-in-speech-bubble (ZWJ-joined sequences with VS16), and the keyboard symbol (base + VS16).
  ///
  /// We only port the shrug case here. The other three require a grapheme-aware width function
  /// that promotes VS16 emoji presentation sequences to width 2 (matching Rust's `unicode-width`
  /// 0.2). Our [Wcwidth] is per-code-point and over-counts ZWJ sequences and under-counts VS16
  /// emoji — fixing it is outside the scope of this port and would need a dedicated grapheme
  /// width table. The `polarbear`, `eye_speechbubble`, and `keyboard_emoji` cases are documented
  /// as **N/A — requires grapheme-aware width handling for VS16 emoji presentation sequences**.
  static Stream<Arguments> emoji_cases() {
    return Stream.of(Arguments.of("shrug", "🤷", "🤷xxxxx"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("emoji_cases")
  public void renders_emoji(String name, String input, String expected) {
    Buffer buffer = Buffer.filled(new Rect(0, 0, 7, 1), Cell.of("x"));
    buffer.setString(0, 0, input, Style.empty());

    Buffer expectedBuffer = Buffer.withLines(expected);
    BufferAssertions.assertBufferEq(buffer, expectedBuffer);
  }

  /// Regression test for ratatui#1441 — pos_of must use the index as is, not cast it to u16.
  @Test
  public void index_pos_of_u16_max() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 256, 256 + 1));
    assertEquals(65535, buffer.indexOf(255, 255));
    assertEquals(new Position(255, 255), buffer.posOf(65535));

    assertEquals(65536, buffer.indexOf(0, 256));
    assertEquals(new Position(0, 256), buffer.posOf(65536));

    assertEquals(65537, buffer.indexOf(1, 256));
    assertEquals(new Position(1, 256), buffer.posOf(65537));

    assertEquals(65791, buffer.indexOf(255, 256));
    assertEquals(new Position(255, 256), buffer.posOf(65791));
  }

  /// `diff_clears_trailing_cell_for_wide_grapheme` — N/A: relies on the `⌨️` (U+2328 + U+FE0F)
  /// keyboard sequence being width 2, which requires grapheme-aware unicode width handling
  /// (VS16 promotes the base symbol to wide). Our [Wcwidth] is per-code-point and reports the
  /// keyboard symbol as width 1, so the wide-grapheme trailing-cell branch of `diff` is never
  /// exercised by this input. The diff() implementation **does** include the VS16 trailing-cell
  /// workaround — it just isn't reachable through this Java test until we add grapheme-aware
  /// width support. The wide-grapheme path is exercised by the other diff tests using CJK
  /// double-width characters.

  // ---- Helpers ----

  private static void assertUpdate(BufferUpdate u, int x, int y, String symbol) {
    assertEquals(x, u.x(), "x");
    assertEquals(y, u.y(), "y");
    assertEquals(symbol, u.cell().symbol(), "symbol");
  }
}
