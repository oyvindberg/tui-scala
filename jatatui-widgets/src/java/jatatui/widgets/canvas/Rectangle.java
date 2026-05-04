package jatatui.widgets.canvas;

import jatatui.core.style.Color;

/// A rectangle to draw on a [Canvas].
///
/// Sizes used here are **not** in terminal cells. This is much more similar to the mathematical
/// coordinate system. The rectangle is positioned from its bottom left corner.
///
/// Mirrors `ratatui_widgets::canvas::Rectangle` (v0.30).
public record Rectangle(double x, double y, double width, double height, Color color)
    implements Shape {

  /// Convenience factory mirroring upstream `Rectangle::new`.
  public static Rectangle of(double x, double y, double width, double height, Color color) {
    return new Rectangle(x, y, width, height, color);
  }

  @Override
  public void draw(Painter painter) {
    Line[] lines = new Line[] {
      new Line(x, y, x, y + height, color),
      new Line(x, y + height, x + width, y + height, color),
      new Line(x + width, y, x + width, y + height, color),
      new Line(x, y, x + width, y, color),
    };
    for (Line line : lines) {
      line.draw(painter);
    }
  }
}
