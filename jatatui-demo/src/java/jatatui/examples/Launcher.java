package jatatui.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/// Single entry point for every ported example. Pass the example name as the
/// first argument; remaining arguments are forwarded to the example's `main`.
///
/// Example: `java -cp ... jatatui.examples.Launcher hello-world`
///
/// Run with no argument (or `--list` / `-l`) to see all known names.
public final class Launcher {

  @FunctionalInterface
  private interface ExampleEntry {
    void run(String[] args) throws IOException;
  }

  private static final Map<String, ExampleEntry> EXAMPLES = new LinkedHashMap<>();

  static {
    EXAMPLES.put("advanced-widget-impl", jatatui.examples.advancedwidgetimpl.AdvancedWidgetImplExample::main);
    EXAMPLES.put("calendar-explorer",    jatatui.examples.calendarexplorer.CalendarExplorerExample::main);
    EXAMPLES.put("canvas",               jatatui.examples.canvas.CanvasExample::main);
    EXAMPLES.put("chart",                jatatui.examples.chart.ChartExample::main);
    EXAMPLES.put("color-explorer",       jatatui.examples.colorexplorer.ColorExplorerExample::main);
    EXAMPLES.put("colors-rgb",           jatatui.examples.colorsrgb.ColorsRgbExample::main);
    EXAMPLES.put("constraint-explorer",  jatatui.examples.constraintexplorer.ConstraintExplorerExample::main);
    EXAMPLES.put("constraints",          jatatui.examples.constraints.ConstraintsExample::main);
    EXAMPLES.put("custom-widget",        jatatui.examples.customwidget.CustomWidgetExample::main);
    EXAMPLES.put("demo",                 jatatui.examples.demo.DemoExample::main);
    EXAMPLES.put("demo2",                jatatui.examples.demo2.Demo2Example::main);
    EXAMPLES.put("flex",                 jatatui.examples.flex.FlexExample::main);
    EXAMPLES.put("gauge",                jatatui.examples.gauge.GaugeExample::main);
    EXAMPLES.put("hello-world",          jatatui.examples.helloworld.HelloWorldExample::main);
    EXAMPLES.put("hyperlink",            jatatui.examples.hyperlink.HyperlinkExample::main);
    EXAMPLES.put("inline",               jatatui.examples.inline.InlineExample::main);
    EXAMPLES.put("input-form",           jatatui.examples.inputform.InputFormExample::main);
    EXAMPLES.put("minimal",              jatatui.examples.minimal.MinimalExample::main);
    EXAMPLES.put("modifiers",            jatatui.examples.modifiers.ModifiersExample::main);
    EXAMPLES.put("mouse-drawing",        jatatui.examples.mousedrawing.MouseDrawingExample::main);
    EXAMPLES.put("panic",                jatatui.examples.panic.PanicExample::main);
    EXAMPLES.put("popup",                jatatui.examples.popup.PopupExample::main);
    EXAMPLES.put("scrollbar",            jatatui.examples.scrollbar.ScrollbarExample::main);
    EXAMPLES.put("table",                jatatui.examples.table.TableExample::main);
    EXAMPLES.put("todo-list",            jatatui.examples.todolist.TodoListExample::main);
    EXAMPLES.put("user-input",           jatatui.examples.userinput.UserInputExample::main);
    EXAMPLES.put("widget-ref-container", jatatui.examples.widgetrefcontainer.WidgetRefContainerExample::main);
  }

  private Launcher() {}

  public static void main(String[] args) throws IOException {
    if (args.length == 0 || args[0].equals("--list") || args[0].equals("-l")) {
      printList();
      return;
    }
    String name = args[0];
    ExampleEntry entry = EXAMPLES.get(name);
    if (entry == null) {
      System.err.println("Unknown example: " + name);
      printList();
      System.exit(2);
      return;
    }
    String[] rest = Arrays.copyOfRange(args, 1, args.length);
    entry.run(rest);
  }

  private static void printList() {
    System.out.println("Available examples:");
    for (String name : EXAMPLES.keySet()) {
      System.out.println("  " + name);
    }
  }
}
