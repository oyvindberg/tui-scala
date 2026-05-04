package jatatui.widgets.canvas;

import jatatui.core.style.Color;

/// A group of points with a given color.
///
/// Mirrors `ratatui_widgets::canvas::Points` (v0.30). Points are passed as an array of
/// [Coord]s — a domain record replacing upstream's `&'a [(f64, f64)]` slice.
public record Points(Coord[] coords, Color color) implements Shape {

  /// Convenience factory mirroring upstream `Points::new`.
  public static Points of(Coord[] coords, Color color) {
    return new Points(coords, color);
  }

  @Override
  public void draw(Painter painter) {
    for (Coord coord : coords) {
      painter.getPoint(coord.x(), coord.y()).ifPresent(p -> painter.paint(p.x(), p.y(), color));
    }
  }
}
