package tui.internal;

import java.util.function.IntConsumer;

public final class Ranges {
  private Ranges() {}

  public static void revRange(int fromInclusive, int toExclusive, IntConsumer f) {
    int idx = toExclusive - 1;
    while (idx >= fromInclusive) {
      f.accept(idx);
      idx -= 1;
    }
  }

  public static void range(int fromInclusive, int toExclusive, IntConsumer f) {
    int idx = fromInclusive;
    while (idx < toExclusive) {
      f.accept(idx);
      idx += 1;
    }
  }
}
