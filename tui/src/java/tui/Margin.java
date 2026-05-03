package tui;

public record Margin(int vertical, int horizontal) {
  public static Margin of(int value) {
    return new Margin(value, value);
  }
}
