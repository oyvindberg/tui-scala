package jatatui.core.layout;

/// A simple rectangle: a `Position` plus a `Size`. Width and height are clamped at construction
/// so that `area()` cannot exceed `u16::MAX`, preserving aspect ratio when clipped.
public record Rect(int x, int y, int width, int height) {
  /// Construct a Rect, clipping width and height so the area fits in `u16::MAX`. Aspect ratio
  /// is preserved when clipping.
  public static Rect of(int x, int y, int width, int height) {
    long maxArea = Position.U16_MAX;
    int clippedW = width;
    int clippedH = height;
    long area = (long) width * (long) height;
    if (area > maxArea) {
      double aspect = (double) width / (double) height;
      double maxH = Math.sqrt((double) maxArea / aspect);
      double maxW = aspect * maxH;
      clippedW = (int) maxW;
      clippedH = (int) maxH;
    }
    return new Rect(x, y, clippedW, clippedH);
  }

  /// Build a Rect from a Position (top-left) and Size.
  public static Rect fromPositionAndSize(Position pos, Size size) {
    return new Rect(pos.x(), pos.y(), size.width(), size.height());
  }

  /// Build a Rect at the origin with the given size.
  public static Rect fromSize(Size size) {
    return new Rect(0, 0, size.width(), size.height());
  }

  public long area() {
    return (long) width * (long) height;
  }

  public boolean isEmpty() {
    return width == 0 || height == 0;
  }

  public int left() {
    return x;
  }

  public int right() {
    return saturatingAdd(x, width);
  }

  public int top() {
    return y;
  }

  public int bottom() {
    return saturatingAdd(y, height);
  }

  /// Shrink by `margin` on every side. Returns an empty rect (`x`, `y` preserved) if the
  /// margins exceed the dimensions.
  public Rect inner(Margin margin) {
    long doubledH = saturatingMul(margin.horizontal(), 2);
    long doubledV = saturatingMul(margin.vertical(), 2);
    if (width < doubledH || height < doubledV) {
      return new Rect(x, y, 0, 0);
    }
    return new Rect(
        saturatingAdd(x, margin.horizontal()),
        saturatingAdd(y, margin.vertical()),
        saturatingSub(width, (int) doubledH),
        saturatingSub(height, (int) doubledV));
  }

  /// Grow by `margin` on every side, clamping to `u16::MAX`.
  public Rect outer(Margin margin) {
    return new Rect(
        Math.max(0, x - margin.horizontal()),
        Math.max(0, y - margin.vertical()),
        clampU16((long) width + 2L * margin.horizontal()),
        clampU16((long) height + 2L * margin.vertical()));
  }

  /// Move by an Offset. Coordinates clamp to `[0, u16::MAX]`.
  public Rect offset(Offset offset) {
    long newX = (long) x + (long) offset.x();
    long newY = (long) y + (long) offset.y();
    return new Rect(clampU16(newX), clampU16(newY), width, height);
  }

  public Rect resize(Size size) {
    return new Rect(x, y, size.width(), size.height());
  }

  /// Smallest rect containing both this and `other`.
  public Rect union(Rect other) {
    int x1 = Math.min(x, other.x);
    int y1 = Math.min(y, other.y);
    int x2 = Math.max(right(), other.right());
    int y2 = Math.max(bottom(), other.bottom());
    return new Rect(x1, y1, x2 - x1, y2 - y1);
  }

  /// Overlap of this and `other`. If they don't overlap the result has zero width/height.
  public Rect intersection(Rect other) {
    int x1 = Math.max(x, other.x);
    int y1 = Math.max(y, other.y);
    int x2 = Math.min(right(), other.right());
    int y2 = Math.min(bottom(), other.bottom());
    return new Rect(x1, y1, saturatingSub(x2, x1), saturatingSub(y2, y1));
  }

  public boolean intersects(Rect other) {
    return x < other.right() && right() > other.x && y < other.bottom() && bottom() > other.y;
  }

  public boolean contains(Position p) {
    return p.x() >= x && p.x() < right() && p.y() >= y && p.y() < bottom();
  }

  /// Move (and possibly resize) so that this Rect fits inside `other`. Width/height clamp to
  /// `other`'s dimensions; position is shifted so the right/bottom edge stays inside.
  public Rect clamp(Rect other) {
    int newW = Math.min(width, other.width);
    int newH = Math.min(height, other.height);
    int newX = Math.min(Math.max(x, other.x), other.right() - newW);
    int newY = Math.min(Math.max(y, other.y), other.bottom() - newH);
    return new Rect(newX, newY, newW, newH);
  }

  public Position asPosition() {
    return new Position(x, y);
  }

  public Size asSize() {
    return new Size(width, height);
  }

  @Override
  public String toString() {
    return width + "x" + height + "+" + x + "+" + y;
  }

  private static int saturatingAdd(int a, int b) {
    long r = (long) a + (long) b;
    return clampU16(r);
  }

  private static int saturatingSub(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    if (r > Position.U16_MAX) return Position.U16_MAX;
    return (int) r;
  }

  private static long saturatingMul(int a, int b) {
    long r = (long) a * (long) b;
    if (r > Position.U16_MAX) return Position.U16_MAX;
    if (r < 0) return 0;
    return r;
  }

  private static int clampU16(long v) {
    if (v < 0) return 0;
    if (v > Position.U16_MAX) return Position.U16_MAX;
    return (int) v;
  }
}
