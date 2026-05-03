package tui;

public final class Viewport {
  public Rect area;
  public final ResizeBehavior resizeBehavior;

  public Viewport(Rect area, ResizeBehavior resizeBehavior) {
    this.area = area;
    this.resizeBehavior = resizeBehavior;
  }

  // UNSTABLE
  public static Viewport fixed(Rect area) {
    return new Viewport(area, ResizeBehavior.Fixed);
  }
}
