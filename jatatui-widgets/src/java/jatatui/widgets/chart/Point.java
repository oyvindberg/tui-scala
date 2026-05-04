package jatatui.widgets.chart;

import jatatui.widgets.canvas.Coord;

/// A 2D data point in the chart's coordinate system. The X axis grows left-to-right and the Y
/// axis grows bottom-to-top (math convention), unlike [jatatui.core.layout.Rect].
///
/// Replaces upstream's `(f64, f64)` tuple per the project's "tuples get dedicated record types"
/// rule. The `Coord` record in [jatatui.widgets.canvas] uses the same shape — converters are
/// provided to bridge into the canvas API.
public record Point(double x, double y) {

  /// Returns the equivalent [Coord] for use with the canvas drawing primitives.
  public Coord toCoord() {
    return new Coord(x, y);
  }

  /// Wraps a [Coord] into a chart [Point].
  public static Point fromCoord(Coord c) {
    return new Point(c.x(), c.y());
  }
}
