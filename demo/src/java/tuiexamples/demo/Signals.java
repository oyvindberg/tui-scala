package tuiexamples.demo;

import tui.Point;

public final class Signals {
  public final SignalPoint sin1;
  public final SignalPoint sin2;
  public Point window;

  public Signals(SignalPoint sin1, SignalPoint sin2, Point window) {
    this.sin1 = sin1;
    this.sin2 = sin2;
    this.window = window;
  }

  public void onTick() {
    sin1.onTick();
    sin2.onTick();
    window = new Point(window.x() + 1.0, window.y() + 1.0);
  }
}
