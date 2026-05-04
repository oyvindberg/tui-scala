package jatatui.core.terminal;

/// Options to pass to [Terminal#withOptions(jatatui.core.backend.Backend, TerminalOptions)].
///
/// Mirrors upstream `ratatui_core::terminal::Options` (re-exported as `TerminalOptions` from
/// `terminal.rs`).
public record TerminalOptions(Viewport viewport) {

  /// Returns the default options — a [Viewport.Fullscreen] viewport.
  public static TerminalOptions fullscreen() {
    return new TerminalOptions(Viewport.fullscreen());
  }
}
