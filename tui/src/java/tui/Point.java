package tui;

public record Point(double x, double y) {
  public static final Point Zero = new Point(0.0, 0.0);
}
