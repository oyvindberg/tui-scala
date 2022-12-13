package tui
package widgets
package canvas

import tui.Style
import tui.internal.ranges

/// The Canvas widget may be used to draw more detailed figures using braille patterns (each
/// cell can have a braille character in 8 different positions).
/// # Examples
///
/// ```
/// # use tui.widgets.{Block, Borders};
/// # use tui.layout.Rect;
/// # use tui.widgets.canvas.{Canvas, Shape, Line, Rectangle, Map, MapResolution};
/// # use tui.style.Color;
/// Canvas.default()
///     .block(Block.default().title("Canvas").borders(Borders.ALL))
///     .x_bounds([-180.0, 180.0])
///     .y_bounds([-90.0, 90.0])
///     .paint(|ctx| {
///         ctx.draw(&Map {
///             resolution: MapResolution.High,
///             color: Color.White
///         });
///         ctx.layer();
///         ctx.draw(&Line {
///             x1: 0.0,
///             y1: 10.0,
///             x2: 10.0,
///             y2: 10.0,
///             color: Color.White,
///         });
///         ctx.draw(&Rectangle {
///             x: 10.0,
///             y: 20.0,
///             width: 10.0,
///             height: 10.0,
///             color: Color.Red
///         });
///     });
/// ```
case class CanvasWidget(
    block: Option[BlockWidget] = None,
    x_bounds: Point = Point.Zero,
    y_bounds: Point = Point.Zero,
    painter: Option[Context => Unit] = None,
    background_color: Color = Color.Reset,
    /// Change the type of points used to draw the shapes. By default the braille patterns are used
    /// as they provide a more fine grained result but you might want to use the simple dot or
    /// block instead if the targeted terminal does not support those symbols.
    ///
    /// # Examples
    ///
    /// ```
    /// # use tui.widgets.canvas.Canvas;
    /// # use tui.symbols;
    /// Canvas.default().marker(symbols.Marker.Braille).paint(|ctx| {});
    ///
    /// Canvas.default().marker(symbols.Marker.Dot).paint(|ctx| {});
    ///
    /// Canvas.default().marker(symbols.Marker.Block).paint(|ctx| {});
    /// ```
    marker: symbols.Marker = symbols.Marker.Braille
) extends Widget {
  override def render(area: Rect, buf: Buffer): Unit = {
    val canvas_area = this.block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.render(area, buf)
        inner_area
      case None => area
    }

    buf.set_style(canvas_area, Style.DEFAULT.bg(this.background_color))

    val painter = this.painter match {
      case Some(p) => p
      case None    => return
    }

    // Create a blank context that match the size of the canvas
    val ctx = Context(
      canvas_area.width,
      canvas_area.height,
      this.x_bounds,
      this.y_bounds,
      this.marker
    )
    // Paint to this context
    painter(ctx)
    ctx.finish()

    // Retreive painted points for each layer
    ctx.layers.foreach { layer =>
      ranges.range(0, math.min(layer.string.length, layer.colors.length)) { i =>
        val ch = layer.string.charAt(i)
        val color = layer.colors(i)
        if (ch != ' ' && ch != '\u2800') {
          val (x, y) = (i % canvas_area.width, i / canvas_area.width)
          buf
            .get(x + canvas_area.left, y + canvas_area.top)
            .set_char(ch)
            .set_fg(color)
          ()
        }
      }
    }

    // Finally draw the labels
    val left = this.x_bounds.x
    val right = this.x_bounds.y
    val top = this.y_bounds.y
    val bottom = this.y_bounds.x
    val width = math.abs(this.x_bounds.y - this.x_bounds.x)
    val height = math.abs(this.y_bounds.y - this.y_bounds.x)
    val resolution = {
      val width = (canvas_area.width - 1).toDouble
      val height = (canvas_area.height - 1).toDouble
      (width, height)
    }
    ranges.range(0, ctx.labels.length) { i =>
      val l = ctx.labels(i)
      if (l.x >= left && l.x <= right && l.y <= top && l.y >= bottom) {
        val label = l
        val x = ((label.x - left) * resolution._1 / width).toInt + canvas_area.left
        val y = ((top - label.y) * resolution._2 / height).toInt + canvas_area.top
        buf.set_spans(x, y, label.spans, canvas_area.right - x)
        ()
      }
    }
  }
}
