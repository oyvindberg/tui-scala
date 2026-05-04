package jatatui.widgets.canvas;

import jatatui.core.style.Color;
import java.util.Optional;

/// Painter is an abstraction over the [Context] that allows shapes to be drawn on the grid.
///
/// It is used by the [Shape] interface to draw shapes on the grid. It can be useful to think of
/// this as similar to the [jatatui.core.buffer.Buffer] struct that is used to draw widgets on the
/// terminal.
///
/// Mirrors `ratatui_widgets::canvas::Painter` (v0.30). Painter is **mutable** in the sense that
/// it mutates the [Context]'s grid as the caller draws on it.
public final class Painter {

  private final Context context;
  private final Grid.Resolution resolution;

  /// Construct a [Painter] from a [Context]. Equivalent to upstream `Painter::from(&mut Context)`.
  public Painter(Context context) {
    this.context = context;
    this.resolution = context.grid().resolution();
  }

  /// Returns the [Grid.Resolution] of the underlying grid.
  public Grid.Resolution resolution() {
    return resolution;
  }

  /// Bounds of the canvas coordinate system: x and y, each as `[lo, hi]`.
  public record Bounds(double[] x, double[] y) {}

  /// Convert the `(x, y)` coordinates to location of a point on the grid.
  ///
  /// `(x, y)` coordinates are expressed in the coordinate system of the canvas. The origin is in
  /// the lower left corner of the canvas (unlike most other coordinates in jatatui where the
  /// origin is the upper left corner). The `x` and `y` bounds of the canvas define the specific
  /// area of some coordinate system that will be drawn on the canvas. The resolution of the grid
  /// is used to convert the `(x, y)` coordinates to the location of a point on the grid.
  ///
  /// Returns [Optional#empty()] when the `(x, y)` falls outside the canvas bounds, or when the
  /// width or height of the bounds is non-positive.
  public Optional<GridPoint> getPoint(double x, double y) {
    double left = context.xBounds()[0];
    double right = context.xBounds()[1];
    double bottom = context.yBounds()[0];
    double top = context.yBounds()[1];
    if (x < left || x > right || y < bottom || y > top) {
      return Optional.empty();
    }
    double width = right - left;
    double height = top - bottom;
    if (width <= 0.0 || height <= 0.0) {
      return Optional.empty();
    }
    int gx = (int) Math.round((x - left) * (resolution.x() - 1.0) / width);
    int gy = (int) Math.round((top - y) * (resolution.y() - 1.0) / height);
    return Optional.of(new GridPoint(gx, gy));
  }

  /// Paint a point of the grid.
  public void paint(int x, int y, Color color) {
    context.grid().paint(x, y, color);
  }

  /// Canvas context bounds by axis.
  public Bounds bounds() {
    return new Bounds(context.xBounds(), context.yBounds());
  }

  /// A point on the [Grid], in dot coordinates (origin top-left).
  public record GridPoint(int x, int y) {}
}
