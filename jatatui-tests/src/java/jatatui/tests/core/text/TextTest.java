package jatatui.tests.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Ports of the inline tests from `ratatui-core/src/text/text.rs`.
///
/// **Skipped**: every test in the upstream `mod widget` (rendering to a `Buffer`) — buffer is not
/// ported. Display / Debug formatting tests are skipped where they assert on Rust-specific
/// `{:#?}`-style output that has no Java analogue.
public class TextTest {

  @Test
  public void raw() {
    Text text = Text.raw("The first line\nThe second line");
    assertEquals(List.of(Line.from("The first line"), Line.from("The second line")), text.lines);
  }

  @Test
  public void styled() {
    Style style = Style.empty().yellow().italic();
    Text styledText = Text.styled("The first line\nThe second line", style);

    Text text = Text.raw("The first line\nThe second line").withStyle(style);

    assertEquals(text, styledText);
  }

  @Test
  public void width() {
    Text text = Text.from("The first line\nThe second line");
    assertEquals(15, text.width());
  }

  @Test
  public void height() {
    Text text = Text.from("The first line\nThe second line");
    assertEquals(2, text.height());
  }

  @Test
  public void patch_style() {
    Style style = Style.empty().yellow().italic();
    Style style2 = Style.empty().red().underlined();
    Text text = Text.styled("The first line\nThe second line", style).patchStyle(style2);

    Style expected = Style.empty().red().italic().underlined();
    Text expectedText = Text.styled("The first line\nThe second line", expected);

    assertEquals(expectedText, text);
  }

  @Test
  public void reset_style() {
    Style style = Style.empty().yellow().italic();
    Text text = Text.styled("The first line\nThe second line", style).resetStyle();

    assertEquals(Style.reset(), text.style);
  }

  @Test
  public void from_string() {
    Text text = Text.from("The first line\nThe second line");
    assertEquals(List.of(Line.from("The first line"), Line.from("The second line")), text.lines);
  }

  @Test
  public void from_str() {
    Text text = Text.from("The first line\nThe second line");
    assertEquals(List.of(Line.from("The first line"), Line.from("The second line")), text.lines);
  }

  @Test
  public void from_span() {
    Style style = Style.empty().yellow().italic();
    Text text = Text.from(Span.styled("The first line\nThe second line", style));
    assertEquals(
        List.of(Line.from(Span.styled("The first line\nThe second line", style))), text.lines);
  }

  @Test
  public void from_line() {
    Text text = Text.from(Line.from("The first line"));
    assertEquals(List.of(Line.from("The first line")), text.lines);
  }

  @Test
  public void from_vec_line() {
    Text text = Text.fromLines(List.of(Line.from("The first line"), Line.from("The second line")));
    assertEquals(List.of(Line.from("The first line"), Line.from("The second line")), text.lines);
  }

  @Test
  public void from_iterator() {
    Text text = Text.fromIter(List.of("The first line", "The second line"));
    assertEquals(List.of(Line.from("The first line"), Line.from("The second line")), text.lines);
  }

  @Test
  public void into_iter() {
    Text text = Text.from("The first line\nThe second line");
    Iterator<Line> iter = text.iterator();
    assertEquals(Line.from("The first line"), iter.next());
    assertEquals(Line.from("The second line"), iter.next());
    assertFalse(iter.hasNext());
  }

  @Test
  public void add_line() {
    Text actual = Text.raw("Red").red().plus(Line.raw("Blue").blue());
    Text expected = Text.fromLines(List.of(Line.raw("Red"), Line.raw("Blue").blue())).red();
    assertEquals(expected, actual);
  }

  @Test
  public void add_text() {
    Text actual = Text.raw("Red").red().plus(Text.raw("Blue").blue());
    Text expected = Text.fromLines(List.of(Line.raw("Red"), Line.raw("Blue"))).red();
    assertEquals(expected, actual);
  }

  @Test
  public void extend() {
    Text text =
        Text.from("The first line\nThe second line")
            .extended(List.of(Line.from("The third line"), Line.from("The fourth line")));
    assertEquals(
        List.of(
            Line.from("The first line"),
            Line.from("The second line"),
            Line.from("The third line"),
            Line.from("The fourth line")),
        text.lines);
  }

  @Test
  public void extend_from_iter_str() {
    Text text =
        Text.from("The first line\nThe second line")
            .extendedStrings(List.of("The third line", "The fourth line"));
    assertEquals(
        List.of(
            Line.from("The first line"),
            Line.from("The second line"),
            Line.from("The third line"),
            Line.from("The fourth line")),
        text.lines);
  }

  @Test
  public void stylize() {
    assertEquals(Style.empty().withFg(Color.GREEN), Text.empty().green().style);
    assertEquals(Style.empty().withBg(Color.GREEN), Text.empty().onGreen().style);
    assertEquals(Style.empty().withAddModifier(Modifier.ITALIC), Text.empty().italic().style);
  }

  @Test
  public void left_aligned() {
    Text text = Text.from("Hello, world!").leftAligned();
    assertEquals(Optional.of(HorizontalAlignment.Left), text.alignment);
  }

  @Test
  public void centered() {
    Text text = Text.from("Hello, world!").centered();
    assertEquals(Optional.of(HorizontalAlignment.Center), text.alignment);
  }

  @Test
  public void right_aligned() {
    Text text = Text.from("Hello, world!").rightAligned();
    assertEquals(Optional.of(HorizontalAlignment.Right), text.alignment);
  }

  @Test
  public void push_line() {
    Text text =
        Text.from("A")
            .withPushedLine(Line.from("B"))
            .withPushedLine(Span.from("C"))
            .withPushedLine("D");
    assertEquals(List.of(Line.raw("A"), Line.raw("B"), Line.raw("C"), Line.raw("D")), text.lines);
  }

  @Test
  public void push_line_empty() {
    Text text = Text.empty().withPushedLine(Line.from("Hello, world!"));
    assertEquals(List.of(Line.from("Hello, world!")), text.lines);
  }

  @Test
  public void push_span() {
    Text text = Text.from("A").withPushedSpan(Span.raw("B")).withPushedSpan("C");
    assertEquals(
        List.of(Line.fromSpans(List.of(Span.raw("A"), Span.raw("B"), Span.raw("C")))), text.lines);
  }

  @Test
  public void push_span_empty() {
    Text text = Text.empty().withPushedSpan(Span.raw("Hello, world!"));
    // empty Text has no lines, so pushSpan creates a new line. Note: Text.empty()'s lines is
    // []; the new line will contain only that span.
    assertEquals(List.of(Line.from(Span.raw("Hello, world!"))), text.lines);
  }
}
