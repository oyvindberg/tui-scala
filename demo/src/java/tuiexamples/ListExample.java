package tuiexamples;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.Borders;
import tui.Color;
import tui.Corner;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Modifier;
import tui.Rect;
import tui.Span;
import tui.Spans;
import tui.Style;
import tui.Terminal;
import tui.Text;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.ListWidget;

public final class ListExample {
  private ListExample() {}

  public record LabeledCount(String label, int count) {}

  public record Event2(String name, String level) {}

  public static final class StatefulList {
    public final ListWidget.State state;
    public final LabeledCount[] items;

    public StatefulList(ListWidget.State state, LabeledCount[] items) {
      this.state = state;
      this.items = items;
    }

    public void next() {
      int i;
      if (state.selected.isPresent()) {
        int s = state.selected.get();
        i = s >= items.length - 1 ? 0 : s + 1;
      } else {
        i = 0;
      }
      state.select(Optional.of(i));
    }

    public void previous() {
      int i;
      if (state.selected.isPresent()) {
        int s = state.selected.get();
        i = s == 0 ? items.length - 1 : s - 1;
      } else {
        i = 0;
      }
      state.select(Optional.of(i));
    }

    public void unselect() {
      state.select(Optional.empty());
    }
  }

  public static final class App {
    public final StatefulList items;
    public final ArrayDeque<Event2> events;

    public App(StatefulList items, ArrayDeque<Event2> events) {
      this.items = items;
      this.events = events;
    }

    public void onTick() {
      Event2 ev = events.removeFirst();
      events.addLast(ev);
    }
  }

  public static final LabeledCount[] ITEMS = {
    new LabeledCount("Item0", 1),
    new LabeledCount("Item1", 2),
    new LabeledCount("Item2", 1),
    new LabeledCount("Item3", 3),
    new LabeledCount("Item4", 1),
    new LabeledCount("Item5", 4),
    new LabeledCount("Item6", 1),
    new LabeledCount("Item7", 3),
    new LabeledCount("Item8", 1),
    new LabeledCount("Item9", 6),
    new LabeledCount("Item10", 1),
    new LabeledCount("Item11", 3),
    new LabeledCount("Item12", 1),
    new LabeledCount("Item13", 2),
    new LabeledCount("Item14", 1),
    new LabeledCount("Item15", 1),
    new LabeledCount("Item16", 4),
    new LabeledCount("Item17", 1),
    new LabeledCount("Item18", 5),
    new LabeledCount("Item19", 4),
    new LabeledCount("Item20", 1),
    new LabeledCount("Item21", 2),
    new LabeledCount("Item22", 1),
    new LabeledCount("Item23", 3),
    new LabeledCount("Item24", 1)
  };

  public static final Event2[] EVENTS = {
    new Event2("Event1", "INFO"),
    new Event2("Event2", "INFO"),
    new Event2("Event3", "CRITICAL"),
    new Event2("Event4", "ERROR"),
    new Event2("Event5", "INFO"),
    new Event2("Event6", "INFO"),
    new Event2("Event7", "WARNING"),
    new Event2("Event8", "INFO"),
    new Event2("Event9", "INFO"),
    new Event2("Event10", "INFO"),
    new Event2("Event11", "CRITICAL"),
    new Event2("Event12", "INFO"),
    new Event2("Event13", "INFO"),
    new Event2("Event14", "INFO"),
    new Event2("Event15", "INFO"),
    new Event2("Event16", "INFO"),
    new Event2("Event17", "ERROR"),
    new Event2("Event18", "ERROR"),
    new Event2("Event19", "INFO"),
    new Event2("Event20", "INFO"),
    new Event2("Event21", "WARNING"),
    new Event2("Event22", "INFO"),
    new Event2("Event23", "INFO"),
    new Event2("Event24", "WARNING"),
    new Event2("Event25", "INFO"),
    new Event2("Event26", "INFO")
  };

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(250);
      ArrayDeque<Event2> events = new ArrayDeque<>();
      for (Event2 e : EVENTS) events.addLast(e);
      App app = new App(new StatefulList(ListWidget.State.empty(), ITEMS), events);
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
          tui.crossterm.KeyCode code = key.keyEvent().code();
          if (code instanceof KeyCode.Char c && c.c() == 'q') {
            return;
          } else if (code instanceof KeyCode.Left
              || (code instanceof KeyCode.Char l && l.c() == 'h')) {
            app.items.unselect();
          } else if (code instanceof KeyCode.Down
              || (code instanceof KeyCode.Char d && d.c() == 'j')) {
            app.items.next();
          } else if (code instanceof KeyCode.Up
              || (code instanceof KeyCode.Char u && u.c() == 'k')) {
            app.items.previous();
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
            Direction.Horizontal,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)}, true);
    Rect[] chunks = layout.split(f.size);

    ListWidget.Item[] items0 = new ListWidget.Item[app.items.items.length];
    for (int i = 0; i < items0.length; i++) {
      LabeledCount lc = app.items.items[i];
      List<Spans> lines = new ArrayList<>();
      lines.add(Spans.nostyle(lc.label()));
      for (int k = 0; k < lc.count(); k++) {
        lines.add(
            Spans.from(
                Span.styled(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                    Style.empty().withAddModifier(Modifier.ITALIC))));
      }
      items0[i] =
          new ListWidget.Item(
              new Text(lines.toArray(new Spans[0])),
              Style.empty().withFg(Color.Black).withBg(Color.White));
    }

    ListWidget itemsWidget =
        ListWidget.empty(items0)
            .withBlock(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle("List")))
            .withHighlightStyle(
                Style.empty().withBg(Color.LightGreen).withAddModifier(Modifier.BOLD))
            .withHighlightSymbol(">> ");

    f.renderStatefulWidget(itemsWidget, chunks[0], app.items.state);

    Event2[] eventsArr = app.events.toArray(new Event2[0]);
    ListWidget.Item[] eventItems = new ListWidget.Item[eventsArr.length];
    for (int i = 0; i < eventsArr.length; i++) {
      Event2 ev = eventsArr[eventsArr.length - 1 - i];
      Style s;
      switch (ev.level()) {
        case "CRITICAL" -> s = Style.empty().withFg(Color.Red);
        case "ERROR" -> s = Style.empty().withFg(Color.Magenta);
        case "WARNING" -> s = Style.empty().withFg(Color.Yellow);
        case "INFO" -> s = Style.empty().withFg(Color.Blue);
        default -> s = Style.DEFAULT;
      }
      Spans header =
          Spans.from(
              Span.styled(padRight(ev.level(), 9, ' '), s),
              Span.nostyle(" "),
              Span.styled(
                  "2020-01-01 10:00:00", Style.empty().withAddModifier(Modifier.ITALIC)));
      Spans log = Spans.nostyle(ev.name());
      eventItems[i] =
          new ListWidget.Item(
              Text.fromMany(
                  Spans.nostyle("-".repeat(chunks[1].width())), header, Spans.nostyle(""), log),
              Style.DEFAULT);
    }

    ListWidget eventsList =
        ListWidget.empty(eventItems)
            .withBlock(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle("List")))
            .withStartCorner(Corner.BottomLeft);

    f.renderWidget(eventsList, chunks[1]);
  }

  private static String padRight(String s, int width, char ch) {
    if (s.length() >= width) return s;
    StringBuilder sb = new StringBuilder(s);
    while (sb.length() < width) sb.append(ch);
    return sb.toString();
  }
}
