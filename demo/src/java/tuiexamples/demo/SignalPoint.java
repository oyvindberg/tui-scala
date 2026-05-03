package tuiexamples.demo;

import java.util.Iterator;
import tui.Point;

public final class SignalPoint {
  public final Iterator<Point> source;
  public Point[] points;
  public final int tickRate;

  public SignalPoint(Iterator<Point> source, Point[] points, int tickRate) {
    this.source = source;
    this.points = points;
    this.tickRate = tickRate;
  }

  public void onTick() {
    int n = points.length;
    Point[] next = new Point[n];
    System.arraycopy(points, tickRate, next, 0, n - tickRate);
    for (int i = 0; i < tickRate; i++) {
      next[n - tickRate + i] = source.next();
    }
    points = next;
  }
}
