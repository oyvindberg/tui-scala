package jatatui.crossterm;

import jatatui.core.style.Modifier;
import java.util.List;
import tui.crossterm.Attribute;
import tui.crossterm.Attributes;

/// Conversion from the JNI binding's [Attribute]/[Attributes] to jatatui's [Modifier] bitflags.
///
/// Mirrors the `FromCrossterm<CrosstermAttribute> for Modifier` and
/// `FromCrossterm<CrosstermAttributes> for Modifier` impls in upstream
/// `ratatui-crossterm/src/lib.rs`.
public final class CrosstermModifierConv {

  private CrosstermModifierConv() {}

  /// Convert a single [Attribute] to a [Modifier]. Wraps the attribute in a one-element
  /// [Attributes] and delegates, matching upstream.
  public static Modifier fromCrossterm(Attribute attribute) {
    return fromCrossterm(new Attributes(List.of(attribute)));
  }

  /// Convert an [Attributes] set to a [Modifier]. Mirrors upstream
  /// `FromCrossterm<CrosstermAttributes> for Modifier`.
  public static Modifier fromCrossterm(Attributes attributes) {
    Modifier res = Modifier.EMPTY;
    if (has(attributes, Attribute.Bold)) {
      res = res.insert(Modifier.BOLD);
    }
    if (has(attributes, Attribute.Dim)) {
      res = res.insert(Modifier.DIM);
    }
    if (has(attributes, Attribute.Italic)) {
      res = res.insert(Modifier.ITALIC);
    }
    if (has(attributes, Attribute.Underlined)
        || has(attributes, Attribute.DoubleUnderlined)
        || has(attributes, Attribute.Undercurled)
        || has(attributes, Attribute.Underdotted)
        || has(attributes, Attribute.Underdashed)) {
      res = res.insert(Modifier.UNDERLINED);
    }
    if (has(attributes, Attribute.SlowBlink)) {
      res = res.insert(Modifier.SLOW_BLINK);
    }
    if (has(attributes, Attribute.RapidBlink)) {
      res = res.insert(Modifier.RAPID_BLINK);
    }
    if (has(attributes, Attribute.Reverse)) {
      res = res.insert(Modifier.REVERSED);
    }
    if (has(attributes, Attribute.Hidden)) {
      res = res.insert(Modifier.HIDDEN);
    }
    if (has(attributes, Attribute.CrossedOut)) {
      res = res.insert(Modifier.CROSSED_OUT);
    }
    return res;
  }

  /// Returns true iff [attributes] contains [attribute] — analogue of crossterm's
  /// `Attributes::has`.
  public static boolean has(Attributes attributes, Attribute attribute) {
    return attributes.attributes().contains(attribute);
  }
}
