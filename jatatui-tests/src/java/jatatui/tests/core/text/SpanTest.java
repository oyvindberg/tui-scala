package jatatui.tests.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Ports of the inline tests from `ratatui-core/src/text/span.rs`.
///
/// **Skipped**: every test in the upstream `mod widget` (rendering to a `Buffer`) — the buffer
/// type has not been ported yet. Same for the `Display` / Debug formatting tests where Java has
/// no equivalent of Rust's `{:.4}` truncation specifiers, and the `Cow`-distinguishing
/// `from_*_cow` cases (Java has no Cow, all strings collapse to plain `String`).
public class SpanTest {

  @Test
  public void default_() {
    Span span = Span.empty();
    assertEquals("", span.content);
    assertEquals(Style.empty(), span.style);
  }

  @Test
  public void raw_str() {
    Span span = Span.raw("test content");
    assertEquals("test content", span.content);
    assertEquals(Style.empty(), span.style);
  }

  @Test
  public void raw_string() {
    String content = "test content";
    Span span = Span.raw(content);
    assertEquals(content, span.content);
    assertEquals(Style.empty(), span.style);
  }

  @Test
  public void styled_str() {
    Style style = Style.empty().red();
    Span span = Span.styled("test content", style);
    assertEquals("test content", span.content);
    assertEquals(Style.empty().red(), span.style);
  }

  @Test
  public void styled_string() {
    String content = "test content";
    Style style = Style.empty().green();
    Span span = Span.styled(content, style);
    assertEquals(content, span.content);
    assertEquals(style, span.style);
  }

  @Test
  public void set_content() {
    Span span = Span.empty().withContent("test content");
    assertEquals("test content", span.content);
  }

  @Test
  public void set_style() {
    Span span = Span.empty().withStyle(Style.empty().green());
    assertEquals(Style.empty().green(), span.style);
  }

  @Test
  public void from_str() {
    String content = "test content";
    Span span = Span.from(content);
    assertEquals(content, span.content);
    assertEquals(Style.empty(), span.style);
  }

  @Test
  public void reset_style() {
    Span span = Span.styled("test content", Style.empty().green()).resetStyle();
    assertEquals(Style.reset(), span.style);
  }

  @Test
  public void patch_style() {
    Span span =
        Span.styled("test content", Style.empty().green().onYellow())
            .patchStyle(Style.empty().red().bold());
    assertEquals(Style.empty().red().onYellow().bold(), span.style);
  }

  @Test
  public void width() {
    assertEquals(0, Span.raw("").width());
    assertEquals(4, Span.raw("test").width());
    assertEquals(12, Span.raw("test content").width());
    // Upstream Rust asserts this is 12 with a comment referencing
    // <https://github.com/ratatui/ratatui/issues/1271> ("Needs reconsideration"). Java's
    // Wcwidth.of() returns -1 for control chars (newline included), and our Span.width clamps
    // those to 0, so the answer here is 11. This is the more-correct behavior; the upstream
    // value was already known-quirky. Documented Java deviation.
    assertEquals(11, Span.raw("test\ncontent").width());
  }

  @Test
  public void stylize() {
    Span span = Span.raw("test content").green();
    assertEquals("test content", span.content);
    assertEquals(Style.empty().green(), span.style);

    Span styled = Span.styled("test content", Style.empty().green()).onYellow().bold();
    assertEquals("test content", styled.content);
    assertEquals(Style.empty().green().onYellow().bold(), styled.style);
  }

  @Test
  public void left_aligned() {
    Span span = Span.styled("Test Content", Style.empty().green().italic());
    Line line = span.intoLeftAlignedLine();
    assertEquals(Optional.of(HorizontalAlignment.Left), line.alignment);
  }

  @Test
  public void centered() {
    Span span = Span.styled("Test Content", Style.empty().green().italic());
    Line line = span.intoCenteredLine();
    assertEquals(Optional.of(HorizontalAlignment.Center), line.alignment);
  }

  @Test
  public void right_aligned() {
    Span span = Span.styled("Test Content", Style.empty().green().italic());
    Line line = span.intoRightAlignedLine();
    assertEquals(Optional.of(HorizontalAlignment.Right), line.alignment);
  }

  @Test
  public void add() {
    assertEquals(
        Line.fromSpans(List.of(Span.empty(), Span.empty())), Span.empty().plus(Span.empty()));

    assertEquals(
        Line.fromSpans(List.of(Span.empty(), Span.raw("test"))),
        Span.empty().plus(Span.raw("test")));

    assertEquals(
        Line.fromSpans(List.of(Span.raw("test"), Span.empty())),
        Span.raw("test").plus(Span.empty()));

    assertEquals(
        Line.fromSpans(List.of(Span.raw("test"), Span.raw("content"))),
        Span.raw("test").plus(Span.raw("content")));
  }
}
