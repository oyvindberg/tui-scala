package jatatui.examples.chart;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Marker;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.chart.Axis;
import jatatui.widgets.chart.Chart;
import jatatui.widgets.chart.Dataset;
import jatatui.widgets.chart.GraphType;
import jatatui.widgets.chart.HiddenLegendConstraints;
import jatatui.widgets.chart.LegendPosition;
import jatatui.widgets.chart.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to handle charts.
///
/// Mirrors `examples/apps/chart/src/main.rs` from ratatui v0.30.0.
public final class ChartExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private ChartExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  /// Sin-wave signal source. Each call to [#nextPoint()] advances by `interval` along the X axis.
  private static final class SinSignal {
    double x;
    final double interval;
    final double period;
    final double scale;

    SinSignal(double interval, double period, double scale) {
      this.x = 0.0;
      this.interval = interval;
      this.period = period;
      this.scale = scale;
    }

    Point nextPoint() {
      Point p = new Point(x, Math.sin(x * 1.0 / period) * scale);
      x += interval;
      return p;
    }

    List<Point> take(int n) {
      List<Point> out = new ArrayList<>(n);
      for (int i = 0; i < n; i++) out.add(nextPoint());
      return out;
    }
  }

  private static final class App {
    final SinSignal signal1 = new SinSignal(0.2, 3.0, 18.0);
    final SinSignal signal2 = new SinSignal(0.1, 2.0, 10.0);
    List<Point> data1 = new ArrayList<>();
    List<Point> data2 = new ArrayList<>();
    double windowLo = 0.0;
    double windowHi = 20.0;

    App() {
      data1 = new ArrayList<>(signal1.take(200));
      data2 = new ArrayList<>(signal2.take(200));
    }

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      // 250 ms tick rate
      long tickRateNanos = 250_000_000L;
      long lastTickNanos = System.nanoTime();
      while (true) {
        terminal.draw(frame -> render(frame));
        long elapsedNanos = System.nanoTime() - lastTickNanos;
        long remainingNanos = Math.max(0L, tickRateNanos - elapsedNanos);
        Duration timeout =
            new Duration(remainingNanos / 1_000_000_000L, (int) (remainingNanos % 1_000_000_000L));
        if (!JNI.poll(timeout)) {
          onTick();
          lastTickNanos = System.nanoTime();
          continue;
        }
        Event ev = JNI.read();
        if (ev instanceof Event.Key keyEv) {
          KeyEvent key = keyEv.keyEvent();
          if (key.kind() == KeyEventKind.Press
              && key.code() instanceof KeyCode.Char ch
              && ch.c() == 'q') {
            return;
          }
        }
      }
    }

    void onTick() {
      // drop the first 5 elements of data1, append next 5 from signal1
      for (int i = 0; i < 5 && !data1.isEmpty(); i++) {
        data1.remove(0);
      }
      data1.addAll(signal1.take(5));

      for (int i = 0; i < 10 && !data2.isEmpty(); i++) {
        data2.remove(0);
      }
      data2.addAll(signal2.take(10));

      windowLo += 1.0;
      windowHi += 1.0;
    }

    void render(Frame frame) {
      Layout vertical = Layout.vertical(new Constraint.Fill(1), new Constraint.Fill(1));
      Rect[] rows = frame.area().layout(vertical, 2);
      Rect top = rows[0];
      Rect bottom = rows[1];

      Layout topHorizontal =
          Layout.horizontal(new Constraint.Fill(1), new Constraint.Length(29));
      Rect[] topCols = top.layout(topHorizontal, 2);
      Rect animatedChart = topCols[0];
      Rect barChart = topCols[1];

      Layout bottomHorizontal = Layout.horizontal(new Constraint.Fill(1), new Constraint.Fill(1));
      Rect[] bottomCols = bottom.layout(bottomHorizontal, 2);
      Rect lineChart = bottomCols[0];
      Rect scatter = bottomCols[1];

      renderAnimatedChart(frame, animatedChart);
      renderBarChart(frame, barChart);
      renderLineChart(frame, lineChart);
      renderScatter(frame, scatter);
    }

    void renderAnimatedChart(Frame frame, Rect area) {
      List<Line> xLabels =
          List.of(
              Line.from(
                  Span.styled(
                      Long.toString((long) windowLo),
                      Style.empty().withAddModifier(Modifier.BOLD))),
              Line.from(Span.from(Double.toString((windowLo + windowHi) / 2.0))),
              Line.from(
                  Span.styled(
                      Long.toString((long) windowHi),
                      Style.empty().withAddModifier(Modifier.BOLD))));

      Dataset ds1 =
          Dataset.empty()
              .withName("data2")
              .withMarker(Marker.Dot)
              .withStyle(Style.empty().withFg(Color.CYAN))
              .withData(data1);
      Dataset ds2 =
          Dataset.empty()
              .withName("data3")
              .withMarker(Marker.Braille)
              .withStyle(Style.empty().withFg(Color.YELLOW))
              .withData(data2);

      Chart chart =
          Chart.of(ds1, ds2)
              .withBlock(Block.bordered())
              .withXAxis(
                  Axis.empty()
                      .withTitle("X Axis")
                      .withStyle(Style.empty().withFg(Color.GRAY))
                      .withLabels(xLabels)
                      .withBounds(windowLo, windowHi))
              .withYAxis(
                  Axis.empty()
                      .withTitle("Y Axis")
                      .withStyle(Style.empty().withFg(Color.GRAY))
                      .withLabels(
                          Line.from("-20").bold(),
                          Line.from("0"),
                          Line.from("20").bold())
                      .withBounds(-20.0, 20.0));

      frame.renderWidget(chart, area);
    }
  }

  private static void renderBarChart(Frame frame, Rect barChartArea) {
    Dataset dataset =
        Dataset.empty()
            .withMarker(Marker.HalfBlock)
            .withStyle(Style.empty().withFg(Color.BLUE))
            .withGraphType(GraphType.Bar)
            // a bell curve
            .withData(
                new Point(0.0, 0.4),
                new Point(10.0, 2.9),
                new Point(20.0, 13.5),
                new Point(30.0, 41.1),
                new Point(40.0, 80.1),
                new Point(50.0, 100.0),
                new Point(60.0, 80.1),
                new Point(70.0, 41.1),
                new Point(80.0, 13.5),
                new Point(90.0, 2.9),
                new Point(100.0, 0.4));

    Chart chart =
        Chart.of(dataset)
            .withBlock(
                Block.bordered().withTitleTop(Line.from("Bar chart").cyan().bold().centered()))
            .withXAxis(
                Axis.empty()
                    .withStyle(Style.empty().gray())
                    .withBounds(0.0, 100.0)
                    .withLabels(Line.from("0").bold(), Line.from("50"), Line.from("100.0").bold()))
            .withYAxis(
                Axis.empty()
                    .withStyle(Style.empty().gray())
                    .withBounds(0.0, 100.0)
                    .withLabels(Line.from("0").bold(), Line.from("50"), Line.from("100.0").bold()))
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Ratio(1, 2), new Constraint.Ratio(1, 2)));

    frame.renderWidget(chart, barChartArea);
  }

  private static void renderLineChart(Frame frame, Rect area) {
    Dataset dataset =
        Dataset.empty()
            .withName(Line.from("Line from only 2 points").italic())
            .withMarker(Marker.Braille)
            .withStyle(Style.empty().withFg(Color.YELLOW))
            .withGraphType(GraphType.Line)
            .withData(new Point(1.0, 1.0), new Point(4.0, 4.0));

    Chart chart =
        Chart.of(dataset)
            .withBlock(
                Block.bordered().withTitle(Line.from("Line chart").cyan().bold().centered()))
            .withXAxis(
                Axis.empty()
                    .withTitle("X Axis")
                    .withStyle(Style.empty().gray())
                    .withBounds(0.0, 5.0)
                    .withLabels(Line.from("0").bold(), Line.from("2.5"), Line.from("5.0").bold()))
            .withYAxis(
                Axis.empty()
                    .withTitle("Y Axis")
                    .withStyle(Style.empty().gray())
                    .withBounds(0.0, 5.0)
                    .withLabels(Line.from("0").bold(), Line.from("2.5"), Line.from("5.0").bold()))
            .withLegendPosition(LegendPosition.TopLeft)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Ratio(1, 2), new Constraint.Ratio(1, 2)));

    frame.renderWidget(chart, area);
  }

  private static void renderScatter(Frame frame, Rect area) {
    Dataset heavy =
        Dataset.empty()
            .withName("Heavy")
            .withMarker(Marker.Dot)
            .withGraphType(GraphType.Scatter)
            .withStyle(Style.empty().yellow())
            .withData(HEAVY_PAYLOAD_DATA);
    Dataset medium =
        Dataset.empty()
            .withName(Line.from("Medium").underlined())
            .withMarker(Marker.Braille)
            .withGraphType(GraphType.Scatter)
            .withStyle(Style.empty().magenta())
            .withData(MEDIUM_PAYLOAD_DATA);
    Dataset small =
        Dataset.empty()
            .withName("Small")
            .withMarker(Marker.Dot)
            .withGraphType(GraphType.Scatter)
            .withStyle(Style.empty().cyan())
            .withData(SMALL_PAYLOAD_DATA);

    Chart chart =
        Chart.of(heavy, medium, small)
            .withBlock(
                Block.bordered().withTitle(Line.from("Scatter chart").cyan().bold().centered()))
            .withXAxis(
                Axis.empty()
                    .withTitle("Year")
                    .withBounds(1960.0, 2020.0)
                    .withStyle(Style.empty().withFg(Color.GRAY))
                    .withLabels("1960", "1990", "2020"))
            .withYAxis(
                Axis.empty()
                    .withTitle("Cost")
                    .withBounds(0.0, 75000.0)
                    .withStyle(Style.empty().withFg(Color.GRAY))
                    .withLabels("0", "37 500", "75 000"))
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Ratio(1, 2), new Constraint.Ratio(1, 2)));

    frame.renderWidget(chart, area);
  }

  // Data from https://ourworldindata.org/space-exploration-satellites
  private static final List<Point> HEAVY_PAYLOAD_DATA =
      List.of(
          new Point(1965.0, 8200.0),
          new Point(1967.0, 5400.0),
          new Point(1981.0, 65400.0),
          new Point(1989.0, 30800.0),
          new Point(1997.0, 10200.0),
          new Point(2004.0, 11600.0),
          new Point(2014.0, 4500.0),
          new Point(2016.0, 7900.0),
          new Point(2018.0, 1500.0));

  private static final List<Point> MEDIUM_PAYLOAD_DATA =
      List.of(
          new Point(1963.0, 29500.0),
          new Point(1964.0, 30600.0),
          new Point(1965.0, 177_900.0),
          new Point(1965.0, 21000.0),
          new Point(1966.0, 17900.0),
          new Point(1966.0, 8400.0),
          new Point(1975.0, 17500.0),
          new Point(1982.0, 8300.0),
          new Point(1985.0, 5100.0),
          new Point(1988.0, 18300.0),
          new Point(1990.0, 38800.0),
          new Point(1990.0, 9900.0),
          new Point(1991.0, 18700.0),
          new Point(1992.0, 9100.0),
          new Point(1994.0, 10500.0),
          new Point(1994.0, 8500.0),
          new Point(1994.0, 8700.0),
          new Point(1997.0, 6200.0),
          new Point(1999.0, 18000.0),
          new Point(1999.0, 7600.0),
          new Point(1999.0, 8900.0),
          new Point(1999.0, 9600.0),
          new Point(2000.0, 16000.0),
          new Point(2001.0, 10000.0),
          new Point(2002.0, 10400.0),
          new Point(2002.0, 8100.0),
          new Point(2010.0, 2600.0),
          new Point(2013.0, 13600.0),
          new Point(2017.0, 8000.0));

  private static final List<Point> SMALL_PAYLOAD_DATA =
      List.of(
          new Point(1961.0, 118_500.0),
          new Point(1962.0, 14900.0),
          new Point(1975.0, 21400.0),
          new Point(1980.0, 32800.0),
          new Point(1988.0, 31100.0),
          new Point(1990.0, 41100.0),
          new Point(1993.0, 23600.0),
          new Point(1994.0, 20600.0),
          new Point(1994.0, 34600.0),
          new Point(1996.0, 50600.0),
          new Point(1997.0, 19200.0),
          new Point(1997.0, 45800.0),
          new Point(1998.0, 19100.0),
          new Point(2000.0, 73100.0),
          new Point(2003.0, 11200.0),
          new Point(2008.0, 12600.0),
          new Point(2010.0, 30500.0),
          new Point(2012.0, 20000.0),
          new Point(2013.0, 10600.0),
          new Point(2013.0, 34500.0),
          new Point(2015.0, 10600.0),
          new Point(2018.0, 23100.0),
          new Point(2019.0, 17300.0));
}
