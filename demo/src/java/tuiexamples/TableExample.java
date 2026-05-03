package tuiexamples;

import java.util.Optional;
import tui.Borders;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Modifier;
import tui.Rect;
import tui.Spans;
import tui.Style;
import tui.Terminal;
import tui.Text;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.TableWidget;

public final class TableExample {
  private TableExample() {}

  public static final String[][] ITEMS = {
    {"Row11", "Row12", "Row13"},
    {"Row21", "Row22", "Row23"},
    {"Row31", "Row32", "Row33"},
    {"Row41", "Row42", "Row43"},
    {"Row51", "Row52", "Row53"},
    {"Row61", "Row62\nTest", "Row63"},
    {"Row71", "Row72", "Row73"},
    {"Row81", "Row82", "Row83"},
    {"Row91", "Row92", "Row93"},
    {"Row101", "Row102", "Row103"},
    {"Row111", "Row112", "Row113"},
    {"Row121", "Row122", "Row123"},
    {"Row131", "Row132", "Row133"},
    {"Row141", "Row142", "Row143"},
    {"Row151", "Row152", "Row153"},
    {"Row161", "Row162", "Row163"},
    {"Row171", "Row172", "Row173"},
    {"Row181", "Row182", "Row183"},
    {"Row191", "Row192", "Row193"}
  };

  public static final class App {
    public final TableWidget.State state;
    public final String[][] items;

    public App(TableWidget.State state, String[][] items) {
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
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      App app = new App(TableWidget.State.empty(), ITEMS);
      runApp(terminal, app, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, App app, CrosstermJni jni) {
    while (true) {
      terminal.draw(f -> ui(f, app));

      Event ev = jni.read();
      if (ev instanceof Event.Key key) {
        tui.crossterm.KeyCode code = key.keyEvent().code();
        if (code instanceof KeyCode.Char c && c.c() == 'q') {
          return;
        } else if (code instanceof KeyCode.Down
            || (code instanceof KeyCode.Char d && d.c() == 'j')) {
          app.next();
        } else if (code instanceof KeyCode.Up
            || (code instanceof KeyCode.Char u && u.c() == 'k')) {
          app.previous();
        }
      }
    }
  }

  public static void ui(Frame f, App app) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(5),
            new Constraint[] {new Constraint.Percentage(100)}, true);
    Rect[] rects = layout.split(f.size);

    Style selectedStyle = Style.empty().withAddModifier(Modifier.REVERSED);
    Style normalStyle = Style.empty().withBg(Color.Blue);
    String[] headers = {"Header1", "Header2", "Header3"};
    TableWidget.Cell[] headerCells = new TableWidget.Cell[headers.length];
    for (int i = 0; i < headers.length; i++) {
      headerCells[i] =
          new TableWidget.Cell(
              Text.nostyle(headers[i]), Style.empty().withFg(Color.Red));
    }
    TableWidget.Row header = new TableWidget.Row(headerCells, 1, normalStyle, 1);

    TableWidget.Row[] rows = new TableWidget.Row[app.items.length];
    for (int i = 0; i < app.items.length; i++) {
      String[] item = app.items[i];
      int height = 0;
      for (String c : item) {
        int newlines = 0;
        for (int k = 0; k < c.length(); k++) if (c.charAt(k) == '\n') newlines++;
        if (newlines > height) height = newlines;
      }
      height += 1;
      TableWidget.Cell[] cells = new TableWidget.Cell[item.length];
      for (int j = 0; j < item.length; j++) {
        cells[j] = new TableWidget.Cell(Text.nostyle(item[j]), Style.DEFAULT);
      }
      rows[i] = new TableWidget.Row(cells, height, Style.DEFAULT, 1);
    }

    TableWidget t =
        TableWidget.empty(rows)
            .withBlock(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle("Table")))
            .withWidths(
                new Constraint[] {
                  new Constraint.Percentage(50),
                  new Constraint.Length(30),
                  new Constraint.Min(10)
                })
            .withHighlightStyle(selectedStyle)
            .withHighlightSymbol(">> ")
            .withHeader(header);

    f.renderStatefulWidget(t, rects[0], app.state);
  }
}
