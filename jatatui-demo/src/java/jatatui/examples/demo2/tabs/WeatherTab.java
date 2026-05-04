package jatatui.examples.demo2.tabs;

import static jatatui.examples.demo2.Theme.THEME;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Direction;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Line;
import jatatui.examples.demo2.Colors;
import jatatui.widgets.Clear;
import jatatui.widgets.barchart.Bar;
import jatatui.widgets.barchart.BarChart;
import jatatui.widgets.barchart.BarGroup;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.Padding;
import jatatui.widgets.calendar.Calendar;
import jatatui.widgets.calendar.CalendarEventStore;
import jatatui.widgets.gauge.LineGauge;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/// Mirrors `apps/demo2/src/tabs/weather.rs`.
///
/// The "Weather" tab. Shows a calendar of the current month, a vertical bar chart of weekly
/// temperatures, a horizontal bar chart of seasonal temperatures, and a line gauge whose color
/// hue depends on the simulated download progress.
public final class WeatherTab {

  /// Current download-progress counter. Mirrors `pub download_progress: usize`.
  public int downloadProgress;

  private WeatherTab() {
    this.downloadProgress = 0;
  }

  /// Constructs a new [WeatherTab] with default state.
  public static WeatherTab defaultTab() {
    return new WeatherTab();
  }

  /// Simulate a download indicator by decrementing the progress.
  public void prev() {
    if (downloadProgress > 0) downloadProgress--;
  }

  /// Simulate a download indicator by incrementing the progress.
  public void next() {
    if (downloadProgress < Integer.MAX_VALUE) downloadProgress++;
  }

  /// Render this tab into the given area of the buffer. Mirrors `impl Widget for WeatherTab`.
  public void render(Rect outerArea, Buffer buf) {
    new Colors.RgbSwatch().render(outerArea, buf);
    Rect area = outerArea.inner(new Margin(2, 1));
    Clear.instance().render(area, buf);
    Block.empty().withStyle(THEME.content).render(area, buf);

    area = area.inner(new Margin(2, 1));
    Layout tabLayout =
        Layout.vertical(new Constraint.Min(0), new Constraint.Length(1), new Constraint.Length(1));
    Rect[] tabSplit = area.layout(tabLayout, 3);
    Rect main = tabSplit[0];
    // tabSplit[1] is a one-row gap (constraint Length(1) with no content).
    Rect gauges = tabSplit[2];
    Layout mainLayout = Layout.horizontal(new Constraint.Length(23), new Constraint.Min(0));
    Rect[] mainSplit = main.layout(mainLayout, 2);
    Rect calendar = mainSplit[0];
    Rect charts = mainSplit[1];
    Layout chartsLayout = Layout.vertical(new Constraint.Length(29), new Constraint.Min(0));
    Rect[] chartsSplit = charts.layout(chartsLayout, 2);
    Rect simple = chartsSplit[0];
    Rect horizontal = chartsSplit[1];

    renderCalendar(calendar, buf);
    renderSimpleBarchart(simple, buf);
    renderHorizontalBarchart(horizontal, buf);
    renderGauge(downloadProgress, gauges, buf);
  }

  private static void renderCalendar(Rect area, Buffer buf) {
    LocalDate date = LocalDate.now();
    Calendar.of(
            date,
            CalendarEventStore.today(
                Style.empty().withFg(Color.RED).withAddModifier(Modifier.BOLD)))
        .withBlock(Block.empty().withPadding(new Padding(0, 0, 2, 0)))
        .withShowMonthHeader(Style.empty().withAddModifier(Modifier.BOLD))
        .withShowWeekdaysHeader(Style.empty().withAddModifier(Modifier.ITALIC))
        .render(area, buf);
  }

  private record DayBar(String label, long value) {}

  private static void renderSimpleBarchart(Rect area, Buffer buf) {
    DayBar[] data = {
      new DayBar("Sat", 76),
      new DayBar("Sun", 69),
      new DayBar("Mon", 65),
      new DayBar("Tue", 67),
      new DayBar("Wed", 65),
      new DayBar("Thu", 69),
      new DayBar("Fri", 73)
    };
    List<Bar> bars = new ArrayList<>(data.length);
    for (DayBar d : data) {
      Style barStyle =
          (d.value() > 70) ? Style.empty().withFg(Color.RED) : Style.empty().withFg(Color.YELLOW);
      Style valueStyle =
          (d.value() > 70)
              ? Style.empty().withFg(Color.GRAY).withBg(Color.RED).withAddModifier(Modifier.BOLD)
              : Style.empty()
                  .withFg(Color.DARK_GRAY)
                  .withBg(Color.YELLOW)
                  .withAddModifier(Modifier.BOLD);
      bars.add(
          Bar.empty()
              .withValue(d.value())
              .withTextValue(d.value() + "°")
              .withStyle(barStyle)
              .withValueStyle(valueStyle)
              .withLabel(d.label()));
    }
    BarGroup group = BarGroup.empty().withBars(bars);
    BarChart.empty().withGroup(group).withBarWidth(3).withBarGap(1).render(area, buf);
  }

  private static void renderHorizontalBarchart(Rect area, Buffer buf) {
    Color bg = new Color.Rgb(32, 48, 96);
    Bar[] data = {
      Bar.empty().withTextValue("Winter 37-51").withValue(51),
      Bar.empty().withTextValue("Spring 40-65").withValue(65),
      Bar.empty().withTextValue("Summer 54-77").withValue(77),
      Bar.empty()
          .withTextValue("Fall 41-71")
          .withValue(71)
          .withValueStyle(Style.empty().withAddModifier(Modifier.BOLD)) // current season
    };
    List<Bar> bars = new ArrayList<>(data.length);
    for (Bar b : data) bars.add(b);
    BarGroup group = BarGroup.empty().withLabel("GPU").withBars(bars);
    BarChart.empty()
        .withBlock(Block.empty().withPadding(new Padding(0, 0, 2, 0)))
        .withDirection(Direction.Horizontal)
        .withGroup(group)
        .withBarGap(1)
        .withBarStyle(Style.empty().withFg(bg))
        .withValueStyle(Style.empty().withBg(bg).withFg(Color.GRAY))
        .render(area, buf);
  }

  /// Mirrors `pub fn render_gauge`. Maps the integer progress counter to a percentage that maxes
  /// out at 100 (incrementing by `3` per key press).
  public static void renderGauge(int progress, Rect area, Buffer buf) {
    double percent = Math.min((double) progress * 3.0, 100.0);
    renderLineGauge(percent, area, buf);
  }

  private static void renderLineGauge(double percent, Rect area, Buffer buf) {
    // cycle color hue based on the percent for a neat effect yellow -> red
    float hue = 90.0f - (float) percent * 0.6f;
    float value = Colors.MAX_VALUE;
    Color filledColor = Colors.colorFromOklab(hue, Colors.MAX_SATURATION, value);
    Color unfilledColor = Colors.colorFromOklab(hue, Colors.MAX_SATURATION, value * 0.5f);
    String label = (percent < 100.0) ? "Downloading: " + percent + "%" : "Download Complete!";
    LineGauge.empty()
        .withRatio(percent / 100.0)
        .withLabel(label)
        .withStyle(Style.empty().withFg(Color.LIGHT_BLUE))
        .withFilledStyle(Style.empty().withFg(filledColor))
        .withUnfilledStyle(Style.empty().withFg(unfilledColor))
        .withFilledSymbol(Line.THICK_HORIZONTAL)
        .withUnfilledSymbol(Line.THICK_HORIZONTAL)
        .render(area, buf);
  }
}
