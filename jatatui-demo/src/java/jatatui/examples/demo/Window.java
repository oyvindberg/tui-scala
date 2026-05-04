package jatatui.examples.demo;

/// The visible X-axis window for the chart on the first tab.
///
/// Replaces upstream `[f64; 2]` (`window: [f64; 2]` in `Signals`) per the project rule that
/// tuples become dedicated record types. Mutable: the demo advances both ends every tick.
public final class Window {

  /// Left (lower) bound of the window.
  public double lo;

  /// Right (upper) bound of the window.
  public double hi;

  public Window(double lo, double hi) {
    this.lo = lo;
    this.hi = hi;
  }
}
