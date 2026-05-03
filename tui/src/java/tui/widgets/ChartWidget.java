package tui.widgets;

import java.util.Optional;
import tui.Alignment;
import tui.Borders;
import tui.Buffer;
import tui.Color;
import tui.Constraint;
import tui.Grapheme;
import tui.Point;
import tui.Position;
import tui.Rect;
import tui.Span;
import tui.Spans;
import tui.Style;
import tui.Symbols;
import tui.Widget;
import tui.internal.Ranges;
import tui.internal.Saturating;
import tui.widgets.canvas.CanvasWidget;
import tui.widgets.canvas.Line;
import tui.widgets.canvas.Points;

/// A widget to plot one or more dataset in a cartesian coordinate system
public final class ChartWidget implements Widget {
  public final Dataset[] datasets;
  public final Optional<BlockWidget> block;
  public final Axis xAxis;
  public final Axis yAxis;
  public final Style style;
  public final HiddenLegendConstraints hiddenLegendConstraints;

  public ChartWidget(
      Dataset[] datasets,
      Optional<BlockWidget> block,
      Axis xAxis,
      Axis yAxis,
      Style style,
      HiddenLegendConstraints hiddenLegendConstraints) {
    this.datasets = datasets;
    this.block = block;
    this.xAxis = xAxis;
    this.yAxis = yAxis;
    this.style = style;
    this.hiddenLegendConstraints = hiddenLegendConstraints;
  }

  public static ChartWidget empty(Dataset[] datasets) {
    return new ChartWidget(
        datasets,
        Optional.empty(),
        Axis.empty(),
        Axis.empty(),
        Style.DEFAULT,
        new HiddenLegendConstraints(new Constraint.Ratio(1, 4), new Constraint.Ratio(1, 4)));
  }

  public ChartWidget withBlock(BlockWidget b) {
    return new ChartWidget(datasets, Optional.of(b), xAxis, yAxis, style, hiddenLegendConstraints);
  }

  public ChartWidget withXAxis(Axis a) {
    return new ChartWidget(datasets, block, a, yAxis, style, hiddenLegendConstraints);
  }

  public ChartWidget withYAxis(Axis a) {
    return new ChartWidget(datasets, block, xAxis, a, style, hiddenLegendConstraints);
  }

  public ChartWidget withStyle(Style s) {
    return new ChartWidget(datasets, block, xAxis, yAxis, s, hiddenLegendConstraints);
  }

  public ChartWidget withHiddenLegendConstraints(HiddenLegendConstraints h) {
    return new ChartWidget(datasets, block, xAxis, yAxis, style, h);
  }

  /// Compute the internal layout of the chart given the area. If the area is too small some elements may be automatically hidden
  public ChartLayout layout(Rect area) {
    ChartLayout layout = ChartLayout.empty();
    if (area.height() == 0 || area.width() == 0) {
      return layout;
    }
    int x = area.left();
    int y = area.bottom() - 1;

    if (xAxis.labels.isPresent() && y > area.top()) {
      layout = layout.withLabelX(Optional.of(y));
      y -= 1;
    }

    layout = layout.withLabelY(yAxis.labels.isPresent() ? Optional.of(x) : Optional.empty());
    x += maxWidthOfLabelsLeftOfYAxis(area, yAxis.labels.isPresent());

    if (xAxis.labels.isPresent() && y > area.top()) {
      layout = layout.withAxisX(Optional.of(y));
      y -= 1;
    }

    if (yAxis.labels.isPresent() && x + 1 < area.right()) {
      layout = layout.withAxisY(Optional.of(x));
      x += 1;
    }

    if (x < area.right() && y > 1) {
      layout = layout.withGraphArea(new Rect(x, area.top(), area.right() - x, y - area.top() + 1));
    }

    if (xAxis.title.isPresent()) {
      Spans title = xAxis.title.get();
      int w = title.width();
      if (w < layout.graphArea.width() && layout.graphArea.height() > 2) {
        layout = layout.withTitleX(Optional.of(new Position(x + layout.graphArea.width() - w, y)));
      }
    }

    if (yAxis.title.isPresent()) {
      Spans title = yAxis.title.get();
      int w = title.width();
      if (w + 1 < layout.graphArea.width() && layout.graphArea.height() > 2) {
        layout = layout.withTitleY(Optional.of(new Position(x, area.top())));
      }
    }

    int innerWidth = -1;
    for (Dataset d : datasets) {
      int w = new Grapheme(d.name).width();
      if (w > innerWidth) innerWidth = w;
    }
    if (innerWidth >= 0) {
      int legendWidth = innerWidth + 2;
      int legendHeight = datasets.length + 2;
      int maxLegendWidth = hiddenLegendConstraints.first.apply(layout.graphArea.width());
      int maxLegendHeight = hiddenLegendConstraints.second.apply(layout.graphArea.height());
      if (innerWidth > 0 && legendWidth < maxLegendWidth && legendHeight < maxLegendHeight) {
        Rect rect =
            new Rect(
                layout.graphArea.right() - legendWidth,
                layout.graphArea.top(),
                legendWidth,
                legendHeight);
        layout = layout.withLegendArea(Optional.of(rect));
      }
    }
    return layout;
  }

  public int maxWidthOfLabelsLeftOfYAxis(Rect area, boolean hasYAxis) {
    int maxWidth = 0;
    if (yAxis.labels.isPresent()) {
      for (Span s : yAxis.labels.get()) {
        int w = s.width();
        if (w > maxWidth) maxWidth = w;
      }
    }

    if (xAxis.labels.isPresent() && xAxis.labels.get().length > 0) {
      Span firstXLabel = xAxis.labels.get()[0];
      int firstLabelWidth = new Grapheme(firstXLabel.content()).width();
      int widthLeftOfYAxis =
          switch (xAxis.labelsAlignment) {
            case Left -> {
              int yAxisOffset = hasYAxis ? 1 : 0;
              yield Saturating.saturatingSubUnsigned(firstLabelWidth, yAxisOffset);
            }
            case Center -> firstLabelWidth / 2;
            case Right -> 0;
          };
      maxWidth = Math.max(maxWidth, widthLeftOfYAxis);
    }
    return Math.min(maxWidth, area.width() / 3);
  }

  public void renderXLabels(Buffer buf, ChartLayout layout, Rect chartArea, Rect graphArea) {
    if (layout.labelX.isEmpty()) return;
    int y = layout.labelX.get();
    Span[] labels = xAxis.labels.orElse(new Span[0]);
    int labelsLen = labels.length;
    if (labelsLen < 2) return;

    int widthBetweenTicks = graphArea.width() / labelsLen;

    Rect labelArea =
        firstXLabelArea(y, labels[0].width(), widthBetweenTicks, chartArea, graphArea);

    Alignment labelAlignment =
        switch (xAxis.labelsAlignment) {
          case Left -> Alignment.Right;
          case Center -> Alignment.Center;
          case Right -> Alignment.Left;
        };

    renderLabel(buf, labels[0], labelArea, labelAlignment);

    Ranges.range(
        0,
        labels.length - 2,
        i -> {
          Span label = labels[i + 1];
          int xx = graphArea.left() + (i + 1) * widthBetweenTicks + 1;
          Rect la = new Rect(xx, y, Saturating.saturatingSubUnsigned(widthBetweenTicks, 1), 1);
          renderLabel(buf, label, la, Alignment.Center);
        });

    int xx = graphArea.right() - widthBetweenTicks;
    Rect labelArea1 = new Rect(xx, y, widthBetweenTicks, 1);
    renderLabel(buf, labels[labels.length - 1], labelArea1, Alignment.Right);
  }

  public Rect firstXLabelArea(
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
        minX = Saturating.saturatingSubUnsigned(graphArea.left(), 1);
        maxX = graphArea.left() + maxWidthAfterYAxis;
      }
      default -> {
        minX = 0;
        maxX = 0;
      }
    }
    return new Rect(minX, y, maxX - minX, 1);
  }

  public Position renderLabel(Buffer buf, Span label, Rect labelArea, Alignment alignment) {
    int labelWidth = label.width();
    int boundedLabelWidth = Math.min(labelArea.width(), labelWidth);

    int x =
        switch (alignment) {
          case Left -> labelArea.left();
          case Center -> labelArea.left() + labelArea.width() / 2 - boundedLabelWidth / 2;
          case Right -> labelArea.right() - boundedLabelWidth;
        };

    return buf.setSpan(x, labelArea.top(), label, boundedLabelWidth);
  }

  public void renderYLabels(Buffer buf, ChartLayout layout, Rect chartArea, Rect graphArea) {
    if (layout.labelY.isEmpty()) return;
    int x = layout.labelY.get();
    Span[] labels = yAxis.labels.orElse(new Span[0]);
    int labelsLen = labels.length;
    Ranges.range(
        0,
        labels.length,
        i -> {
          Span label = labels[i];
          int dy = i * (graphArea.height() - 1) / (labelsLen - 1);
          if (dy < graphArea.bottom()) {
            Rect la =
                new Rect(
                    x,
                    Saturating.saturatingSubUnsigned(graphArea.bottom(), 1) - dy,
                    Saturating.saturatingSubUnsigned(graphArea.left() - chartArea.left(), 1),
                    1);
            renderLabel(buf, label, la, yAxis.labelsAlignment);
          }
        });
  }

  @Override
  public void render(Rect area, Buffer buf) {
    if (area.area() == 0) {
      return;
    }
    buf.setStyle(area, style);
    Style originalStyle = buf.get(area.left(), area.top()).style();

    Rect chartArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      chartArea = innerArea;
    } else {
      chartArea = area;
    }

    ChartLayout layout = layout(chartArea);
    Rect graphArea = layout.graphArea;
    if (graphArea.width() < 1 || graphArea.height() < 1) {
      return;
    }

    renderXLabels(buf, layout, chartArea, graphArea);
    renderYLabels(buf, layout, chartArea, graphArea);

    if (layout.axisX.isPresent()) {
      int y = layout.axisX.get();
      Ranges.range(
          graphArea.left(),
          graphArea.right(),
          x -> buf.get(x, y).setSymbol(Symbols.line.HORIZONTAL).setStyle(xAxis.style));
    }

    if (layout.axisY.isPresent()) {
      int x = layout.axisY.get();
      Ranges.range(
          graphArea.top(),
          graphArea.bottom(),
          y -> buf.get(x, y).setSymbol(Symbols.line.VERTICAL).setStyle(yAxis.style));
    }

    if (layout.axisX.isPresent() && layout.axisY.isPresent()) {
      int y = layout.axisX.get();
      int x = layout.axisY.get();
      buf.get(x, y).setSymbol(Symbols.line.BOTTOM_LEFT).setStyle(xAxis.style);
    }

    for (Dataset dataset : datasets) {
      CanvasWidget cw =
          new CanvasWidget(
                  Optional.empty(),
                  xAxis.bounds,
                  yAxis.bounds,
                  style.bg().orElse(Color.Reset),
                  dataset.marker,
                  ctx -> {
                    Points points =
                        new Points(dataset.data, dataset.style.fg().orElse(Color.Reset));
                    ctx.draw(points);
                    if (dataset.graphType == GraphType.Line) {
                      Point[] data = dataset.data;
                      for (int i = 0; i + 1 < data.length; i++) {
                        Point one = data[i];
                        Point two = data[i + 1];
                        Line line =
                            new Line(
                                one.x(),
                                one.y(),
                                two.x(),
                                two.y(),
                                dataset.style.fg().orElse(Color.Reset));
                        ctx.draw(line);
                      }
                    }
                  });
      cw.render(graphArea, buf);
    }

    if (layout.legendArea.isPresent()) {
      Rect legendArea = layout.legendArea.get();
      buf.setStyle(legendArea, originalStyle);
      BlockWidget.empty().withBorders(Borders.ALL).render(legendArea, buf);
      Ranges.range(
          0,
          datasets.length,
          i -> {
            Dataset dataset = datasets[i];
            buf.setString(
                legendArea.x() + 1, legendArea.y() + 1 + i, dataset.name, dataset.style);
          });
    }

    if (layout.titleX.isPresent()) {
      Position pos = layout.titleX.get();
      int x = pos.x();
      int y = pos.y();
      Spans title = xAxis.title.get();
      // Use the title's own width so the chart's overall style isn't
      // applied to the rest of the row (which would override the dataset
      // style of the top graph line).
      int width = title.width();
      buf.setStyle(new Rect(x, y, width, 1), originalStyle);
      buf.setSpans(x, y, title, width);
    }

    if (layout.titleY.isPresent()) {
      Position pos = layout.titleY.get();
      int x = pos.x();
      int y = pos.y();
      Spans title = yAxis.title.get();
      int width = title.width();
      buf.setStyle(new Rect(x, y, width, 1), originalStyle);
      buf.setSpans(x, y, title, width);
    }
  }

  public record HiddenLegendConstraints(Constraint first, Constraint second) {}

  /// An X or Y axis for the chart widget
  public static final class Axis {
    public final Optional<Spans> title;
    public final Point bounds;
    public final Optional<Span[]> labels;
    public final Style style;
    public final Alignment labelsAlignment;

    public Axis(
        Optional<Spans> title,
        Point bounds,
        Optional<Span[]> labels,
        Style style,
        Alignment labelsAlignment) {
      this.title = title;
      this.bounds = bounds;
      this.labels = labels;
      this.style = style;
      this.labelsAlignment = labelsAlignment;
    }

    public static Axis empty() {
      return new Axis(
          Optional.empty(), Point.Zero, Optional.empty(), Style.DEFAULT, Alignment.Left);
    }

    public Axis withTitle(Spans t) {
      return new Axis(Optional.of(t), bounds, labels, style, labelsAlignment);
    }

    public Axis withBounds(Point b) {
      return new Axis(title, b, labels, style, labelsAlignment);
    }

    public Axis withLabels(Span[] l) {
      return new Axis(title, bounds, Optional.of(l), style, labelsAlignment);
    }

    public Axis withStyle(Style s) {
      return new Axis(title, bounds, labels, s, labelsAlignment);
    }

    public Axis withLabelsAlignment(Alignment a) {
      return new Axis(title, bounds, labels, style, a);
    }
  }

  /// Used to determine which style of graphing to use
  public enum GraphType {
    /// Draw each point
    Scatter,
    /// Draw each point and lines between each point using the same marker
    Line
  }

  /// A group of data points
  public static final class Dataset {
    public final String name;
    public final Point[] data;
    public final Symbols.Marker marker;
    public final GraphType graphType;
    public final Style style;

    public Dataset(
        String name,
        Point[] data,
        Symbols.Marker marker,
        GraphType graphType,
        Style style) {
      this.name = name;
      this.data = data;
      this.marker = marker;
      this.graphType = graphType;
      this.style = style;
    }

    public static Dataset empty() {
      return new Dataset("", new Point[0], Symbols.Marker.Dot, GraphType.Scatter, Style.DEFAULT);
    }

    public Dataset withName(String n) {
      return new Dataset(n, data, marker, graphType, style);
    }

    public Dataset withData(Point[] d) {
      return new Dataset(name, d, marker, graphType, style);
    }

    public Dataset withMarker(Symbols.Marker m) {
      return new Dataset(name, data, m, graphType, style);
    }

    public Dataset withGraphType(GraphType g) {
      return new Dataset(name, data, marker, g, style);
    }

    public Dataset withStyle(Style s) {
      return new Dataset(name, data, marker, graphType, s);
    }
  }

  /// A container that holds all the infos about where to display each elements of the chart.
  public static final class ChartLayout {
    public final Optional<Position> titleX;
    public final Optional<Position> titleY;
    public final Optional<Integer> labelX;
    public final Optional<Integer> labelY;
    public final Optional<Integer> axisX;
    public final Optional<Integer> axisY;
    public final Optional<Rect> legendArea;
    public final Rect graphArea;

    public ChartLayout(
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

    public static ChartLayout empty() {
      return new ChartLayout(
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Rect.DEFAULT);
    }

    public ChartLayout withTitleX(Optional<Position> v) {
      return new ChartLayout(v, titleY, labelX, labelY, axisX, axisY, legendArea, graphArea);
    }

    public ChartLayout withTitleY(Optional<Position> v) {
      return new ChartLayout(titleX, v, labelX, labelY, axisX, axisY, legendArea, graphArea);
    }

    public ChartLayout withLabelX(Optional<Integer> v) {
      return new ChartLayout(titleX, titleY, v, labelY, axisX, axisY, legendArea, graphArea);
    }

    public ChartLayout withLabelY(Optional<Integer> v) {
      return new ChartLayout(titleX, titleY, labelX, v, axisX, axisY, legendArea, graphArea);
    }

    public ChartLayout withAxisX(Optional<Integer> v) {
      return new ChartLayout(titleX, titleY, labelX, labelY, v, axisY, legendArea, graphArea);
    }

    public ChartLayout withAxisY(Optional<Integer> v) {
      return new ChartLayout(titleX, titleY, labelX, labelY, axisX, v, legendArea, graphArea);
    }

    public ChartLayout withLegendArea(Optional<Rect> v) {
      return new ChartLayout(titleX, titleY, labelX, labelY, axisX, axisY, v, graphArea);
    }

    public ChartLayout withGraphArea(Rect v) {
      return new ChartLayout(titleX, titleY, labelX, labelY, axisX, axisY, legendArea, v);
    }
  }
}
