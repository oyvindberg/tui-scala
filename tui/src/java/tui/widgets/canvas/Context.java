package tui.widgets.canvas;

import java.util.ArrayList;
import java.util.List;
import tui.Point;
import tui.Spans;
import tui.Symbols;

/// Holds the state of the Canvas when painting to it.
public final class Context {
  public final Point xBounds;
  public final Point yBounds;
  public final Grid grid;
  public boolean dirty;
  public final List<Layer> layers;
  public final List<Label> labels;

  public Context(
      Point xBounds,
      Point yBounds,
      Grid grid,
      boolean dirty,
      List<Layer> layers,
      List<Label> labels) {
    this.xBounds = xBounds;
    this.yBounds = yBounds;
    this.grid = grid;
    this.dirty = dirty;
    this.layers = layers;
    this.labels = labels;
  }

  /// Draw any object that may implement the Shape trait
  public void draw(Shape shape) {
    this.dirty = true;
    Painter painter = Painter.from(this);
    shape.draw(painter);
  }

  /// Go one layer above in the canvas.
  public void layer() {
    layers.add(grid.save());
    grid.reset();
    dirty = false;
  }

  /// Print a string on the canvas at the given position
  public void print(double x, double y, Spans spans) {
    labels.add(new Label(x, y, spans));
  }

  /// Push the last layer if necessary
  public void finish() {
    if (dirty) {
      layer();
    }
  }

  public static Context create(
      int width, int height, Point xBounds, Point yBounds, Symbols.Marker marker) {
    char dot = '•';
    char block = Symbols.block.FULL.charAt(0);
    char bar = Symbols.bar.HALF.charAt(0);
    Grid grid =
        switch (marker) {
          case Dot -> CharGrid.create(width, height, dot);
          case Block -> CharGrid.create(width, height, block);
          case Bar -> CharGrid.create(width, height, bar);
          case Braille -> BrailleGrid.create(width, height);
        };
    return new Context(xBounds, yBounds, grid, false, new ArrayList<>(), new ArrayList<>());
  }
}
