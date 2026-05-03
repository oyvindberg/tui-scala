package tui.widgets.canvas;

import java.util.Optional;
import tui.Color;
import tui.Position;
import tui.internal.Ranges;
import tui.internal.Saturating;

/// Shape to draw a line from (x1, y1) to (x2, y2) with the given color
public final class Line implements Shape {
  public final double x1;
  public final double y1;
  public final double x2;
  public final double y2;
  public final Color color;

  public Line(double x1, double y1, double x2, double y2, Color color) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.color = color;
  }

  @Override
  public void draw(Painter painter) {
    Optional<Position> p1 = painter.getPoint(this.x1, this.y1);
    if (p1.isEmpty()) return;
    Optional<Position> p2 = painter.getPoint(this.x2, this.y2);
    if (p2.isEmpty()) return;
    int ix1 = p1.get().x();
    int iy1 = p1.get().y();
    int ix2 = p2.get().x();
    int iy2 = p2.get().y();

    int dx;
    int xRangeFrom;
    int xRangeTo;
    if (ix2 >= ix1) {
      dx = ix2 - ix1;
      xRangeFrom = ix1;
      xRangeTo = ix2 + 1;
    } else {
      dx = ix1 - ix2;
      xRangeFrom = ix2;
      xRangeTo = ix1 + 1;
    }
    int dy;
    int yRangeFrom;
    int yRangeTo;
    if (iy2 >= iy1) {
      dy = iy2 - iy1;
      yRangeFrom = iy1;
      yRangeTo = iy2 + 1;
    } else {
      dy = iy1 - iy2;
      yRangeFrom = iy2;
      yRangeTo = iy1 + 1;
    }

    if (dx == 0) {
      Ranges.range(yRangeFrom, yRangeTo, y -> painter.paint(ix1, y, color));
    } else if (dy == 0) {
      Ranges.range(xRangeFrom, xRangeTo, x -> painter.paint(x, iy1, color));
    } else if (dy < dx) {
      if (ix1 > ix2) {
        drawLineLow(painter, ix2, iy2, ix1, iy1, color);
      } else {
        drawLineLow(painter, ix1, iy1, ix2, iy2, color);
      }
    } else if (iy1 > iy2) {
      drawLineHigh(painter, ix2, iy2, ix1, iy1, color);
    } else {
      drawLineHigh(painter, ix1, iy1, ix2, iy2, color);
    }
  }

  public static void drawLineLow(
      Painter painter, int x1, int y1, int x2, int y2, Color color) {
    int dx = x2 - x1;
    int dy = Math.abs(y2 - y1);
    int[] d = {2 * dy - dx};
    int[] yRef = {y1};
    Ranges.range(
        x1,
        x2 + 1,
        x -> {
          painter.paint(x, yRef[0], color);
          if (d[0] > 0) {
            yRef[0] =
                (y1 > y2) ? Saturating.saturatingSubUnsigned(yRef[0], 1) : Saturating.saturatingAdd(yRef[0], 1);
            d[0] -= 2 * dx;
          }
          d[0] += 2 * dy;
        });
  }

  public static void drawLineHigh(
      Painter painter, int x1, int y1, int x2, int y2, Color color) {
    int dx = Math.abs(x2 - x1);
    int dy = y2 - y1;
    int[] d = {2 * dx - dy};
    int[] xRef = {x1};
    Ranges.range(
        y1,
        y2 + 1,
        y -> {
          painter.paint(xRef[0], y, color);
          if (d[0] > 0) {
            xRef[0] =
                (x1 > x2) ? Saturating.saturatingSubUnsigned(xRef[0], 1) : Saturating.saturatingAdd(xRef[0], 1);
            d[0] -= 2 * dy;
          }
          d[0] += 2 * dx;
        });
  }
}
