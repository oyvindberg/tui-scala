package jatatui.examples.demo;

import jatatui.widgets.chart.Point;

/// A sine-wave signal generator.
///
/// Mirrors `examples/apps/demo/src/app.rs` — `SinSignal` (impl Iterator&lt;Item = (f64, f64)&gt;).
/// Each [#next()] returns `(x, sin(x / period) * scale)` and advances `x` by `interval`.
public final class SinSignal {

  private final double interval;
  private final double period;
  private final double scale;
  private double x;

  /// Creates a new [SinSignal] starting at `x = 0`.
  public SinSignal(double interval, double period, double scale) {
    this.interval = interval;
    this.period = period;
    this.scale = scale;
    this.x = 0.0;
  }

  /// Returns the next `(x, y)` data point and advances the internal `x` cursor by `interval`.
  public Point next() {
    Point point = new Point(x, Math.sin(x * 1.0 / period) * scale);
    this.x += interval;
    return point;
  }
}
