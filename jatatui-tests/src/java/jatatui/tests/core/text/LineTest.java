package jatatui.tests.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.StyledGrapheme;
import jatatui.core.text.Text;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Ports of the inline tests from `ratatui-core/src/text/line.rs`.
///
/// **Skipped**: every test in the upstream `mod widget` (rendering to a `Buffer`) — buffer is not
/// ported. The Rust `to_line()` test relies on a `Display` blanket impl over arbitrary types
/// (e.g. `42.to_line()`) which has no Java equivalent. Debug-format / `iter_mut` tests are
/// skipped because Java has no `iter_mut` (the immutable-record pattern in the port forbids it).
public class LineTest {

  @Test
  public void raw_str() {
    Line line = Line.raw("test content");
    assertEquals(List.of(Span.raw("test content")), line.spans);
    assertEquals(Optional.empty(), line.alignment);

    Line splitLine = Line.raw("a\nb");
    assertEquals(List.of(Span.raw("a"), Span.raw("b")), splitLine.spans);
    assertEquals(Optional.empty(), splitLine.alignment);
  }

  @Test
  public void styled_str() {
    Style style = Style.empty().yellow();
    String content = "Hello, world!";
    Line line = Line.styled(content, style);
    assertEquals(List.of(Span.raw(content)), line.spans);
    assertEquals(style, line.style);
  }

  @Test
  public void styled_string() {
    Style style = Style.empty().yellow();
    String content = "Hello, world!";
    Line line = Line.styled(content, style);
    assertEquals(List.of(Span.raw(content)), line.spans);
    assertEquals(style, line.style);
  }

  @Test
  public void spans_vec() {
    Line line =
        Line.empty()
            .withSpans(
                List.of(
                    Span.styled("Hello", Style.empty().blue()),
                    Span.styled(" world!", Style.empty().green())));
    assertEquals(
        List.of(
            Span.styled("Hello", Style.empty().blue()),
            Span.styled(" world!", Style.empty().green())),
        line.spans);
  }

  @Test
  public void style() {
    Line line = Line.empty().withStyle(Style.empty().red());
    assertEquals(Style.empty().red(), line.style);
  }

  @Test
  public void alignment() {
    Line line = Line.from("This is left").withAlignment(HorizontalAlignment.Left);
    assertEquals(Optional.of(HorizontalAlignment.Left), line.alignment);

    Line def = Line.from("This is default");
    assertEquals(Optional.empty(), def.alignment);
  }

  @Test
  public void width() {
    Line line =
        Line.from(
            new Span[] {
              Span.styled("My", Style.empty().withFg(Color.YELLOW)), Span.raw(" text"),
            });
    assertEquals(7, line.width());

    Line empty = Line.empty();
    assertEquals(0, empty.width());
  }

  @Test
  public void patch_style() {
    Line raw = Line.styled("foobar", Style.empty().withFg(Color.YELLOW));
    Line styled =
        Line.styled(
            "foobar", Style.empty().withFg(Color.YELLOW).withAddModifier(Modifier.ITALIC));

    assertNotEquals(raw, styled);

    Line patched = raw.patchStyle(Style.fromModifier(Modifier.ITALIC));
    assertEquals(styled, patched);
  }

  @Test
  public void reset_style() {
    Line line =
        Line.styled("foobar", Style.empty().yellow().onRed().italic()).resetStyle();
    assertEquals(Style.reset(), line.style);
  }

  @Test
  public void stylize() {
    assertEquals(Style.empty().withFg(Color.GREEN), Line.empty().green().style);
    assertEquals(Style.empty().withBg(Color.GREEN), Line.empty().onGreen().style);
    assertEquals(Style.empty().withAddModifier(Modifier.ITALIC), Line.empty().italic().style);
  }

  @Test
  public void from_string() {
    Line line = Line.from("Hello, world!");
    assertEquals(List.of(Span.from("Hello, world!")), line.spans);

    Line splitLine = Line.from("Hello\nworld!");
    assertEquals(List.of(Span.from("Hello"), Span.from("world!")), splitLine.spans);
  }

  @Test
  public void from_str() {
    Line line = Line.from("Hello, world!");
    assertEquals(List.of(Span.from("Hello, world!")), line.spans);

    Line splitLine = Line.from("Hello\nworld!");
    assertEquals(List.of(Span.from("Hello"), Span.from("world!")), splitLine.spans);
  }

  @Test
  public void from_vec() {
    List<Span> spans =
        List.of(
            Span.styled("Hello,", Style.empty().withFg(Color.RED)),
            Span.styled(" world!", Style.empty().withFg(Color.GREEN)));
    Line line = Line.fromSpans(spans);
    assertEquals(spans, line.spans);
  }

  @Test
  public void from_iter() {
    Line line = Line.fromIter(List.of("Hello", " world!"));
    assertEquals(List.of(Span.raw("Hello"), Span.raw(" world!")), line.spans);
  }

  @Test
  public void from_span() {
    Span span = Span.styled("Hello, world!", Style.empty().withFg(Color.YELLOW));
    Line line = Line.from(span);
    assertEquals(List.of(span), line.spans);
  }

  @Test
  public void add_span() {
    Line actual = Line.raw("Red").red().plus(Span.raw("blue").blue());
    Line expected = Line.fromSpans(List.of(Span.raw("Red"), Span.raw("blue").blue())).red();
    assertEquals(expected, actual);
  }

  @Test
  public void add_line() {
    Text actual = Line.raw("Red").red().plus(Line.raw("Blue").blue());
    Text expected = Text.fromLines(List.of(Line.raw("Red").red(), Line.raw("Blue").blue()));
    assertEquals(expected, actual);
  }

  @Test
  public void extend() {
    Line line = Line.from("Hello, ").extended(List.of(Span.raw("world!")));
    assertEquals(List.of(Span.raw("Hello, "), Span.raw("world!")), line.spans);

    Line line2 =
        Line.from("Hello, ").extended(List.of(Span.raw("world! "), Span.raw("How are you?")));
    assertEquals(
        List.of(Span.raw("Hello, "), Span.raw("world! "), Span.raw("How are you?")), line2.spans);
  }

  @Test
  public void into_string() {
    Line line =
        Line.fromSpans(
            List.of(
                Span.styled("Hello,", Style.empty().withFg(Color.RED)),
                Span.styled(" world!", Style.empty().withFg(Color.GREEN))));
    assertEquals("Hello, world!", line.toContentString());
  }

  @Test
  public void styled_graphemes() {
    Style RED = Style.empty().red();
    Style GREEN = Style.empty().green();
    Style BLUE = Style.empty().blue();
    Style RED_ON_WHITE = Style.empty().red().onWhite();
    Style GREEN_ON_WHITE = Style.empty().green().onWhite();
    Style BLUE_ON_WHITE = Style.empty().blue().onWhite();

    Line line =
        Line.fromSpans(
            List.of(Span.styled("He", RED), Span.styled("ll", GREEN), Span.styled("o!", BLUE)));
    List<StyledGrapheme> graphemes = line.styledGraphemes(Style.empty().withBg(Color.WHITE));
    assertEquals(
        List.of(
            new StyledGrapheme("H", RED_ON_WHITE),
            new StyledGrapheme("e", RED_ON_WHITE),
            new StyledGrapheme("l", GREEN_ON_WHITE),
            new StyledGrapheme("l", GREEN_ON_WHITE),
            new StyledGrapheme("o", BLUE_ON_WHITE),
            new StyledGrapheme("!", BLUE_ON_WHITE)),
        graphemes);
  }

  @Test
  public void left_aligned() {
    Line line = Line.from("Hello, world!").leftAligned();
    assertEquals(Optional.of(HorizontalAlignment.Left), line.alignment);
  }

  @Test
  public void centered() {
    Line line = Line.from("Hello, world!").centered();
    assertEquals(Optional.of(HorizontalAlignment.Center), line.alignment);
  }

  @Test
  public void right_aligned() {
    Line line = Line.from("Hello, world!").rightAligned();
    assertEquals(Optional.of(HorizontalAlignment.Right), line.alignment);
  }

  @Test
  public void push_span() {
    Line line = Line.from("A").withPushedSpan(Span.raw("B")).withPushedSpan("C");
    assertEquals(List.of(Span.raw("A"), Span.raw("B"), Span.raw("C")), line.spans);
  }

  // ---- iterators ----

  @Test
  public void iter() {
    Style blueFg = Style.fromFg(Color.BLUE);
    Style greenFg = Style.fromFg(Color.GREEN);
    Line helloWorld =
        Line.fromSpans(
            List.of(Span.styled("Hello ", blueFg), Span.styled("world!", greenFg)));
    Iterator<Span> iter = helloWorld.iterator();
    assertEquals(Span.styled("Hello ", blueFg), iter.next());
    assertEquals(Span.styled("world!", greenFg), iter.next());
    org.junit.jupiter.api.Assertions.assertFalse(iter.hasNext());
  }

  @Test
  public void for_loop_ref() {
    Style blueFg = Style.fromFg(Color.BLUE);
    Style greenFg = Style.fromFg(Color.GREEN);
    Line helloWorld =
        Line.fromSpans(
            List.of(Span.styled("Hello ", blueFg), Span.styled("world!", greenFg)));
    StringBuilder result = new StringBuilder();
    for (Span span : helloWorld) {
      result.append(span.content);
    }
    assertEquals("Hello world!", result.toString());
  }
}
