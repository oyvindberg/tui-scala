/// A simple example demonstrating how to handle user input.
package tuiexamples;

import tui.Borders;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Grapheme;
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
import tui.widgets.ParagraphWidget;

public final class UserInputExample {
  private UserInputExample() {}

  public enum InputMode {
    Normal,
    Editing
  }

  public static final class App {
    public String input;
    public InputMode inputMode;
    public String[] messages;

    public App(String input, InputMode inputMode, String[] messages) {
      this.input = input;
      this.inputMode = inputMode;
      this.messages = messages;
    }

    public static App empty() {
      return new App("", InputMode.Normal, new String[0]);
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      App app = App.empty();
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
        switch (app.inputMode) {
          case Normal -> {
            if (code instanceof KeyCode.Char c) {
              if (c.c() == 'e') {
                app.inputMode = InputMode.Editing;
              } else if (c.c() == 'q') {
                return;
              }
            }
          }
          case Editing -> {
            if (code instanceof KeyCode.Enter) {
              String[] next = new String[app.messages.length + 1];
              System.arraycopy(app.messages, 0, next, 0, app.messages.length);
              next[app.messages.length] = app.input;
              app.messages = next;
              app.input = "";
            } else if (code instanceof KeyCode.Char c) {
              app.input = app.input + c.c();
            } else if (code instanceof KeyCode.Backspace) {
              app.input = app.input.substring(0, app.input.length() - 1);
            } else if (code instanceof KeyCode.Esc) {
              app.inputMode = InputMode.Normal;
            }
          }
        }
      }
    }
  }

  public static void ui(Frame f, App app) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(2),
            new Constraint[] {
              new Constraint.Length(1),
              new Constraint.Length(3),
              new Constraint.Min(5)
            }, true);
    Rect[] chunks = layout.split(f.size);

    Text msg;
    Style style;
    switch (app.inputMode) {
      case Normal -> {
        msg =
            Text.fromSpans(
                Span.nostyle("Press "),
                Span.styled("q", Style.DEFAULT.withAddModifier(Modifier.BOLD)),
                Span.nostyle(" to exit, "),
                Span.styled("e", Style.DEFAULT.withAddModifier(Modifier.BOLD)),
                Span.nostyle(" to start editing."));
        style = Style.DEFAULT.withAddModifier(Modifier.RAPID_BLINK);
      }
      case Editing -> {
        msg =
            Text.fromSpans(
                Span.nostyle("Press "),
                Span.styled("Esc", Style.DEFAULT.withAddModifier(Modifier.BOLD)),
                Span.nostyle(" to stop editing, "),
                Span.styled("Enter", Style.DEFAULT.withAddModifier(Modifier.BOLD)),
                Span.nostyle(" to record the message"));
        style = Style.DEFAULT;
      }
      default -> throw new RuntimeException("unreachable");
    }
    Text text = msg.overwrittenStyle(style);

    ParagraphWidget helpMessage = ParagraphWidget.empty(text);
    f.renderWidget(helpMessage, chunks[0]);

    Style inputStyle =
        switch (app.inputMode) {
          case Normal -> Style.DEFAULT;
          case Editing -> Style.DEFAULT.withFg(Color.Yellow);
        };
    ParagraphWidget input =
        ParagraphWidget.empty(Text.nostyle(app.input))
            .withBlock(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle("Input")))
            .withStyle(inputStyle);
    f.renderWidget(input, chunks[1]);

    switch (app.inputMode) {
      case Normal -> {
        // Hide the cursor. `Frame` does this by default, so we don't need to do anything here
      }
      case Editing -> f.setCursor(
          chunks[1].x() + new Grapheme(app.input).width() + 1, chunks[1].y() + 1);
    }

    ListWidget.Item[] items = new ListWidget.Item[app.messages.length];
    for (int i = 0; i < app.messages.length; i++) {
      items[i] = new ListWidget.Item(Text.nostyle(i + ": " + app.messages[i]), Style.DEFAULT);
    }

    ListWidget messages =
        ListWidget.empty(items)
            .withBlock(
                BlockWidget.empty()
                    .withBorders(Borders.ALL)
                    .withTitle(Spans.nostyle("Messages")));
    f.renderWidget(messages, chunks[2]);
  }
}
