package tui
package widgets
package canvas

import tui.Style
import tui.internal.ranges

import scala.collection.mutable

/// Interface for all shapes that may be drawn on a Canvas widget.
trait Shape {
  def draw(painter: Painter): Unit
}

/// Label to draw some text on the canvas
case class Label(
    x: Double,
    y: Double,
    spans: Spans
)

case class Layer(
    string: String,
    colors: Array[Color]
)

trait Grid {
  def width: Int

  def height: Int

  def resolution: Point

  def paint(x: Int, y: Int, color: Color): Unit

  def save(): Layer

  def reset(): Unit
}

case class BrailleGrid(
    width: Int,
    height: Int,
    cells: Array[Int],
    colors: Array[Color]
) extends Grid {
  override def resolution: Point =
    Point(
      this.width.toDouble * 2.0 - 1.0,
      this.height.toDouble * 4.0 - 1.0
    )

  override def save(): Layer =
    Layer(
      string = new String(this.cells, 0, cells.length),
      colors = this.colors.clone()
    )

  override def reset(): Unit = {
    ranges.range(0, this.cells.length) { i =>
      this.cells(i) = symbols.braille.BLANK
    }
    ranges.range(0, this.colors.length) { i =>
      this.colors(i) = Color.Reset
    }
  }

  override def paint(x: Int, y: Int, color: Color): Unit = {
    val index = y / 4 * this.width + x / 2
    if (index < this.cells.length) {
      val c = this.cells(index)
      val chosenDots = symbols.braille.DOTS(y % 4)
      val chosenDot = x % 2 match {
        case 0 => chosenDots._1
        case 1 => chosenDots._2
      }
      val newC = c | chosenDot
      this.cells(index) = newC
    }
    if (index < this.colors.length) {
      this.colors(index) = color
    }
  }
}

object BrailleGrid {
  def apply(width: Int, height: Int): BrailleGrid = {
    val length = width * height
    BrailleGrid(
      width,
      height,
      cells = Array.fill(length)(symbols.braille.BLANK),
      colors = Array.fill(length)(Color.Reset)
    )
  }
}

object CharGrid {
  def apply(width: Int, height: Int, cell_char: Char): CharGrid = {
    val length = width * height
    CharGrid(
      width,
      height,
      cells = Array.fill(length)(' '),
      colors = Array.fill(length)(Color.Reset),
      cell_char
    )
  }
}

case class CharGrid(
    width: Int,
    height: Int,
    cells: Array[Char],
    colors: Array[Color],
    cell_char: Char
) extends Grid {

  override def resolution: Point =
    Point(this.width.toDouble - 1.0, this.height.toDouble - 1.0)

  override def save(): Layer =
    Layer(
      string = new String(this.cells),
      colors = this.colors.map(identity)
    )

  override def reset(): Unit = {
    ranges.range(0, this.cells.length) { i =>
      this.cells(i) = ' '
    }
    ranges.range(0, this.colors.length) { i =>
      this.colors(i) = Color.Reset
    }
  }

  override def paint(x: Int, y: Int, color: Color): Unit = {
    val index = y * this.width + x
    if (index < this.cells.length) {
      this.cells(index) = this.cell_char
    }
    if (index < this.colors.length) {
      this.colors(index) = color
    }
  }
}

case class Painter(
    context: Context,
    resolution: Point
) {
  /// Convert the (x, y) coordinates to location of a point on the grid
  ///
  /// # Examples:
  /// ```
  /// use tui.{symbols, widgets.canvas.{Painter, Context}};
  ///
  /// var ctx = Context.new(2, 2, [1.0, 2.0], [0.0, 2.0], symbols.Marker.Braille);
  /// var painter = Painter.from(ctx);
  /// val point = painter.get_point(1.0, 0.0);
  /// assert_eq!(point, Some((0, 7)));
  /// val point = painter.get_point(1.5, 1.0);
  /// assert_eq!(point, Some((1, 3)));
  /// val point = painter.get_point(0.0, 0.0);
  /// assert_eq!(point, None);
  /// val point = painter.get_point(2.0, 2.0);
  /// assert_eq!(point, Some((3, 0)));
  /// val point = painter.get_point(1.0, 2.0);
  /// assert_eq!(point, Some((0, 0)));
  /// ```
  def get_point(x: Double, y: Double): Option[(Int, Int)] = {
    val left = this.context.x_bounds.x
    val right = this.context.x_bounds.y
    val top = this.context.y_bounds.y
    val bottom = this.context.y_bounds.x
    if (x < left || x > right || y < bottom || y > top) {
      return None
    }
    val width = math.abs(this.context.x_bounds.y - this.context.x_bounds.x)
    val height = math.abs(this.context.y_bounds.y - this.context.y_bounds.x)
    if (width == 0.0 || height == 0.0) {
      return None
    }
    val x0 = ((x - left) * this.resolution.x / width).toInt
    val y0 = ((top - y) * this.resolution.y / height).toInt
    Some((x0, y0))
  }

  /// Paint a point of the grid
  ///
  /// # Examples:
  /// ```
  /// use tui.{style.Color, symbols, widgets.canvas.{Painter, Context}};
  ///
  /// var ctx = Context.new(1, 1, [0.0, 2.0], [0.0, 2.0], symbols.Marker.Braille);
  /// var painter = Painter.from(ctx);
  /// val cell = painter.paint(1, 3, Color.Red);
  /// ```
  def paint(x: Int, y: Int, color: Color): Unit =
    this.context.grid.paint(x, y, color)
}

object Painter {
  def from(context: Context): Painter = {
    val resolution = context.grid.resolution
    Painter(context, resolution)
  }
}

/// Holds the state of the Canvas when painting to it.
case class Context(
    x_bounds: Point,
    y_bounds: Point,
    grid: Grid,
    var dirty: Boolean,
    layers: mutable.ArrayBuffer[Layer],
    labels: mutable.ArrayBuffer[Label]
) {
  /// Draw any object that may implement the Shape trait
  def draw(shape: Shape): Unit = {
    this.dirty = true
    val painter = Painter.from(this)
    shape.draw(painter)
  }

  /// Go one layer above in the canvas.
  def layer(): Unit = {
    this.layers.addOne(this.grid.save())
    this.grid.reset()
    this.dirty = false
  }

  /// Print a string on the canvas at the given position
  def print(x: Double, y: Double, spans: Spans): Unit =
    this.labels.addOne(Label(x, y, spans))

  /// Push the last layer if necessary
  def finish(): Unit =
    if (this.dirty) {
      this.layer()
    }
}

object Context {
  def apply(
      width: Int,
      height: Int,
      x_bounds: Point,
      y_bounds: Point,
      marker: symbols.Marker
  ): Context = {
    val grid: Grid = marker match {
      case symbols.Marker.Dot     => CharGrid(width, height, '•')
      case symbols.Marker.Block   => CharGrid(width, height, '▄')
      case symbols.Marker.Braille => BrailleGrid(width, height)
    }
    Context(
      x_bounds,
      y_bounds,
      grid,
      dirty = false,
      layers = mutable.ArrayBuffer.empty,
      labels = mutable.ArrayBuffer.empty
    )
  }
}

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
case class Canvas(
    block: Option[Block] = None,
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
