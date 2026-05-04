package jatatui.core.text;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A string split over one or more [Line]s.
///
/// [Text] is used wherever text is displayed in the terminal and represents one or more [Line]s of
/// text. When a [Text] is rendered, each line is rendered as a single line of text from top to
/// bottom of the area. The text can be styled and aligned.
public final class Text implements Stylize<Text>, Iterable<Line> {

  /// The lines that make up this piece of text.
  public final List<Line> lines;

  /// The style of this text. Applied before each line's style during rendering.
  public final Style style;

  /// The alignment of this text. Optional: when empty, the rendering widget decides.
  public final Optional<HorizontalAlignment> alignment;

  Text(List<Line> lines, Style style, Optional<HorizontalAlignment> alignment) {
    this.lines = List.copyOf(lines);
    this.style = style;
    this.alignment = alignment;
  }

  // ---- Constructor / factory methods ----

  /// Creates an empty [Text] with no lines, the default style and no alignment.
  public static Text empty() {
    return new Text(List.of(), Style.empty(), Optional.empty());
  }

  /// Create some text (potentially multiple lines) with no style.
  public static Text raw(String content) {
    return new Text(splitToLines(content), Style.empty(), Optional.empty());
  }

  /// Create some text (potentially multiple lines) with a style.
  public static Text styled(String content, Style style) {
    return raw(content).patchStyle(style);
  }

  // ---- Conversion methods ----

  /// Creates a [Text] from a string. Identical to [#raw(String)].
  public static Text from(String content) {
    return raw(content);
  }

  /// Creates a [Text] from a single [Span] (one line containing only that span).
  public static Text from(Span span) {
    List<Line> lines = new ArrayList<>(1);
    lines.add(Line.from(span));
    return new Text(lines, Style.empty(), Optional.empty());
  }

  /// Creates a [Text] from a single [Line].
  public static Text from(Line line) {
    List<Line> lines = new ArrayList<>(1);
    lines.add(line);
    return new Text(lines, Style.empty(), Optional.empty());
  }

  /// Creates a [Text] from a varargs of [Line]s.
  public static Text from(Line... lines) {
    List<Line> list = new ArrayList<>(lines.length);
    for (Line l : lines) {
      list.add(l);
    }
    return new Text(list, Style.empty(), Optional.empty());
  }

  /// Creates a [Text] from a list of [Line]s.
  public static Text fromLines(List<Line> lines) {
    return new Text(new ArrayList<>(lines), Style.empty(), Optional.empty());
  }

  /// Creates a [Text] by mapping each item to a [Line] via [Line#raw(String)].
  public static Text fromIter(Iterable<String> items) {
    List<Line> list = new ArrayList<>();
    for (String s : items) {
      list.add(Line.raw(s));
    }
    return new Text(list, Style.empty(), Optional.empty());
  }

  /// Creates a [Text] from a [Masked] string by masking the underlying value.
  public static Text from(Masked masked) {
    return raw(masked.value());
  }

  // ---- Width / height ----

  /// Returns the maximum width across all lines.
  public int width() {
    int max = 0;
    for (Line l : lines) {
      int w = l.width();
      if (w > max) max = w;
    }
    return max;
  }

  /// Returns the height (number of lines).
  public int height() {
    return lines.size();
  }

  // ---- Fluent setters ----

  /// Returns a copy with the style replaced.
  public Text withStyle(Style style) {
    return new Text(lines, style, alignment);
  }

  /// Returns a copy with the style patched (modifiers from the given style added).
  public Text patchStyle(Style style) {
    return new Text(lines, this.style.patch(style), alignment);
  }

  /// Returns a copy with the style reset to [Style#RESET].
  public Text resetStyle() {
    return patchStyle(Style.reset());
  }

  /// Returns a copy with the alignment set.
  public Text withAlignment(HorizontalAlignment alignment) {
    return new Text(lines, style, Optional.of(alignment));
  }

  /// Convenience shortcut for `withAlignment(HorizontalAlignment.Left)`.
  public Text leftAligned() {
    return withAlignment(HorizontalAlignment.Left);
  }

  /// Convenience shortcut for `withAlignment(HorizontalAlignment.Center)`.
  public Text centered() {
    return withAlignment(HorizontalAlignment.Center);
  }

  /// Convenience shortcut for `withAlignment(HorizontalAlignment.Right)`.
  public Text rightAligned() {
    return withAlignment(HorizontalAlignment.Right);
  }

  // ---- Mutation-like helpers (return new Text) ----

  /// Returns a new [Text] with `line` appended.
  public Text withPushedLine(Line line) {
    List<Line> next = new ArrayList<>(lines.size() + 1);
    next.addAll(lines);
    next.add(line);
    return new Text(next, style, alignment);
  }

  /// Returns a new [Text] with a single-span [Line] (built from the given span) appended.
  public Text withPushedLine(Span span) {
    return withPushedLine(Line.from(span));
  }

  /// Returns a new [Text] with a single-span [Line] (built from the given string) appended.
  public Text withPushedLine(String s) {
    return withPushedLine(Line.raw(s));
  }

  /// Returns a new [Text] with `span` appended to the last line. If [#lines] is empty, a new line
  /// is created containing only that span.
  public Text withPushedSpan(Span span) {
    List<Line> next = new ArrayList<>(lines);
    if (next.isEmpty()) {
      next.add(Line.from(span));
    } else {
      Line last = next.remove(next.size() - 1);
      next.add(last.withPushedSpan(span));
    }
    return new Text(next, style, alignment);
  }

  /// Returns a new [Text] with a span (created from the string) appended to the last line.
  public Text withPushedSpan(String s) {
    return withPushedSpan(Span.raw(s));
  }

  /// Returns a new [Text] with the given lines appended (mirrors Rust `Extend`).
  public Text extended(Iterable<Line> more) {
    List<Line> next = new ArrayList<>(lines);
    for (Line l : more) {
      next.add(l);
    }
    return new Text(next, style, alignment);
  }

  /// Returns a new [Text] with each string converted to a [Line] and appended.
  public Text extendedStrings(Iterable<String> more) {
    List<Line> next = new ArrayList<>(lines);
    for (String s : more) {
      next.add(Line.raw(s));
    }
    return new Text(next, style, alignment);
  }

  // ---- Operator-style helpers ----

  /// Returns a copy with `line` appended.
  public Text plus(Line line) {
    return withPushedLine(line);
  }

  /// Returns a copy with the lines from `other` appended (style/alignment of `other` are ignored).
  public Text plus(Text other) {
    return extended(other.lines);
  }

  // ---- Iterable ----

  @Override
  public Iterator<Line> iterator() {
    return lines.iterator();
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Text setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Helpers ----

  private static List<Line> splitToLines(String content) {
    List<Line> out = new ArrayList<>();
    if (content.isEmpty()) {
      // Mirror Rust: empty content yields a single empty line.
      out.add(Line.from(""));
      return out;
    }
    int len = content.length();
    int i = 0;
    while (i < len) {
      int j = content.indexOf('\n', i);
      int end = (j == -1) ? len : j;
      // Strip trailing \r before \n.
      int lineEnd = (end > i && content.charAt(end - 1) == '\r') ? end - 1 : end;
      out.add(Line.from(content.substring(i, lineEnd)));
      if (j == -1) break;
      i = j + 1;
    }
    return out;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Text other)) return false;
    return lines.equals(other.lines)
        && style.equals(other.style)
        && alignment.equals(other.alignment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lines, style, alignment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lines.size(); i++) {
      if (i > 0) sb.append('\n');
      sb.append(lines.get(i));
    }
    return sb.toString();
  }
}
