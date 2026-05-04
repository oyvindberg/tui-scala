package jatatui.core.style;

/// A trait for objects that have a `Style`.
///
/// This trait enables generic code to be written that can interact with any object that has a
/// `Style`. It is used by [Stylize] to allow generic code to be written that can interact with any
/// object that can be styled.
///
/// `Self` is the implementing type, returned by [#setStyle(Style)] (so that style-mutating
/// builders chain properly).
public interface Styled<Self> {
  /// Returns the current style of the object.
  Style style();

  /// Returns a copy with the given style set.
  Self setStyle(Style style);
}
