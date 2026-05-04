package jatatui.examples.demo;

import java.util.ArrayList;
import java.util.List;

/// A scrolling buffer of `long` values fed by a [RandomSignal] source.
///
/// Mirrors the `Signal&lt;S&gt;` generic struct from `examples/apps/demo/src/app.rs` for the case
/// `S = RandomSignal` (sparkline data). Java has no generic specialization, so this and
/// [PointSignal] are concrete sibling classes for each upstream `S`.
///
/// On every [#onTick()], the first `tickRate` items are removed and `tickRate` fresh values
/// pulled from the source are appended.
public final class LongSignal {

  private final RandomSignal source;
  /// Mutable in place: the demo reads the most recent points to feed the sparkline widget.
  public final List<Long> points;
  private final int tickRate;

  public LongSignal(RandomSignal source, List<Long> points, int tickRate) {
    this.source = source;
    this.points = new ArrayList<>(points);
    this.tickRate = tickRate;
  }

  /// Pre-fills the buffer with `count` values from `source` and returns a new [LongSignal].
  public static LongSignal initial(RandomSignal source, int count, int tickRate) {
    List<Long> initial = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      initial.add(source.next());
    }
    return new LongSignal(source, initial, tickRate);
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
