package tui
package widgets

import tui.Style
import tui.buffer.Buffer
import tui.internal.ranges._
import tui.internal.saturating._
import tui.layout.{Alignment, Rect}
import tui.symbols.line
import tui.text.Spans

//#[derive(Debug, Clone, Copy, PartialEq, Eq)]
sealed trait BorderType
object BorderType {
  case object Plain extends BorderType
  case object Rounded extends BorderType
  case object Double extends BorderType
  case object Thick extends BorderType

  def line_symbols(border_type: BorderType): line.Set =
    border_type match {
      case BorderType.Plain   => line.NORMAL
      case BorderType.Rounded => line.ROUNDED
      case BorderType.Double  => line.DOUBLE
      case BorderType.Thick   => line.THICK
    }
}

/// Base widget to be used with all upper level ones. It may be used to display a box border around
/// the widget and/or add a title.
///
/// # Examples
///
/// ```
/// # use tui::widgets::{Block, BorderType, Borders};
/// # use tui::style::{Style, Color};
/// Block::default()
///     .title("Block")
///     .borders(Borders::LEFT | Borders::RIGHT)
///     .border_style(Style::DEFAULT.fg(Color::White))
///     .border_type(BorderType::Rounded)
///     .style(Style::DEFAULT.bg(Color::Black));
/// ```
//#[derive(Debug, Clone, PartialEq, Eq)]
case class Block(
    /// Optional title place on the upper left of the block
    title: Option[Spans] = None,
    /// Title alignment. The default is top left of the block, but one can choose to place
    /// title in the top middle, or top right of the block
    title_alignment: Alignment = Alignment.Left,
    /// Visible borders
    borders: Borders = Borders.NONE,
    /// Border style
    border_style: Style = Style.DEFAULT,
    /// Type of the border. The default is plain lines but one can choose to have rounded corners
    /// or doubled lines instead.
    border_type: BorderType = BorderType.Plain,
    /// Widget style
    style: Style = Style.DEFAULT
) extends Widget {
  /// Compute the inner area of a block based on its border visibility rules.
  def inner(area: Rect): Rect = {
    var inner = area;
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
      return;
    }
    buf.set_style(area, style);
    val symbols = BorderType.line_symbols(border_type);

    // Sides
    if (borders.intersects(Borders.LEFT)) {
      range(area.top, area.bottom) { y =>
        buf
          .get(area.left, y)
          .set_symbol(symbols.vertical)
          .set_style(border_style);
        ()
      }
    }

    if (borders.intersects(Borders.TOP)) {
      range(area.left, area.right) { x =>
        buf
          .get(x, area.top)
          .set_symbol(symbols.horizontal)
          .set_style(border_style)
        ()
      }
    }
    if (borders.intersects(Borders.RIGHT)) {
      val x = area.right - 1;
      range(area.top, area.bottom) { y =>
        buf
          .get(x, y)
          .set_symbol(symbols.vertical)
          .set_style(border_style)
        ()
      }
    }
    if (borders.intersects(Borders.BOTTOM)) {
      val y = area.bottom - 1;
      range(area.left, area.right) { x =>
        buf
          .get(x, y)
          .set_symbol(symbols.horizontal)
          .set_style(border_style)
        ()
      }
    }

    // Corners
    if (borders.contains(Borders.RIGHT | Borders.BOTTOM)) {
      buf
        .get(area.right - 1, area.bottom - 1)
        .set_symbol(symbols.bottom_right)
        .set_style(border_style);
    }
    if (borders.contains(Borders.RIGHT | Borders.TOP)) {
      buf
        .get(area.right - 1, area.top)
        .set_symbol(symbols.top_right)
        .set_style(border_style);
    }
    if (borders.contains(Borders.LEFT | Borders.BOTTOM)) {
      buf
        .get(area.left, area.bottom - 1)
        .set_symbol(symbols.bottom_left)
        .set_style(border_style);
    }
    if (borders.contains(Borders.LEFT | Borders.TOP)) {
      buf
        .get(area.left, area.top)
        .set_symbol(symbols.top_left)
        .set_style(border_style);
    }

    // Title
    title.foreach { title =>
      val left_border_dx = if (borders.intersects(Borders.LEFT)) 1 else 0

      val right_border_dx = if (borders.intersects(Borders.RIGHT)) { 1 }
      else { 0 }

      val title_area_width = area.width
        .saturating_sub_unsigned(left_border_dx)
        .saturating_sub_unsigned(right_border_dx);

      val title_dx = title_alignment match {
        case Alignment.Left   => left_border_dx
        case Alignment.Center => area.width.saturating_sub_unsigned(title.width) / 2
        case Alignment.Right =>
          area.width
            .saturating_sub_unsigned(title.width)
            .saturating_sub_unsigned(right_border_dx)
      };

      val title_x = area.left + title_dx;
      val title_y = area.top;

      buf.set_spans(title_x, title_y, title, title_area_width);
    }
  }
}
