package tui;

import java.util.Optional;

/// `style` contains the primitives used to control how your user interface will look.
///
/// Style let you control the main characteristics of the displayed elements.
///
/// It represents an incremental change. If you apply the styles S1, S2, S3 to a cell of the terminal buffer, the style of this cell will be the result of the
/// merge of S1, S2 and S3, not just S3.
public record Style(
    Optional<Color> fg, Optional<Color> bg, Modifier addModifier, Modifier subModifier) {

  public static Style empty() {
    return new Style(Optional.empty(), Optional.empty(), Modifier.EMPTY, Modifier.EMPTY);
  }

  /// Changes the foreground color.
  public Style withFg(Color color) {
    return new Style(Optional.of(color), bg, addModifier, subModifier);
  }

  /// Changes the background color.
  public Style withBg(Color color) {
    return new Style(fg, Optional.of(color), addModifier, subModifier);
  }

  /// Changes the text emphasis.
  ///
  /// When applied, it adds the given modifier to the `Style` modifiers.
  public Style withAddModifier(Modifier modifier) {
    return new Style(fg, bg, addModifier.insert(modifier), subModifier.remove(modifier));
  }

  /// Changes the text emphasis.
  ///
  /// When applied, it removes the given modifier from the `Style` modifiers.
  public Style withRemoveModifier(Modifier modifier) {
    return new Style(fg, bg, addModifier.remove(modifier), subModifier.insert(modifier));
  }

  /// Results in a combined style that is equivalent to applying the two individual styles to a style one after the other.
  public Style patch(Style other) {
    return new Style(
        other.fg.isPresent() ? other.fg : this.fg,
        other.bg.isPresent() ? other.bg : this.bg,
        addModifier.remove(other.subModifier).insert(other.addModifier),
        subModifier.remove(other.addModifier).insert(other.subModifier));
  }

  public static final Style DEFAULT = Style.empty();

  /// Returns a `Style` resetting all properties.
  public static final Style RESET =
      new Style(Optional.of(Color.Reset), Optional.of(Color.Reset), Modifier.EMPTY, Modifier.ALL);
}
