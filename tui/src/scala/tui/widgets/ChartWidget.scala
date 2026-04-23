package tui
package widgets

import tui.Style
import tui.internal.ranges
import tui.internal.saturating._
import tui.widgets.canvas.{CanvasWidget, Line, Points}

/** A widget to plot one or more dataset in a cartesian coordinate system
  *
  * @param datasets
  *   A reference to the datasets
  * @param block
  *   A block to display around the widget eventually
  * @param xAxis
  *   The horizontal axis
  * @param yAxis
  *   The vertical axis
  * @param style
  *   The widget base style
  * @param hiddenLegendConstraints
  *   Set the constraints used to determine whether the legend should be shown or not.
  */
case class ChartWidget(
    datasets: Array[ChartWidget.Dataset],
    xAxis: ChartWidget.Axis = ChartWidget.Axis.default,
    yAxis: ChartWidget.Axis = ChartWidget.Axis.default,
    style: Style = Style.DEFAULT,
    hiddenLegendConstraints: (Constraint, Constraint) = (Constraint.Ratio(1, 4), Constraint.Ratio(1, 4))
) extends Widget {

  /** Compute the internal layout of the chart given the area. If the area is too small some elements may be automatically hidden
    */
  def layout(area: Rect): ChartWidget.ChartLayout = {
    var layout = ChartWidget.ChartLayout.default
    if (area.height == 0 || area.width == 0) {
      return layout
    }
    var x = area.left
    var y = area.bottom - 1

    if (xAxis.labels.isDefined && y > area.top) {
      layout = layout.copy(labelX = Some(y))
      y -= 1
    }

    layout = layout.copy(labelY = this.yAxis.labels.map(_ => x))
    x += this.maxWidthOfLabelsLeftOfYAxis(area, this.yAxis.labels.isDefined)

    if (this.xAxis.labels.isDefined && y > area.top) {
      layout = layout.copy(axisX = Some(y))
      y -= 1
    }

    if (this.yAxis.labels.isDefined && x + 1 < area.right) {
      layout = layout.copy(axisY = Some(x))
      x += 1
    }

    if (x < area.right && y > 1) {
      layout = layout.copy(graphArea = Rect(x, area.top, area.right - x, y - area.top + 1))
    }

    this.xAxis.title match {
      case None => ()
      case Some(title) =>
        val w = title.width
        if (w < layout.graphArea.width && layout.graphArea.height > 2) {
          layout = layout.copy(titleX = Some((x + layout.graphArea.width - w, y)))
        }
    }

    this.yAxis.title match {
      case None => ()
      case Some(title) =>
        val w = title.width
        if (w + 1 < layout.graphArea.width && layout.graphArea.height > 2) {
          layout = layout.copy(titleY = Some((x, area.top)))
        }
    }

    this.datasets.map(x => Grapheme(x.name).width).maxOption match {
      case None => ()
      case Some(inner_width) =>
        val legend_width = inner_width + 2
        val legend_height = this.datasets.length + 2
        val max_legend_width = this.hiddenLegendConstraints._1(layout.graphArea.width)
        val max_legend_height = this.hiddenLegendConstraints._2(layout.graphArea.height)
        if (inner_width > 0 && legend_width < max_legend_width && legend_height < max_legend_height) {
          val rect = Rect(
            x = layout.graphArea.right - legend_width,
            y = layout.graphArea.top,
            width = legend_width,
            height = legend_height
          )
          layout = layout.copy(legendArea = Some(rect))
        }
    }
    layout
  }

  def maxWidthOfLabelsLeftOfYAxis(area: Rect, has_y_axis: Boolean): Int = {
    var max_width = this.yAxis.labels.flatMap(labels => labels.map(_.width).maxOption).getOrElse(0)

    this.xAxis.labels.flatMap(labels => labels.headOption) match {
      case None => ()
      case Some(first_x_label) =>
        val first_label_width = Grapheme(first_x_label.content).width
        val width_left_of_y_axis = this.xAxis.labelsAlignment match {
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

  def renderXLabels(buf: Buffer, layout: ChartWidget.ChartLayout, chartArea: Rect, graphArea: Rect): Unit = {
    val y = layout.labelX match {
      case Some(y) => y
      case None    => return
    }
    val labels = this.xAxis.labels.getOrElse(Array.empty[Span])
    val labels_len = labels.length
    if (labels_len < 2) {
      return
    }

    val width_between_ticks = graphArea.width / labels_len

    val label_area = this.firstXLabelArea(y, labels.head.width, width_between_ticks, chartArea, graphArea)

    val label_alignment = this.xAxis.labelsAlignment match {
      case Alignment.Left   => Alignment.Right
      case Alignment.Center => Alignment.Center
      case Alignment.Right  => Alignment.Left
    }

    renderLabel(buf, labels.head, label_area, label_alignment)

    ranges.range(0, labels.length /* first and last not rendered  here */ - 2) { i =>
      val label = labels(i + 1)
      // We add 1 to x (and width-1 below) to leave at least one space before each intermediate labels
      val x = graphArea.left + (i + 1) * width_between_ticks + 1
      val label_area = Rect(x, y, width_between_ticks.saturating_sub_unsigned(1), 1)

      renderLabel(buf, label, label_area, Alignment.Center)
      ()
    }

    val x = graphArea.right - width_between_ticks
    val label_area1 = Rect(x, y, width_between_ticks, 1)
    // The last label should be aligned Right to be at the edge of the graph area
    renderLabel(buf, labels.last, label_area1, Alignment.Right)
    ()
  }

  def firstXLabelArea(y: Int, labelWidth: Int, maxWidthAfterYAxis: Int, chartArea: Rect, graphArea: Rect): Rect = {
    val (min_x, max_x) = this.xAxis.labelsAlignment match {
      case Alignment.Left => (chartArea.left, graphArea.left)
      case Alignment.Center =>
        (
          chartArea.left,
          graphArea.left + maxWidthAfterYAxis.min(labelWidth)
        )
      case Alignment.Right =>
        (
          graphArea.left.saturating_sub_unsigned(1),
          graphArea.left + maxWidthAfterYAxis
        )
    }

    Rect(min_x, y, max_x - min_x, 1)
  }

  def renderLabel(buf: Buffer, label: Span, labelArea: Rect, alignment: Alignment): (Int, Int) = {
    val label_width = label.width
    val bounded_label_width = labelArea.width.min(label_width)

    val x = alignment match {
      case Alignment.Left   => labelArea.left
      case Alignment.Center => labelArea.left + labelArea.width / 2 - bounded_label_width / 2
      case Alignment.Right  => labelArea.right - bounded_label_width
    }

    buf.setSpan(x, labelArea.top, label, bounded_label_width)
  }

  def renderYLabels(buf: Buffer, layout: ChartWidget.ChartLayout, chartArea: Rect, graphArea: Rect): Unit = {
    val x = layout.labelY match {
      case Some(x) => x
      case None    => return
    }
    val labels = this.yAxis.labels.getOrElse(Array.empty[Span])
    val labels_len = labels.length
    ranges.range(0, labels.length) { i =>
      val label = labels(i)
      val dy = i * (graphArea.height - 1) / (labels_len - 1)
      if (dy < graphArea.bottom) {
        val label_area = Rect(
          x,
          graphArea.bottom.saturating_sub_unsigned(1) - dy,
          (graphArea.left - chartArea.left).saturating_sub_unsigned(1),
          1
        )
        renderLabel(buf, label, label_area, this.yAxis.labelsAlignment)
        ()
      }
    }
  }

  override def render(area: Rect, buf: Buffer): Unit = {
    if (area.area == 0) {
      return
    }
    buf.setStyle(area, this.style)
    // Sample the style of the entire widget. This sample will be used to reset the style of
    // the cells that are part of the components put on top of the grah area (i.e legend and
    // axis names).
    val original_style = buf.get(area.left, area.top).style

    val layout = this.layout(area)
    val graph_area = layout.graphArea
    if (graph_area.width < 1 || graph_area.height < 1) {
      return
    }

    this.renderXLabels(buf, layout, area, graph_area)
    this.renderYLabels(buf, layout, area, graph_area)

    layout.axisX match {
      case None => ()
      case Some(y) =>
        ranges.range(graph_area.left, graph_area.right) { x =>
          buf.get(x, y).setSymbol(symbols.line.HORIZONTAL).setStyle(this.xAxis.style)
          ()
        }
    }

    layout.axisY match {
      case None => ()
      case Some(x) =>
        ranges.range(graph_area.top, graph_area.bottom) { y =>
          buf.get(x, y).setSymbol(symbols.line.VERTICAL).setStyle(this.yAxis.style)
          ()
        }
    }

    layout.axisX match {
      case None => ()
      case Some(y) =>
        layout.axisY match {
          case None    => ()
          case Some(x) => buf.get(x, y).setSymbol(symbols.line.BOTTOM_LEFT).setStyle(this.xAxis.style)
        }
    }

    this.datasets.foreach { dataset =>
      CanvasWidget(
        backgroundColor = original_style.bg.getOrElse(Color.Reset),
        xBounds = this.xAxis.bounds,
        yBounds = this.yAxis.bounds,
        marker = dataset.marker
      ) { ctx =>
        val points = Points(coords = dataset.data, color = dataset.style.fg.getOrElse(Color.Reset))
        ctx.draw(points)
        dataset.graphType match {
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
        .render(graph_area, buf)
    }

    layout.legendArea match {
      case None => ()
      case Some(legend_area) =>
        buf.setStyle(legend_area, original_style)
        BlockWidget.noChildren(borders = Borders.ALL).render(legend_area, buf)
        ranges.range(0, this.datasets.length) { i =>
          val dataset = this.datasets(i)
          buf.setString(
            legend_area.x + 1,
            legend_area.y + 1 + i,
            dataset.name,
            dataset.style
          )
          ()
        }
    }

    layout.titleX match {
      case None => ()
      case Some((x, y)) =>
        val title = this.xAxis.title.get
        val width = graph_area.right.saturating_sub_unsigned(x)
        buf.setStyle(Rect(x, y, width, height = 1), original_style)
        buf.setSpans(x, y, title, width)
    }

    layout.titleY match {
      case None => ()
      case Some((x, y)) =>
        val title = this.yAxis.title.get
        val width = graph_area.right.saturating_sub_unsigned(x)
        buf.setStyle(Rect(x, y, width, height = 1), original_style)
        buf.setSpans(x, y, title, width)
        ()
    }
  }
}

object ChartWidget {

  /** An X or Y axis for the chart widget
    * @param title
    *   Title displayed next to axis end
    * @param bounds
    *   Bounds for the axis (all data points outside these limits will not be represented)
    * @param labels
    *   A list of labels to put to the left or below the axis
    * @param style
    *   The style used to draw the axis itself
    * @param labelsAlignment
    *   Defines the alignment of the labels of the axis. The alignment behaves differently based on the axis:
    *   - Y-Axis: The labels are aligned within the area on the left of the axis
    *   - X-Axis: The first X-axis label is aligned relative to the Y-axis
    */
  case class Axis(
      title: Option[Spans] = None,
      bounds: Point = Point.Zero,
      labels: Option[Array[Span]] = None,
      style: Style = Style.DEFAULT,
      labelsAlignment: Alignment = Alignment.Left
  )

  object Axis {
    val default: Axis = Axis()
  }

  /** Used to determine which style of graphing to use
    */
  sealed trait GraphType

  object GraphType {

    /** Draw each point
      */
    case object Scatter extends GraphType

    /** Draw each point and lines between each point using the same marker
      */
    case object Line extends GraphType
  }

  /** A group of data points
    *
    * @param name
    *   Name of the dataset (used in the legend if shown)
    * @param data
    *   A reference to the actual data
    * @param marker
    *   Symbol used for each points of this dataset
    * @param graphType
    *   Determines graph type used for drawing points
    * @param style
    *   Style used to plot this dataset
    */
  case class Dataset(
      name: String = "",
      data: Array[Point] = Array.empty,
      marker: symbols.Marker = symbols.Marker.Dot,
      graphType: GraphType = GraphType.Scatter,
      style: Style = Style.DEFAULT
  )

  /** A container that holds all the infos about where to display each elements of the chart (axis, labels, legend, ...).
    *
    * @param titleX
    *   Location of the title of the x axis
    * @param titleY
    *   Location of the title of the y axis
    * @param labelX
    *   Location of the first label of the x axis
    * @param labelY
    *   Location of the first label of the y axis
    * @param axisX
    *   Y coordinate of the horizontal axis
    * @param axisY
    *   X coordinate of the vertical axis
    * @param legendArea
    *   Area of the legend
    * @param graphArea
    *   Area of the graph
    */
  case class ChartLayout(
      titleX: Option[(Int, Int)] = None,
      titleY: Option[(Int, Int)] = None,
      labelX: Option[Int] = None,
      labelY: Option[Int] = None,
      axisX: Option[Int] = None,
      axisY: Option[Int] = None,
      legendArea: Option[Rect] = None,
      graphArea: Rect = Rect.default
  )

  object ChartLayout {
    val default: ChartLayout = ChartLayout()
  }
}
