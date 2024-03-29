package tui
package widgets
package canvas

import tui.Style
import tui.internal.ranges

/** The Canvas widget may be used to draw more detailed figures using braille patterns (each cell can have a braille character in 8 different positions).
  *
  * @param block
  * @param xBounds
  * @param yBounds
  * @param backgroundColor
  * @param marker
  *   Change the type of points used to draw the shapes. By default the braille patterns are used as they provide a more fine grained result but you might want
  *   to use the simple dot or block instead if the targeted terminal does not support those symbols.
  * @param painter
  */
case class CanvasWidget(
    block: Option[BlockWidget] = None,
    xBounds: Point = Point.Zero,
    yBounds: Point = Point.Zero,
    backgroundColor: Color = Color.Reset,
    marker: symbols.Marker = symbols.Marker.Braille
)(painter: Context => Unit)
    extends Widget {
  override def render(area: Rect, buf: Buffer): Unit = {
    val canvas_area = this.block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.render(area, buf)
        inner_area
      case None => area
    }

    buf.setStyle(canvas_area, Style.DEFAULT.bg(this.backgroundColor))

    // Create a blank context that match the size of the canvas
    val ctx = Context(
      canvas_area.width,
      canvas_area.height,
      this.xBounds,
      this.yBounds,
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
            .setChar(ch)
            .setFg(color)
          ()
        }
      }
    }

    // Finally draw the labels
    val left = this.xBounds.x
    val right = this.xBounds.y
    val top = this.yBounds.y
    val bottom = this.yBounds.x
    val width = math.abs(this.xBounds.y - this.xBounds.x)
    val height = math.abs(this.yBounds.y - this.yBounds.x)
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
        buf.setSpans(x, y, label.spans, canvas_area.right - x)
        ()
      }
    }
  }
}
