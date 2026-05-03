package tui.internal;

public final class Saturating {
  private Saturating() {}

  public static int saturatingAdd(int i1, int i2) {
    int res = i1 + i2;
    if (res < i2) return Integer.MAX_VALUE;
    return res;
  }

  public static int saturatingSubSigned(int i1, int i2) {
    int res = i1 - i2;
    if (res > i2) return Integer.MIN_VALUE;
    return res;
  }

  public static int saturatingSubUnsigned(int i1, int i2) {
    int res = i1 - i2;
    return Math.max(0, res);
  }
}
