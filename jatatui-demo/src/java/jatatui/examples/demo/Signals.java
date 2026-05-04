package jatatui.examples.demo;

/// Holds the two sine signals and the chart's X-axis window.
///
/// Mirrors `examples/apps/demo/src/app.rs` — `Signals`. Mutable in place; [#onTick()] advances
/// both signals and the [Window] bounds.
public final class Signals {

  public final PointSignal sin1;
  public final PointSignal sin2;
  public final Window window;

  public Signals(PointSignal sin1, PointSignal sin2, Window window) {
    this.sin1 = sin1;
    this.sin2 = sin2;
    this.window = window;
  }

  /// Advance both sine signals and shift the X-axis window by `1.0` on both ends.
  public void onTick() {
    sin1.onTick();
    sin2.onTick();
    window.lo += 1.0;
    window.hi += 1.0;
  }
}
