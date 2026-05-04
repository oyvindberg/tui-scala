package jatatui.core.text;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A line of text consisting of one or more [Span]s.
///
/// [Line]s are used wherever text is displayed in the terminal and represent a single line of
/// text. When a [Line] is rendered, it is rendered as a single line of text with each [Span] being
/// rendered in order (left to right).
///
/// Any newlines in the content are removed when creating a [Line] from a string.
public final class Line implements Stylize<Line>, Iterable<Span> {

  /// The spans that make up this line of text.
  public final List<Span> spans;

  /// The style of this line of text.
  public final Style style;

  /// The alignment of this line of text. Optional: when empty, the rendering widget decides.
  public final Optional<HorizontalAlignment> alignment;

  Line(List<Span> spans, Style style, Optional<HorizontalAlignment> alignment) {
    this.spans = List.copyOf(spans);
    this.style = style;
    this.alignment = alignment;
  }

  // ---- Factory / constructor methods ----

  /// Creates an empty [Line] with no spans, the default style and no alignment.
  public static Line empty() {
    return new Line(List.of(), Style.empty(), Optional.empty());
  }

  /// Create a line with the default style.
  ///
  /// Newlines in the content split it into multiple [Span]s within the same line.
  public static Line raw(String content) {
    return new Line(splitToSpans(content), Style.empty(), Optional.empty());
  }

  /// Create a line with the given style.
  public static Line styled(String content, Style style) {
    return new Line(splitToSpans(content), style, Optional.empty());
  }

  // ---- Conversion methods ----

  /// Creates a [Line] from a string. Newlines in the content split it into multiple [Span]s.
  public static Line from(String content) {
    return raw(content);
  }

  /// Creates a [Line] from a single [Span] with the default style and no alignment.
  public static Line from(Span span) {
    List<Span> spans = new ArrayList<>(1);
    spans.add(span);
    return new Line(spans, Style.empty(), Optional.empty());
  }

  /// Creates a [Line] from a varargs of [Span]s with the default style and no alignment.
  public static Line from(Span... spans) {
    List<Span> list = new ArrayList<>(spans.length);
    for (Span s : spans) {
      list.add(s);
    }
    return new Line(list, Style.empty(), Optional.empty());
  }

  /// Creates a [Line] from a list of [Span]s with the default style and no alignment.
  public static Line fromSpans(List<Span> spans) {
    return new Line(new ArrayList<>(spans), Style.empty(), Optional.empty());
  }

  /// Creates a [Line] by mapping each item to a [Span] via [Span#raw(String)].
  public static Line fromIter(Iterable<String> items) {
    List<Span> list = new ArrayList<>();
    for (String s : items) {
      list.add(Span.raw(s));
    }
    return new Line(list, Style.empty(), Optional.empty());
  }

  /// Returns the underlying string content of this line (concatenation of every span's content).
  public String toContentString() {
    StringBuilder sb = new StringBuilder();
    for (Span s : spans) {
      sb.append(s.content);
    }
    return sb.toString();
  }

  // ---- Fluent setters ----

  /// Returns a copy with the spans replaced.
  public Line withSpans(List<Span> spans) {
    return new Line(new ArrayList<>(spans), style, alignment);
  }

  /// Returns a copy with the style replaced (use [#patchStyle(Style)] for layered patching).
  public Line withStyle(Style style) {
    return new Line(spans, style, alignment);
  }

  /// Returns a copy with the alignment set.
  public Line withAlignment(HorizontalAlignment alignment) {
    return new Line(spans, style, Optional.of(alignment));
  }

  /// Convenience shortcut for `withAlignment(HorizontalAlignment.Left)`.
  public Line leftAligned() {
    return withAlignment(HorizontalAlignment.Left);
  }

  /// Convenience shortcut for `withAlignment(HorizontalAlignment.Center)`.
  public Line centered() {
    return withAlignment(HorizontalAlignment.Center);
  }

  /// Convenience shortcut for `withAlignment(HorizontalAlignment.Right)`.
  public Line rightAligned() {
    return withAlignment(HorizontalAlignment.Right);
  }

  /// Returns a copy with the style patched (modifiers from the given style added).
  public Line patchStyle(Style style) {
    return new Line(spans, this.style.patch(style), alignment);
  }

  /// Returns a copy with the style reset to [Style#RESET].
  public Line resetStyle() {
    return patchStyle(Style.reset());
  }

  // ---- Mutation-like helpers (return new Line) ----

  /// Returns a new [Line] with `span` appended to the existing spans.
  public Line withPushedSpan(Span span) {
    List<Span> next = new ArrayList<>(spans.size() + 1);
    next.addAll(spans);
    next.add(span);
    return new Line(next, style, alignment);
  }

  /// Returns a new [Line] with `span` (created from the string) appended.
  public Line withPushedSpan(String span) {
    return withPushedSpan(Span.raw(span));
  }

  /// Returns a new [Line] with the given spans appended (mirrors Rust `Extend`).
  public Line extended(Iterable<Span> more) {
    List<Span> next = new ArrayList<>(spans);
    for (Span s : more) {
      next.add(s);
    }
    return new Line(next, style, alignment);
  }

  // ---- Width / graphemes ----

  /// Returns the unicode display width of this line (sum of every span's width).
  public int width() {
    int sum = 0;
    for (Span s : spans) {
      sum += s.width();
    }
    return sum;
  }

  /// Returns the graphemes held by this line. The patched style is `baseStyle` -> [#style] ->
  /// per-span style, and is then applied to each grapheme.
  public List<StyledGrapheme> styledGraphemes(Style baseStyle) {
    Style patched = baseStyle.patch(style);
    List<StyledGrapheme> out = new ArrayList<>();
    for (Span s : spans) {
      out.addAll(s.styledGraphemes(patched));
    }
    return out;
  }

  // ---- Operator-style helpers ----

  /// Returns a copy with `span` appended (mirrors Rust `Line + Span`).
  public Line plus(Span span) {
    return withPushedSpan(span);
  }

  /// Returns a [Text] with both lines (mirrors Rust `Line + Line`).
  public Text plus(Line other) {
    List<Line> lines = new ArrayList<>(2);
    lines.add(this);
    lines.add(other);
    return Text.fromLines(lines);
  }

  // ---- Iterable ----

  @Override
  public Iterator<Span> iterator() {
    return spans.iterator();
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Line setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Helpers ----

  private static List<Span> splitToSpans(String content) {
    List<Span> out = new ArrayList<>();
    if (content.isEmpty()) {
      // Mirror Rust `str::lines()` returning an empty iterator for the empty string. The default
      // [Line] then has zero spans (callers can build a single empty span explicitly if needed).
      return out;
    }
    // Mirror Rust `str::lines()`: split on \n, also strip a trailing \r before the \n. The final
    // empty line after a trailing newline is dropped.
    int len = content.length();
    int i = 0;
    while (i < len) {
      int j = content.indexOf('\n', i);
      int end = (j == -1) ? len : j;
      // Strip trailing \r before \n.
      int spanEnd = (end > i && content.charAt(end - 1) == '\r') ? end - 1 : end;
      out.add(Span.raw(content.substring(i, spanEnd)));
      if (j == -1) break;
      i = j + 1;
    }
    return out;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Line other)) return false;
    return spans.equals(other.spans)
        && style.equals(other.style)
        && alignment.equals(other.alignment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spans, style, alignment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Span s : spans) {
      sb.append(s);
    }
    return sb.toString();
  }
}
