package tuiexamples;

import tui.Alignment;
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
import tui.Text;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.ClearWidget;
import tui.widgets.ParagraphWidget;

public final class PopupExample {
  private PopupExample() {}

  public static final class App {
    public boolean showPopup;

    public App(boolean showPopup) {
      this.showPopup = showPopup;
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      App app = new App(false);
      runApp(terminal, app, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, App app, CrosstermJni jni) {
    while (true) {
      terminal.draw(f -> ui(f, app));

      Event ev = jni.read();
      if (ev instanceof Event.Key key) {
        if (key.keyEvent().code() instanceof KeyCode.Char c) {
          if (c.c() == 'q') {
            return;
          } else if (c.c() == 'p') {
            app.showPopup = !app.showPopup;
          }
        }
      }
    }
  }

  public static void ui(Frame f, App app) {
    Rect size = f.size;

    Layout layout =
        new Layout(
            Direction.Vertical,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Percentage(20), new Constraint.Percentage(80)}, true);
    Rect[] chunks = layout.split(size);

    String text = app.showPopup ? "Press p to close the popup" : "Press p to show the popup";
    ParagraphWidget paragraph =
        ParagraphWidget.empty(
                Text.from(
                    Span.styled(text, Style.empty().withAddModifier(Modifier.SLOW_BLINK))))
            .withAlignment(Alignment.Center)
            .withWrap(new ParagraphWidget.Wrap(true));
    f.renderWidget(paragraph, chunks[0]);

    BlockWidget block =
        BlockWidget.empty()
            .withTitle(Spans.nostyle("Content"))
            .withBorders(Borders.ALL)
            .withStyle(Style.DEFAULT.withBg(Color.Blue));
    f.renderWidget(block, chunks[1]);

    if (app.showPopup) {
      BlockWidget popup =
          BlockWidget.empty()
              .withTitle(Spans.nostyle("Popup"))
              .withBorders(Borders.ALL);
      Rect area = centeredRect(60, 20, size);
      f.renderWidget(ClearWidget.INSTANCE, area);
      f.renderWidget(popup, area);
    }
  }

  public static Rect centeredRect(int percentX, int percentY, Rect r) {
    Layout popupLayout =
        new Layout(
            Direction.Vertical,
            new Margin(0, 0),
            new Constraint[] {
              new Constraint.Percentage((100 - percentY) / 2),
              new Constraint.Percentage(percentY),
              new Constraint.Percentage((100 - percentY) / 2)
            },
            false);
    Rect[] popupChunks = popupLayout.split(r);

    Layout horizontal =
        new Layout(
            Direction.Horizontal,
            new Margin(0, 0),
            new Constraint[] {
              new Constraint.Percentage((100 - percentX) / 2),
              new Constraint.Percentage(percentX),
              new Constraint.Percentage((100 - percentX) / 2)
            },
            false);
    return horizontal.split(popupChunks[1])[1];
  }
}
