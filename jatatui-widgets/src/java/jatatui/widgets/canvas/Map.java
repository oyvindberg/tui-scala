package jatatui.widgets.canvas;

import jatatui.core.style.Color;

/// A world map.
///
/// A world map can be rendered with different [resolutions](MapResolution) and [colors](Color).
///
/// Mirrors `ratatui_widgets::canvas::Map` (v0.30).
public record Map(MapResolution resolution, Color color) implements Shape {

  /// Returns a [Map] with the default resolution ([MapResolution#Low]) and the default color
  /// ([Color#RESET]).
  public static Map empty() {
    return new Map(MapResolution.defaultResolution(), Color.RESET);
  }

  /// Convenience builder updating the [MapResolution].
  public Map withResolution(MapResolution resolution) {
    return new Map(resolution, color);
  }

  /// Convenience builder updating the [Color].
  public Map withColor(Color color) {
    return new Map(resolution, color);
  }

  @Override
  public void draw(Painter painter) {
    for (Coord coord : resolution.data()) {
      painter
          .getPoint(coord.x(), coord.y())
          .ifPresent(p -> painter.paint(p.x(), p.y(), color));
    }
  }
}
