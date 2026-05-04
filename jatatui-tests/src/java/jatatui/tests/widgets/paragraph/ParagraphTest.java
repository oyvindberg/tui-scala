package jatatui.tests.widgets.paragraph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.tests._support.BufferAssertions;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.TitlePosition;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Scroll;
import jatatui.widgets.paragraph.Wrap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Port of the inline `#[cfg(test)] mod tests` from
/// `submodules/ratatui/ratatui-widgets/src/paragraph.rs`.
public class ParagraphTest {

  /// Tests the [Paragraph] widget against the expected [Buffer] by rendering it onto an equal
  /// area and comparing the rendered and expected content.
  static void testCase(Paragraph paragraph, Buffer expected) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, expected.area().width(), expected.area().height()));
    paragraph.render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void zero_width_char_at_end_of_line() {
    String line = "foo​";
    List<Paragraph> paragraphs =
        List.of(
            Paragraph.of(line),
            Paragraph.of(line).withWrap(new Wrap(false)),
            Paragraph.of(line).withWrap(new Wrap(true)));
    for (Paragraph p : paragraphs) {
      testCase(p, Buffer.withLines("foo"));
      testCase(p, Buffer.withLines("foo   "));
      testCase(p, Buffer.withLines("foo   ", "      "));
      testCase(p, Buffer.withLines("foo", "   "));
    }
  }

  @Test
  public void test_render_empty_paragraph() {
    List<Paragraph> paragraphs =
        List.of(
            Paragraph.of(""),
            Paragraph.of("").withWrap(new Wrap(false)),
            Paragraph.of("").withWrap(new Wrap(true)));
    for (Paragraph p : paragraphs) {
      testCase(p, Buffer.withLines(" "));
      testCase(p, Buffer.withLines("          "));
      String[] tenFiveSpaces = new String[10];
      for (int i = 0; i < 10; i++) tenFiveSpaces[i] = "     ";
      testCase(p, Buffer.withLines(tenFiveSpaces));
      testCase(p, Buffer.withLines(" ", " "));
    }
  }

  @Test
  public void test_render_single_line_paragraph() {
    String text = "Hello, world!";
    List<Paragraph> paragraphs =
        List.of(
            Paragraph.of(text),
            Paragraph.of(text).withWrap(new Wrap(false)),
            Paragraph.of(text).withWrap(new Wrap(true)));
    for (Paragraph p : paragraphs) {
      testCase(p, Buffer.withLines("Hello, world!  "));
      testCase(p, Buffer.withLines("Hello, world!"));
      testCase(p, Buffer.withLines("Hello, world!  ", "               "));
      testCase(p, Buffer.withLines("Hello, world!", "             "));
    }
  }

  @Test
  public void test_render_multi_line_paragraph() {
    String text = "This is a\nmultiline\nparagraph.";
    List<Paragraph> paragraphs =
        List.of(
            Paragraph.of(text),
            Paragraph.of(text).withWrap(new Wrap(false)),
            Paragraph.of(text).withWrap(new Wrap(true)));
    for (Paragraph p : paragraphs) {
      testCase(p, Buffer.withLines("This is a ", "multiline ", "paragraph."));
      testCase(p, Buffer.withLines("This is a      ", "multiline      ", "paragraph.     "));
      testCase(
          p,
          Buffer.withLines(
              "This is a      ",
              "multiline      ",
              "paragraph.     ",
              "               ",
              "               "));
    }
  }

  @Test
  public void test_render_paragraph_with_block() {
    String text = "Hello, worlds!";
    Paragraph truncated = Paragraph.of(text).withBlock(Block.bordered().withTitle("Title"));
    Paragraph wrapped = truncated.withWrap(new Wrap(false));
    Paragraph trimmed = truncated.withWrap(new Wrap(true));

    for (Paragraph p : List.of(truncated, wrapped, trimmed)) {
      testCase(p, Buffer.withLines("┌Title─────────┐", "│Hello, worlds!│", "└──────────────┘"));
      testCase(
          p, Buffer.withLines("┌Title───────────┐", "│Hello, worlds!  │", "└────────────────┘"));
      testCase(
          p,
          Buffer.withLines(
              "┌Title────────────┐",
              "│Hello, worlds!   │",
              "│                 │",
              "└─────────────────┘"));
    }

    testCase(
        truncated,
        Buffer.withLines("┌Title───────┐", "│Hello, world│", "│            │", "└────────────┘"));
    testCase(
        wrapped,
        Buffer.withLines("┌Title──────┐", "│Hello,     │", "│worlds!    │", "└───────────┘"));
    testCase(
        trimmed,
        Buffer.withLines("┌Title──────┐", "│Hello,     │", "│worlds!    │", "└───────────┘"));
  }

  @Test
  public void test_render_line_styled() {
    Line l0 = Line.raw("unformatted");
    Line l1 = Line.styled("bold text", Style.empty().bold());
    Line l2 = Line.styled("cyan text", Style.empty().cyan());
    Line l3 = Line.styled("dim text", Style.empty().dim());
    Paragraph paragraph = Paragraph.of(List.of(l0, l1, l2, l3));

    Buffer expected = Buffer.withLines("unformatted", "bold text", "cyan text", "dim text");
    expected.setStyle(new Rect(0, 1, 9, 1), Style.empty().bold());
    expected.setStyle(new Rect(0, 2, 9, 1), Style.empty().cyan());
    expected.setStyle(new Rect(0, 3, 8, 1), Style.empty().dim());

    testCase(paragraph, expected);
  }

  @Test
  public void test_render_line_spans_styled() {
    Line l0 =
        Line.from(
            Span.styled("bold", Style.empty().bold()),
            Span.raw(" and "),
            Span.styled("cyan", Style.empty().cyan()));
    Line l1 = Line.from(Span.raw("unformatted"));
    Paragraph paragraph = Paragraph.of(List.of(l0, l1));

    Buffer expected = Buffer.withLines("bold and cyan", "unformatted");
    expected.setStyle(new Rect(0, 0, 4, 1), Style.empty().bold());
    expected.setStyle(new Rect(9, 0, 4, 1), Style.empty().cyan());

    testCase(paragraph, expected);
  }

  @Test
  public void test_render_paragraph_with_block_with_bottom_title_and_border() {
    Block block =
        Block.empty()
            .withBorders(Borders.BOTTOM)
            .withTitlePosition(TitlePosition.Bottom)
            .withTitle("Title");
    Paragraph paragraph = Paragraph.of("Hello, world!").withBlock(block);
    testCase(paragraph, Buffer.withLines("Hello, world!  ", "Title──────────"));
  }

  @Test
  public void test_render_paragraph_with_word_wrap() {
    String text =
        "This is a long line of text that should wrap      and contains a superultramegagigalong"
            + " word.";
    Paragraph wrapped = Paragraph.of(text).withWrap(new Wrap(false));
    Paragraph trimmed = Paragraph.of(text).withWrap(new Wrap(true));

    testCase(
        wrapped,
        Buffer.withLines(
            "This is a long line",
            "of text that should",
            "wrap      and      ",
            "contains a         ",
            "superultramegagigal",
            "ong word.          "));
    testCase(
        wrapped,
        Buffer.withLines(
            "This is a   ",
            "long line of",
            "text that   ",
            "should wrap ",
            "    and     ",
            "contains a  ",
            "superultrame",
            "gagigalong  ",
            "word.       "));

    testCase(
        trimmed,
        Buffer.withLines(
            "This is a long line",
            "of text that should",
            "wrap      and      ",
            "contains a         ",
            "superultramegagigal",
            "ong word.          "));
    testCase(
        trimmed,
        Buffer.withLines(
            "This is a   ",
            "long line of",
            "text that   ",
            "should wrap ",
            "and contains",
            "a           ",
            "superultrame",
            "gagigalong  ",
            "word.       "));
  }

  @Test
  public void test_render_wrapped_paragraph_with_whitespace_only_line() {
    List<Line> lines = new ArrayList<>();
    for (String s : List.of("A", "  ", "B", "  a", "C")) {
      lines.add(Line.from(s));
    }
    Text text = Text.fromLines(lines);
    Paragraph paragraph = Paragraph.of(text).withWrap(new Wrap(false));
    Paragraph trimmed = Paragraph.of(text).withWrap(new Wrap(true));

    testCase(paragraph, Buffer.withLines("A", "  ", "B", "  a", "C"));
    testCase(trimmed, Buffer.withLines("A", "", "B", "a", "C"));
  }

  @Test
  public void test_render_paragraph_with_line_truncation() {
    String text = "This is a long line of text that should be truncated.";
    Paragraph truncated = Paragraph.of(text);

    testCase(truncated, Buffer.withLines("This is a long line of"));
    testCase(truncated, Buffer.withLines("This is a long line of te"));
    testCase(truncated, Buffer.withLines("This is a long line of "));
    testCase(truncated.withScroll(new Scroll(0, 2)), Buffer.withLines("is is a long line of te"));
  }

  @Test
  public void test_render_paragraph_with_left_alignment() {
    String text = "Hello, world!";
    Paragraph truncated = Paragraph.of(text).withAlignment(HorizontalAlignment.Left);
    Paragraph wrapped = truncated.withWrap(new Wrap(false));
    Paragraph trimmed = truncated.withWrap(new Wrap(true));

    for (Paragraph p : List.of(truncated, wrapped, trimmed)) {
      testCase(p, Buffer.withLines("Hello, world!  "));
      testCase(p, Buffer.withLines("Hello, world!"));
    }

    testCase(truncated, Buffer.withLines("Hello, wor"));
    testCase(wrapped, Buffer.withLines("Hello,    ", "world!    "));
    testCase(trimmed, Buffer.withLines("Hello,    ", "world!    "));
  }

  @Test
  public void test_render_paragraph_with_center_alignment() {
    String text = "Hello, world!";
    Paragraph truncated = Paragraph.of(text).withAlignment(HorizontalAlignment.Center);
    Paragraph wrapped = truncated.withWrap(new Wrap(false));
    Paragraph trimmed = truncated.withWrap(new Wrap(true));

    for (Paragraph p : List.of(truncated, wrapped, trimmed)) {
      testCase(p, Buffer.withLines(" Hello, world! "));
      testCase(p, Buffer.withLines("  Hello, world! "));
      testCase(p, Buffer.withLines("  Hello, world!  "));
      testCase(p, Buffer.withLines("Hello, world!"));
    }

    testCase(truncated, Buffer.withLines("Hello, wor"));
    testCase(wrapped, Buffer.withLines("  Hello,  ", "  world!  "));
    testCase(trimmed, Buffer.withLines("  Hello,  ", "  world!  "));
  }

  @Test
  public void test_render_paragraph_with_right_alignment() {
    String text = "Hello, world!";
    Paragraph truncated = Paragraph.of(text).withAlignment(HorizontalAlignment.Right);
    Paragraph wrapped = truncated.withWrap(new Wrap(false));
    Paragraph trimmed = truncated.withWrap(new Wrap(true));

    for (Paragraph p : List.of(truncated, wrapped, trimmed)) {
      testCase(p, Buffer.withLines("  Hello, world!"));
      testCase(p, Buffer.withLines("Hello, world!"));
    }

    testCase(truncated, Buffer.withLines("Hello, wor"));
    testCase(wrapped, Buffer.withLines("    Hello,", "    world!"));
    testCase(trimmed, Buffer.withLines("    Hello,", "    world!"));
  }

  @Test
  public void test_render_paragraph_with_scroll_offset() {
    String text = "This is a\ncool\nmultiline\nparagraph.";
    Paragraph truncated = Paragraph.of(text).withScroll(new Scroll(2, 0));
    Paragraph wrapped = truncated.withWrap(new Wrap(false));
    Paragraph trimmed = truncated.withWrap(new Wrap(true));

    for (Paragraph p : List.of(truncated, wrapped, trimmed)) {
      testCase(p, Buffer.withLines("multiline   ", "paragraph.  ", "            "));
      testCase(p, Buffer.withLines("multiline   "));
    }

    testCase(truncated.withScroll(new Scroll(2, 4)), Buffer.withLines("iline   ", "graph.  "));
    testCase(wrapped, Buffer.withLines("cool   ", "multili", "ne     "));
  }

  @Test
  public void test_render_paragraph_with_zero_width_area() {
    String text = "Hello, world!";
    Rect area = new Rect(0, 0, 0, 3);

    for (Paragraph p :
        List.of(
            Paragraph.of(text),
            Paragraph.of(text).withWrap(new Wrap(false)),
            Paragraph.of(text).withWrap(new Wrap(true)))) {
      testCase(p, Buffer.empty(area));
      testCase(p.withScroll(new Scroll(2, 4)), Buffer.empty(area));
    }
  }

  @Test
  public void test_render_paragraph_with_zero_height_area() {
    String text = "Hello, world!";
    Rect area = new Rect(0, 0, 10, 0);

    for (Paragraph p :
        List.of(
            Paragraph.of(text),
            Paragraph.of(text).withWrap(new Wrap(false)),
            Paragraph.of(text).withWrap(new Wrap(true)))) {
      testCase(p, Buffer.empty(area));
      testCase(p.withScroll(new Scroll(2, 4)), Buffer.empty(area));
    }
  }

  @Test
  public void test_render_paragraph_with_styled_text() {
    Line text =
        Line.from(
            Span.styled("Hello, ", Style.empty().withFg(Color.RED)),
            Span.styled("world!", Style.empty().withFg(Color.BLUE)));

    Buffer expected = Buffer.withLines("Hello, world!");
    expected.setStyle(new Rect(0, 0, 7, 1), Style.empty().withFg(Color.RED).withBg(Color.GREEN));
    expected.setStyle(new Rect(7, 0, 6, 1), Style.empty().withFg(Color.BLUE).withBg(Color.GREEN));

    for (Paragraph p :
        List.of(
            Paragraph.of(text),
            Paragraph.of(text).withWrap(new Wrap(false)),
            Paragraph.of(text).withWrap(new Wrap(true)))) {
      testCase(p.withStyle(Style.empty().withBg(Color.GREEN)), expected);
    }
  }

  @Test
  public void test_render_paragraph_with_special_characters() {
    String text = "Hello, <world>!";
    for (Paragraph p :
        List.of(
            Paragraph.of(text),
            Paragraph.of(text).withWrap(new Wrap(false)),
            Paragraph.of(text).withWrap(new Wrap(true)))) {
      testCase(p, Buffer.withLines("Hello, <world>!"));
      testCase(p, Buffer.withLines("Hello, <world>!     "));
      testCase(p, Buffer.withLines("Hello, <world>!     ", "                    "));
      testCase(p, Buffer.withLines("Hello, <world>!", "               "));
    }
  }

  @Test
  public void test_render_paragraph_with_unicode_characters() {
    String text = "こんにちは, 世界! 😃";
    Paragraph truncated = Paragraph.of(text);
    Paragraph wrapped = Paragraph.of(text).withWrap(new Wrap(false));
    Paragraph trimmed = Paragraph.of(text).withWrap(new Wrap(true));

    for (Paragraph p : List.of(truncated, wrapped, trimmed)) {
      testCase(p, Buffer.withLines("こんにちは, 世界! 😃"));
      testCase(p, Buffer.withLines("こんにちは, 世界! 😃     "));
    }

    testCase(truncated, Buffer.withLines("こんにちは, 世 "));
    testCase(wrapped, Buffer.withLines("こんにちは,    ", "世界! 😃      "));
    testCase(trimmed, Buffer.withLines("こんにちは,    ", "世界! 😃      "));
  }

  @Test
  public void can_be_stylized() {
    assertEquals(
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM),
        Paragraph.of("").black().onWhite().bold().notDim().style);
  }

  @Test
  public void widgets_paragraph_count_rendered_lines() {
    Paragraph paragraph = Paragraph.of("Hello World");
    assertEquals(1, paragraph.lineCount(20));
    assertEquals(1, paragraph.lineCount(10));
    paragraph = Paragraph.of("Hello World").withWrap(new Wrap(false));
    assertEquals(1, paragraph.lineCount(20));
    assertEquals(2, paragraph.lineCount(10));
    paragraph = Paragraph.of("Hello World").withWrap(new Wrap(true));
    assertEquals(1, paragraph.lineCount(20));
    assertEquals(2, paragraph.lineCount(10));

    String text = "Hello World ".repeat(100);
    paragraph = Paragraph.of(text.trim());
    assertEquals(1, paragraph.lineCount(11));
    assertEquals(1, paragraph.lineCount(6));
    paragraph = paragraph.withWrap(new Wrap(false));
    assertEquals(100, paragraph.lineCount(11));
    assertEquals(200, paragraph.lineCount(6));
    paragraph = paragraph.withWrap(new Wrap(true));
    assertEquals(100, paragraph.lineCount(11));
    assertEquals(200, paragraph.lineCount(6));
  }

  @Test
  public void widgets_paragraph_rendered_line_count_accounts_block() {
    Block block = Block.empty();
    Paragraph paragraph = Paragraph.of("Hello World").withBlock(block);
    assertEquals(1, paragraph.lineCount(20));
    assertEquals(1, paragraph.lineCount(10));

    paragraph = paragraph.withBlock(Block.empty().withBorders(Borders.TOP));
    assertEquals(2, paragraph.lineCount(20));
    assertEquals(2, paragraph.lineCount(10));

    paragraph = paragraph.withBlock(Block.empty().withBorders(Borders.BOTTOM));
    assertEquals(2, paragraph.lineCount(20));
    assertEquals(2, paragraph.lineCount(10));

    paragraph = paragraph.withBlock(Block.empty().withBorders(Borders.TOP.or(Borders.BOTTOM)));
    assertEquals(3, paragraph.lineCount(20));
    assertEquals(3, paragraph.lineCount(10));

    paragraph = paragraph.withBlock(Block.bordered());
    assertEquals(3, paragraph.lineCount(20));
    assertEquals(3, paragraph.lineCount(10));

    paragraph = paragraph.withBlock(Block.bordered()).withWrap(new Wrap(true));
    assertEquals(3, paragraph.lineCount(20));
    assertEquals(4, paragraph.lineCount(10));

    paragraph = paragraph.withBlock(Block.bordered()).withWrap(new Wrap(false));
    assertEquals(3, paragraph.lineCount(20));
    assertEquals(4, paragraph.lineCount(10));

    String text = "Hello World ".repeat(100);
    paragraph = Paragraph.of(text.trim()).withBlock(Block.empty());
    assertEquals(1, paragraph.lineCount(11));

    paragraph = paragraph.withBlock(Block.bordered());
    assertEquals(3, paragraph.lineCount(11));
    assertEquals(3, paragraph.lineCount(6));

    paragraph = paragraph.withBlock(Block.empty().withBorders(Borders.TOP));
    assertEquals(2, paragraph.lineCount(11));
    assertEquals(2, paragraph.lineCount(6));

    paragraph = paragraph.withBlock(Block.empty().withBorders(Borders.BOTTOM));
    assertEquals(2, paragraph.lineCount(11));
    assertEquals(2, paragraph.lineCount(6));

    paragraph = paragraph.withBlock(Block.empty().withBorders(Borders.LEFT.or(Borders.RIGHT)));
    assertEquals(1, paragraph.lineCount(11));
    assertEquals(1, paragraph.lineCount(6));
  }

  @Test
  public void widgets_paragraph_line_width() {
    Paragraph paragraph = Paragraph.of("Hello World");
    assertEquals(11, paragraph.lineWidth());
    paragraph = Paragraph.of("Hello World").withWrap(new Wrap(false));
    assertEquals(11, paragraph.lineWidth());
    paragraph = Paragraph.of("Hello World").withWrap(new Wrap(true));
    assertEquals(11, paragraph.lineWidth());

    String text = "Hello World ".repeat(100);
    paragraph = Paragraph.of(text);
    assertEquals(1200, paragraph.lineWidth());
    paragraph = paragraph.withWrap(new Wrap(false));
    assertEquals(1200, paragraph.lineWidth());
    paragraph = paragraph.withWrap(new Wrap(true));
    assertEquals(1200, paragraph.lineWidth());
  }

  @Test
  public void widgets_paragraph_line_width_accounts_for_block() {
    Paragraph paragraph = Paragraph.of("Hello World").withBlock(Block.bordered());
    assertEquals(13, paragraph.lineWidth());

    paragraph = Paragraph.of("Hello World").withBlock(Block.empty().withBorders(Borders.LEFT));
    assertEquals(12, paragraph.lineWidth());

    paragraph =
        Paragraph.of("Hello World")
            .withBlock(Block.empty().withBorders(Borders.LEFT))
            .withWrap(new Wrap(true));
    assertEquals(12, paragraph.lineWidth());

    paragraph =
        Paragraph.of("Hello World")
            .withBlock(Block.empty().withBorders(Borders.LEFT))
            .withWrap(new Wrap(false));
    assertEquals(12, paragraph.lineWidth());
  }

  @Test
  public void left_aligned() {
    Paragraph p = Paragraph.of("Hello, world!").leftAligned();
    assertEquals(HorizontalAlignment.Left, p.alignment);
  }

  @Test
  public void centered() {
    Paragraph p = Paragraph.of("Hello, world!").centered();
    assertEquals(HorizontalAlignment.Center, p.alignment);
  }

  @Test
  public void right_aligned() {
    Paragraph p = Paragraph.of("Hello, world!").rightAligned();
    assertEquals(HorizontalAlignment.Right, p.alignment);
  }

  @Test
  public void paragraph_block_text_style() {
    Text text = Text.styled("Styled text", Style.empty().withFg(Color.GREEN));
    Paragraph paragraph = Paragraph.of(text).withBlock(Block.bordered());

    Buffer buf = Buffer.empty(new Rect(0, 0, 20, 3));
    paragraph.render(new Rect(0, 0, 20, 3), buf);

    Buffer expected =
        Buffer.withLines("┌──────────────────┐", "│Styled text       │", "└──────────────────┘");
    expected.setStyle(new Rect(1, 1, 11, 1), Style.empty().withFg(Color.GREEN));
    BufferAssertions.assertBufferEq(buf, expected);
  }

  static Stream<Arguments> outOfBoundsCases() {
    return Stream.of(
        Arguments.of(new Rect(0, 5, 15, 1)),
        Arguments.of(new Rect(20, 0, 15, 1)),
        Arguments.of(new Rect(20, 5, 15, 1)));
  }

  @ParameterizedTest
  @MethodSource("outOfBoundsCases")
  public void test_render_paragraph_out_of_bounds(Rect area) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Paragraph.of("Beyond the pale").render(area, buffer);
    BufferAssertions.assertBufferEq(
        buffer, Buffer.withLines("          ", "          ", "          "));
  }

  @Test
  public void partial_out_of_bounds() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 15, 3));
    Paragraph.of("Hello World").render(new Rect(10, 0, 10, 3), buffer);
    BufferAssertions.assertBufferEq(
        buffer, Buffer.withLines("          Hello", "               ", "               "));
  }

  @Test
  public void render_in_minimal_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 1, 1));
    Paragraph paragraph = Paragraph.of("Lorem ipsum");
    paragraph.render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("L"));
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 0));
    Paragraph paragraph = Paragraph.of("Lorem ipsum");
    paragraph.render(buffer.area, buffer);
  }
}
