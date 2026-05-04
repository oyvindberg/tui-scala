package jatatui.widgets.canvas;

import jatatui.core.style.Color;
import java.util.Optional;

/// A cell within a canvas [Layer].
///
/// If a [Context] contains multiple layers, then the symbol, foreground, and background colors
/// for a character will be determined by the top-most layer that provides a value for that
/// character. For example, a chart drawn with `Marker.Block` may provide the background color,
/// and a later chart drawn with `Marker.Braille` may provide the symbol and foreground color.
///
/// Mirrors the upstream `LayerCell` struct.
public record LayerCell(Optional<String> symbol, Optional<Color> fg, Optional<Color> bg) {

  /// An empty layer cell — no symbol, no fg, no bg.
  public static LayerCell empty() {
    return new LayerCell(Optional.empty(), Optional.empty(), Optional.empty());
  }
}
