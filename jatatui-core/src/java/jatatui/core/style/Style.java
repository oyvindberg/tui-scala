package jatatui.core.style;

import java.util.Optional;

/// Style controls the main characteristics of displayed elements.
///
/// All fields are optional. `fg`, `bg`, `underlineColor` are foreground / background / underline
/// colors that may or may not be set. `addModifier` and `subModifier` are bit-sets of modifiers
/// to add and remove, respectively.
///
/// Styles represent an incremental change: applying styles S1, S2, S3 to a cell merges them,
/// it does not replace with just S3. To clear all properties up to a point, use [#reset()].
///
/// ## Patch semantics
///
/// `this.patch(other)`:
/// - Other's set fg/bg/underlineColor overrides this's.
/// - Modifiers are merged: other.subModifier wins over this.addModifier; other.addModifier
///   wins over this.subModifier.
public record Style(
    Optional<Color> fg,
    Optional<Color> bg,
    Optional<Color> underlineColor,
    Modifier addModifier,
    Modifier subModifier) {

  /// A `Style` with no fields set (no fg, no bg, no modifiers).
  public static final Style DEFAULT =
      new Style(
          Optional.empty(), Optional.empty(), Optional.empty(), Modifier.EMPTY, Modifier.EMPTY);

  /// A `Style` that resets all properties (sets sub_modifier = ALL).
  public static final Style RESET =
      new Style(
          Optional.of(Color.RESET),
          Optional.of(Color.RESET),
          Optional.of(Color.RESET),
          Modifier.EMPTY,
          Modifier.ALL);

  /// Returns a `Style` with default properties (no fields set).
  public static Style empty() {
    return DEFAULT;
  }

  /// Returns a `Style` resetting all properties.
  public static Style reset() {
    return RESET;
  }

  // ---- Builder methods ----

  /// Returns a copy with the foreground color set.
  public Style withFg(Color color) {
    return new Style(Optional.of(color), bg, underlineColor, addModifier, subModifier);
  }

  /// Returns a copy with the background color set.
  public Style withBg(Color color) {
    return new Style(fg, Optional.of(color), underlineColor, addModifier, subModifier);
  }

  /// Returns a copy with the underline color set.
  public Style withUnderlineColor(Color color) {
    return new Style(fg, bg, Optional.of(color), addModifier, subModifier);
  }

  /// Returns a copy with the given modifier added (and removed from the sub set).
  public Style withAddModifier(Modifier modifier) {
    Modifier newSub = subModifier.difference(modifier);
    Modifier newAdd = addModifier.union(modifier);
    return new Style(fg, bg, underlineColor, newAdd, newSub);
  }

  /// Returns a copy with the given modifier removed (and added to the sub set).
  public Style withRemoveModifier(Modifier modifier) {
    Modifier newAdd = addModifier.difference(modifier);
    Modifier newSub = subModifier.union(modifier);
    return new Style(fg, bg, underlineColor, newAdd, newSub);
  }

  /// Returns true iff this style has the given modifier set in addModifier and not in subModifier.
  public boolean hasModifier(Modifier modifier) {
    return addModifier.contains(modifier) && !subModifier.contains(modifier);
  }

  /// Combines two styles into one that is equivalent to applying the two individual styles
  /// to a style one after the other.
  ///
  /// Other's set fg/bg/underlineColor wins over this's; modifiers are merged with sub winning
  /// over add and add winning over sub in turn.
  public Style patch(Style other) {
    Optional<Color> newFg = other.fg.isPresent() ? other.fg : fg;
    Optional<Color> newBg = other.bg.isPresent() ? other.bg : bg;
    Optional<Color> newUnderline =
        other.underlineColor.isPresent() ? other.underlineColor : underlineColor;

    Modifier add = addModifier.remove(other.subModifier).insert(other.addModifier);
    Modifier sub = subModifier.remove(other.addModifier).insert(other.subModifier);

    return new Style(newFg, newBg, newUnderline, add, sub);
  }

  // ---- `From` conversions ----

  /// Creates a new `Style` with the given foreground color.
  public static Style fromFg(Color color) {
    return DEFAULT.withFg(color);
  }

  /// Creates a new `Style` with the given foreground and background colors.
  public static Style fromFgBg(Color fg, Color bg) {
    return DEFAULT.withFg(fg).withBg(bg);
  }

  /// Creates a new `Style` with the given modifier added.
  public static Style fromModifier(Modifier modifier) {
    return DEFAULT.withAddModifier(modifier);
  }

  /// Creates a new `Style` with the given modifiers added and removed.
  public static Style fromModifierModifier(Modifier add, Modifier sub) {
    return DEFAULT.withAddModifier(add).withRemoveModifier(sub);
  }

  /// Creates a new `Style` with the given foreground color and modifier added.
  public static Style fromFgModifier(Color fg, Modifier modifier) {
    return DEFAULT.withFg(fg).withAddModifier(modifier);
  }

  /// Creates a new `Style` with the given foreground, background colors and modifier added.
  public static Style fromFgBgModifier(Color fg, Color bg, Modifier modifier) {
    return DEFAULT.withFg(fg).withBg(bg).withAddModifier(modifier);
  }

  /// Creates a new `Style` with the given foreground, background colors and modifiers
  // added/removed.
  public static Style fromFgBgModifierModifier(Color fg, Color bg, Modifier add, Modifier sub) {
    return DEFAULT.withFg(fg).withBg(bg).withAddModifier(add).withRemoveModifier(sub);
  }

  // ---- Color shorthand methods (mirrors color! macro from upstream) ----

  public Style black() {
    return withFg(Color.BLACK);
  }

  public Style onBlack() {
    return withBg(Color.BLACK);
  }

  public Style red() {
    return withFg(Color.RED);
  }

  public Style onRed() {
    return withBg(Color.RED);
  }

  public Style green() {
    return withFg(Color.GREEN);
  }

  public Style onGreen() {
    return withBg(Color.GREEN);
  }

  public Style yellow() {
    return withFg(Color.YELLOW);
  }

  public Style onYellow() {
    return withBg(Color.YELLOW);
  }

  public Style blue() {
    return withFg(Color.BLUE);
  }

  public Style onBlue() {
    return withBg(Color.BLUE);
  }

  public Style magenta() {
    return withFg(Color.MAGENTA);
  }

  public Style onMagenta() {
    return withBg(Color.MAGENTA);
  }

  public Style cyan() {
    return withFg(Color.CYAN);
  }

  public Style onCyan() {
    return withBg(Color.CYAN);
  }

  public Style gray() {
    return withFg(Color.GRAY);
  }

  public Style onGray() {
    return withBg(Color.GRAY);
  }

  public Style darkGray() {
    return withFg(Color.DARK_GRAY);
  }

  public Style onDarkGray() {
    return withBg(Color.DARK_GRAY);
  }

  public Style lightRed() {
    return withFg(Color.LIGHT_RED);
  }

  public Style onLightRed() {
    return withBg(Color.LIGHT_RED);
  }

  public Style lightGreen() {
    return withFg(Color.LIGHT_GREEN);
  }

  public Style onLightGreen() {
    return withBg(Color.LIGHT_GREEN);
  }

  public Style lightYellow() {
    return withFg(Color.LIGHT_YELLOW);
  }

  public Style onLightYellow() {
    return withBg(Color.LIGHT_YELLOW);
  }

  public Style lightBlue() {
    return withFg(Color.LIGHT_BLUE);
  }

  public Style onLightBlue() {
    return withBg(Color.LIGHT_BLUE);
  }

  public Style lightMagenta() {
    return withFg(Color.LIGHT_MAGENTA);
  }

  public Style onLightMagenta() {
    return withBg(Color.LIGHT_MAGENTA);
  }

  public Style lightCyan() {
    return withFg(Color.LIGHT_CYAN);
  }

  public Style onLightCyan() {
    return withBg(Color.LIGHT_CYAN);
  }

  public Style white() {
    return withFg(Color.WHITE);
  }

  public Style onWhite() {
    return withBg(Color.WHITE);
  }

  // ---- Modifier shorthand methods (mirrors modifier! macro from upstream) ----

  public Style bold() {
    return withAddModifier(Modifier.BOLD);
  }

  public Style notBold() {
    return withRemoveModifier(Modifier.BOLD);
  }

  public Style dim() {
    return withAddModifier(Modifier.DIM);
  }

  public Style notDim() {
    return withRemoveModifier(Modifier.DIM);
  }

  public Style italic() {
    return withAddModifier(Modifier.ITALIC);
  }

  public Style notItalic() {
    return withRemoveModifier(Modifier.ITALIC);
  }

  public Style underlined() {
    return withAddModifier(Modifier.UNDERLINED);
  }

  public Style notUnderlined() {
    return withRemoveModifier(Modifier.UNDERLINED);
  }

  public Style slowBlink() {
    return withAddModifier(Modifier.SLOW_BLINK);
  }

  public Style notSlowBlink() {
    return withRemoveModifier(Modifier.SLOW_BLINK);
  }

  public Style rapidBlink() {
    return withAddModifier(Modifier.RAPID_BLINK);
  }

  public Style notRapidBlink() {
    return withRemoveModifier(Modifier.RAPID_BLINK);
  }

  public Style reversed() {
    return withAddModifier(Modifier.REVERSED);
  }

  public Style notReversed() {
    return withRemoveModifier(Modifier.REVERSED);
  }

  public Style hidden() {
    return withAddModifier(Modifier.HIDDEN);
  }

  public Style notHidden() {
    return withRemoveModifier(Modifier.HIDDEN);
  }

  public Style crossedOut() {
    return withAddModifier(Modifier.CROSSED_OUT);
  }

  public Style notCrossedOut() {
    return withRemoveModifier(Modifier.CROSSED_OUT);
  }
}
