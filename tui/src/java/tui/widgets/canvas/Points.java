package tui.widgets.canvas;

import java.util.Optional;
import tui.Color;
import tui.Point;
import tui.Position;

/// A shape to draw a group of points with the given color
public final class Points implements Shape {
  public final Point[] coords;
  public final Color color;

  public Points(Point[] coords, Color color) {
    this.coords = coords;
    this.color = color;
  }

  public static Points empty() {
    return new Points(new Point[0], Color.Reset);
  }

  @Override
  public void draw(Painter painter) {
    for (Point p : coords) {
      Optional<Position> pos = painter.getPoint(p.x(), p.y());
      if (pos.isPresent()) {
        painter.paint(pos.get().x(), pos.get().y(), color);
      }
    }
  }
}
