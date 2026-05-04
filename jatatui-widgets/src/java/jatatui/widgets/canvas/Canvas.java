package jatatui.widgets.canvas;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.symbols.Marker;
import jatatui.core.widgets.Widget;
import jatatui.widgets.block.Block;
import java.util.Optional;
import java.util.function.Consumer;

/// The [Canvas] widget provides a means to draw shapes (Lines, Rectangles, Circles, etc.) on a
/// grid.
///
/// By default the grid is made of Braille patterns but you may change the marker to use a
/// different set of symbols. If your terminal or font does not support this unicode block, you
/// will see unicode replacement characters (`�`) instead of braille dots. The Braille patterns (as
/// well the octant character patterns) provide a more fine grained result with a 2x4 resolution
/// per character, but you might want to use a simple dot, block, or bar instead by calling the
/// [#withMarker(Marker)] method if your target environment does not support those symbols.
///
/// Mirrors `ratatui_widgets::canvas::Canvas` (v0.30). The closure that draws on the canvas is
/// modelled as a [Consumer]<[Context]>; pass it via [#withPaintFn(Consumer)].
public final class Canvas implements Widget {

  private final Optional<Block> block;
  private final double[] xBounds;
  private final double[] yBounds;
  private final Optional<Consumer<Context>> paintFunc;
  private final Color backgroundColor;
  private final Marker marker;

  private Canvas(
      Optional<Block> block,
      double[] xBounds,
      double[] yBounds,
      Optional<Consumer<Context>> paintFunc,
      Color backgroundColor,
      Marker marker) {
    this.block = block;
    this.xBounds = new double[] {xBounds[0], xBounds[1]};
    this.yBounds = new double[] {yBounds[0], yBounds[1]};
    this.paintFunc = paintFunc;
    this.backgroundColor = backgroundColor;
    this.marker = marker;
  }

  /// Returns a new empty [Canvas] with default settings.
  ///
  /// Mirrors upstream `Canvas::default()`. Default marker is [Marker#Braille], default background
  /// is [Color#RESET], no block, both bounds `[0.0, 0.0]`.
  public static Canvas empty() {
    return new Canvas(
        Optional.empty(),
        new double[] {0.0, 0.0},
        new double[] {0.0, 0.0},
        Optional.empty(),
        Color.RESET,
        Marker.Braille);
  }

  /// Wraps the canvas with a custom [Block] widget.
  public Canvas withBlock(Block block) {
    return new Canvas(Optional.of(block), xBounds, yBounds, paintFunc, backgroundColor, marker);
  }

  /// Define the viewport of the canvas (x axis).
  public Canvas withXBounds(double[] bounds) {
    return new Canvas(block, bounds, yBounds, paintFunc, backgroundColor, marker);
  }

  /// Define the viewport of the canvas (y axis).
  public Canvas withYBounds(double[] bounds) {
    return new Canvas(block, xBounds, bounds, paintFunc, backgroundColor, marker);
  }

  /// Store the closure that will be used to draw to the [Canvas].
  public Canvas withPaintFn(Consumer<Context> paintFn) {
    return new Canvas(block, xBounds, yBounds, Optional.of(paintFn), backgroundColor, marker);
  }

  /// Change the background [Color] of the entire canvas.
  public Canvas withBackgroundColor(Color color) {
    return new Canvas(block, xBounds, yBounds, paintFunc, color, marker);
  }

  /// Change the type of points used to draw the shapes.
  public Canvas withMarker(Marker marker) {
    return new Canvas(block, xBounds, yBounds, paintFunc, backgroundColor, marker);
  }

  // ---- Accessors (rarely needed by user code; useful for tests/composition) ----

  public Optional<Block> block() {
    return block;
  }

  public double[] xBounds() {
    return new double[] {xBounds[0], xBounds[1]};
  }

  public double[] yBounds() {
    return new double[] {yBounds[0], yBounds[1]};
  }

  public Color backgroundColor() {
    return backgroundColor;
  }

  public Marker marker() {
    return marker;
  }

  @Override
  public void render(Rect area, Buffer buf) {
    // Render the optional block frame and compute the inner area.
    Rect canvasArea = area;
    if (block.isPresent()) {
      Block b = block.get();
      b.render(area, buf);
      canvasArea = b.inner(area);
    }
    if (canvasArea.isEmpty()) return;

    buf.setStyle(canvasArea, Style.empty().withBg(backgroundColor));

    int width = canvasArea.width();

    if (paintFunc.isEmpty()) return;

    // Create a blank context that matches the size of the canvas.
    Context ctx = new Context(canvasArea.width(), canvasArea.height(), xBounds, yBounds, marker);
    // Paint to this context.
    paintFunc.get().accept(ctx);
    ctx.finish();

    // Retrieve painted points for each layer.
    for (Layer layer : ctx.layers()) {
      var contents = layer.contents();
      for (int index = 0; index < contents.size(); index++) {
        LayerCell layerCell = contents.get(index);
        int x = (index % width) + canvasArea.left();
        int y = (index / width) + canvasArea.top();
        Cell cell = buf.cellAt(x, y);
        layerCell.symbol().ifPresent(cell::setSymbol);
        layerCell.fg().ifPresent(cell::setFg);
        layerCell.bg().ifPresent(cell::setBg);
      }
    }

    // Finally draw the labels.
    double left = xBounds[0];
    double right = xBounds[1];
    double top = yBounds[1];
    double bottom = yBounds[0];
    double boundsWidth = Math.abs(xBounds[1] - xBounds[0]);
    double boundsHeight = Math.abs(yBounds[1] - yBounds[0]);
    double resW = (double) (canvasArea.width() - 1);
    double resH = (double) (canvasArea.height() - 1);
    for (Label label : ctx.labels()) {
      if (label.x() < left || label.x() > right || label.y() > top || label.y() < bottom) continue;
      int x = (int) ((label.x() - left) * resW / boundsWidth) + canvasArea.left();
      int y = (int) ((top - label.y()) * resH / boundsHeight) + canvasArea.top();
      buf.setLine(x, y, label.line(), canvasArea.right() - x);
    }
  }
}
