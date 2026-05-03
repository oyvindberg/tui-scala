package tui;

import tui.internal.Wcwidth;

public final class Grapheme {
  public final String str;
  private int width = -1;

  public Grapheme(String str) {
    this.str = str;
  }

  public int width() {
    if (width == -1) {
      int sum = str.codePoints().map(Wcwidth::of).sum();
      width = Math.max(0, sum);
    }
    return width;
  }

  public static final Grapheme Empty = new Grapheme(" ");

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Grapheme g && g.str.equals(str);
  }

  @Override
  public int hashCode() {
    return str.hashCode();
  }

  @Override
  public String toString() {
    return "Grapheme(" + str + ")";
  }
}
