package jatatui.core.layout;

/// A simple size struct for representing dimensions in the terminal: `width` columns and `height`
// rows.
public record Size(int width, int height) {
  public static final Size ZERO = new Size(0, 0);
  public static final Size MIN = ZERO;
  public static final Size MAX = new Size(Position.U16_MAX, Position.U16_MAX);

  public static Size of(int width, int height) {
    return new Size(width, height);
  }

  /// Build a Size from the width and height of the given Rect.
  public static Size fromRect(Rect rect) {
    return new Size(rect.width(), rect.height());
  }

  /// Total cells covered by this size. Returned as `long` to avoid overflow when both
  /// dimensions approach `u16::MAX` (their product fits in `u32` upstream, here in `long`).
  public long area() {
    return (long) width * (long) height;
  }

  @Override
  public String toString() {
    return width + "x" + height;
  }
}
