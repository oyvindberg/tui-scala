package tui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// Primitives for styled text.
///
/// A terminal UI is at its root a lot of strings. In order to make it accessible and stylish, those strings may be associated to a set of styles. `tui` has
/// three ways to represent them:
///   - A single line string where all graphemes have the same style is represented by a `Span`.
///   - A single line string where each grapheme may have its own style is represented by `Spans`.
///   - A multiple line string where each grapheme may have its own style is represented by a `Text`.
public record Text(Spans[] lines) {

  /// Returns the max width of all the lines.
  public int width() {
    int max = 0;
    for (Spans line : lines) {
      int w = line.width();
      if (w > max) max = w;
    }
    return max;
  }

  /// Returns the height.
  public int height() {
    return lines.length;
  }

  /// Apply a new style to existing text.
  public Text overwrittenStyle(Style style) {
    Spans[] out = new Spans[lines.length];
    for (int i = 0; i < lines.length; i++) {
      Span[] inSpans = lines[i].spans();
      Span[] outSpans = new Span[inSpans.length];
      for (int j = 0; j < inSpans.length; j++) {
        Span sp = inSpans[j];
        outSpans[j] = new Span(sp.content(), sp.style().patch(style));
      }
      out[i] = new Spans(outSpans);
    }
    return new Text(out);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Text t && Arrays.equals(lines, t.lines);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(lines);
  }

  /// Create some text (potentially multiple lines) with no style.
  public static Text nostyle(String content) {
    if (content.isEmpty()) {
      return new Text(new Spans[] {Spans.nostyle("")});
    }
    List<Spans> result = new ArrayList<>();
    content.lines().forEach(line -> result.add(Spans.nostyle(line)));
    return new Text(result.toArray(new Spans[0]));
  }

  public static Text from(Span span) {
    return from(new Spans(new Span[] {span}));
  }

  public static Text fromSpans(Span... spans) {
    return from(new Spans(spans));
  }

  public static Text from(Spans spans) {
    return new Text(new Spans[] {spans});
  }

  public static Text fromMany(Spans... spans) {
    return new Text(spans);
  }

  /// Returns a new Text with every Span's style reset to Style.RESET.
  public Text resetStyle() {
    Spans[] out = new Spans[lines.length];
    for (int i = 0; i < lines.length; i++) {
      out[i] = lines[i].resetStyle();
    }
    return new Text(out);
  }
}
