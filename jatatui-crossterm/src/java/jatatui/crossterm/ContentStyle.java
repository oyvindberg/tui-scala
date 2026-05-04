package jatatui.crossterm;

import java.util.Optional;
import tui.crossterm.Attributes;

/// Mirror of crossterm's `ContentStyle` struct, kept inside `jatatui-crossterm` because the local
/// JNI binding does not expose it. The `underline_color` field is included unconditionally;
/// upstream gates it behind the `underline-color` cargo feature, but Java has no equivalent.
public record ContentStyle(
    Optional<tui.crossterm.Color> foregroundColor,
    Optional<tui.crossterm.Color> backgroundColor,
    Optional<tui.crossterm.Color> underlineColor,
    Attributes attributes) {

  /// Returns an empty content style — analogue of `ContentStyle::default()`.
  public static ContentStyle empty() {
    return new ContentStyle(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        new Attributes(java.util.List.of()));
  }

  /// Returns a copy with [foregroundColor] set.
  public ContentStyle withForegroundColor(tui.crossterm.Color color) {
    return new ContentStyle(Optional.of(color), backgroundColor, underlineColor, attributes);
  }

  /// Returns a copy with [backgroundColor] set.
  public ContentStyle withBackgroundColor(tui.crossterm.Color color) {
    return new ContentStyle(foregroundColor, Optional.of(color), underlineColor, attributes);
  }

  /// Returns a copy with [underlineColor] set.
  public ContentStyle withUnderlineColor(tui.crossterm.Color color) {
    return new ContentStyle(foregroundColor, backgroundColor, Optional.of(color), attributes);
  }

  /// Returns a copy with [attributes] set.
  public ContentStyle withAttributes(Attributes attrs) {
    return new ContentStyle(foregroundColor, backgroundColor, underlineColor, attrs);
  }
}
