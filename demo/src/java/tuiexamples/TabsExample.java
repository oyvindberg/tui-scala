package tuiexamples;

import tui.Borders;
import tui.Color;
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
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.tabs.TabsWidget;

public final class TabsExample {
  private TabsExample() {}

  public static final class App {
    public final String[] titles;
    public int index;

    public App(String[] titles, int index) {
      this.titles = titles;
      this.index = index;
    }

    public void next() {
      index = (index + 1) % titles.length;
    }

    public void previous() {
      if (index > 0) {
        index -= 1;
      } else {
        index = titles.length - 1;
      }
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      App app = new App(new String[] {"Tab0", "Tab1", "Tab2", "Tab3"}, 0);
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
        } else if (code instanceof KeyCode.Right) {
          app.next();
        } else if (code instanceof KeyCode.Left) {
          app.previous();
        }
      }
    }
  }

  public static void ui(Frame f, App app) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            new Margin(5, 5),
            new Constraint[] {new Constraint.Length(3), new Constraint.Min(0)}, true);
    Rect[] chunks = layout.split(f.size);

    BlockWidget block =
        BlockWidget.empty().withStyle(Style.empty().withBg(Color.White).withFg(Color.Black));
    f.renderWidget(block, f.size);

    Spans[] titles = new Spans[app.titles.length];
    for (int i = 0; i < app.titles.length; i++) {
      String t = app.titles[i];
      String first = t.substring(0, 1);
      String rest = t.substring(1);
      titles[i] =
          Spans.from(
              Span.styled(first, Style.empty().withFg(Color.Yellow)),
              Span.styled(rest, Style.empty().withFg(Color.Green)));
    }

    TabsWidget tabs =
        TabsWidget.empty(titles)
            .withBlock(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle("Tabs")))
            .withSelected(app.index)
            .withStyle(Style.empty().withFg(Color.Cyan))
            .withHighlightStyle(
                Style.empty().withAddModifier(Modifier.BOLD).withBg(Color.Black));
    f.renderWidget(tabs, chunks[0]);

    BlockWidget inner =
        switch (app.index) {
          case 0 ->
              BlockWidget.empty()
                  .withTitle(Spans.nostyle("Inner 0"))
                  .withBorders(Borders.ALL);
          case 1 ->
              BlockWidget.empty()
                  .withTitle(Spans.nostyle("Inner 1"))
                  .withBorders(Borders.ALL);
          case 2 ->
              BlockWidget.empty()
                  .withTitle(Spans.nostyle("Inner 2"))
                  .withBorders(Borders.ALL);
          case 3 ->
              BlockWidget.empty()
                  .withTitle(Spans.nostyle("Inner 3"))
                  .withBorders(Borders.ALL);
          default -> throw new RuntimeException("unreachable");
        };
    f.renderWidget(inner, chunks[1]);
  }
}
