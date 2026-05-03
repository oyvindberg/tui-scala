package tui.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public final class StepBy {
  private StepBy() {}

  @SuppressWarnings("unchecked")
  public static <T> T[] stepBy(T[] ts, int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("n must be positive");
    }
    List<T> out = new ArrayList<>();
    int i = 0;
    while (i < ts.length) {
      out.add(ts[i]);
      i += n;
    }
    T[] result = (T[]) Array.newInstance(ts.getClass().getComponentType(), out.size());
    return out.toArray(result);
  }
}
