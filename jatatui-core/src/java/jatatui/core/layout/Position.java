package jatatui.core.layout;

/// Position in the terminal coordinate system.
///
/// Origin is the top-left corner (0, 0). The x axis is horizontal increasing to the right;
/// the y axis is vertical increasing downwards. Coordinates are non-negative; values that would
/// fall outside `[0, U16_MAX]` are clamped by [offset], [plus], and [minus].
public record Position(int x, int y) {
  /// Maximum coordinate, mirroring `u16::MAX` upstream. Operations clamp to this.
  public static final int U16_MAX = 65_535;

  public static final Position ORIGIN = new Position(0, 0);
  public static final Position MIN = ORIGIN;
  public static final Position MAX = new Position(U16_MAX, U16_MAX);

  public static Position of(int x, int y) {
    return new Position(x, y);
  }

  /// Top-left corner of the given Rect.
  public static Position fromRect(Rect rect) {
    return new Position(rect.x(), rect.y());
  }

  /// Move by the given Offset. Coordinates are clamped to `[0, U16_MAX]`.
  public Position offset(Offset offset) {
    return plus(offset);
  }

  public Position plus(Offset offset) {
    long newX = (long) x + (long) offset.x();
    long newY = (long) y + (long) offset.y();
    return new Position(clamp(newX), clamp(newY));
  }

  public Position minus(Offset offset) {
    long newX = (long) x - (long) offset.x();
    long newY = (long) y - (long) offset.y();
    return new Position(clamp(newX), clamp(newY));
  }

  private static int clamp(long v) {
    if (v < 0) return 0;
    if (v > U16_MAX) return U16_MAX;
    return (int) v;
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
