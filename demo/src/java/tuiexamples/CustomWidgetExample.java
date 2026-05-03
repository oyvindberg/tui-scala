package tuiexamples;

import tui.Buffer;
import tui.Frame;
import tui.Rect;
import tui.Style;
import tui.Terminal;
import tui.Widget;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;

public final class CustomWidgetExample {
  private CustomWidgetExample() {}

  public static final class Label implements Widget {
    public final String text;

    public Label(String text) {
      this.text = text;
    }

    @Override
    public void render(Rect area, Buffer buf) {
      buf.setString(area.left(), area.top(), text, Style.DEFAULT);
    }
  }

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
    Rect size = f.size;
    Label label = new Label("Test");
    f.renderWidget(label, size);
  }
}
