package jatatui.core.terminal;

import jatatui.core.layout.Rect;

/// Represents the viewport of the terminal — the area of the terminal that is currently visible to
/// the user. It can be either fullscreen, inline or fixed.
///
/// When the viewport is fullscreen, the whole terminal is used to draw the application.
///
/// When the viewport is inline, it is drawn inline with the rest of the terminal. The height of the
/// viewport is fixed, but the width is the same as the terminal width.
///
/// When the viewport is fixed, it is drawn in a fixed area of the terminal. The area is specified
/// by a [Rect].
///
/// See [Terminal#withOptions(jatatui.core.backend.Backend, TerminalOptions)] for more information.
///
/// Mirrors `ratatui_core::terminal::Viewport` (v0.30). Upstream is a `enum` with three variants —
/// modeled here as a sealed interface plus three records.
public sealed interface Viewport permits Viewport.Fullscreen, Viewport.Inline, Viewport.Fixed {

  /// The viewport is fullscreen.
  record Fullscreen() implements Viewport {
    @Override
    public String toString() {
      return "Fullscreen";
    }
  }

  /// The viewport is inline with the rest of the terminal.
  ///
  /// The viewport's height is fixed and specified in number of lines. The width is the same as the
  /// terminal's width. The viewport is drawn below the cursor position.
  record Inline(int height) implements Viewport {
    @Override
    public String toString() {
      return "Inline(" + height + ")";
    }
  }

  /// The viewport is drawn in a fixed area of the terminal. The area is specified by a [Rect].
  record Fixed(Rect area) implements Viewport {
    @Override
    public String toString() {
      return "Fixed(" + area + ")";
    }
  }

  /// Default viewport: [Fullscreen].
  Viewport FULLSCREEN = new Fullscreen();

  /// Constructs a [Fullscreen] viewport. Mirrors upstream `Viewport::Fullscreen` (the
  /// `#[default]` variant).
  static Viewport fullscreen() {
    return FULLSCREEN;
  }

  /// Constructs an [Inline] viewport with the given height in rows.
  static Viewport inline(int height) {
    return new Inline(height);
  }

  /// Constructs a [Fixed] viewport with the given area.
  static Viewport fixed(Rect area) {
    return new Fixed(area);
  }
}
