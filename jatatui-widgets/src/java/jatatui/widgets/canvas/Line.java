package jatatui.widgets.canvas;

import jatatui.core.style.Color;
import java.util.Optional;

/// A line from `(x1, y1)` to `(x2, y2)` with the given color.
///
/// Mirrors `ratatui_widgets::canvas::Line` (v0.30).
///
/// Note: kept in the `canvas` sub-package to avoid the name clash with
/// [jatatui.core.text.Line].
public record Line(double x1, double y1, double x2, double y2, Color color) implements Shape {

  /// Convenience factory mirroring upstream `Line::new`.
  public static Line of(double x1, double y1, double x2, double y2, Color color) {
    return new Line(x1, y1, x2, y2, color);
  }

  /// World-coordinate clip rectangle returned by [#clipLine].
  private record Clipped(double x1, double y1, double x2, double y2) {}

  @Override
  public void draw(Painter painter) {
    Painter.Bounds bounds = painter.bounds();
    Optional<Clipped> clippedOpt =
        clipLine(bounds.x()[0], bounds.x()[1], bounds.y()[0], bounds.y()[1], x1, y1, x2, y2);
    if (clippedOpt.isEmpty()) return;
    Clipped clipped = clippedOpt.get();

    Optional<Painter.GridPoint> p1Opt = painter.getPoint(clipped.x1(), clipped.y1());
    Optional<Painter.GridPoint> p2Opt = painter.getPoint(clipped.x2(), clipped.y2());
    if (p1Opt.isEmpty() || p2Opt.isEmpty()) return;
    Painter.GridPoint p1 = p1Opt.get();
    Painter.GridPoint p2 = p2Opt.get();

    int gx1 = p1.x();
    int gy1 = p1.y();
    int gx2 = p2.x();
    int gy2 = p2.y();

    int dx;
    int xLo;
    int xHi;
    if (gx2 >= gx1) {
      dx = gx2 - gx1;
      xLo = gx1;
      xHi = gx2;
    } else {
      dx = gx1 - gx2;
      xLo = gx2;
      xHi = gx1;
    }

    int dy;
    int yLo;
    int yHi;
    if (gy2 >= gy1) {
      dy = gy2 - gy1;
      yLo = gy1;
      yHi = gy2;
    } else {
      dy = gy1 - gy2;
      yLo = gy2;
      yHi = gy1;
    }

    if (dx == 0) {
      for (int y = yLo; y <= yHi; y++) {
        painter.paint(gx1, y, color);
      }
    } else if (dy == 0) {
      for (int x = xLo; x <= xHi; x++) {
        painter.paint(x, gy1, color);
      }
    } else if (dy < dx) {
      if (gx1 > gx2) {
        drawLineLow(painter, gx2, gy2, gx1, gy1, color);
      } else {
        drawLineLow(painter, gx1, gy1, gx2, gy2, color);
      }
    } else if (gy1 > gy2) {
      drawLineHigh(painter, gx2, gy2, gx1, gy1, color);
    } else {
      drawLineHigh(painter, gx1, gy1, gx2, gy2, color);
    }
  }

  // Bresenham low-slope line (|dy| < |dx|).
  private static void drawLineLow(Painter painter, int x1, int y1, int x2, int y2, Color color) {
    int dx = x2 - x1;
    int dy = Math.abs(y2 - y1);
    int d = 2 * dy - dx;
    int y = y1;
    for (int x = x1; x <= x2; x++) {
      painter.paint(x, y, color);
      if (d > 0) {
        if (y1 > y2) {
          y = Math.max(0, y - 1);
        } else {
          y = y + 1;
        }
        d -= 2 * dx;
      }
      d += 2 * dy;
    }
  }

  // Bresenham high-slope line (|dy| >= |dx|).
  private static void drawLineHigh(Painter painter, int x1, int y1, int x2, int y2, Color color) {
    int dx = Math.abs(x2 - x1);
    int dy = y2 - y1;
    int d = 2 * dx - dy;
    int x = x1;
    for (int y = y1; y <= y2; y++) {
      painter.paint(x, y, color);
      if (d > 0) {
        if (x1 > x2) {
          x = Math.max(0, x - 1);
        } else {
          x = x + 1;
        }
        d -= 2 * dy;
      }
      d += 2 * dx;
    }
  }

  // ---- Cohen-Sutherland line clipping ----

  private static final int INSIDE = 0;
  private static final int LEFT = 1;
  private static final int RIGHT = 2;
  private static final int BOTTOM = 4;
  private static final int TOP = 8;

  private static int outcode(
      double x, double y, double xmin, double xmax, double ymin, double ymax) {
    int code = INSIDE;
    if (x < xmin) code |= LEFT;
    else if (x > xmax) code |= RIGHT;
    if (y < ymin) code |= BOTTOM;
    else if (y > ymax) code |= TOP;
    return code;
  }

  /// Cohen-Sutherland line clipping. Returns the clipped segment, or empty when fully outside the
  /// window. Mirrors upstream's call to `line_clipping::cohen_sutherland::clip_line`.
  static Optional<Clipped> clipLine(
      double xmin,
      double xmax,
      double ymin,
      double ymax,
      double x1,
      double y1,
      double x2,
      double y2) {
    // Normalize so xmin/ymin are the lower bounds.
    double lx = Math.min(xmin, xmax);
    double hx = Math.max(xmin, xmax);
    double ly = Math.min(ymin, ymax);
    double hy = Math.max(ymin, ymax);

    int code1 = outcode(x1, y1, lx, hx, ly, hy);
    int code2 = outcode(x2, y2, lx, hx, ly, hy);

    while (true) {
      if ((code1 | code2) == 0) {
        // Both endpoints inside.
        return Optional.of(new Clipped(x1, y1, x2, y2));
      }
      if ((code1 & code2) != 0) {
        // Both endpoints share an outside region — fully outside.
        return Optional.empty();
      }
      // At least one endpoint is outside; pick that one.
      int outcode = code1 != 0 ? code1 : code2;
      double x;
      double y;
      if ((outcode & TOP) != 0) {
        x = x1 + (x2 - x1) * (hy - y1) / (y2 - y1);
        y = hy;
      } else if ((outcode & BOTTOM) != 0) {
        x = x1 + (x2 - x1) * (ly - y1) / (y2 - y1);
        y = ly;
      } else if ((outcode & RIGHT) != 0) {
        y = y1 + (y2 - y1) * (hx - x1) / (x2 - x1);
        x = hx;
      } else { // LEFT
        y = y1 + (y2 - y1) * (lx - x1) / (x2 - x1);
        x = lx;
      }
      if (outcode == code1) {
        x1 = x;
        y1 = y;
        code1 = outcode(x1, y1, lx, hx, ly, hy);
      } else {
        x2 = x;
        y2 = y;
        code2 = outcode(x2, y2, lx, hx, ly, hy);
      }
    }
  }
}
