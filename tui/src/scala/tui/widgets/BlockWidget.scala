package tui
package widgets

import tui.Style
import tui.internal.ranges._
import tui.internal.saturating._

/** Base widget to be used with all upper level ones. It may be used to display a box border around the widget and/or add a title.
  *
  * @param title
  *   Optional title place on the upper left of the block
  * @param titleAlignment
  *   Title alignment. The default is top left of the block, but one can choose to place title in the top middle, or top right of the block
  * @param borders
  *   Visible borders
  * @param borderStyle
  *   Border style
  * @param borderType
  *   Type of the border. The default is plain lines but one can choose to have rounded corners or doubled lines instead.
  * @param style
  *   Widget style
  */
case class BlockWidget(
    title: Option[Spans] = None,
    titleAlignment: Alignment = Alignment.Left,
    borders: Borders = Borders.NONE,
    borderStyle: Style = Style.DEFAULT,
    borderType: BlockWidget.BorderType = BlockWidget.BorderType.Plain,
    style: Style = Style.DEFAULT
) extends Widget {

  /** Compute the inner area of a block based on its border visibility rules.
    */
  def inner(area: Rect): Rect = {
    var inner = area
    if (borders.intersects(Borders.LEFT)) {
      inner = inner.copy(
        x = inner.x.saturating_add(1).min(inner.right),
        width = inner.width.saturating_sub_unsigned(1)
      )
    }
    if (borders.intersects(Borders.TOP) || title.isDefined) {
      inner = inner.copy(
        y = inner.y.saturating_add(1).min(inner.bottom),
        height = inner.height.saturating_sub_unsigned(1)
      )
    }
    if (borders.intersects(Borders.RIGHT)) {
      inner = inner.copy(width = inner.width.saturating_sub_unsigned(1))
    }
    if (borders.intersects(Borders.BOTTOM)) {
      inner = inner.copy(height = inner.height.saturating_sub_unsigned(1))
    }
    inner
  }

  def render(area: Rect, buf: Buffer): Unit = {
    if (area.area == 0) {
      return
    }
    buf.setStyle(area, style)
    val symbols = BlockWidget.BorderType.lineSymbols(borderType)

    // Sides
    if (borders.intersects(Borders.LEFT)) {
      range(area.top, area.bottom) { y =>
        buf
          .get(area.left, y)
          .setSymbol(symbols.vertical)
          .setStyle(borderStyle)
        ()
      }
    }

    if (borders.intersects(Borders.TOP)) {
      range(area.left, area.right) { x =>
        buf
          .get(x, area.top)
          .setSymbol(symbols.horizontal)
          .setStyle(borderStyle)
        ()
      }
    }
    if (borders.intersects(Borders.RIGHT)) {
      val x = area.right - 1
      range(area.top, area.bottom) { y =>
        buf
          .get(x, y)
          .setSymbol(symbols.vertical)
          .setStyle(borderStyle)
        ()
      }
    }
    if (borders.intersects(Borders.BOTTOM)) {
      val y = area.bottom - 1
      range(area.left, area.right) { x =>
        buf
          .get(x, y)
          .setSymbol(symbols.horizontal)
          .setStyle(borderStyle)
        ()
      }
    }

    // Corners
    if (borders.contains(Borders.RIGHT | Borders.BOTTOM)) {
      buf
        .get(area.right - 1, area.bottom - 1)
        .setSymbol(symbols.bottomRight)
        .setStyle(borderStyle)
    }
    if (borders.contains(Borders.RIGHT | Borders.TOP)) {
      buf
        .get(area.right - 1, area.top)
        .setSymbol(symbols.topRight)
        .setStyle(borderStyle)
    }
    if (borders.contains(Borders.LEFT | Borders.BOTTOM)) {
      buf
        .get(area.left, area.bottom - 1)
        .setSymbol(symbols.bottomLeft)
        .setStyle(borderStyle)
    }
    if (borders.contains(Borders.LEFT | Borders.TOP)) {
      buf
        .get(area.left, area.top)
        .setSymbol(symbols.topLeft)
        .setStyle(borderStyle)
    }

    // Title
    title.foreach { title =>
      val left_border_dx = if (borders.intersects(Borders.LEFT)) 1 else 0

      val right_border_dx = if (borders.intersects(Borders.RIGHT)) { 1 }
      else { 0 }

      val title_area_width = area.width
        .saturating_sub_unsigned(left_border_dx)
        .saturating_sub_unsigned(right_border_dx)

      val title_dx = titleAlignment match {
        case Alignment.Left   => left_border_dx
        case Alignment.Center => area.width.saturating_sub_unsigned(title.width) / 2
        case Alignment.Right =>
          area.width
            .saturating_sub_unsigned(title.width)
            .saturating_sub_unsigned(right_border_dx)
      }

      val title_x = area.left + title_dx
      val title_y = area.top

      buf.setSpans(title_x, title_y, title, title_area_width);
    }
  }
}

object BlockWidget {
  sealed trait BorderType

  object BorderType {
    case object Plain extends BorderType

    case object Rounded extends BorderType

    case object Double extends BorderType

    case object Thick extends BorderType

    def lineSymbols(borderType: BorderType): symbols.line.Set =
      borderType match {
        case BorderType.Plain   => symbols.line.NORMAL
        case BorderType.Rounded => symbols.line.ROUNDED
        case BorderType.Double  => symbols.line.DOUBLE
        case BorderType.Thick   => symbols.line.THICK
      }
  }

}
