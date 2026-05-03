package tui.widgets.canvas;

import java.util.Optional;
import tui.Color;
import tui.Point;
import tui.Position;

public final class Painter {
  public final Context context;
  public final Point resolution;

  public Painter(Context context, Point resolution) {
    this.context = context;
    this.resolution = resolution;
  }

  /// Convert the (x, y) coordinates to location of a point on the grid
  public Optional<Position> getPoint(double x, double y) {
    double left = context.xBounds.x();
    double right = context.xBounds.y();
    double top = context.yBounds.y();
    double bottom = context.yBounds.x();
    if (x < left || x > right || y < bottom || y > top) {
      return Optional.empty();
    }
    double width = Math.abs(context.xBounds.y() - context.xBounds.x());
    double height = Math.abs(context.yBounds.y() - context.yBounds.x());
    if (width == 0.0 || height == 0.0) {
      return Optional.empty();
    }
    int x0 = (int) ((x - left) * resolution.x() / width);
    int y0 = (int) ((top - y) * resolution.y() / height);
    return Optional.of(new Position(x0, y0));
  }

  /// Paint a point of the grid
  public void paint(int x, int y, Color color) {
    context.grid.paint(x, y, color);
  }

  public static Painter from(Context context) {
    return new Painter(context, context.grid.resolution());
  }
}
