package jatatui.widgets.canvas;

import jatatui.core.symbols.Bar;
import jatatui.core.symbols.Block;
import jatatui.core.symbols.Braille;
import jatatui.core.symbols.Marker;
import jatatui.core.symbols.Pixel;
import jatatui.core.text.Line;
import java.util.ArrayList;
import java.util.List;

/// Holds the state of the [Canvas] when painting to it.
///
/// This is used by the [Canvas] widget to draw shapes on the grid. It can be useful to think of
/// this as similar to the `Frame` struct that is used to draw widgets on the terminal.
///
/// Mirrors `ratatui_widgets::canvas::Context` (v0.30). Context is **mutable**: it owns the
/// current [Grid] (which mutates as shapes are drawn), the saved layers, and the labels.
public final class Context {

  /// Width of the canvas in cells.
  ///
  /// This is NOT the resolution in dots/pixels as this varies by marker type.
  private final int width;

  /// Height of the canvas in cells.
  private final int height;

  /// Canvas coordinate system width: `[left, right]`.
  private final double[] xBounds;

  /// Canvas coordinate system height: `[bottom, top]`.
  private final double[] yBounds;

  private Grid grid;
  private boolean dirty;
  private final List<Layer> layers;
  private final List<Label> labels;

  /// Create a new Context with the given width and height measured in terminal columns and rows
  /// respectively. The `x` and `y` bounds define the specific area of some coordinate system that
  /// will be drawn on the canvas. The marker defines the type of points used to draw the shapes.
  ///
  /// Applications should not use this directly but rather use the [Canvas] widget. This will be
  /// created by the [Canvas#withPaintFn(java.util.function.Consumer)] method and passed to the
  /// closure that is used to draw on the canvas.
  public Context(int width, int height, double[] xBounds, double[] yBounds, Marker marker) {
    if (xBounds.length != 2) throw new IllegalArgumentException("xBounds must have length 2");
    if (yBounds.length != 2) throw new IllegalArgumentException("yBounds must have length 2");
    this.width = width;
    this.height = height;
    this.xBounds = new double[] {xBounds[0], xBounds[1]};
    this.yBounds = new double[] {yBounds[0], yBounds[1]};
    this.grid = markerToGrid(width, height, marker);
    this.dirty = false;
    this.layers = new ArrayList<>();
    this.labels = new ArrayList<>();
  }

  private static Grid markerToGrid(int width, int height, Marker marker) {
    return switch (marker) {
      case Block -> CharGrid.withColorToBg(width, height, Block.FULL);
      case Bar -> CharGrid.plain(width, height, Bar.HALF);
      case Braille -> PatternGrid.fromChars(2, 4, width, height, Braille.BRAILLE);
      case HalfBlock -> new HalfBlockGrid(width, height);
      case Quadrant -> PatternGrid.fromChars(2, 2, width, height, Pixel.QUADRANTS);
      case Sextant -> new PatternGrid(2, 3, width, height, Pixel.SEXTANTS);
      case Octant -> new PatternGrid(2, 4, width, height, Pixel.OCTANTS);
      case Dot -> CharGrid.plain(width, height, Marker.DOT);
    };
  }

  /// Change the marker being used in this context.
  ///
  /// This will save the last layer if necessary and reset the grid to use the new marker.
  public void marker(Marker marker) {
    finish();
    this.grid = markerToGrid(width, height, marker);
  }

  /// Draw the given [Shape] in this context.
  public void draw(Shape shape) {
    this.dirty = true;
    Painter painter = new Painter(this);
    shape.draw(painter);
  }

  /// Save the existing state of the grid as a layer.
  ///
  /// Save the existing state as a layer to be rendered and reset the grid to its initial state
  /// for the next layer.
  ///
  /// This allows the canvas to be drawn in multiple layers. This is useful if you want to draw
  /// multiple shapes on the [Canvas] in specific order.
  public void layer() {
    layers.add(grid.save());
    grid.reset();
    dirty = false;
  }

  /// Print a [Line] on the [Canvas] at the given position.
  ///
  /// Note that the text is always printed on top of the canvas and is **not** affected by the
  /// layers.
  public void print(double x, double y, Line line) {
    labels.add(new Label(x, y, line));
  }

  /// Convenience overload taking a raw string (uses [Line#raw(String)]).
  public void print(double x, double y, String text) {
    labels.add(new Label(x, y, Line.raw(text)));
  }

  /// Save the last layer if necessary.
  void finish() {
    if (dirty) layer();
  }

  // Package-private accessors for Painter and Canvas.
  Grid grid() {
    return grid;
  }

  double[] xBounds() {
    return xBounds;
  }

  double[] yBounds() {
    return yBounds;
  }

  List<Layer> layers() {
    return layers;
  }

  List<Label> labels() {
    return labels;
  }
}
