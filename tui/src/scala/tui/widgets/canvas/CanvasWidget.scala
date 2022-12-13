package tui
package widgets
package canvas

import tui.Style
import tui.internal.ranges

/// The Canvas widget may be used to draw more detailed figures using braille patterns (each
/// cell can have a braille character in 8 different positions).
case class CanvasWidget(
    block: Option[BlockWidget] = None,
    x_bounds: Point = Point.Zero,
    y_bounds: Point = Point.Zero,
    background_color: Color = Color.Reset,
    /// Change the type of points used to draw the shapes. By default the braille patterns are used
    /// as they provide a more fine grained result but you might want to use the simple dot or
    /// block instead if the targeted terminal does not support those symbols.
    marker: symbols.Marker = symbols.Marker.Braille
)(painter: Context => Unit)
    extends Widget {
  override def render(area: Rect, buf: Buffer): Unit = {
    val style = Style.DEFAULT.bg(this.background_color)

    val canvas_area = this.block match {
      case Some(block) =>
        val inner_area = block.inner(area)
        block.patchedStyle(style).render(area, buf)
        inner_area
      case None => area
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

    buf.update_style(buf.area, style)

    // Retreive painted points for each layer
    ctx.layers.foreach { layer =>
      ranges.range(0, math.min(layer.string.length, layer.colors.length)) { i =>
        val ch = layer.string.charAt(i)
        val color = layer.colors(i)
        if (ch != ' ' && ch != '\u2800') {
          val (x, y) = (i % canvas_area.width, i / canvas_area.width)
          buf.set(x + canvas_area.left, y + canvas_area.top, Cell(ch.toString, style.fg(color)))
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
    ctx.labels.foreach { l =>
      if (l.x >= left && l.x <= right && l.y <= top && l.y >= bottom) {
        val label = l
        val x = ((label.x - left) * resolution._1 / width).toInt + canvas_area.left
        val y = ((top - label.y) * resolution._2 / height).toInt + canvas_area.top
        buf.set_spans(x, y, style / label.spans, canvas_area.right - x)
        ()
      }
    }
  }
}
