package tui.widgets.canvas;

import java.util.Optional;
import java.util.function.Consumer;
import tui.Buffer;
import tui.Color;
import tui.Point;
import tui.Rect;
import tui.Style;
import tui.Symbols;
import tui.Widget;
import tui.internal.Ranges;
import tui.widgets.BlockWidget;

/// The Canvas widget may be used to draw more detailed figures using braille patterns (each cell can have a braille character in 8 different positions).
public final class CanvasWidget implements Widget {
  public final Optional<BlockWidget> block;
  public final Point xBounds;
  public final Point yBounds;
  public final Color backgroundColor;
  public final Symbols.Marker marker;
  public final Consumer<Context> painter;

  public CanvasWidget(
      Optional<BlockWidget> block,
      Point xBounds,
      Point yBounds,
      Color backgroundColor,
      Symbols.Marker marker,
      Consumer<Context> painter) {
    this.block = block;
    this.xBounds = xBounds;
    this.yBounds = yBounds;
    this.backgroundColor = backgroundColor;
    this.marker = marker;
    this.painter = painter;
  }

  public static CanvasWidget empty(Consumer<Context> painter) {
    return new CanvasWidget(
        Optional.empty(),
        Point.Zero,
        Point.Zero,
        Color.Reset,
        Symbols.Marker.Braille,
        painter);
  }

  public CanvasWidget withBlock(BlockWidget b) {
    return new CanvasWidget(Optional.of(b), xBounds, yBounds, backgroundColor, marker, painter);
  }

  public CanvasWidget withXBounds(Point p) {
    return new CanvasWidget(block, p, yBounds, backgroundColor, marker, painter);
  }

  public CanvasWidget withYBounds(Point p) {
    return new CanvasWidget(block, xBounds, p, backgroundColor, marker, painter);
  }

  public CanvasWidget withBackgroundColor(Color c) {
    return new CanvasWidget(block, xBounds, yBounds, c, marker, painter);
  }

  public CanvasWidget withMarker(Symbols.Marker m) {
    return new CanvasWidget(block, xBounds, yBounds, backgroundColor, m, painter);
  }

  @Override
  public void render(Rect area, Buffer buf) {
    Rect canvasArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      canvasArea = innerArea;
    } else {
      canvasArea = area;
    }

    buf.setStyle(canvasArea, Style.DEFAULT.withBg(backgroundColor));

    Context ctx =
        Context.create(canvasArea.width(), canvasArea.height(), xBounds, yBounds, marker);
    painter.accept(ctx);
    ctx.finish();

    for (Layer layer : ctx.layers) {
      int n = Math.min(layer.string().length(), layer.colors().length);
      Ranges.range(
          0,
          n,
          i -> {
            char ch = layer.string().charAt(i);
            Color color = layer.colors()[i];
            if (ch != ' ' && ch != '⠀') {
              int x = i % canvasArea.width();
              int y = i / canvasArea.width();
              buf.get(x + canvasArea.left(), y + canvasArea.top())
                  .setChar(ch)
                  .setFg(color);
            }
          });
    }

    double left = xBounds.x();
    double right = xBounds.y();
    double top = yBounds.y();
    double bottom = yBounds.x();
    double width = Math.abs(xBounds.y() - xBounds.x());
    double height = Math.abs(yBounds.y() - yBounds.x());
    double resWidth = (double) (canvasArea.width() - 1);
    double resHeight = (double) (canvasArea.height() - 1);
    Ranges.range(
        0,
        ctx.labels.size(),
        i -> {
          Label l = ctx.labels.get(i);
          if (l.x() >= left && l.x() <= right && l.y() <= top && l.y() >= bottom) {
            int x = (int) ((l.x() - left) * resWidth / width) + canvasArea.left();
            int y = (int) ((top - l.y()) * resHeight / height) + canvasArea.top();
            buf.setSpans(x, y, l.spans(), canvasArea.right() - x);
          }
        });
  }
}
