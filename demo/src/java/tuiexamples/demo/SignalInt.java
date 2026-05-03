package tuiexamples.demo;

import java.util.Iterator;

public final class SignalInt {
  public final Iterator<Integer> source;
  public int[] points;
  public final int tickRate;

  public SignalInt(Iterator<Integer> source, int[] points, int tickRate) {
    this.source = source;
    this.points = points;
    this.tickRate = tickRate;
  }

  public void onTick() {
    int n = points.length;
    int[] next = new int[n];
    System.arraycopy(points, tickRate, next, 0, n - tickRate);
    for (int i = 0; i < tickRate; i++) {
      next[n - tickRate + i] = source.next();
    }
    points = next;
  }
}
