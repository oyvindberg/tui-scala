package tui
package widgets

import tui.Style
import tui.internal.ranges
import tui.internal.saturating._
import tui.widgets.canvas.{CanvasWidget, Line, Points}

/// A widget to plot one or more dataset in a cartesian coordinate system
///
/// # Examples
///
/// ```
/// # use tui.symbols
/// # use tui.widgets.{Block, Borders, Chart, Axis, Dataset, GraphType}
/// # use tui.style.{Style, Color}
/// # use tui.text.Span
/// val datasets = vec![
///     Dataset.default()
///         .name("data1")
///         .marker(symbols.Marker.Dot)
///         .graph_type(GraphType.Scatter)
///         .style(Style.DEFAULT.fg(Color.Cyan))
///         .data(&[(0.0, 5.0), (1.0, 6.0), (1.5, 6.434)]),
///     Dataset.default()
///         .name("data2")
///         .marker(symbols.Marker.Braille)
///         .graph_type(GraphType.Line)
///         .style(Style.DEFAULT.fg(Color.Magenta))
///         .data(&[(4.0, 5.0), (5.0, 8.0), (7.66, 13.5)]),
/// ]
/// Chart.new(datasets)
///     .block(Block.default().title("Chart"))
///     .x_axis(Axis.default()
///         .title(Span.styled("X Axis", Style.DEFAULT.fg(Color.Red)))
///         .style(Style.DEFAULT.fg(Color.White))
///         .bounds([0.0, 10.0])
///         .labels(["0.0", "5.0", "10.0"].iter().cloned().map(Span.from).collect()))
///     .y_axis(Axis.default()
///         .title(Span.styled("Y Axis", Style.DEFAULT.fg(Color.Red)))
///         .style(Style.DEFAULT.fg(Color.White))
///         .bounds([0.0, 10.0])
///         .labels(["0.0", "5.0", "10.0"].iter().cloned().map(Span.from).collect()))
/// ```

case class ChartWidget(
    /// A block to display around the widget eventually
    block: Option[BlockWidget] = None,
    /// The horizontal axis
    x_axis: ChartWidget.Axis = ChartWidget.Axis.default,
    /// The vertical axis
    y_axis: ChartWidget.Axis = ChartWidget.Axis.default,
    /// A reference to the datasets
    datasets: Array[ChartWidget.Dataset],
    /// The widget base style
    style: Style = Style.DEFAULT,
    /// Set the constraints used to determine whether the legend should be shown or not.
    ///
    /// # Examples
    ///
    /// ```
    /// # use tui.widgets.Chart
    /// # use tui.layout.Constraint
    /// val constraints = (
    ///     Constraint.Ratio(1, 3),
    ///     Constraint.Ratio(1, 4)
    /// )
    /// // Hide the legend when either its width is greater than 33% of the total widget width
    /// // or if its height is greater than 25% of the total widget height.
    /// val _chart: Chart = Chart.new(vec![])
    ///     .hidden_legend_constraints(constraints)
    /// ```
    hidden_legend_constraints: (Constraint, Constraint) = (Constraint.Ratio(1, 4), Constraint.Ratio(1, 4))
) extends Widget {
  /// Compute the internal layout of the chart given the area. If the area is too small some
  /// elements may be automatically hidden
  def layout(area: Rect): ChartWidget.ChartLayout = {
    var layout = ChartWidget.ChartLayout.default
    if (area.height == 0 || area.width == 0) {
      return layout
    }
    var x = area.left
    var y = area.bottom - 1

    if (x_axis.labels.isDefined && y > area.top) {
      layout = layout.copy(label_x = Some(y))
      y -= 1
    }

    layout = layout.copy(label_y = this.y_axis.labels.map(_ => x))
    x += this.max_width_of_labels_left_of_y_axis(area, this.y_axis.labels.isDefined)

    if (this.x_axis.labels.isDefined && y > area.top) {
      layout = layout.copy(axis_x = Some(y))
      y -= 1
    }

    if (this.y_axis.labels.isDefined && x + 1 < area.right) {
      layout = layout.copy(axis_y = Some(x))
      x += 1
    }

    if (x < area.right && y > 1) {
      layout = layout.copy(graph_area = Rect(x, area.top, area.right - x, y - area.top + 1))
    }

    this.x_axis.title match {
      case None => ()
      case Some(title) =>
        val w = title.width
        if (w < layout.graph_area.width && layout.graph_area.height > 2) {
          layout = layout.copy(title_x = Some((x + layout.graph_area.width - w, y)))
        }
    }

    this.y_axis.title match {
      case None => ()
      case Some(title) =>
        val w = title.width
        if (w + 1 < layout.graph_area.width && layout.graph_area.height > 2) {
          layout = layout.copy(title_y = Some((x, area.top)))
        }
    }

    this.datasets.map(x => Grapheme(x.name).width).maxOption match {
      case None => ()
      case Some(inner_width) =>
        val legend_width = inner_width + 2
        val legend_height = this.datasets.length + 2
        val max_legend_width = this.hidden_legend_constraints._1(layout.graph_area.width)
        val max_legend_height = this.hidden_legend_constraints._2(layout.graph_area.height)
        if (inner_width > 0 && legend_width < max_legend_width && legend_height < max_legend_height) {
          val rect = Rect(
            x = layout.graph_area.right - legend_width,
            y = layout.graph_area.top,
            width = legend_width,
            height = legend_height
          )
          layout = layout.copy(legend_area = Some(rect))
        }
    }
    layout
  }

  def max_width_of_labels_left_of_y_axis(area: Rect, has_y_axis: Boolean): Int = {
    var max_width = this.y_axis.labels.flatMap(labels => labels.map(_.width).maxOption).getOrElse(0)

    this.x_axis.labels.flatMap(labels => labels.headOption) match {
      case None => ()
      case Some(first_x_label) =>
        val first_label_width = Grapheme(first_x_label.content).width
        val width_left_of_y_axis = this.x_axis.labels_alignment match {
          case Alignment.Left =>
            // The last character of the label should be below the Y-Axis when it exists, not on its left
            val y_axis_offset = if (has_y_axis) 1 else 0
            first_label_width.saturating_sub_unsigned(y_axis_offset)
          case Alignment.Center => first_label_width / 2
          case Alignment.Right  => 0
        }
        max_width = math.max(max_width, width_left_of_y_axis)
    }
    // labels of y axis and first label of x axis can take at most 1/3rd of the total width
    max_width.min(area.width / 3)
  }

  def render_x_labels(buf: Buffer, layout: ChartWidget.ChartLayout, chart_area: Rect, graph_area: Rect): Unit = {
    val y = layout.label_x match {
      case Some(y) => y
      case None    => return
    }
    val labels = this.x_axis.labels.getOrElse(Array.empty[Span])
    val labels_len = labels.length
    if (labels_len < 2) {
      return
    }

    val width_between_ticks = graph_area.width / labels_len

    val label_area = this.first_x_label_area(y, labels.head.width, width_between_ticks, chart_area, graph_area)

    val label_alignment = this.x_axis.labels_alignment match {
      case Alignment.Left   => Alignment.Right
      case Alignment.Center => Alignment.Center
      case Alignment.Right  => Alignment.Left
    }

    render_label(buf, labels.head, label_area, label_alignment)

    ranges.range(0, labels.length /* first and last not rendered  here */ - 2) { i =>
      val label = labels(i + 1)
      // We add 1 to x (and width-1 below) to leave at least one space before each intermediate labels
      val x = graph_area.left + (i + 1) * width_between_ticks + 1
      val label_area = Rect(x, y, width_between_ticks.saturating_sub_unsigned(1), 1)

      render_label(buf, label, label_area, Alignment.Center)
      ()
    }

    val x = graph_area.right - width_between_ticks
    val label_area1 = Rect(x, y, width_between_ticks, 1)
    // The last label should be aligned Right to be at the edge of the graph area
    render_label(buf, labels.last, label_area1, Alignment.Right)
    ()
  }

  def first_x_label_area(y: Int, label_width: Int, max_width_after_y_axis: Int, chart_area: Rect, graph_area: Rect): Rect = {
    val (min_x, max_x) = this.x_axis.labels_alignment match {
      case Alignment.Left => (chart_area.left, graph_area.left)
      case Alignment.Center =>
        (
          chart_area.left,
          graph_area.left + max_width_after_y_axis.min(label_width)
        )
      case Alignment.Right =>
        (
          graph_area.left.saturating_sub_unsigned(1),
          graph_area.left + max_width_after_y_axis
        )
    }

    Rect(min_x, y, max_x - min_x, 1)
  }

  def render_label(buf: Buffer, label: Span, label_area: Rect, alignment: Alignment): (Int, Int) = {
    val label_width = label.width
    val bounded_label_width = label_area.width.min(label_width)

    val x = alignment match {
      case Alignment.Left   => label_area.left
      case Alignment.Center => label_area.left + label_area.width / 2 - bounded_label_width / 2
      case Alignment.Right  => label_area.right - bounded_label_width
    }

    buf.set_span(x, label_area.top, label, bounded_label_width)
  }

  def render_y_labels(buf: Buffer, layout: ChartWidget.ChartLayout, chart_area: Rect, graph_area: Rect): Unit = {
    val x = layout.label_y match {
      case Some(x) => x
      case None    => return
    }
    val labels = this.y_axis.labels.getOrElse(Array.empty[Span])
    val labels_len = labels.length
    ranges.range(0, labels.length) { i =>
      val label = labels(i)
      val dy = i * (graph_area.height - 1) / (labels_len - 1)
      if (dy < graph_area.bottom) {
        val label_area = Rect(
          x,
          graph_area.bottom.saturating_sub_unsigned(1) - dy,
          (graph_area.left - chart_area.left).saturating_sub_unsigned(1),
          1
        )
        render_label(buf, label, label_area, this.y_axis.labels_alignment)
        ()
      }
    }
  }

  override def render(area: Rect, buf: Buffer): Unit = {
    if (area.area == 0) {
      return
    }
    buf.set_style(area, this.style)
    // Sample the style of the entire widget. This sample will be used to reset the style of
    // the cells that are part of the components put on top of the grah area (i.e legend and
    // axis names).
    val original_style = buf.get(area.left, area.top).style

    val chart_area = this.block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.render(area, buf)
        inner_area
      case None => area
    }

    val layout = this.layout(chart_area)
    val graph_area = layout.graph_area
    if (graph_area.width < 1 || graph_area.height < 1) {
      return
    }

    this.render_x_labels(buf, layout, chart_area, graph_area)
    this.render_y_labels(buf, layout, chart_area, graph_area)

    layout.axis_x match {
      case None => ()
      case Some(y) =>
        ranges.range(graph_area.left, graph_area.right) { x =>
          buf.get(x, y).set_symbol(symbols.line.HORIZONTAL).set_style(this.x_axis.style)
          ()
        }
    }

    layout.axis_y match {
      case None => ()
      case Some(x) =>
        ranges.range(graph_area.top, graph_area.bottom) { y =>
          buf.get(x, y).set_symbol(symbols.line.VERTICAL).set_style(this.y_axis.style)
          ()
        }
    }

    layout.axis_x match {
      case None => ()
      case Some(y) =>
        layout.axis_y match {
          case None    => ()
          case Some(x) => buf.get(x, y).set_symbol(symbols.line.BOTTOM_LEFT).set_style(this.x_axis.style)
        }
    }

    this.datasets.foreach { dataset =>
      CanvasWidget(
        background_color = this.style.bg.getOrElse(Color.Reset),
        x_bounds = this.x_axis.bounds,
        y_bounds = this.y_axis.bounds,
        marker = dataset.marker,
        painter = Some { ctx =>
          val points = Points(coords = dataset.data, color = dataset.style.fg.getOrElse(Color.Reset))
          ctx.draw(points)
          dataset.graph_type match {
            case ChartWidget.GraphType.Scatter => ()
            case ChartWidget.GraphType.Line =>
              dataset.data.sliding(2).foreach {
                case Array(one, two) =>
                  val line = Line(x1 = one.x, y1 = one.y, x2 = two.x, y2 = two.y, color = dataset.style.fg.getOrElse(Color.Reset))
                  ctx.draw(line)
                case _ => ()
              }
          }
        }
      )
        .render(graph_area, buf)
    }

    layout.legend_area match {
      case None => ()
      case Some(legend_area) =>
        buf.set_style(legend_area, original_style)
        BlockWidget(borders = Borders.ALL).render(legend_area, buf)
        ranges.range(0, this.datasets.length) { i =>
          val dataset = this.datasets(i)
          buf.set_string(
            legend_area.x + 1,
            legend_area.y + 1 + i,
            dataset.name,
            dataset.style
          )
          ()
        }
    }

    layout.title_x match {
      case None => ()
      case Some((x, y)) =>
        val title = this.x_axis.title.get
        val width = graph_area.right.saturating_sub_unsigned(x)
        buf.set_style(Rect(x, y, width, height = 1), original_style)
        buf.set_spans(x, y, title, width)
    }

    layout.title_y match {
      case None => ()
      case Some((x, y)) =>
        val title = this.y_axis.title.get
        val width = graph_area.right.saturating_sub_unsigned(x)
        buf.set_style(Rect(x, y, width, height = 1), original_style)
        buf.set_spans(x, y, title, width)
        ()
    }
  }
}

object ChartWidget {
  /// An X or Y axis for the chart widget
  case class Axis(
      /// Title displayed next to axis end
      title: Option[Spans] = None,
      /// Bounds for the axis (all data points outside these limits will not be represented)
      bounds: Point = Point.Zero,
      /// A list of labels to put to the left or below the axis
      labels: Option[Array[Span]] = None,
      /// The style used to draw the axis itself
      style: Style = Style.DEFAULT,
      /// Defines the alignment of the labels of the axis.
      /// The alignment behaves differently based on the axis:
      /// - Y-Axis: The labels are aligned within the area on the left of the axis
      /// - X-Axis: The first X-axis label is aligned relative to the Y-axis
      labels_alignment: Alignment = Alignment.Left
  )

  object Axis {
    val default = Axis()
  }

  /// Used to determine which style of graphing to use
  sealed trait GraphType

  object GraphType {
    /// Draw each point
    case object Scatter extends GraphType

    /// Draw each point and lines between each point using the same marker
    case object Line extends GraphType
  }

  /// A group of data points
  case class Dataset(
      /// Name of the dataset (used in the legend if shown)
      name: String = "",
      /// A reference to the actual data
      data: Array[Point] = Array.empty,
      /// Symbol used for each points of this dataset
      marker: symbols.Marker = symbols.Marker.Dot,
      /// Determines graph type used for drawing points
      graph_type: GraphType = GraphType.Scatter,
      /// Style used to plot this dataset
      style: Style = Style.DEFAULT
  )

  /// A container that holds all the infos about where to display each elements of the chart (axis,
  /// labels, legend, ...).
  case class ChartLayout(
      /// Location of the title of the x axis
      title_x: Option[(Int, Int)] = None,
      /// Location of the title of the y axis
      title_y: Option[(Int, Int)] = None,
      /// Location of the first label of the x axis
      label_x: Option[Int] = None,
      /// Location of the first label of the y axis
      label_y: Option[Int] = None,
      /// Y coordinate of the horizontal axis
      axis_x: Option[Int] = None,
      /// X coordinate of the vertical axis
      axis_y: Option[Int] = None,
      /// Area of the legend
      legend_area: Option[Rect] = None,
      /// Area of the graph
      graph_area: Rect = Rect.default
  )

  object ChartLayout {
    val default = ChartLayout()
  }
}
