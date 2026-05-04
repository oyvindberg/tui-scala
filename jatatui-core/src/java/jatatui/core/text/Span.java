package jatatui.core.text;

import jatatui.core.internal.Wcwidth;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/// Represents a part of a [Line] that is contiguous and where all characters share the same
/// [Style].
///
/// A [Span] is the smallest unit of text that can be styled. It is usually combined in the [Line]
/// type to represent a line of text where each [Span] may have a different style.
///
/// # Constructor methods
///
/// - [#empty()] creates a span with empty content and the default style.
/// - [#raw(String)] creates a span with the specified content and the default style.
/// - [#styled(String, Style)] creates a span with the specified content and style.
///
/// # Setter methods
///
/// These methods are fluent setters. They return a new [Span] with the specified property set.
///
/// - [#withContent(String)] sets the content of the span.
/// - [#withStyle(Style)] sets the style of the span.
///
/// # Other methods
///
/// - [#patchStyle(Style)] patches the style of the span, adding modifiers from the given style.
/// - [#resetStyle()] resets the style of the span.
/// - [#width()] returns the unicode width of the content held by this span.
/// - [#styledGraphemes(Style)] returns a [List] of the graphemes held by this span.
public final class Span implements Stylize<Span> {

  /// The content of the span.
  public final String content;

  /// The style of the span.
  public final Style style;

  Span(String content, Style style) {
    this.content = content;
    this.style = style;
  }

  // ---- Constructor / factory methods ----

  /// Creates an empty [Span] with no content and the default style.
  public static Span empty() {
    return new Span("", Style.empty());
  }

  /// Create a span with the default style.
  public static Span raw(String content) {
    return new Span(content, Style.empty());
  }

  /// Create a span with the specified style.
  public static Span styled(String content, Style style) {
    return new Span(content, style);
  }

  /// Creates a [Span] from the given string with the default style.
  public static Span from(String content) {
    return raw(content);
  }

  // ---- Fluent setters ----

  /// Returns a copy with the given content set.
  public Span withContent(String content) {
    return new Span(content, style);
  }

  /// Returns a copy with the given style set, replacing the current style.
  public Span withStyle(Style style) {
    return new Span(content, style);
  }

  /// Returns a copy with the style patched (modifiers from the given style added).
  public Span patchStyle(Style style) {
    return new Span(content, this.style.patch(style));
  }

  /// Returns a copy with the style reset (equivalent to [#patchStyle(Style)] with [Style#RESET]).
  public Span resetStyle() {
    return patchStyle(Style.reset());
  }

  // ---- Width / graphemes ----

  /// Returns the unicode display width of the content held by this span.
  public int width() {
    return Wcwidth.width(content);
  }

  /// Returns the graphemes held by this span as a list of [StyledGrapheme]s.
  ///
  /// `baseStyle` is the [Style] that will be patched with the [Span]'s [#style] to get the
  /// resulting style. Graphemes that contain a control character are filtered out.
  public List<StyledGrapheme> styledGraphemes(Style baseStyle) {
    Style merged = baseStyle.patch(style);
    List<StyledGrapheme> out = new ArrayList<>();
    BreakIterator boundary = BreakIterator.getCharacterInstance(Locale.getDefault());
    boundary.setText(content);
    int start = boundary.first();
    int end = boundary.next();
    while (end != BreakIterator.DONE) {
      String chunk = content.substring(start, end);
      if (!containsControl(chunk)) {
        out.add(new StyledGrapheme(chunk, merged));
      }
      start = end;
      end = boundary.next();
    }
    return out;
  }

  private static boolean containsControl(String s) {
    final int len = s.length();
    int i = 0;
    while (i < len) {
      int cp = s.codePointAt(i);
      // Mirrors Rust `char::is_control`: Unicode general category "Cc" (control).
      // In ASCII: 0x00-0x1F and 0x7F. Plus the C1 control range 0x80-0x9F.
      int type = Character.getType(cp);
      if (type == Character.CONTROL) return true;
      i += Character.charCount(cp);
    }
    return false;
  }

  // ---- Conversions to Line ----

  /// Converts this [Span] into a left-aligned [Line].
  public Line intoLeftAlignedLine() {
    return Line.from(this).leftAligned();
  }

  /// Converts this [Span] into a center-aligned [Line].
  public Line intoCenteredLine() {
    return Line.from(this).centered();
  }

  /// Converts this [Span] into a right-aligned [Line].
  public Line intoRightAlignedLine() {
    return Line.from(this).rightAligned();
  }

  // ---- Operator-style helpers ----

  /// Returns a [Line] consisting of this span followed by `other`.
  public Line plus(Span other) {
    List<Span> spans = new ArrayList<>(2);
    spans.add(this);
    spans.add(other);
    return Line.fromSpans(spans);
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Span setStyle(Style style) {
    return withStyle(style);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Span other)) return false;
    return content.equals(other.content) && style.equals(other.style);
  }

  @Override
  public int hashCode() {
    return 31 * content.hashCode() + style.hashCode();
  }

  @Override
  public String toString() {
    // Mirrors Rust `Display` impl: writes each line of `content` without separators (so newlines
    // are dropped). Useful only as a debug aid in Java, but matches upstream behavior for tests.
    StringBuilder sb = new StringBuilder(content.length());
    for (String line : content.split("\n", -1)) {
      sb.append(line);
    }
    return sb.toString();
  }
}
