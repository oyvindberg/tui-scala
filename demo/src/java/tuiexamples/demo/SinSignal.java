package tuiexamples.demo;

import java.util.Iterator;
import tui.Point;

public final class SinSignal implements Iterator<Point> {
  public final double interval;
  public final double period;
  public final double scale;
  public double x;

  public SinSignal(double interval, double period, double scale) {
    this.interval = interval;
    this.period = period;
    this.scale = scale;
    this.x = 0.0;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public Point next() {
    Point p = new Point(x, Math.sin(x * 1.0 / period) * scale);
    x += interval;
    return p;
  }

  public Point[] take(int n) {
    Point[] arr = new Point[n];
    for (int i = 0; i < n; i++) arr[i] = next();
    return arr;
  }
}
