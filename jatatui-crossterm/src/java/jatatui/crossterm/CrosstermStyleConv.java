package jatatui.crossterm;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import java.util.Optional;
import tui.crossterm.Attribute;

/// Conversion from the JNI binding's [ContentStyle] to ratatui's [Style].
///
/// Mirrors the `FromCrossterm<ContentStyle> for Style` impl in upstream
/// `ratatui-crossterm/src/lib.rs`. The `underline_color` field is included unconditionally —
/// upstream gates it behind the `underline-color` cargo feature, but Java has no equivalent.
public final class CrosstermStyleConv {

  private CrosstermStyleConv() {}

  /// Convert a [ContentStyle] to a ratatui [Style].
  public static Style fromCrossterm(ContentStyle value) {
    Modifier subModifier = Modifier.EMPTY;
    if (CrosstermModifierConv.has(value.attributes(), Attribute.NoBold)) {
      subModifier = subModifier.insert(Modifier.BOLD);
    }
    if (CrosstermModifierConv.has(value.attributes(), Attribute.NoItalic)) {
      subModifier = subModifier.insert(Modifier.ITALIC);
    }
    if (CrosstermModifierConv.has(value.attributes(), Attribute.NotCrossedOut)) {
      subModifier = subModifier.insert(Modifier.CROSSED_OUT);
    }
    if (CrosstermModifierConv.has(value.attributes(), Attribute.NoUnderline)) {
      subModifier = subModifier.insert(Modifier.UNDERLINED);
    }
    if (CrosstermModifierConv.has(value.attributes(), Attribute.NoHidden)) {
      subModifier = subModifier.insert(Modifier.HIDDEN);
    }
    if (CrosstermModifierConv.has(value.attributes(), Attribute.NoBlink)) {
      subModifier = subModifier.insert(Modifier.RAPID_BLINK).insert(Modifier.SLOW_BLINK);
    }
    if (CrosstermModifierConv.has(value.attributes(), Attribute.NoReverse)) {
      subModifier = subModifier.insert(Modifier.REVERSED);
    }

    Optional<Color> fg = value.foregroundColor().map(CrosstermColorConv::fromCrossterm);
    Optional<Color> bg = value.backgroundColor().map(CrosstermColorConv::fromCrossterm);
    Optional<Color> underline = value.underlineColor().map(CrosstermColorConv::fromCrossterm);
    Modifier addModifier = CrosstermModifierConv.fromCrossterm(value.attributes());

    return new Style(fg, bg, underline, addModifier, subModifier);
  }
}
