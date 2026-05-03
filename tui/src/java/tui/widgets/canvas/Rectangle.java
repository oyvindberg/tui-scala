package tui.widgets.canvas;

import tui.Color;

/// Shape to draw a rectangle from a `Rect` with the given color
public final class Rectangle implements Shape {
  public final double x;
  public final double y;
  public final double width;
  public final double height;
  public final Color color;

  public Rectangle(double x, double y, double width, double height, Color color) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.color = color;
  }

  @Override
  public void draw(Painter painter) {
    Line[] lines =
        new Line[] {
          new Line(x, y, x, y + height, color),
          new Line(x, y + height, x + width, y + height, color),
          new Line(x + width, y, x + width, y + height, color),
          new Line(x, y, x + width, y, color)
        };
    for (Line line : lines) {
      line.draw(painter);
    }
  }
}
