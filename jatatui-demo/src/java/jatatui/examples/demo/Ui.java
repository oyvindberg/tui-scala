package jatatui.examples.demo;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Bar;
import jatatui.core.symbols.Line;
import jatatui.core.symbols.Marker;
import jatatui.core.terminal.Frame;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.widgets.barchart.BarChart;
import jatatui.widgets.barchart.BarGroup;
import jatatui.widgets.block.Block;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Circle;
import jatatui.widgets.canvas.Map;
import jatatui.widgets.canvas.MapResolution;
import jatatui.widgets.canvas.Rectangle;
import jatatui.widgets.chart.Axis;
import jatatui.widgets.chart.Chart;
import jatatui.widgets.chart.Dataset;
import jatatui.widgets.gauge.Gauge;
import jatatui.widgets.gauge.LineGauge;
import jatatui.widgets.list.List;
import jatatui.widgets.list.ListItem;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Wrap;
import jatatui.widgets.sparkline.Sparkline;
import jatatui.widgets.table.Row;
import jatatui.widgets.table.Table;
import jatatui.widgets.table.TableCell;
import jatatui.widgets.tabs.Tabs;
import java.util.ArrayList;

/// Rendering for the demo example.
///
/// Mirrors `examples/apps/demo/src/ui.rs`. One static [#render] entry-point dispatches to a
/// per-tab draw method; all helpers are package-private statics matching the Rust functions.
public final class Ui {

  private Ui() {}

  /// Renders the entire demo into the given [Frame], reading state from `app`.
  ///
  /// Mirrors upstream `pub fn render(frame: &amp;mut Frame, app: &amp;mut App)`.
  public static void render(Frame frame, App app) {
    Rect[] chunks =
        Layout.vertical(new Constraint.Length(3), new Constraint.Min(0)).split(frame.area());

    java.util.List<jatatui.core.text.Line> tabTitles = new ArrayList<>(app.tabs.titles.size());
    for (String t : app.tabs.titles) {
      tabTitles.add(jatatui.core.text.Line.from(Span.styled(t, Style.empty().withFg(Color.GREEN))));
    }
    Tabs tabs =
        Tabs.of(tabTitles)
            .withBlock(Block.bordered().withTitle(app.title))
            .withHighlightStyle(Style.empty().withFg(Color.YELLOW))
            .withSelected(app.tabs.index);
    frame.renderWidget(tabs, chunks[0]);

    switch (app.tabs.index) {
      case 0 -> drawFirstTab(frame, app, chunks[1]);
      case 1 -> drawSecondTab(frame, app, chunks[1]);
      case 2 -> drawThirdTab(frame, app, chunks[1]);
      default -> {
        // ignore
      }
    }
  }

  static void drawFirstTab(Frame frame, App app, Rect area) {
    Rect[] chunks =
        Layout.vertical(new Constraint.Length(9), new Constraint.Min(8), new Constraint.Length(7))
            .split(area);
    drawGauges(frame, app, chunks[0]);
    drawCharts(frame, app, chunks[1]);
    drawText(frame, chunks[2]);
  }

  static void drawGauges(Frame frame, App app, Rect area) {
    Rect[] chunks =
        Layout.vertical(
                new Constraint.Length(2), new Constraint.Length(3), new Constraint.Length(2))
            .withMargin(1)
            .split(area);
    Block block = Block.bordered().withTitle("Graphs");
    frame.renderWidget(block, area);

    String label = String.format("%.2f%%", app.progress * 100.0);
    Gauge gauge =
        Gauge.empty()
            .withBlock(Block.empty().withTitle("Gauge:"))
            .withGaugeStyle(
                Style.empty()
                    .withFg(Color.MAGENTA)
                    .withBg(Color.BLACK)
                    .withAddModifier(Modifier.ITALIC.or(Modifier.BOLD)))
            .withUseUnicode(app.enhancedGraphics)
            .withLabel(label)
            .withRatio(app.progress);
    frame.renderWidget(gauge, chunks[0]);

    Bar.Set sparkBars = app.enhancedGraphics ? Bar.NINE_LEVELS : Bar.THREE_LEVELS;
    Sparkline sparkline =
        Sparkline.empty()
            .withBlock(Block.empty().withTitle("Sparkline:"))
            .withStyle(Style.empty().withFg(Color.GREEN))
            .withDataLongs(toLongArray(app.sparkline.points))
            .withBarSet(sparkBars);
    frame.renderWidget(sparkline, chunks[1]);

    String filledSym = app.enhancedGraphics ? Line.THICK_HORIZONTAL : Line.HORIZONTAL;
    String unfilledSym = app.enhancedGraphics ? Line.THICK_HORIZONTAL : Line.HORIZONTAL;
    LineGauge lineGauge =
        LineGauge.empty()
            .withBlock(Block.empty().withTitle("LineGauge:"))
            .withFilledStyle(Style.empty().withFg(Color.MAGENTA))
            .withFilledSymbol(filledSym)
            .withUnfilledSymbol(unfilledSym)
            .withRatio(app.progress);
    frame.renderWidget(lineGauge, chunks[2]);
  }

  static void drawCharts(Frame frame, App app, Rect area) {
    java.util.List<Constraint> constraints;
    if (app.showChart) {
      constraints = java.util.List.of(new Constraint.Percentage(50), new Constraint.Percentage(50));
    } else {
      constraints = java.util.List.of(new Constraint.Percentage(100));
    }
    Rect[] chunks = Layout.horizontal(constraints).split(area);
    {
      Rect[] leftChunks =
          Layout.vertical(new Constraint.Percentage(50), new Constraint.Percentage(50))
              .split(chunks[0]);
      {
        Rect[] topChunks =
            Layout.horizontal(new Constraint.Percentage(50), new Constraint.Percentage(50))
                .split(leftChunks[0]);

        // Draw tasks
        java.util.List<ListItem> taskItems = new ArrayList<>(app.tasks.items.size());
        for (String i : app.tasks.items) {
          taskItems.add(ListItem.of(jatatui.core.text.Line.from(Span.raw(i))));
        }
        List tasks =
            List.of(taskItems)
                .withBlock(Block.bordered().withTitle("List"))
                .withHighlightStyle(Style.empty().withAddModifier(Modifier.BOLD))
                .withHighlightSymbol("> ");
        frame.renderStatefulWidget(tasks, topChunks[0], app.tasks.state);

        // Draw logs
        Style infoStyle = Style.empty().withFg(Color.BLUE);
        Style warningStyle = Style.empty().withFg(Color.YELLOW);
        Style errorStyle = Style.empty().withFg(Color.MAGENTA);
        Style criticalStyle = Style.empty().withFg(Color.RED);
        java.util.List<ListItem> logItems = new ArrayList<>(app.logs.items.size());
        for (LogEntry entry : app.logs.items) {
          Style s =
              switch (entry.level()) {
                case "ERROR" -> errorStyle;
                case "CRITICAL" -> criticalStyle;
                case "WARNING" -> warningStyle;
                default -> infoStyle;
              };
          jatatui.core.text.Line line =
              jatatui.core.text.Line.from(
                  Span.styled(String.format("%-9s", entry.level()), s), Span.raw(entry.event()));
          logItems.add(ListItem.of(line));
        }
        List logs = List.of(logItems).withBlock(Block.bordered().withTitle("List"));
        frame.renderStatefulWidget(logs, topChunks[1], app.logs.state);
      }

      Bar.Set barSet = app.enhancedGraphics ? Bar.NINE_LEVELS : Bar.THREE_LEVELS;
      java.util.List<BarGroup.LabelledValue> barData = new ArrayList<>(app.barchart.size());
      for (BarEntry e : app.barchart) {
        barData.add(BarGroup.LabelledValue.of(e.label(), e.value()));
      }
      BarChart barchart =
          BarChart.empty()
              .withBlock(Block.bordered().withTitle("Bar chart"))
              .withData(barData)
              .withBarWidth(3)
              .withBarGap(2)
              .withBarSet(barSet)
              .withValueStyle(
                  Style.empty()
                      .withFg(Color.BLACK)
                      .withBg(Color.GREEN)
                      .withAddModifier(Modifier.ITALIC))
              .withLabelStyle(Style.empty().withFg(Color.YELLOW))
              .withBarStyle(Style.empty().withFg(Color.GREEN));
      frame.renderWidget(barchart, leftChunks[1]);
    }
    if (app.showChart) {
      java.util.List<Span> xLabels =
          java.util.List.of(
              Span.styled(
                  String.format("%s", app.signals.window.lo),
                  Style.empty().withAddModifier(Modifier.BOLD)),
              Span.raw(String.format("%s", (app.signals.window.lo + app.signals.window.hi) / 2.0)),
              Span.styled(
                  String.format("%s", app.signals.window.hi),
                  Style.empty().withAddModifier(Modifier.BOLD)));
      java.util.List<jatatui.core.text.Line> xLabelLines = new ArrayList<>(xLabels.size());
      for (Span s : xLabels) xLabelLines.add(jatatui.core.text.Line.from(s));

      Marker data3Marker = app.enhancedGraphics ? Marker.Braille : Marker.Dot;
      java.util.List<Dataset> datasets =
          java.util.List.of(
              Dataset.empty()
                  .withName("data2")
                  .withMarker(Marker.Dot)
                  .withStyle(Style.empty().withFg(Color.CYAN))
                  .withData(app.signals.sin1.points),
              Dataset.empty()
                  .withName("data3")
                  .withMarker(data3Marker)
                  .withStyle(Style.empty().withFg(Color.YELLOW))
                  .withData(app.signals.sin2.points));
      Chart chart =
          Chart.of(datasets)
              .withBlock(
                  Block.bordered()
                      .withTitle(
                          jatatui.core.text.Line.from(
                              Span.styled(
                                  "Chart",
                                  Style.empty()
                                      .withFg(Color.CYAN)
                                      .withAddModifier(Modifier.BOLD)))))
              .withXAxis(
                  Axis.empty()
                      .withTitle("X Axis")
                      .withStyle(Style.empty().withFg(Color.GRAY))
                      .withBounds(app.signals.window.lo, app.signals.window.hi)
                      .withLabels(xLabelLines))
              .withYAxis(
                  Axis.empty()
                      .withTitle("Y Axis")
                      .withStyle(Style.empty().withFg(Color.GRAY))
                      .withBounds(-20.0, 20.0)
                      .withLabels(
                          jatatui.core.text.Line.from(
                              Span.styled("-20", Style.empty().withAddModifier(Modifier.BOLD))),
                          jatatui.core.text.Line.from(Span.raw("0")),
                          jatatui.core.text.Line.from(
                              Span.styled("20", Style.empty().withAddModifier(Modifier.BOLD)))));
      frame.renderWidget(chart, chunks[1]);
    }
  }

  static void drawText(Frame frame, Rect area) {
    Text text =
        Text.fromLines(
            java.util.List.of(
                jatatui.core.text.Line.from(
                    "This is a paragraph with several lines. You can change style your text the way"
                        + " you want"),
                jatatui.core.text.Line.from(""),
                jatatui.core.text.Line.from(
                    Span.from("For example: "),
                    Span.styled("under", Style.empty().withFg(Color.RED)),
                    Span.raw(" "),
                    Span.styled("the", Style.empty().withFg(Color.GREEN)),
                    Span.raw(" "),
                    Span.styled("rainbow", Style.empty().withFg(Color.BLUE)),
                    Span.raw(".")),
                jatatui.core.text.Line.from(
                    Span.raw("Oh and if you didn't "),
                    Span.styled("notice", Style.empty().withAddModifier(Modifier.ITALIC)),
                    Span.raw(" you can "),
                    Span.styled("automatically", Style.empty().withAddModifier(Modifier.BOLD)),
                    Span.raw(" "),
                    Span.styled("wrap", Style.empty().withAddModifier(Modifier.REVERSED)),
                    Span.raw(" your "),
                    Span.styled("text", Style.empty().withAddModifier(Modifier.UNDERLINED)),
                    Span.raw(".")),
                jatatui.core.text.Line.from(
                    "One more thing is that it should display unicode characters: 10€")));
    Block block =
        Block.bordered()
            .withTitle(
                jatatui.core.text.Line.from(
                    Span.styled(
                        "Footer",
                        Style.empty().withFg(Color.MAGENTA).withAddModifier(Modifier.BOLD))));
    Paragraph paragraph = Paragraph.of(text).withBlock(block).withWrap(new Wrap(true));
    frame.renderWidget(paragraph, area);
  }

  static void drawSecondTab(Frame frame, App app, Rect area) {
    Rect[] chunks =
        Layout.horizontal(new Constraint.Percentage(30), new Constraint.Percentage(70)).split(area);
    Style upStyle = Style.empty().withFg(Color.GREEN);
    Style failureStyle =
        Style.empty()
            .withFg(Color.RED)
            .withAddModifier(Modifier.RAPID_BLINK.or(Modifier.CROSSED_OUT));
    java.util.List<Row> rows = new ArrayList<>(app.servers.size());
    for (Server s : app.servers) {
      Style style = s.status().equals("Up") ? upStyle : failureStyle;
      rows.add(Row.ofStrings(s.name(), s.location(), s.status()).withStyle(style));
    }
    Table table =
        Table.of(
                rows,
                java.util.List.of(
                    new Constraint.Length(15),
                    new Constraint.Length(15),
                    new Constraint.Length(10)))
            .withHeader(
                Row.ofStrings("Server", "Location", "Status")
                    .withStyle(Style.empty().withFg(Color.YELLOW))
                    .withBottomMargin(1))
            .withBlock(Block.bordered().withTitle("Servers"));
    frame.renderWidget(table, chunks[0]);

    final java.util.List<Server> servers = app.servers;
    Marker mapMarker = app.enhancedGraphics ? Marker.Braille : Marker.Dot;
    Canvas map =
        Canvas.empty()
            .withBlock(Block.bordered().withTitle("World"))
            .withPaintFn(
                ctx -> {
                  ctx.draw(new Map(MapResolution.High, Color.WHITE));
                  ctx.layer();
                  ctx.draw(new Rectangle(0.0, 30.0, 10.0, 10.0, Color.YELLOW));
                  Server brazil = servers.get(2);
                  ctx.draw(new Circle(brazil.longitude(), brazil.latitude(), 10.0, Color.GREEN));
                  for (int i = 0; i < servers.size(); i++) {
                    Server s1 = servers.get(i);
                    for (int j = i + 1; j < servers.size(); j++) {
                      Server s2 = servers.get(j);
                      ctx.draw(
                          new jatatui.widgets.canvas.Line(
                              s1.longitude(),
                              s1.latitude(),
                              s2.longitude(),
                              s2.latitude(),
                              Color.YELLOW));
                    }
                  }
                  for (Server server : servers) {
                    Color color = server.status().equals("Up") ? Color.GREEN : Color.RED;
                    ctx.print(
                        server.longitude(),
                        server.latitude(),
                        jatatui.core.text.Line.from(Span.styled("X", Style.empty().withFg(color))));
                  }
                })
            .withMarker(mapMarker)
            .withXBounds(new double[] {-180.0, 180.0})
            .withYBounds(new double[] {-90.0, 90.0});
    frame.renderWidget(map, chunks[1]);
  }

  static void drawThirdTab(Frame frame, App app, Rect area) {
    Rect[] chunks =
        Layout.horizontal(new Constraint.Ratio(1, 2), new Constraint.Ratio(1, 2)).split(area);
    Color[] colors =
        new Color[] {
          Color.RESET,
          Color.BLACK,
          Color.RED,
          Color.GREEN,
          Color.YELLOW,
          Color.BLUE,
          Color.MAGENTA,
          Color.CYAN,
          Color.GRAY,
          Color.DARK_GRAY,
          Color.LIGHT_RED,
          Color.LIGHT_GREEN,
          Color.LIGHT_YELLOW,
          Color.LIGHT_BLUE,
          Color.LIGHT_MAGENTA,
          Color.LIGHT_CYAN,
          Color.WHITE,
        };
    java.util.List<Row> items = new ArrayList<>(colors.length);
    for (Color c : colors) {
      java.util.List<TableCell> cells =
          java.util.List.of(
              TableCell.of(Text.from(Span.raw(c.toString() + ": "))),
              TableCell.of(Text.from(Span.styled("Foreground", Style.empty().withFg(c)))),
              TableCell.of(Text.from(Span.styled("Background", Style.empty().withBg(c)))));
      items.add(Row.of(cells));
    }
    Table table =
        Table.of(
                items,
                java.util.List.of(
                    new Constraint.Ratio(1, 3),
                    new Constraint.Ratio(1, 3),
                    new Constraint.Ratio(1, 3)))
            .withBlock(Block.bordered().withTitle("Colors"));
    frame.renderWidget(table, chunks[0]);
  }

  private static long[] toLongArray(java.util.List<Long> in) {
    long[] out = new long[in.size()];
    for (int i = 0; i < in.size(); i++) out[i] = in.get(i);
    return out;
  }
}
