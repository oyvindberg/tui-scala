package tuiexamples;

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
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.ParagraphWidget;

/// Renders a grid of foreground/background colors with all modifiers applied — useful for
/// testing how a terminal emulator handles different modifiers.
public final class ModifiersExample {
  private ModifiersExample() {}

  private static final Color[] COLORS = {
    Color.Black, Color.DarkGray, Color.Gray, Color.White, Color.Red
  };

  private static final ModifierEntry[] MODIFIERS = {
    new ModifierEntry(Modifier.EMPTY, "(empty)"),
    new ModifierEntry(Modifier.BOLD, "BOLD"),
    new ModifierEntry(Modifier.DIM, "DIM"),
    new ModifierEntry(Modifier.ITALIC, "ITALIC"),
    new ModifierEntry(Modifier.UNDERLINED, "UNDERLINED"),
    new ModifierEntry(Modifier.SLOW_BLINK, "SLOW_BLINK"),
    new ModifierEntry(Modifier.RAPID_BLINK, "RAPID_BLINK"),
    new ModifierEntry(Modifier.REVERSED, "REVERSED"),
    new ModifierEntry(Modifier.HIDDEN, "HIDDEN"),
    new ModifierEntry(Modifier.CROSSED_OUT, "CROSSED_OUT")
  };

  private record ModifierEntry(Modifier modifier, String name) {}

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      runApp(terminal, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, CrosstermJni jni) {
    while (true) {
      terminal.draw(ModifiersExample::ui);
      Duration timeout = new Duration(0, 250_000_000);
      if (jni.poll(timeout)) {
        Event ev = jni.read();
        if (ev instanceof Event.Key key
            && key.keyEvent().code() instanceof KeyCode.Char c
            && c.c() == 'q') {
          return;
        }
      }
    }
  }

  public static void ui(Frame frame) {
    Layout outer =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {new Constraint.Length(1), new Constraint.Min(0)},
            true);
    Rect[] outerChunks = outer.split(frame.size);

    ParagraphWidget header =
        ParagraphWidget.empty(Text.nostyle("Note: not all terminals support all modifiers"))
            .withStyle(Style.empty().withFg(Color.Red).withAddModifier(Modifier.BOLD));
    frame.renderWidget(header, outerChunks[0]);

    int rows = COLORS.length * COLORS.length * MODIFIERS.length;
    Constraint[] rowConstraints = new Constraint[rows];
    for (int i = 0; i < rows; i++) rowConstraints[i] = new Constraint.Length(1);
    Layout rowsLayout =
        new Layout(Direction.Vertical, Margin.of(0), rowConstraints, true);
    Rect[] rowAreas = rowsLayout.split(outerChunks[1]);

    int idx = 0;
    for (Color bg : COLORS) {
      for (Color fg : COLORS) {
        for (ModifierEntry entry : MODIFIERS) {
          if (idx >= rowAreas.length) break;
          String label = padRight(entry.name(), 12);
          Style style =
              Style.empty().withFg(fg).withBg(bg).withAddModifier(entry.modifier());
          ParagraphWidget paragraph =
              ParagraphWidget.empty(Text.from(Spans.from(Span.styled(label, style))));
          frame.renderWidget(paragraph, rowAreas[idx]);
          idx++;
        }
      }
    }
  }

  private static String padRight(String s, int width) {
    if (s.length() >= width) return s;
    StringBuilder sb = new StringBuilder(s);
    while (sb.length() < width) sb.append(' ');
    return sb.toString();
  }
}
