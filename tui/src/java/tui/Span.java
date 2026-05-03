package tui;

import java.util.ArrayList;
import java.util.List;
import tui.internal.UnicodeSegmentation;

/// string where all graphemes have the same style.
public record Span(String content, Style style) {

  /// Returns the width of the content held by this span. This calculates the visual display width, not the string length.
  public int width() {
    int sum = 0;
    for (Grapheme g : UnicodeSegmentation.graphemes(content, true)) {
      sum += g.width();
    }
    return sum;
  }

  /// Returns an iterator over the graphemes held by this span.
  ///
  /// @param baseStyle the `Style` that will be patched with each grapheme `Style` to get the resulting `Style`.
  public StyledGrapheme[] styledGraphemes(Style baseStyle) {
    Grapheme[] graphemes = UnicodeSegmentation.graphemes(content, true);
    List<StyledGrapheme> out = new ArrayList<>(graphemes.length);
    for (Grapheme g : graphemes) {
      if (!g.str.equals("\n")) {
        out.add(new StyledGrapheme(g, baseStyle.patch(style)));
      }
    }
    return out.toArray(new StyledGrapheme[0]);
  }

  /// Create a span with no style.
  public static Span nostyle(String content) {
    return new Span(content, Style.empty());
  }

  /// Create a span with a style.
  public static Span styled(String content, Style style) {
    return new Span(content, style);
  }

  /// Returns a new Span whose style is patched with the given style.
  public Span patchStyle(Style other) {
    return new Span(content, style.patch(other));
  }

  /// Returns a new Span with the style reset to Style.RESET.
  public Span resetStyle() {
    return patchStyle(Style.RESET);
  }
}
