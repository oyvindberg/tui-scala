package tuiexamples;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import tui.crossterm.CrosstermJni;

public final class Launcher {
  private Launcher() {}

  public static final Map<String, Consumer<String[]>> DEMOS = new LinkedHashMap<>();

  static {
    DEMOS.put("barchart", BarChartExample::main);
    DEMOS.put("block", BlockExample::main);
    DEMOS.put("canvas", CanvasExample::main);
    DEMOS.put("chart", ChartExample::main);
    DEMOS.put("colors", ColorsExample::main);
    DEMOS.put("colors_rgb", ColorsRgbExample::main);
    DEMOS.put("custom_widget", CustomWidgetExample::main);
    DEMOS.put("demo", tuiexamples.demo.Demo::main);
    DEMOS.put("gauge", GaugeExample::main);
    DEMOS.put("hello_world", HelloWorldExample::main);
    DEMOS.put("layout", LayoutExample::main);
    DEMOS.put("list", ListExample::main);
    DEMOS.put("modifiers", ModifiersExample::main);
    DEMOS.put("paragraph", ParagraphExample::main);
    DEMOS.put("popup", PopupExample::main);
    DEMOS.put("sparkline", SparklineExample::main);
    DEMOS.put("table", TableExample::main);
    DEMOS.put("tabs", TabsExample::main);
    DEMOS.put("user_input", UserInputExample::main);
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("specify which demo one of " + String.join(",", DEMOS.keySet()) + " as parameter");
      return;
    }
    String first = args[0];
    if (first.equals("check")) {
      new CrosstermJni(); // run for the side effect of testing jni library
      System.out.println("ok");
      return;
    }
    Consumer<String[]> demo = DEMOS.get(first);
    if (demo == null) {
      System.err.println(first + " is not among " + String.join(",", DEMOS.keySet()));
      return;
    }
    String[] rest = Arrays.copyOfRange(args, 1, args.length);
    demo.accept(rest);
  }
}
