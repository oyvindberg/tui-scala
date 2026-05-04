package jatatui.widgets.canvas;

import jatatui.core.style.Color;

/// A circle with a given center and radius and with a given color.
///
/// Mirrors `ratatui_widgets::canvas::Circle` (v0.30).
public record Circle(double x, double y, double radius, Color color) implements Shape {

  /// Convenience factory mirroring upstream `Circle::new`.
  public static Circle of(double x, double y, double radius, Color color) {
    return new Circle(x, y, radius, color);
  }

  @Override
  public void draw(Painter painter) {
    for (int angle = 0; angle < 360; angle++) {
      double radians = Math.toRadians(angle);
      double cx = Math.fma(radius, Math.cos(radians), x);
      double cy = Math.fma(radius, Math.sin(radians), y);
      painter.getPoint(cx, cy).ifPresent(p -> painter.paint(p.x(), p.y(), color));
    }
  }
}
