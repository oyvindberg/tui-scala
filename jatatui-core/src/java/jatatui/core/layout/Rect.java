package jatatui.core.layout;

import jatatui.core.layout.solver.Either;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

  /// Move the rect by an offset without changing its size. If the offset would move any of the
  /// rect's edges outside `[0, u16::MAX]`, the rect's position is clamped to the nearest edge.
  ///
  /// Equivalent to upstream `Rect + Offset`.
  public Rect plus(Offset offset) {
    long maxX = (long) Position.U16_MAX - (long) width;
    long maxY = (long) Position.U16_MAX - (long) height;
    long sumX = saturatingAddI32((long) x, (long) offset.x());
    long sumY = saturatingAddI32((long) y, (long) offset.y());
    int newX = (int) Math.max(0L, Math.min(sumX, maxX));
    int newY = (int) Math.max(0L, Math.min(sumY, maxY));
    return new Rect(newX, newY, width, height);
  }

  /// Subtract an offset from the rect without changing its size. If the offset would move any of
  /// the rect's edges outside `[0, u16::MAX]`, the rect's position is clamped to the nearest edge.
  ///
  /// Equivalent to upstream `Rect - Offset`.
  public Rect minus(Offset offset) {
    long maxX = (long) Position.U16_MAX - (long) width;
    long maxY = (long) Position.U16_MAX - (long) height;
    long subX = saturatingSubI32((long) x, (long) offset.x());
    long subY = saturatingSubI32((long) y, (long) offset.y());
    int newX = (int) Math.max(0L, Math.min(subX, maxX));
    int newY = (int) Math.max(0L, Math.min(subY, maxY));
    return new Rect(newX, newY, width, height);
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

  // --- Layout-dependent operations ----------------------------------------

  /// Returns a new Rect, centered horizontally based on the provided constraint.
  public Rect centeredHorizontally(Constraint constraint) {
    Layout layout = Layout.horizontal(constraint).withFlex(Flex.Center);
    return layout.split(this)[0];
  }

  /// Returns a new Rect, centered vertically based on the provided constraint.
  public Rect centeredVertically(Constraint constraint) {
    Layout layout = Layout.vertical(constraint).withFlex(Flex.Center);
    return layout.split(this)[0];
  }

  /// Returns a new Rect, centered horizontally and vertically based on the provided constraints.
  public Rect centered(Constraint horizontalConstraint, Constraint verticalConstraint) {
    return centeredHorizontally(horizontalConstraint).centeredVertically(verticalConstraint);
  }

  /// Split this rect according to the given layout, requiring exactly `expected` segments.
  /// Throws an `IllegalArgumentException` if the constraint count does not match.
  public Rect[] layout(Layout layout, int expected) {
    return layout.areas(this, expected);
  }

  /// Split this rect according to the given layout, returning the segments as an array.
  public Rect[] layout(Layout layout) {
    return layout.split(this);
  }

  /// Split this rect according to the given layout, returning the segments as a list.
  public List<Rect> layoutVec(Layout layout) {
    return layout.splitVec(this);
  }

  /// Split this rect according to the given layout, returning an `Either.Left` (with an error
  /// message) if the segment count does not match `expected`.
  public Either<String, Rect[]> tryLayout(Layout layout, int expected) {
    return layout.tryAreas(this, expected);
  }

  // --- Iterators ----------------------------------------------------------

  /// Returns an iterator over the rows within this Rect. Each yielded Rect has height 1.
  public Rows rows() {
    return new Rows(this);
  }

  /// Returns an iterator over the columns within this Rect. Each yielded Rect has width 1.
  public Columns columns() {
    return new Columns(this);
  }

  /// Returns an iterator over the positions within this Rect, in row-major order.
  public Positions positions() {
    return new Positions(this);
  }

  /// Iterator over the rows of a Rect — supports both forward (`next`) and backward (`nextBack`)
  /// iteration. Each yielded value is a row with height 1.
  public static final class Rows implements Iterator<Rect> {
    private final Rect rect;
    private int currentRowFwd;
    private int currentRowBack;

    public Rows(Rect rect) {
      this.rect = rect;
      this.currentRowFwd = rect.y();
      this.currentRowBack = rect.bottom();
    }

    @Override
    public boolean hasNext() {
      return currentRowFwd < currentRowBack;
    }

    @Override
    public Rect next() {
      if (!hasNext()) throw new NoSuchElementException();
      Rect row = new Rect(rect.x(), currentRowFwd, rect.width(), 1);
      currentRowFwd += 1;
      return row;
    }

    /// Yield the next row from the back, or `Optional.empty()` if exhausted.
    public Optional<Rect> nextBack() {
      if (currentRowBack <= currentRowFwd) return Optional.empty();
      currentRowBack -= 1;
      return Optional.of(new Rect(rect.x(), currentRowBack, rect.width(), 1));
    }

    /// Number of rows remaining to iterate.
    public int remaining() {
      int startCount = saturatingSub(currentRowFwd, rect.top());
      int endCount = saturatingSub(rect.bottom(), currentRowBack);
      return saturatingSub(saturatingSub(rect.height(), startCount), endCount);
    }
  }

  /// Iterator over the columns of a Rect — supports both forward (`next`) and backward
  /// (`nextBack`) iteration. Each yielded value is a column with width 1.
  public static final class Columns implements Iterator<Rect> {
    private final Rect rect;
    private int currentColumnFwd;
    private int currentColumnBack;

    public Columns(Rect rect) {
      this.rect = rect;
      this.currentColumnFwd = rect.x();
      this.currentColumnBack = rect.right();
    }

    @Override
    public boolean hasNext() {
      return currentColumnFwd < currentColumnBack;
    }

    @Override
    public Rect next() {
      if (!hasNext()) throw new NoSuchElementException();
      Rect column = new Rect(currentColumnFwd, rect.y(), 1, rect.height());
      currentColumnFwd += 1;
      return column;
    }

    /// Yield the next column from the back, or `Optional.empty()` if exhausted.
    public Optional<Rect> nextBack() {
      if (currentColumnBack <= currentColumnFwd) return Optional.empty();
      currentColumnBack -= 1;
      return Optional.of(new Rect(currentColumnBack, rect.y(), 1, rect.height()));
    }

    /// Number of columns remaining to iterate.
    public int remaining() {
      int startCount = saturatingSub(currentColumnFwd, rect.left());
      int endCount = saturatingSub(rect.right(), currentColumnBack);
      return saturatingSub(saturatingSub(rect.width(), startCount), endCount);
    }
  }

  /// Iterator over positions within a Rect, in row-major order.
  public static final class Positions implements Iterator<Position> {
    private final Rect rect;
    private Position currentPosition;

    public Positions(Rect rect) {
      this.rect = rect;
      this.currentPosition = new Position(rect.x(), rect.y());
    }

    @Override
    public boolean hasNext() {
      return rect.contains(currentPosition);
    }

    @Override
    public Position next() {
      if (!hasNext()) throw new NoSuchElementException();
      Position position = currentPosition;
      int newX = position.x() + 1;
      int newY = position.y();
      if (newX >= rect.right()) {
        newX = rect.x();
        newY = position.y() + 1;
      }
      currentPosition = new Position(newX, newY);
      return position;
    }

    /// Number of positions remaining to iterate.
    public int remaining() {
      int rowCount = saturatingSub(rect.bottom(), currentPosition.y());
      if (rowCount == 0) return 0;
      int columnCount = saturatingSub(rect.right(), currentPosition.x());
      // subtract 1 from the row count to account for the current row
      int prod = saturatingMulI(rowCount - 1, rect.width());
      return saturatingAddI(prod, columnCount);
    }
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

  // i32-saturating arithmetic for plus/minus over Offset, which uses Java int (i32) ranges.
  private static long saturatingAddI32(long a, long b) {
    long r = a + b;
    if (r > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (r < Integer.MIN_VALUE) return Integer.MIN_VALUE;
    return r;
  }

  private static long saturatingSubI32(long a, long b) {
    long r = a - b;
    if (r > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (r < Integer.MIN_VALUE) return Integer.MIN_VALUE;
    return r;
  }

  // saturating int arithmetic clamped to u16 (used by iterator size_hint helpers)
  private static int saturatingMulI(int a, int b) {
    long r = (long) a * (long) b;
    if (r > Position.U16_MAX) return Position.U16_MAX;
    if (r < 0) return 0;
    return (int) r;
  }

  private static int saturatingAddI(int a, int b) {
    long r = (long) a + (long) b;
    if (r > Position.U16_MAX) return Position.U16_MAX;
    if (r < 0) return 0;
    return (int) r;
  }
}
