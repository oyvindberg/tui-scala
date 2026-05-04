package jatatui.core.text;

import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import java.util.Objects;

/// A wrapper around a string that is masked when displayed.
///
/// The masked string is displayed as a series of the same character. This might be used to display
/// a password field or similar secure data.
///
/// ## Java vs Rust deviation
///
/// This Java port carries an extra [Style] field so [Masked] can satisfy [Stylize]. The style is
/// not used by [#value()] or [#toString()] (they only mask the inner string) — it is preserved so
/// fluent style chaining (`.red()`, `.bold()`, ...) returns a [Masked] of the same shape. Use
/// [#withStyle(Style)] to set it explicitly. The [Text] / [String] conversions ignore the style;
/// callers that want a styled masked text should chain through [Text#styled(String, Style)].
public final class Masked implements Stylize<Masked> {

  private final String inner;
  private final char maskChar;
  private final Style style;

  Masked(String inner, char maskChar, Style style) {
    this.inner = inner;
    this.maskChar = maskChar;
    this.style = style;
  }

  /// Creates a new [Masked] wrapping the given string, displayed as repetitions of `maskChar`.
  public static Masked of(String s, char maskChar) {
    return new Masked(s, maskChar, Style.empty());
  }

  /// The character used for masking.
  public char maskChar() {
    return maskChar;
  }

  /// The underlying string. Useful for round-tripping; not used for display.
  public String inner() {
    return inner;
  }

  /// The underlying string, with all characters replaced by [#maskChar()].
  public String value() {
    int n = inner.length();
    if (n == 0) return "";
    char[] buf = new char[n];
    for (int i = 0; i < n; i++) {
      buf[i] = maskChar;
    }
    return new String(buf);
  }

  /// Returns a copy with the style replaced.
  public Masked withStyle(Style style) {
    return new Masked(inner, maskChar, style);
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Masked setStyle(Style style) {
    return withStyle(style);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Masked other)) return false;
    return maskChar == other.maskChar && inner.equals(other.inner) && style.equals(other.style);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inner, maskChar, style);
  }

  /// Mirrors the upstream Rust `Display` impl: shows the masked string.
  @Override
  public String toString() {
    return value();
  }
}
