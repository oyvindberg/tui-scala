package jatatui.examples.demo;

import jatatui.widgets.chart.Point;
import java.util.ArrayList;
import java.util.List;

/// A scrolling buffer of `(x, y)` data points fed by a [SinSignal] source.
///
/// Mirrors the `Signal&lt;S&gt;` generic struct from `examples/apps/demo/src/app.rs` for the case
/// `S = SinSignal` (chart data). See [LongSignal] for the sparkline equivalent.
public final class PointSignal {

  private final SinSignal source;
  /// Mutable in place: the demo passes this list to [jatatui.widgets.chart.Dataset].
  public final List<Point> points;
  private final int tickRate;

  public PointSignal(SinSignal source, List<Point> points, int tickRate) {
    this.source = source;
    this.points = new ArrayList<>(points);
    this.tickRate = tickRate;
  }

  /// Pre-fills the buffer with `count` points from `source` and returns a new [PointSignal].
  public static PointSignal initial(SinSignal source, int count, int tickRate) {
    List<Point> initial = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      initial.add(source.next());
    }
    return new PointSignal(source, initial, tickRate);
  }

  /// Drops the first `tickRate` points and appends `tickRate` fresh points from the source.
  public void onTick() {
    int n = Math.min(tickRate, points.size());
    points.subList(0, n).clear();
    for (int i = 0; i < tickRate; i++) {
      points.add(source.next());
    }
  }
}
