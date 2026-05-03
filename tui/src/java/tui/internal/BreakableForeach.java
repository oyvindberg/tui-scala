package tui.internal;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class BreakableForeach {
  private BreakableForeach() {}

  public enum Res {
    Continue,
    Break
  }

  public static <T> void run(Iterator<T> it, Function<T, Res> f) {
    boolean cont = true;
    while (it.hasNext() && cont) {
      switch (f.apply(it.next())) {
        case Continue -> {}
        case Break -> cont = false;
      }
    }
  }

  public static <T> void run(T[] ts, BiFunction<T, Integer, Res> f) {
    boolean cont = true;
    int i = 0;
    while (i < ts.length && cont) {
      switch (f.apply(ts[i], i)) {
        case Continue -> {}
        case Break -> cont = false;
      }
      i += 1;
    }
  }
}
