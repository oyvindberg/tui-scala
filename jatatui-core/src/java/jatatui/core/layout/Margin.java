package jatatui.core.layout;

/// Spacing around a rectangular area: `horizontal` cells on each side and `vertical` cells on top
// and bottom.
public record Margin(int horizontal, int vertical) {
  public static Margin of(int horizontal, int vertical) {
    return new Margin(horizontal, vertical);
  }

  @Override
  public String toString() {
    return horizontal + "x" + vertical;
  }
}
