package tui.crossterm;

/// All supported cursor styles.
///
/// Mirrors `crossterm::cursor::SetCursorStyle` (introduced in crossterm 0.26 to
/// supersede the legacy `CursorShape` / `SetCursorShape` pair).
///
/// # Note
/// - Used with [`Command.SetCursorStyle`].
public enum CursorStyle {
  /// Default cursor shape configured by the user.
  DefaultUserShape,
  /// A blinking block cursor shape.
  BlinkingBlock,
  /// A non blinking block cursor shape (inverse of `BlinkingBlock`).
  SteadyBlock,
  /// A blinking underscore cursor shape.
  BlinkingUnderScore,
  /// A non blinking underscore cursor shape (inverse of `BlinkingUnderScore`).
  SteadyUnderScore,
  /// A blinking cursor bar shape.
  BlinkingBar,
  /// A steady cursor bar shape (inverse of `BlinkingBar`).
  SteadyBar,
}
