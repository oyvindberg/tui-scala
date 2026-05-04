package jatatui.examples.demo;

import java.util.ArrayList;
import java.util.List;

/// Mutable application state for the demo.
///
/// Mirrors `examples/apps/demo/src/app.rs` — `App&lt;'a&gt;`. All fields are mutated in place; this
/// matches the upstream `&amp;mut self` API.
public final class App {

  static final List<String> TASKS;
  static final List<LogEntry> LOGS;
  static final List<BarEntry> EVENTS;

  static {
    List<String> tasks = new ArrayList<>(24);
    for (int i = 1; i <= 24; i++) tasks.add("Item" + i);
    TASKS = List.copyOf(tasks);

    LOGS = List.of(
        new LogEntry("Event1", "INFO"),
        new LogEntry("Event2", "INFO"),
        new LogEntry("Event3", "CRITICAL"),
        new LogEntry("Event4", "ERROR"),
        new LogEntry("Event5", "INFO"),
        new LogEntry("Event6", "INFO"),
        new LogEntry("Event7", "WARNING"),
        new LogEntry("Event8", "INFO"),
        new LogEntry("Event9", "INFO"),
        new LogEntry("Event10", "INFO"),
        new LogEntry("Event11", "CRITICAL"),
        new LogEntry("Event12", "INFO"),
        new LogEntry("Event13", "INFO"),
        new LogEntry("Event14", "INFO"),
        new LogEntry("Event15", "INFO"),
        new LogEntry("Event16", "INFO"),
        new LogEntry("Event17", "ERROR"),
        new LogEntry("Event18", "ERROR"),
        new LogEntry("Event19", "INFO"),
        new LogEntry("Event20", "INFO"),
        new LogEntry("Event21", "WARNING"),
        new LogEntry("Event22", "INFO"),
        new LogEntry("Event23", "INFO"),
        new LogEntry("Event24", "WARNING"),
        new LogEntry("Event25", "INFO"),
        new LogEntry("Event26", "INFO"));

    EVENTS = List.of(
        new BarEntry("B1", 9),
        new BarEntry("B2", 12),
        new BarEntry("B3", 5),
        new BarEntry("B4", 8),
        new BarEntry("B5", 2),
        new BarEntry("B6", 4),
        new BarEntry("B7", 5),
        new BarEntry("B8", 9),
        new BarEntry("B9", 14),
        new BarEntry("B10", 15),
        new BarEntry("B11", 1),
        new BarEntry("B12", 0),
        new BarEntry("B13", 4),
        new BarEntry("B14", 6),
        new BarEntry("B15", 4),
        new BarEntry("B16", 6),
        new BarEntry("B17", 4),
        new BarEntry("B18", 7),
        new BarEntry("B19", 13),
        new BarEntry("B20", 8),
        new BarEntry("B21", 11),
        new BarEntry("B22", 9),
        new BarEntry("B23", 3),
        new BarEntry("B24", 5));
  }

  public final String title;
  public boolean shouldQuit;
  public final TabsState tabs;
  public boolean showChart;
  public double progress;
  public final LongSignal sparkline;
  public final StatefulList<String> tasks;
  public final StatefulList<LogEntry> logs;
  public final Signals signals;
  /// The current bar chart data; mutates in place to mirror `Vec&lt;(&amp;str, u64)&gt;`.
  public final List<BarEntry> barchart;
  public final List<Server> servers;
  public final boolean enhancedGraphics;

  /// Builds a fresh [App] with the given title and graphics quality flag.
  public App(String title, boolean enhancedGraphics) {
    this.title = title;
    this.shouldQuit = false;
    this.tabs = new TabsState(List.of("Tab0", "Tab1", "Tab2"));
    this.showChart = true;
    this.progress = 0.0;

    RandomSignal randSignal = new RandomSignal(0, 100);
    this.sparkline = LongSignal.initial(randSignal, 300, 1);

    SinSignal sin1 = new SinSignal(0.2, 3.0, 18.0);
    PointSignal sin1Sig = PointSignal.initial(sin1, 100, 5);
    SinSignal sin2 = new SinSignal(0.1, 2.0, 10.0);
    PointSignal sin2Sig = PointSignal.initial(sin2, 200, 10);
    this.signals = new Signals(sin1Sig, sin2Sig, new Window(0.0, 20.0));

    this.tasks = StatefulList.withItems(TASKS);
    this.logs = StatefulList.withItems(LOGS);
    this.barchart = new ArrayList<>(EVENTS);

    this.servers = List.of(
        new Server("NorthAmerica-1", "New York City", 40.71, -74.00, "Up"),
        new Server("Europe-1", "Paris", 48.85, 2.35, "Failure"),
        new Server("SouthAmerica-1", "São Paulo", -23.54, -46.62, "Up"),
        new Server("Asia-1", "Singapore", 1.35, 103.86, "Up"));

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
      case 'q' -> this.shouldQuit = true;
      case 't' -> this.showChart = !this.showChart;
      default -> {
        // ignore
      }
    }
  }

  public void onTick() {
    // Update progress
    this.progress += 0.001;
    if (this.progress > 1.0) {
      this.progress = 0.0;
    }

    sparkline.onTick();
    signals.onTick();

    // Cycle the logs: pop the last entry and re-insert at the front.
    LogEntry log = logs.items.remove(logs.items.size() - 1);
    logs.items.add(0, log);

    // Cycle the bar chart entries: pop the last and re-insert at the front.
    BarEntry event = barchart.remove(barchart.size() - 1);
    barchart.add(0, event);
  }
}
