package jatatui.core.style;

/// An extension trait for styling objects.
///
/// In Rust this is a blanket-implemented trait on every `Styled<Item = T>` type. Java has no
/// blanket impls, so we expose `Stylize<Self>` as an interface with `default` methods that delegate
/// to [Styled#style()] / [Styled#setStyle(Style)]. Widget and text types should
/// `implements Stylize<Self>` (where `Self` is the widget type) and provide [Styled#style()] /
/// [Styled#setStyle(Style)] implementations. The default methods then provide the entire
/// fluent vocabulary (`.red()`, `.onBlue()`, `.bold()`, `.notItalic()`, etc.).
///
/// ## Java vs Rust deviation
///
/// - In Rust, `&str` and friends auto-implement [Styled] and yield a `Span` from styling. Java
///   `String` is final and cannot have interfaces grafted on; use [#stylize(String)] /
///   [Span#styled(String, Style)]-style helpers from `text` once that module lands instead.
/// - The bare-helper [#stylize(Style)] and friends here operate on a [Style] value directly,
///   matching the Rust `impl Stylize for Style` use-case.
///
/// ## Naming
///
/// Method names are the upstream `snake_case` translated to `camelCase` (`dark_gray()` →
/// `darkGray()`, `not_bold()` → `notBold()`, `slow_blink()` → `slowBlink()`).
public interface Stylize<Self extends Stylize<Self>> extends Styled<Self> {

  // ---- Core style operations ----

  default Self bg(Color color) {
    return setStyle(style().withBg(color));
  }

  default Self fg(Color color) {
    return setStyle(style().withFg(color));
  }

  default Self underlineColor(Color color) {
    return setStyle(style().withUnderlineColor(color));
  }

  default Self addModifier(Modifier modifier) {
    return setStyle(style().withAddModifier(modifier));
  }

  default Self removeModifier(Modifier modifier) {
    return setStyle(style().withRemoveModifier(modifier));
  }

  /// Resets all style properties.
  default Self reset() {
    return setStyle(Style.RESET);
  }

  // ---- Color shorthand methods ----

  default Self black() {
    return fg(Color.BLACK);
  }

  default Self onBlack() {
    return bg(Color.BLACK);
  }

  default Self red() {
    return fg(Color.RED);
  }

  default Self onRed() {
    return bg(Color.RED);
  }

  default Self green() {
    return fg(Color.GREEN);
  }

  default Self onGreen() {
    return bg(Color.GREEN);
  }

  default Self yellow() {
    return fg(Color.YELLOW);
  }

  default Self onYellow() {
    return bg(Color.YELLOW);
  }

  default Self blue() {
    return fg(Color.BLUE);
  }

  default Self onBlue() {
    return bg(Color.BLUE);
  }

  default Self magenta() {
    return fg(Color.MAGENTA);
  }

  default Self onMagenta() {
    return bg(Color.MAGENTA);
  }

  default Self cyan() {
    return fg(Color.CYAN);
  }

  default Self onCyan() {
    return bg(Color.CYAN);
  }

  default Self gray() {
    return fg(Color.GRAY);
  }

  default Self onGray() {
    return bg(Color.GRAY);
  }

  default Self darkGray() {
    return fg(Color.DARK_GRAY);
  }

  default Self onDarkGray() {
    return bg(Color.DARK_GRAY);
  }

  default Self lightRed() {
    return fg(Color.LIGHT_RED);
  }

  default Self onLightRed() {
    return bg(Color.LIGHT_RED);
  }

  default Self lightGreen() {
    return fg(Color.LIGHT_GREEN);
  }

  default Self onLightGreen() {
    return bg(Color.LIGHT_GREEN);
  }

  default Self lightYellow() {
    return fg(Color.LIGHT_YELLOW);
  }

  default Self onLightYellow() {
    return bg(Color.LIGHT_YELLOW);
  }

  default Self lightBlue() {
    return fg(Color.LIGHT_BLUE);
  }

  default Self onLightBlue() {
    return bg(Color.LIGHT_BLUE);
  }

  default Self lightMagenta() {
    return fg(Color.LIGHT_MAGENTA);
  }

  default Self onLightMagenta() {
    return bg(Color.LIGHT_MAGENTA);
  }

  default Self lightCyan() {
    return fg(Color.LIGHT_CYAN);
  }

  default Self onLightCyan() {
    return bg(Color.LIGHT_CYAN);
  }

  default Self white() {
    return fg(Color.WHITE);
  }

  default Self onWhite() {
    return bg(Color.WHITE);
  }

  // ---- Modifier shorthand methods ----

  default Self bold() {
    return addModifier(Modifier.BOLD);
  }

  default Self notBold() {
    return removeModifier(Modifier.BOLD);
  }

  default Self dim() {
    return addModifier(Modifier.DIM);
  }

  default Self notDim() {
    return removeModifier(Modifier.DIM);
  }

  default Self italic() {
    return addModifier(Modifier.ITALIC);
  }

  default Self notItalic() {
    return removeModifier(Modifier.ITALIC);
  }

  default Self underlined() {
    return addModifier(Modifier.UNDERLINED);
  }

  default Self notUnderlined() {
    return removeModifier(Modifier.UNDERLINED);
  }

  default Self slowBlink() {
    return addModifier(Modifier.SLOW_BLINK);
  }

  default Self notSlowBlink() {
    return removeModifier(Modifier.SLOW_BLINK);
  }

  default Self rapidBlink() {
    return addModifier(Modifier.RAPID_BLINK);
  }

  default Self notRapidBlink() {
    return removeModifier(Modifier.RAPID_BLINK);
  }

  default Self reversed() {
    return addModifier(Modifier.REVERSED);
  }

  default Self notReversed() {
    return removeModifier(Modifier.REVERSED);
  }

  default Self hidden() {
    return addModifier(Modifier.HIDDEN);
  }

  default Self notHidden() {
    return removeModifier(Modifier.HIDDEN);
  }

  default Self crossedOut() {
    return addModifier(Modifier.CROSSED_OUT);
  }

  default Self notCrossedOut() {
    return removeModifier(Modifier.CROSSED_OUT);
  }

  // ---- Standalone helper for arbitrary `Style` building ----

  /// Returns a [StylizedStyle] wrapping the given [Style] for fluent building. This is the
  /// Java equivalent of the Rust `impl Stylize for Style` blanket — call `Stylize.of(...)` and
  /// chain modifiers.
  static StylizedStyle of(Style style) {
    return new StylizedStyle(style);
  }

  /// A standalone fluent wrapper around a mutable style accumulator. Implements [Stylize] so
  /// the entire vocabulary is available on it. Call [#build()] to extract the [Style].
  ///
  /// Mutates internally on each call but returns `this` so chains read like Rust.
  final class StylizedStyle implements Stylize<StylizedStyle> {
    private Style current;

    public StylizedStyle(Style initial) {
      this.current = initial;
    }

    @Override
    public Style style() {
      return current;
    }

    @Override
    public StylizedStyle setStyle(Style style) {
      this.current = style;
      return this;
    }

    /// Returns the underlying accumulated [Style].
    public Style build() {
      return current;
    }
  }
}
