package jatatui.core.layout;

/// Amount by which to move a Rect (or a Position). Positive values move right/down,
/// negative values move left/up.
public record Offset(int x, int y) {
  public static final Offset ZERO = new Offset(0, 0);
  public static final Offset MIN = new Offset(Integer.MIN_VALUE, Integer.MIN_VALUE);
  public static final Offset MAX = new Offset(Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static Offset of(int x, int y) {
    return new Offset(x, y);
  }

  /// Negate the offset. Throws on Integer.MIN_VALUE inputs (matches Rust upstream's overflow
  // panic).
  public Offset negate() {
    return new Offset(Math.negateExact(x), Math.negateExact(y));
  }
}
