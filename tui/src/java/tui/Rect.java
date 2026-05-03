package tui;

/// A simple rectangle used in the computation of the layout and to give widgets a hint about the area they are supposed to render to.
public record Rect(int x, int y, int width, int height) {
  public int area() {
    return tui.internal.Saturating.saturatingMul(width, height);
  }

  /// Returns true when the rect's width or height is zero, i.e. it covers no area.
  public boolean isEmpty() {
    return width == 0 || height == 0;
  }

  public int left() {
    return x;
  }

  public int right() {
    return tui.internal.Saturating.saturatingAdd(x, width);
  }

  public int top() {
    return y;
  }

  public int bottom() {
    return tui.internal.Saturating.saturatingAdd(y, height);
  }

  public Rect inner(Margin margin) {
    int doubledHorizontal = tui.internal.Saturating.saturatingMul(margin.horizontal(), 2);
    int doubledVertical = tui.internal.Saturating.saturatingMul(margin.vertical(), 2);
    if (width < doubledHorizontal || height < doubledVertical) {
      return Rect.DEFAULT;
    } else {
      return new Rect(
          tui.internal.Saturating.saturatingAdd(x, margin.horizontal()),
          tui.internal.Saturating.saturatingAdd(y, margin.vertical()),
          tui.internal.Saturating.saturatingSubUnsigned(width, doubledHorizontal),
          tui.internal.Saturating.saturatingSubUnsigned(height, doubledVertical));
    }
  }

  public Rect union(Rect other) {
    int x1 = Math.min(x, other.x);
    int y1 = Math.min(y, other.y);
    int x2 = Math.max(x + width, other.x + other.width);
    int y2 = Math.max(y + height, other.y + other.height);
    return new Rect(x1, y1, x2 - x1, y2 - y1);
  }

  public Rect intersection(Rect other) {
    int x1 = Math.max(x, other.x);
    int y1 = Math.max(y, other.y);
    int x2 = Math.min(x + width, other.x + other.width);
    int y2 = Math.min(y + height, other.y + other.height);
    return new Rect(
        x1,
        y1,
        tui.internal.Saturating.saturatingSubUnsigned(x2, x1),
        tui.internal.Saturating.saturatingSubUnsigned(y2, y1));
  }

  public boolean intersects(Rect other) {
    return x < (other.x + other.width)
        && (x + width) > other.x
        && y < (other.y + other.height)
        && (y + height) > other.y;
  }

  public static final Rect DEFAULT = new Rect(0, 0, 0, 0);
}
