package jatatui.widgets.chart;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Flex;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.symbols.Line;
import jatatui.core.widgets.Widget;
import jatatui.widgets.block.Block;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Coord;
import jatatui.widgets.canvas.Points;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A widget to plot one or more [Dataset]s in a Cartesian coordinate system.
///
/// To use this widget, start by creating one or more [Dataset]s, then build the [Axis] objects,
/// then pass everything to [Chart#of(List)] (and its `withFoo` builders).
public final class Chart implements Widget, Stylize<Chart> {

  private final Optional<Block> block;
  private final Axis xAxis;
  private final Axis yAxis;
  private final List<Dataset> datasets;
  private final Style style;
  private final HiddenLegendConstraints hiddenLegendConstraints;
  private final Optional<LegendPosition> legendPosition;

  private Chart(
      Optional<Block> block,
      Axis xAxis,
      Axis yAxis,
      List<Dataset> datasets,
      Style style,
      HiddenLegendConstraints hiddenLegendConstraints,
      Optional<LegendPosition> legendPosition) {
    this.block = block;
    this.xAxis = xAxis;
    this.yAxis = yAxis;
    this.datasets = List.copyOf(datasets);
    this.style = style;
    this.hiddenLegendConstraints = hiddenLegendConstraints;
    this.legendPosition = legendPosition;
  }

  // ---- Constructors ----

  /// Creates an empty chart with no datasets and the default axes / legend constraints.
  public static Chart empty() {
    return new Chart(
        Optional.empty(),
        Axis.empty(),
        Axis.empty(),
        List.of(),
        Style.empty(),
        HiddenLegendConstraints.DEFAULT,
        Optional.of(LegendPosition.defaultPosition()));
  }

  /// Creates a chart with the given datasets.
  public static Chart of(List<Dataset> datasets) {
    return new Chart(
        Optional.empty(),
        Axis.empty(),
        Axis.empty(),
        datasets,
        Style.empty(),
        HiddenLegendConstraints.DEFAULT,
        Optional.of(LegendPosition.defaultPosition()));
  }

  /// Creates a chart with the given datasets.
  public static Chart of(Dataset... datasets) {
    return of(List.of(datasets));
  }

  // ---- Builders ----

  /// Wraps the chart with the given [Block].
  public Chart withBlock(Block block) {
    return new Chart(
        Optional.of(block), xAxis, yAxis, datasets, style, hiddenLegendConstraints, legendPosition);
  }

  /// Sets the style of the entire chart.
  public Chart withStyle(Style style) {
    return new Chart(block, xAxis, yAxis, datasets, style, hiddenLegendConstraints, legendPosition);
  }

  /// Sets the X [Axis].
  public Chart withXAxis(Axis axis) {
    return new Chart(block, axis, yAxis, datasets, style, hiddenLegendConstraints, legendPosition);
  }

  /// Sets the Y [Axis].
  public Chart withYAxis(Axis axis) {
    return new Chart(block, xAxis, axis, datasets, style, hiddenLegendConstraints, legendPosition);
  }

  /// Sets the constraints used to determine whether the legend should be shown.
  public Chart withHiddenLegendConstraints(HiddenLegendConstraints constraints) {
    return new Chart(block, xAxis, yAxis, datasets, style, constraints, legendPosition);
  }

  /// Sets the position of the legend (or hides it altogether by passing [Optional#empty()]).
  public Chart withLegendPosition(Optional<LegendPosition> position) {
    return new Chart(block, xAxis, yAxis, datasets, style, hiddenLegendConstraints, position);
  }

  /// Convenience overload setting the legend position to a non-empty value.
  public Chart withLegendPosition(LegendPosition position) {
    return withLegendPosition(Optional.of(position));
  }

  // ---- Accessors ----

  public Optional<Block> block() {
    return block;
  }

  public Axis xAxis() {
    return xAxis;
  }

  public Axis yAxis() {
    return yAxis;
  }

  public List<Dataset> datasets() {
    return datasets;
  }

  public HiddenLegendConstraints hiddenLegendConstraints() {
    return hiddenLegendConstraints;
  }

  public Optional<LegendPosition> legendPosition() {
    return legendPosition;
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Chart setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Internal layout ----

  /// A container that holds info about where to display each element of the chart.
  ///
  /// Mirrors upstream `ChartLayout` exactly. All optional positions become [Optional]s; the
  /// graph area is always present. This class is exposed only to support tests; widget consumers
  /// should not depend on it.
  public static final class ChartLayout {
    public final Optional<Position> titleX;
    public final Optional<Position> titleY;
    public final Optional<Integer> labelX;
    public final Optional<Integer> labelY;
    public final Optional<Integer> axisX;
    public final Optional<Integer> axisY;
    public final Optional<Rect> legendArea;
    public final Rect graphArea;

    ChartLayout(
        Optional<Position> titleX,
        Optional<Position> titleY,
        Optional<Integer> labelX,
        Optional<Integer> labelY,
        Optional<Integer> axisX,
        Optional<Integer> axisY,
        Optional<Rect> legendArea,
        Rect graphArea) {
      this.titleX = titleX;
      this.titleY = titleY;
      this.labelX = labelX;
      this.labelY = labelY;
      this.axisX = axisX;
      this.axisY = axisY;
      this.legendArea = legendArea;
      this.graphArea = graphArea;
    }
  }

  /// Compute the internal layout of the chart given the area. Returns [Optional#empty()] when
  /// the area is zero-sized.
  public Optional<ChartLayout> layout(Rect area) {
    if (area.height() == 0 || area.width() == 0) return Optional.empty();
    int x = area.left();
    int y = area.bottom() - 1;

    Optional<Integer> labelX = Optional.empty();
    if (!xAxis.labels.isEmpty() && y > area.top()) {
      labelX = Optional.of(y);
      y -= 1;
    }

    Optional<Integer> labelY = yAxis.labels.isEmpty() ? Optional.empty() : Optional.of(x);
    x += maxWidthOfLabelsLeftOfYAxis(area, !yAxis.labels.isEmpty());

    Optional<Integer> axisX = Optional.empty();
    if (!xAxis.labels.isEmpty() && y > area.top()) {
      axisX = Optional.of(y);
      y -= 1;
    }

    Optional<Integer> axisY = Optional.empty();
    if (!yAxis.labels.isEmpty() && x + 1 < area.right()) {
      axisY = Optional.of(x);
      x += 1;
    }

    int graphWidth = saturatingSub(area.right(), x);
    int graphHeight = saturatingAdd(saturatingSub(y, area.top()), 1);
    if (graphWidth == 0 || graphHeight == 0) {
      // Mirrors upstream debug_assert_ne — small areas should already have hidden axis & labels.
      return Optional.empty();
    }
    Rect graphArea = new Rect(x, area.top(), graphWidth, graphHeight);

    Optional<Position> titleX = Optional.empty();
    if (xAxis.title.isPresent()) {
      int w = xAxis.title.get().width();
      if (w < graphArea.width() && graphArea.height() > 2) {
        titleX = Optional.of(new Position(x + graphArea.width() - w, y));
      }
    }

    Optional<Position> titleY = Optional.empty();
    if (yAxis.title.isPresent()) {
      int w = yAxis.title.get().width();
      if (w + 1 < graphArea.width() && graphArea.height() > 2) {
        titleY = Optional.of(new Position(x, area.top()));
      }
    }

    Optional<Rect> legendArea = Optional.empty();
    if (legendPosition.isPresent()) {
      LegendPosition lp = legendPosition.get();
      List<Integer> legends = new ArrayList<>();
      for (Dataset d : datasets) {
        if (d.name.isPresent()) legends.add(d.name.get().width());
      }

      if (!legends.isEmpty()) {
        int innerWidth = 0;
        for (int n : legends) if (n > innerWidth) innerWidth = n;
        int legendWidth = innerWidth + 2;
        int legendHeight = legends.size() + 2;

        Rect[] horizSplit =
            Layout.horizontal(hiddenLegendConstraints.width())
                .withFlex(Flex.Start)
                .areas(graphArea, 1);
        Rect maxLegendWidthRect = horizSplit[0];

        Rect[] vertSplit =
            Layout.vertical(hiddenLegendConstraints.height())
                .withFlex(Flex.Start)
                .areas(graphArea, 1);
        Rect maxLegendHeightRect = vertSplit[0];

        if (innerWidth > 0
            && legendWidth <= maxLegendWidthRect.width()
            && legendHeight <= maxLegendHeightRect.height()) {
          int xTitleWidth = 0;
          if (titleX.isPresent() && xAxis.title.isPresent()) {
            xTitleWidth = xAxis.title.get().width();
          }
          int yTitleWidth = 0;
          if (titleY.isPresent() && yAxis.title.isPresent()) {
            yTitleWidth = yAxis.title.get().width();
          }
          legendArea = lp.layout(graphArea, legendWidth, legendHeight, xTitleWidth, yTitleWidth);
        }
      }
    }
    return Optional.of(
        new ChartLayout(titleX, titleY, labelX, labelY, axisX, axisY, legendArea, graphArea));
  }

  private int maxWidthOfLabelsLeftOfYAxis(Rect area, boolean hasYAxis) {
    int maxWidth = 0;
    for (jatatui.core.text.Line l : yAxis.labels) {
      int w = l.width();
      if (w > maxWidth) maxWidth = w;
    }

    if (!xAxis.labels.isEmpty()) {
      jatatui.core.text.Line firstXLabel = xAxis.labels.get(0);
      int firstLabelWidth = firstXLabel.width();
      int widthLeftOfYAxis =
          switch (xAxis.labelsAlignment) {
            case Left -> {
              int yAxisOffset = hasYAxis ? 1 : 0;
              yield saturatingSub(firstLabelWidth, yAxisOffset);
            }
            case Center -> firstLabelWidth / 2;
            case Right -> 0;
          };
      if (widthLeftOfYAxis > maxWidth) maxWidth = widthLeftOfYAxis;
    }
    return Math.min(maxWidth, area.width() / 3);
  }

  private void renderXLabels(Buffer buf, ChartLayout layout, Rect chartArea, Rect graphArea) {
    if (layout.labelX.isEmpty()) return;
    int y = layout.labelX.get();
    List<jatatui.core.text.Line> labels = xAxis.labels;
    int labelsLen = labels.size();
    if (labelsLen < 2) return;

    int widthBetweenTicks = graphArea.width() / labelsLen;

    Rect labelArea =
        firstXLabelArea(y, labels.get(0).width(), widthBetweenTicks, chartArea, graphArea);

    HorizontalAlignment labelAlignment =
        switch (xAxis.labelsAlignment) {
          case Left -> HorizontalAlignment.Right;
          case Center -> HorizontalAlignment.Center;
          case Right -> HorizontalAlignment.Left;
        };

    renderLabel(buf, labels.get(0), labelArea, labelAlignment);

    for (int i = 0; i + 2 < labels.size(); i++) {
      jatatui.core.text.Line label = labels.get(i + 1);
      int x = graphArea.left() + (i + 1) * widthBetweenTicks + 1;
      Rect la = new Rect(x, y, saturatingSub(widthBetweenTicks, 1), 1);
      renderLabel(buf, label, la, HorizontalAlignment.Center);
    }

    int lastX = graphArea.right() - widthBetweenTicks;
    Rect lastArea = new Rect(lastX, y, widthBetweenTicks, 1);
    renderLabel(buf, labels.get(labels.size() - 1), lastArea, HorizontalAlignment.Right);
  }

  private Rect firstXLabelArea(
      int y, int labelWidth, int maxWidthAfterYAxis, Rect chartArea, Rect graphArea) {
    int minX;
    int maxX;
    switch (xAxis.labelsAlignment) {
      case Left -> {
        minX = chartArea.left();
        maxX = graphArea.left();
      }
      case Center -> {
        minX = chartArea.left();
        maxX = graphArea.left() + Math.min(maxWidthAfterYAxis, labelWidth);
      }
      case Right -> {
        minX = saturatingSub(graphArea.left(), 1);
        maxX = graphArea.left() + maxWidthAfterYAxis;
      }
      default -> throw new IllegalStateException("unreachable");
    }
    return new Rect(minX, y, saturatingSub(maxX, minX), 1);
  }

  /// Render a [jatatui.core.text.Line] into `area` using the requested alignment, overwriting
  /// the line's own alignment for this draw.
  private static void renderLabel(
      Buffer buf, jatatui.core.text.Line label, Rect area, HorizontalAlignment alignment) {
    jatatui.core.text.Line aligned =
        switch (alignment) {
          case Left -> label.leftAligned();
          case Center -> label.centered();
          case Right -> label.rightAligned();
        };
    renderLineWithAlignment(aligned, area, buf);
  }

  /// Renders a [jatatui.core.text.Line] into `area`, honouring the line's own alignment (the
  /// label's alignment is always set before this is called via [#renderLabel]).
  private static void renderLineWithAlignment(jatatui.core.text.Line line, Rect area, Buffer buf) {
    Rect clipped = area.intersection(buf.area);
    if (clipped.isEmpty()) return;
    Rect oneRow = new Rect(clipped.x(), clipped.y(), clipped.width(), 1);
    int lineWidth = line.width();
    if (lineWidth == 0) return;

    buf.setStyle(oneRow, line.style);

    HorizontalAlignment alignment = line.alignment.orElse(HorizontalAlignment.Left);
    int areaWidth = oneRow.width();
    if (lineWidth <= areaWidth) {
      int indent =
          switch (alignment) {
            case Center -> Math.max(0, areaWidth - lineWidth) / 2;
            case Right -> Math.max(0, areaWidth - lineWidth);
            case Left -> 0;
          };
      buf.setLine(oneRow.x() + indent, oneRow.y(), line, oneRow.width() - indent);
    } else {
      // Truncate from one side based on alignment. We approximate by simply writing as much as
      // fits.
      buf.setLine(oneRow.x(), oneRow.y(), line, oneRow.width());
    }
  }

  private void renderYLabels(Buffer buf, ChartLayout layout, Rect chartArea, Rect graphArea) {
    if (layout.labelY.isEmpty()) return;
    int x = layout.labelY.get();
    List<jatatui.core.text.Line> labels = yAxis.labels;
    int labelsLen = labels.size();
    for (int i = 0; i < labels.size(); i++) {
      jatatui.core.text.Line label = labels.get(i);
      int dy = i * (graphArea.height() - 1) / Math.max(1, labelsLen - 1);
      if (dy < graphArea.bottom()) {
        Rect labelArea =
            new Rect(
                x,
                saturatingSub(graphArea.bottom(), 1) - dy,
                saturatingSub(graphArea.left() - chartArea.left(), 1),
                1);
        renderLabel(buf, label, labelArea, yAxis.labelsAlignment);
      }
    }
  }

  // ---- Widget rendering ----

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    block.ifPresent(b -> b.render(area, buf));
    Rect chartArea = block.map(b -> b.inner(area)).orElse(area);
    Optional<ChartLayout> layoutOpt = layout(chartArea);
    if (layoutOpt.isEmpty()) return;
    ChartLayout layout = layoutOpt.get();
    Rect graphArea = layout.graphArea;

    // Sample the original style at the top-left to restore it on legend / axis names.
    Style originalStyle = buf.cellAt(area.left(), area.top()).style();

    renderXLabels(buf, layout, chartArea, graphArea);
    renderYLabels(buf, layout, chartArea, graphArea);

    if (layout.axisX.isPresent()) {
      int yLine = layout.axisX.get();
      for (int xx = graphArea.left(); xx < graphArea.right(); xx++) {
        buf.cellAt(xx, yLine).setSymbol(Line.HORIZONTAL).setStyle(xAxis.style);
      }
    }

    if (layout.axisY.isPresent()) {
      int xLine = layout.axisY.get();
      for (int yy = graphArea.top(); yy < graphArea.bottom(); yy++) {
        buf.cellAt(xLine, yy).setSymbol(Line.VERTICAL).setStyle(yAxis.style);
      }
    }

    if (layout.axisX.isPresent() && layout.axisY.isPresent()) {
      int yLine = layout.axisX.get();
      int xLine = layout.axisY.get();
      buf.cellAt(xLine, yLine).setSymbol(Line.BOTTOM_LEFT).setStyle(xAxis.style);
    }

    Color canvasBg = style.bg().orElse(Color.RESET);
    final List<Dataset> ds = datasets;
    Canvas canvas =
        Canvas.empty()
            .withBackgroundColor(canvasBg)
            .withXBounds(xAxis.bounds.toArray())
            .withYBounds(yAxis.bounds.toArray())
            .withPaintFn(
                ctx -> {
                  for (Dataset dataset : ds) {
                    ctx.marker(dataset.marker);
                    Color color = dataset.style.fg().orElse(Color.RESET);
                    Coord[] coords = new Coord[dataset.data.size()];
                    for (int i = 0; i < dataset.data.size(); i++) {
                      coords[i] = dataset.data.get(i).toCoord();
                    }
                    ctx.draw(new Points(coords, color));
                    switch (dataset.graphType) {
                      case Line -> {
                        for (int i = 0; i + 1 < dataset.data.size(); i++) {
                          Point p1 = dataset.data.get(i);
                          Point p2 = dataset.data.get(i + 1);
                          ctx.draw(
                              new jatatui.widgets.canvas.Line(
                                  p1.x(), p1.y(), p2.x(), p2.y(), color));
                        }
                      }
                      case Bar -> {
                        for (Point p : dataset.data) {
                          ctx.draw(
                              new jatatui.widgets.canvas.Line(p.x(), 0.0, p.x(), p.y(), color));
                        }
                      }
                      case Scatter -> {}
                    }
                  }
                });
    canvas.render(graphArea, buf);

    if (layout.titleX.isPresent()) {
      Position pos = layout.titleX.get();
      jatatui.core.text.Line title = xAxis.title.get();
      int width = Math.min(saturatingSub(graphArea.right(), pos.x()), title.width());
      buf.setStyle(new Rect(pos.x(), pos.y(), width, 1), originalStyle);
      buf.setLine(pos.x(), pos.y(), title, width);
    }

    if (layout.titleY.isPresent()) {
      Position pos = layout.titleY.get();
      jatatui.core.text.Line title = yAxis.title.get();
      int width = Math.min(saturatingSub(graphArea.right(), pos.x()), title.width());
      buf.setStyle(new Rect(pos.x(), pos.y(), width, 1), originalStyle);
      buf.setLine(pos.x(), pos.y(), title, width);
    }

    if (layout.legendArea.isPresent()) {
      Rect legendArea = layout.legendArea.get();
      buf.setStyle(legendArea, originalStyle);
      Block.bordered().render(legendArea, buf);

      int row = 0;
      for (Dataset ds2 : datasets) {
        if (ds2.name.isEmpty()) continue;
        jatatui.core.text.Line name = ds2.name.get().patchStyle(ds2.style);
        Rect nameArea =
            new Rect(legendArea.x() + 1, legendArea.y() + 1 + row, legendArea.width() - 2, 1);
        renderLineWithAlignment(name, nameArea, buf);
        row += 1;
      }
    }
  }

  // ---- equals / hashCode ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Chart other)) return false;
    return block.equals(other.block)
        && xAxis.equals(other.xAxis)
        && yAxis.equals(other.yAxis)
        && datasets.equals(other.datasets)
        && style.equals(other.style)
        && hiddenLegendConstraints.equals(other.hiddenLegendConstraints)
        && legendPosition.equals(other.legendPosition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        block, xAxis, yAxis, datasets, style, hiddenLegendConstraints, legendPosition);
  }

  private static int saturatingAdd(int a, int b) {
    long r = (long) a + (long) b;
    if (r > Position.U16_MAX) return Position.U16_MAX;
    if (r < 0) return 0;
    return (int) r;
  }

  private static int saturatingSub(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    return (int) r;
  }
}
