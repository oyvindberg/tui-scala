package tui;

import java.util.Arrays;

/// A string composed of clusters of graphemes, each with their own style.
public record Spans(Span[] spans) {

  /// Returns the width of the underlying string.
  public int width() {
    int sum = 0;
    for (Span s : spans) {
      sum += s.width();
    }
    return sum;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Span s : spans) {
      sb.append(s);
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Spans s && Arrays.equals(spans, s.spans);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(spans);
  }

  public static Spans nostyle(String s) {
    return from(Span.nostyle(s));
  }

  public static Spans styled(String s, Style style) {
    return from(Span.styled(s, style));
  }

  public static Spans from(Span... spans) {
    return new Spans(spans);
  }

  /// Returns a new Spans whose every Span has its style patched with the given style.
  public Spans patchStyle(Style other) {
    Span[] out = new Span[spans.length];
    for (int i = 0; i < spans.length; i++) {
      out[i] = spans[i].patchStyle(other);
    }
    return new Spans(out);
  }

  /// Returns a new Spans whose every Span has its style reset to Style.RESET.
  public Spans resetStyle() {
    return patchStyle(Style.RESET);
  }
}
