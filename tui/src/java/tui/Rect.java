package tui;

/// A simple rectangle used in the computation of the layout and to give widgets a hint about the area they are supposed to render to.
public record Rect(int x, int y, int width, int height) {
  public int area() {
    return width * height;
  }

  public int left() {
    return x;
  }

  public int right() {
    return x + width;
  }

  public int top() {
    return y;
  }

  public int bottom() {
    return y + height;
  }

  public Rect inner(Margin margin) {
    if (width < 2 * margin.horizontal() || height < 2 * margin.vertical()) {
      return Rect.DEFAULT;
    } else {
      return new Rect(
          x + margin.horizontal(),
          y + margin.vertical(),
          width - 2 * margin.horizontal(),
          height - 2 * margin.vertical());
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
    return new Rect(x1, y1, x2 - x1, y2 - y1);
  }

  public boolean intersects(Rect other) {
    return x < (other.x + other.width)
        && (x + width) > other.x
        && y < (other.y + other.height)
        && (y + height) > other.y;
  }

  public static final Rect DEFAULT = new Rect(0, 0, 0, 0);
}
