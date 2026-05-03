package tui.cassowary;

public final class NearZero {
  public static final double EPS = 1e-8;

  private NearZero() {}

  public static boolean apply(double value) {
    if (value < 0.0) {
      return -value < EPS;
    } else {
      return value < EPS;
    }
  }
}
