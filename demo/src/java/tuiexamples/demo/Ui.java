package tuiexamples.demo;

import java.util.Optional;
import tui.Borders;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Modifier;
import tui.Point;
import tui.Rect;
import tui.Span;
import tui.Spans;
import tui.Style;
import tui.Symbols;
import tui.Text;
import tui.widgets.BarChartWidget;
import tui.widgets.BlockWidget;
import tui.widgets.ChartWidget;
import tui.widgets.GaugeWidget;
import tui.widgets.LineGaugeWidget;
import tui.widgets.ListWidget;
import tui.widgets.ParagraphWidget;
import tui.widgets.SparklineWidget;
import tui.widgets.TableWidget;
import tui.widgets.canvas.CanvasWidget;
import tui.widgets.canvas.Line;
import tui.widgets.canvas.MapResolution;
import tui.widgets.canvas.Rectangle;
import tui.widgets.canvas.WorldMap;
import tui.widgets.tabs.TabsWidget;

public final class Ui {
  private Ui() {}

  public static void draw(Frame f, App app) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Length(3), new Constraint.Min(0)}, true);
    Rect[] chunks = layout.split(f.size);

    Spans[] titles = new Spans[app.tabs.titles.length];
    for (int i = 0; i < app.tabs.titles.length; i++) {
      titles[i] =
          Spans.from(Span.styled(app.tabs.titles[i], Style.DEFAULT.withFg(Color.Green)));
    }

    TabsWidget tabs =
        TabsWidget.empty(titles)
            .withBlock(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle(app.title)))
            .withHighlightStyle(Style.DEFAULT.withFg(Color.Yellow))
            .withSelected(app.tabs.index);
    f.renderWidget(tabs, chunks[0]);

    switch (app.tabs.index) {
      case 0 -> drawFirstTab(f, app, chunks[1]);
      case 1 -> drawSecondTab(f, app, chunks[1]);
      case 2 -> drawThirdTab(f, chunks[1]);
      default -> {}
    }
  }

  public static void drawFirstTab(Frame f, App app, Rect area) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            new Margin(0, 0),
            new Constraint[] {
              new Constraint.Length(9), new Constraint.Min(8), new Constraint.Length(7)
            }, true);
    Rect[] chunks = layout.split(area);
    drawGauges(f, app, chunks[0]);
    drawCharts(f, app, chunks[1]);
    drawText(f, chunks[2]);
  }

  public static void drawGauges(Frame f, App app, Rect area) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(1),
            new Constraint[] {
              new Constraint.Length(2), new Constraint.Length(3), new Constraint.Length(1)
            }, true);
    Rect[] chunks = layout.split(area);

    BlockWidget block =
        BlockWidget.empty().withBorders(Borders.ALL).withTitle(Spans.nostyle("Graphs"));
    f.renderWidget(block, area);

    String label = String.format("%.2f", app.progress * 100.0);
    GaugeWidget gauge =
        GaugeWidget.empty()
            .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Gauge:")))
            .withGaugeStyle(
                Style.DEFAULT
                    .withFg(Color.Magenta)
                    .withBg(Color.Black)
                    .withAddModifier(Modifier.ITALIC.or(Modifier.BOLD)))
            .withLabel(Span.nostyle(label))
            .withRatio(new GaugeWidget.Ratio(app.progress));
    f.renderWidget(gauge, chunks[0]);

    SparklineWidget sparkline =
        SparklineWidget.empty()
            .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Sparkline:")))
            .withStyle(Style.DEFAULT.withFg(Color.Green))
            .withData(app.sparkline.points)
            .withBarSet(
                app.enhancedGraphics ? Symbols.bar.NINE_LEVELS : Symbols.bar.THREE_LEVELS);
    f.renderWidget(sparkline, chunks[1]);

    LineGaugeWidget lineGauge =
        LineGaugeWidget.empty()
            .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("LineGauge:")))
            .withGaugeStyle(Style.DEFAULT.withFg(Color.Magenta))
            .withLineSet(app.enhancedGraphics ? Symbols.line.THICK : Symbols.line.NORMAL)
            .withRatio(new GaugeWidget.Ratio(app.progress));
    f.renderWidget(lineGauge, chunks[2]);
  }

  public static void drawCharts(Frame f, App app, Rect area) {
    Constraint[] constraints =
        app.showChart
            ? new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)}
            : new Constraint[] {new Constraint.Percentage(100)};

    Layout layout0 =
        new Layout(Direction.Horizontal, new Margin(0, 0), constraints, true);
    Rect[] chunks0 = layout0.split(area);

    {
      Layout layout1 =
          new Layout(
              Direction.Vertical,
              new Margin(0, 0),
              new Constraint[] {
                new Constraint.Percentage(50), new Constraint.Percentage(50)
              }, true);
      Rect[] chunks1 = layout1.split(chunks0[0]);

      {
        Layout layout2 =
            new Layout(
                Direction.Horizontal,
                new Margin(0, 0),
                new Constraint[] {
                  new Constraint.Percentage(50), new Constraint.Percentage(50)
                }, true);
        Rect[] chunks2 = layout2.split(chunks1[0]);

        ListWidget.Item[] items = new ListWidget.Item[app.tasks.items.size()];
        int idx = 0;
        for (String s : app.tasks.items) {
          items[idx++] = new ListWidget.Item(Text.nostyle(s), Style.DEFAULT);
        }
        ListWidget tasks =
            ListWidget.empty(items)
                .withBlock(
                    BlockWidget.empty()
                        .withBorders(Borders.ALL)
                        .withTitle(Spans.nostyle("List")))
                .withHighlightStyle(Style.DEFAULT.withAddModifier(Modifier.BOLD))
                .withHighlightSymbol("> ");
        f.renderStatefulWidget(tasks, chunks2[0], app.tasks.state);

        ListWidget.Item[] logItems = new ListWidget.Item[app.logs.items.size()];
        int li = 0;
        for (App.Log l : app.logs.items) {
          Style s;
          switch (l.level()) {
            case "ERROR" -> s = Style.DEFAULT.withFg(Color.Magenta);
            case "CRITICAL" -> s = Style.DEFAULT.withFg(Color.Red);
            case "WARNING" -> s = Style.DEFAULT.withFg(Color.Yellow);
            default -> s = Style.DEFAULT.withFg(Color.Blue);
          }
          Text content =
              Text.fromSpans(Span.styled(padRight(l.level(), 9, ' '), s), Span.nostyle(l.name()));
          logItems[li++] = new ListWidget.Item(content, Style.DEFAULT);
        }
        ListWidget logs =
            ListWidget.empty(logItems)
                .withBlock(
                    BlockWidget.empty()
                        .withBorders(Borders.ALL)
                        .withTitle(Spans.nostyle("List")));
        f.renderStatefulWidget(logs, chunks2[1], app.logs.state);
      }

      BarChartWidget.LabelValue[] data =
          app.barchart.toArray(new BarChartWidget.LabelValue[0]);
      BarChartWidget barchart =
          new BarChartWidget(
              Optional.of(
                  BlockWidget.empty()
                      .withBorders(Borders.ALL)
                      .withTitle(Spans.nostyle("Bar chart"))),
              3,
              2,
              app.enhancedGraphics ? Symbols.bar.NINE_LEVELS : Symbols.bar.THREE_LEVELS,
              Style.DEFAULT.withFg(Color.Green),
              Style.DEFAULT
                  .withFg(Color.Black)
                  .withBg(Color.Green)
                  .withAddModifier(Modifier.ITALIC),
              Style.DEFAULT.withFg(Color.Yellow),
              Style.DEFAULT,
              data,
              Optional.empty());
      f.renderWidget(barchart, chunks1[1]);
    }

    if (app.showChart) {
      Span[] xLabels = {
        Span.styled(
            Double.toString(app.signals.window.x()),
            Style.DEFAULT.withAddModifier(Modifier.BOLD)),
        Span.nostyle(
            Double.toString((app.signals.window.x() + app.signals.window.y()) / 2.0)),
        Span.styled(
            Double.toString(app.signals.window.y()),
            Style.DEFAULT.withAddModifier(Modifier.BOLD))
      };

      ChartWidget.Dataset[] datasets = {
        new ChartWidget.Dataset(
            "data2",
            app.signals.sin1.points,
            Symbols.Marker.Dot,
            ChartWidget.GraphType.Scatter,
            Style.DEFAULT.withFg(Color.Cyan)),
        new ChartWidget.Dataset(
            "data3",
            app.signals.sin2.points,
            app.enhancedGraphics ? Symbols.Marker.Braille : Symbols.Marker.Dot,
            ChartWidget.GraphType.Scatter,
            Style.DEFAULT.withFg(Color.Yellow))
      };

      Span title =
          Span.styled(
              "Chart", Style.DEFAULT.withFg(Color.Cyan).withAddModifier(Modifier.BOLD));
      ChartWidget chart =
          ChartWidget.empty(datasets)
              .withBlock(
                  BlockWidget.empty()
                      .withTitle(Spans.from(title))
                      .withBorders(Borders.ALL))
              .withXAxis(
                  ChartWidget.Axis.empty()
                      .withTitle(Spans.nostyle("X Axis"))
                      .withStyle(Style.DEFAULT.withFg(Color.Gray))
                      .withBounds(app.signals.window)
                      .withLabels(xLabels))
              .withYAxis(
                  ChartWidget.Axis.empty()
                      .withTitle(Spans.nostyle("Y Axis"))
                      .withStyle(Style.DEFAULT.withFg(Color.Gray))
                      .withBounds(new Point(-20.0, 20.0))
                      .withLabels(
                          new Span[] {
                            Span.styled(
                                "-20", Style.DEFAULT.withAddModifier(Modifier.BOLD)),
                            Span.nostyle("0"),
                            Span.styled(
                                "20", Style.DEFAULT.withAddModifier(Modifier.BOLD))
                          }));
      f.renderWidget(chart, chunks0[1]);
    }
  }

  public static void drawText(Frame f, Rect area) {
    Text text =
        Text.fromMany(
            Spans.nostyle(
                "This is a paragraph with several lines. You can change style your text the way you want"),
            Spans.nostyle(""),
            Spans.from(
                Span.nostyle("For example: "),
                Span.styled("under", Style.DEFAULT.withFg(Color.Red)),
                Span.nostyle(" "),
                Span.styled("the", Style.DEFAULT.withFg(Color.Green)),
                Span.nostyle(" "),
                Span.styled("rainbow", Style.DEFAULT.withFg(Color.Blue)),
                Span.nostyle(".")),
            Spans.from(
                Span.nostyle("Oh and if you didn't "),
                Span.styled("notice", Style.DEFAULT.withAddModifier(Modifier.ITALIC)),
                Span.nostyle(" you can "),
                Span.styled(
                    "automatically", Style.DEFAULT.withAddModifier(Modifier.BOLD)),
                Span.nostyle(" "),
                Span.styled("wrap", Style.DEFAULT.withAddModifier(Modifier.REVERSED)),
                Span.nostyle(" your "),
                Span.styled("text", Style.DEFAULT.withAddModifier(Modifier.UNDERLINED)),
                Span.nostyle(".")),
            Spans.nostyle(
                "One more thing is that it should display unicode characters: 10€"));

    Style titleStyle =
        Style.DEFAULT.withFg(Color.Magenta).withAddModifier(Modifier.BOLD);
    BlockWidget block =
        BlockWidget.empty()
            .withBorders(Borders.ALL)
            .withTitle(Spans.from(Span.styled("Footer", titleStyle)));
    ParagraphWidget paragraph =
        ParagraphWidget.empty(text)
            .withBlock(block)
            .withWrap(new ParagraphWidget.Wrap(true));
    f.renderWidget(paragraph, area);
  }

  public static void drawSecondTab(Frame f, App app, Rect area) {
    Layout layout =
        new Layout(
            Direction.Horizontal,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Percentage(30), new Constraint.Percentage(70)}, true);
    Rect[] chunks = layout.split(area);

    Style upStyle = Style.DEFAULT.withFg(Color.Green);
    Style failureStyle =
        Style.DEFAULT
            .withFg(Color.Red)
            .withAddModifier(Modifier.RAPID_BLINK.or(Modifier.CROSSED_OUT));

    TableWidget.Row[] rows = new TableWidget.Row[app.servers.length];
    for (int i = 0; i < app.servers.length; i++) {
      Server s = app.servers[i];
      Style style = "Up".equals(s.status()) ? upStyle : failureStyle;
      TableWidget.Cell[] cells = {
        cell(s.name()), cell(s.location()), cell(s.status())
      };
      rows[i] = new TableWidget.Row(cells, 1, style, 0);
    }

    TableWidget.Cell[] headerCells = {cell("Server"), cell("Location"), cell("Status")};
    TableWidget.Row header =
        new TableWidget.Row(headerCells, 1, Style.DEFAULT.withFg(Color.Yellow), 1);

    TableWidget table =
        TableWidget.empty(rows)
            .withHeader(header)
            .withBlock(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Servers"))
                    .withBorders(Borders.ALL))
            .withWidths(
                new Constraint[] {
                  new Constraint.Length(15),
                  new Constraint.Length(15),
                  new Constraint.Length(10)
                });
    f.renderWidget(table, chunks[0]);

    CanvasWidget map =
        new CanvasWidget(
            Optional.of(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("World"))
                    .withBorders(Borders.ALL)),
            new Point(-180.0, 180.0),
            new Point(-90.0, 90.0),
            Color.Reset,
            app.enhancedGraphics ? Symbols.Marker.Braille : Symbols.Marker.Dot,
            ctx -> {
              ctx.draw(new WorldMap(MapResolution.High, Color.White));
              ctx.layer();
              ctx.draw(new Rectangle(0.0, 30.0, 10.0, 10.0, Color.Yellow));
              for (int i = 0; i < app.servers.length; i++) {
                Server s1 = app.servers[i];
                for (int j = i; j < app.servers.length; j++) {
                  Server s2 = app.servers[j];
                  ctx.draw(
                      new Line(
                          s1.coords().y(),
                          s1.coords().x(),
                          s2.coords().y(),
                          s2.coords().x(),
                          Color.Yellow));
                }
              }
              for (Server server : app.servers) {
                Color color = "Up".equals(server.status()) ? Color.Green : Color.Red;
                ctx.print(
                    server.coords().y(),
                    server.coords().x(),
                    Spans.from(Span.styled("X", Style.DEFAULT.withFg(color))));
              }
            });
    f.renderWidget(map, chunks[1]);
  }

  public static void drawThirdTab(Frame f, Rect area) {
    Layout layout =
        new Layout(
            Direction.Horizontal,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Ratio(1, 2), new Constraint.Ratio(1, 2)}, true);
    Rect[] chunks = layout.split(area);

    Color[] colors = {
      Color.Reset,
      Color.Black,
      Color.Red,
      Color.Green,
      Color.Yellow,
      Color.Blue,
      Color.Magenta,
      Color.Cyan,
      Color.Gray,
      Color.DarkGray,
      Color.LightRed,
      Color.LightGreen,
      Color.LightYellow,
      Color.LightBlue,
      Color.LightMagenta,
      Color.LightCyan,
      Color.White
    };

    TableWidget.Row[] items = new TableWidget.Row[colors.length];
    for (int i = 0; i < colors.length; i++) {
      Color c = colors[i];
      TableWidget.Cell[] cells = {
        new TableWidget.Cell(Text.nostyle(c.toString()), Style.DEFAULT),
        new TableWidget.Cell(
            Text.from(Span.styled("Foreground", Style.DEFAULT.withFg(c))), Style.DEFAULT),
        new TableWidget.Cell(
            Text.from(Span.styled("Background", Style.DEFAULT.withBg(c))), Style.DEFAULT)
      };
      items[i] = new TableWidget.Row(cells, 1, Style.DEFAULT, 0);
    }

    TableWidget table =
        TableWidget.empty(items)
            .withBlock(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Colors"))
                    .withBorders(Borders.ALL))
            .withWidths(
                new Constraint[] {
                  new Constraint.Ratio(1, 3),
                  new Constraint.Ratio(1, 3),
                  new Constraint.Ratio(1, 3)
                });
    f.renderWidget(table, chunks[0]);
  }

  private static TableWidget.Cell cell(String s) {
    return new TableWidget.Cell(Text.nostyle(s), Style.DEFAULT);
  }

  private static String padRight(String s, int width, char ch) {
    if (s.length() >= width) return s;
    StringBuilder sb = new StringBuilder(s);
    while (sb.length() < width) sb.append(ch);
    return sb.toString();
  }
}
