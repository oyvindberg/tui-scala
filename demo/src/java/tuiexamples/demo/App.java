package tuiexamples.demo;

import java.util.ArrayDeque;
import java.util.Random;
import tui.Point;
import tui.widgets.BarChartWidget;

public final class App {
  public final String title;
  public boolean shouldQuit;
  public final TabsState tabs;
  public boolean showChart;
  public double progress;
  public final SignalInt sparkline;
  public final StatefulList<String> tasks;
  public final StatefulList<Log> logs;
  public final Signals signals;
  public final ArrayDeque<BarChartWidget.LabelValue> barchart;
  public final Server[] servers;
  public final boolean enhancedGraphics;

  public App(
      String title,
      boolean shouldQuit,
      TabsState tabs,
      boolean showChart,
      double progress,
      SignalInt sparkline,
      StatefulList<String> tasks,
      StatefulList<Log> logs,
      Signals signals,
      ArrayDeque<BarChartWidget.LabelValue> barchart,
      Server[] servers,
      boolean enhancedGraphics) {
    this.title = title;
    this.shouldQuit = shouldQuit;
    this.tabs = tabs;
    this.showChart = showChart;
    this.progress = progress;
    this.sparkline = sparkline;
    this.tasks = tasks;
    this.logs = logs;
    this.signals = signals;
    this.barchart = barchart;
    this.servers = servers;
    this.enhancedGraphics = enhancedGraphics;
  }

  public void onUp() {
    tasks.previous();
  }

  public void onDown() {
    tasks.next();
  }

  public void onRight() {
    tabs.next();
  }

  public void onLeft() {
    tabs.previous();
  }

  public void onKey(char c) {
    switch (c) {
      case 'q' -> shouldQuit = true;
      case 't' -> showChart = !showChart;
      default -> {}
    }
  }

  public void onTick() {
    progress += 0.001;
    if (progress > 1.0) {
      progress = 0.0;
    }
    sparkline.onTick();
    signals.onTick();

    Log log = logs.items.removeLast();
    logs.items.addFirst(log);

    BarChartWidget.LabelValue event = barchart.removeLast();
    barchart.addFirst(event);
  }

  public record Log(String name, String level) {}

  public static App create(String title, boolean enhancedGraphics) {
    RandomSignal randSignal = new RandomSignal(0, 100, new Random());
    int[] sparklinePoints = randSignal.take(300);
    SinSignal sinSignal = new SinSignal(0.2, 3.0, 18.0);
    Point[] sin1Points = sinSignal.take(100);
    SinSignal sinSignal2 = new SinSignal(0.1, 2.0, 10.0);
    Point[] sin2Points = sinSignal2.take(200);

    ArrayDeque<BarChartWidget.LabelValue> barchart = new ArrayDeque<>();
    for (BarChartWidget.LabelValue lv : EVENTS) barchart.addLast(lv);

    return new App(
        title,
        false,
        new TabsState(new String[] {"Tab0", "Tab1", "Tab2"}),
        true,
        0.0,
        new SignalInt(randSignal, sparklinePoints, 1),
        StatefulList.withItems(TASKS),
        StatefulList.withItems(LOGS),
        new Signals(
            new SignalPoint(sinSignal, sin1Points, 5),
            new SignalPoint(sinSignal2, sin2Points, 10),
            new Point(0.0, 20.0)),
        barchart,
        new Server[] {
          new Server("NorthAmerica-1", "New York City", new Point(40.71, -74.00), "Up"),
          new Server("Europe-1", "Paris", new Point(48.85, 2.35), "Failure"),
          new Server("SouthAmerica-1", "São Paulo", new Point(-23.54, -46.62), "Up"),
          new Server("Asia-1", "Singapore", new Point(1.35, 103.86), "Up")
        },
        enhancedGraphics);
  }

  public static final String[] TASKS = {
    "Item1", "Item2", "Item3", "Item4", "Item5", "Item6", "Item7", "Item8", "Item9", "Item10",
    "Item11", "Item12", "Item13", "Item14", "Item15", "Item16", "Item17", "Item18", "Item19",
    "Item20", "Item21", "Item22", "Item23", "Item24"
  };

  public static final Log[] LOGS = {
    new Log("Event1", "INFO"),
    new Log("Event2", "INFO"),
    new Log("Event3", "CRITICAL"),
    new Log("Event4", "ERROR"),
    new Log("Event5", "INFO"),
    new Log("Event6", "INFO"),
    new Log("Event7", "WARNING"),
    new Log("Event8", "INFO"),
    new Log("Event9", "INFO"),
    new Log("Event10", "INFO"),
    new Log("Event11", "CRITICAL"),
    new Log("Event12", "INFO"),
    new Log("Event13", "INFO"),
    new Log("Event14", "INFO"),
    new Log("Event15", "INFO"),
    new Log("Event16", "INFO"),
    new Log("Event17", "ERROR"),
    new Log("Event18", "ERROR"),
    new Log("Event19", "INFO"),
    new Log("Event20", "INFO"),
    new Log("Event21", "WARNING"),
    new Log("Event22", "INFO"),
    new Log("Event23", "INFO"),
    new Log("Event24", "WARNING"),
    new Log("Event25", "INFO"),
    new Log("Event26", "INFO")
  };

  public static final BarChartWidget.LabelValue[] EVENTS = {
    new BarChartWidget.LabelValue("B1", 9),
    new BarChartWidget.LabelValue("B2", 12),
    new BarChartWidget.LabelValue("B3", 5),
    new BarChartWidget.LabelValue("B4", 8),
    new BarChartWidget.LabelValue("B5", 2),
    new BarChartWidget.LabelValue("B6", 4),
    new BarChartWidget.LabelValue("B7", 5),
    new BarChartWidget.LabelValue("B8", 9),
    new BarChartWidget.LabelValue("B9", 14),
    new BarChartWidget.LabelValue("B10", 15),
    new BarChartWidget.LabelValue("B11", 1),
    new BarChartWidget.LabelValue("B12", 0),
    new BarChartWidget.LabelValue("B13", 4),
    new BarChartWidget.LabelValue("B14", 6),
    new BarChartWidget.LabelValue("B15", 4),
    new BarChartWidget.LabelValue("B16", 6),
    new BarChartWidget.LabelValue("B17", 4),
    new BarChartWidget.LabelValue("B18", 7),
    new BarChartWidget.LabelValue("B19", 13),
    new BarChartWidget.LabelValue("B20", 8),
    new BarChartWidget.LabelValue("B21", 11),
    new BarChartWidget.LabelValue("B22", 9),
    new BarChartWidget.LabelValue("B23", 3),
    new BarChartWidget.LabelValue("B24", 5)
  };
}
