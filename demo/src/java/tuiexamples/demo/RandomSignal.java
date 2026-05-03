package tuiexamples.demo;

import java.util.Iterator;
import java.util.Random;

public final class RandomSignal implements Iterator<Integer> {
  public final int lower;
  public final int upper;
  public final Random random;

  public RandomSignal(int lower, int upper, Random random) {
    this.lower = lower;
    this.upper = upper;
    this.random = random;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public Integer next() {
    return lower + random.nextInt(upper - lower);
  }

  public int[] take(int n) {
    int[] arr = new int[n];
    for (int i = 0; i < n; i++) arr[i] = next();
    return arr;
  }
}
