package tuiexamples;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Random;
import tui.Borders;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Rect;
import tui.Spans;
import tui.Style;
import tui.Terminal;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.SparklineWidget;

public final class SparklineExample {
  private SparklineExample() {}

  public static final class RandomSignal implements Iterator<Integer> {
    public final int lower;
    public final int upper;
    public final Random random;

    public RandomSignal(int lower, int upper, Random random) {
      this.lower = lower;
      this.upper = upper;
      this.random = random;
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public Integer next() {
      return random.nextInt(upper - lower) + lower;
    }
  }

  public static final class App {
    public final Iterator<Integer> signal;
    public final ArrayDeque<Integer> data1;
    public final ArrayDeque<Integer> data2;
    public final ArrayDeque<Integer> data3;

    public App(
        Iterator<Integer> signal,
        ArrayDeque<Integer> data1,
        ArrayDeque<Integer> data2,
        ArrayDeque<Integer> data3) {
      this.signal = signal;
      this.data1 = data1;
      this.data2 = data2;
      this.data3 = data3;
    }

    public static App create() {
      Iterator<Integer> signal = new RandomSignal(0, 100, new Random());
      ArrayDeque<Integer> d1 = take(signal, 200);
      ArrayDeque<Integer> d2 = take(signal, 200);
      ArrayDeque<Integer> d3 = take(signal, 200);
      return new App(signal, d1, d2, d3);
    }

    private static ArrayDeque<Integer> take(Iterator<Integer> it, int n) {
      ArrayDeque<Integer> q = new ArrayDeque<>(n);
      for (int i = 0; i < n; i++) q.addLast(it.next());
      return q;
    }

    public void onTick() {
      data1.removeLast();
      data1.addFirst(signal.next());
      data2.removeLast();
      data2.addFirst(signal.next());
      data3.removeLast();
      data3.addFirst(signal.next());
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(250);
      App app = App.create();
      runApp(terminal, app, tickRate, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, App app, Duration tickRate, CrosstermJni jni) {
    Instant[] lastTick = {Instant.now()};

    while (true) {
      terminal.draw(f -> ui(f, app));

      Duration elapsed = Duration.between(lastTick[0], Instant.now());
      Duration remaining = tickRate.minus(elapsed);
      tui.crossterm.Duration timeout =
          new tui.crossterm.Duration(remaining.toSeconds(), remaining.getNano());
      if (jni.poll(timeout)) {
        Event ev = jni.read();
        if (ev instanceof Event.Key key) {
          if (key.keyEvent().code() instanceof KeyCode.Char c && c.c() == 'q') {
            return;
          }
        }
      }
      Duration elapsed2 = Duration.between(lastTick[0], Instant.now());
      if (elapsed2.compareTo(tickRate) >= 0) {
        app.onTick();
        lastTick[0] = Instant.now();
      }
    }
  }

  public static void ui(Frame f, App app) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {
              new Constraint.Length(3),
              new Constraint.Length(3),
              new Constraint.Min(0)
            },
            true);
    Rect[] chunks = layout.split(f.size);

    SparklineWidget sparkline0 =
        SparklineWidget.empty()
            .withBlock(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Data1"))
                    .withBorders(Borders.LEFT.or(Borders.RIGHT)))
            .withData(toIntArray(app.data1))
            .withStyle(Style.empty().withFg(Color.Yellow));
    f.renderWidget(sparkline0, chunks[0]);

    SparklineWidget sparkline1 =
        SparklineWidget.empty()
            .withBlock(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Data2"))
                    .withBorders(Borders.LEFT.or(Borders.RIGHT)))
            .withData(toIntArray(app.data2))
            .withStyle(Style.empty().withBg(Color.Green));
    f.renderWidget(sparkline1, chunks[1]);

    SparklineWidget sparkline2 =
        SparklineWidget.empty()
            .withBlock(
                BlockWidget.empty()
                    .withTitle(Spans.nostyle("Data3"))
                    .withBorders(Borders.LEFT.or(Borders.RIGHT)))
            .withStyle(Style.empty().withFg(Color.Red))
            .withData(toIntArray(app.data3));
    f.renderWidget(sparkline2, chunks[2]);
  }

  private static int[] toIntArray(ArrayDeque<Integer> q) {
    int[] arr = new int[q.size()];
    int i = 0;
    for (Integer v : q) arr[i++] = v;
    return arr;
  }
}
