package jatatui.core.text;

import jatatui.core.style.Style;
import jatatui.core.style.Stylize;

/// A grapheme associated with a [Style].
///
/// `StyledGrapheme` is the smallest divisible unit of text. Despite that, it is not a member of
/// the text type hierarchy ([Text] -> [Line] -> [Span]). It is a separate type used mostly for
/// rendering purposes. A [Span] consists of components that can be split into `StyledGrapheme`s,
/// but it does not contain a collection of `StyledGrapheme`s.
public final class StyledGrapheme implements Stylize<StyledGrapheme> {
  private static final String NBSP = " ";
  private static final String ZWSP = "​";

  /// The grapheme cluster (a string because it can be more than one code point).
  public final String symbol;

  /// The style of the grapheme.
  public final Style style;

  /// Creates a new `StyledGrapheme` with the given symbol and style.
  public StyledGrapheme(String symbol, Style style) {
    this.symbol = symbol;
    this.style = style;
  }

  /// Static factory mirroring the upstream `StyledGrapheme::new` constructor.
  public static StyledGrapheme of(String symbol, Style style) {
    return new StyledGrapheme(symbol, style);
  }

  /// Returns true iff the symbol is exclusively whitespace (ZWSP counts; NBSP does not).
  public boolean isWhitespace() {
    if (symbol.equals(ZWSP)) return true;
    if (symbol.equals(NBSP)) return false;
    final int len = symbol.length();
    int i = 0;
    while (i < len) {
      int cp = symbol.codePointAt(i);
      if (!Character.isWhitespace(cp)) return false;
      i += Character.charCount(cp);
    }
    return len > 0;
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public StyledGrapheme setStyle(Style style) {
    return new StyledGrapheme(symbol, style);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StyledGrapheme other)) return false;
    return symbol.equals(other.symbol) && style.equals(other.style);
  }

  @Override
  public int hashCode() {
    return 31 * symbol.hashCode() + style.hashCode();
  }

  @Override
  public String toString() {
    return "StyledGrapheme(" + symbol + ", " + style + ")";
  }
}
