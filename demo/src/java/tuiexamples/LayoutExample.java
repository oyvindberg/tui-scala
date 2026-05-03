package tuiexamples;

import tui.Borders;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Rect;
import tui.Spans;
import tui.Terminal;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;

public final class LayoutExample {
  private LayoutExample() {}

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      runApp(terminal, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, CrosstermJni jni) {
    while (true) {
      terminal.draw(f -> ui(f));
      Event ev = jni.read();
      if (ev instanceof Event.Key key) {
        if (key.keyEvent().code() instanceof KeyCode.Char c && c.c() == 'q') {
          return;
        }
      }
    }
  }

  public static void ui(Frame f) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            new Margin(0, 0),
            new Constraint[] {
              new Constraint.Percentage(10),
              new Constraint.Percentage(80),
              new Constraint.Percentage(10)
            }, true);
    Rect[] chunks = layout.split(f.size);

    BlockWidget block0 =
        BlockWidget.empty().withTitle(Spans.nostyle("Block")).withBorders(Borders.ALL);
    f.renderWidget(block0, chunks[0]);
    BlockWidget block1 =
        BlockWidget.empty().withTitle(Spans.nostyle("Block 2")).withBorders(Borders.ALL);
    f.renderWidget(block1, chunks[2]);
  }
}
