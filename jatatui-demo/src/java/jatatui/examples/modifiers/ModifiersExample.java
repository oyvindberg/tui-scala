package jatatui.examples.modifiers;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to use modifiers.
///
/// Mirrors `examples/apps/modifiers/src/main.rs`. It renders a grid of combinations of foreground
/// and background colors with all modifiers applied to them. Press any key to exit.
public final class ModifiersExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  /// The list of single-flag modifiers, in upstream display order. `Modifier.empty()` is
  /// prepended at render time.
  private static final List<Modifier> SINGLE_MODIFIERS =
      List.of(
          Modifier.BOLD,
          Modifier.DIM,
          Modifier.ITALIC,
          Modifier.UNDERLINED,
          Modifier.SLOW_BLINK,
          Modifier.RAPID_BLINK,
          Modifier.REVERSED,
          Modifier.HIDDEN,
          Modifier.CROSSED_OUT);

  private static final List<Color> COLORS =
      List.of(Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.WHITE, Color.RED);

  private ModifiersExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> run(terminal));
  }

  private static void run(Terminal<CrosstermBackend> terminal) throws IOException {
    while (true) {
      terminal.draw(ModifiersExample::render);
      Event event = JNI.read();
      if (event instanceof Event.Key key
          && key.keyEvent().kind() == KeyEventKind.Press) {
        return;
      }
    }
  }

  private static void render(Frame frame) {
    Layout outer = Layout.vertical(new Constraint.Length(1), new Constraint.Min(0));
    Rect[] split = frame.area().layout(outer, 2);
    Rect textArea = split[0];
    Rect mainArea = split[1];

    frame.renderWidget(
        Paragraph.of("Note: not all terminals support all modifiers")
            .withStyle(Style.empty().withFg(Color.RED).withAddModifier(Modifier.BOLD)),
        textArea);

    // Build the grid: 50 rows × 5 columns, mirroring upstream.
    Constraint[] rowConstraints = new Constraint[50];
    for (int i = 0; i < 50; i++) {
      rowConstraints[i] = new Constraint.Length(1);
    }
    Rect[] rows = mainArea.layout(Layout.vertical(rowConstraints));

    Constraint[] colConstraints = new Constraint[5];
    for (int i = 0; i < 5; i++) {
      colConstraints[i] = new Constraint.Percentage(20);
    }

    List<Rect> cells = new ArrayList<>(rows.length * 5);
    for (Rect row : rows) {
      Rect[] cols = row.layout(Layout.horizontal(colConstraints));
      for (Rect c : cols) {
        cells.add(c);
      }
    }

    // The combinations: empty modifier prepended, then each single modifier.
    List<Modifier> allModifiers = new ArrayList<>(SINGLE_MODIFIERS.size() + 1);
    allModifiers.add(Modifier.EMPTY);
    allModifiers.addAll(SINGLE_MODIFIERS);

    int index = 0;
    for (Color bg : COLORS) {
      for (Color fg : COLORS) {
        for (Modifier modifier : allModifiers) {
          if (index >= cells.size()) {
            return;
          }
          String modifierName = formatModifierName(modifier);
          String padding = " ".repeat(Math.max(0, 12 - modifierName.length()));
          Paragraph paragraph =
              Paragraph.of(
                  Line.from(
                      Span.styled(
                          modifierName,
                          Style.empty().withFg(fg).withBg(bg).withAddModifier(modifier)),
                      Span.styled(
                          padding,
                          Style.empty().withFg(fg).withBg(bg).withAddModifier(modifier)),
                      // This is a hack to work around a bug in VHS which is used for rendering the
                      // examples to gifs. The bug is that the background color of a paragraph
                      // seems to bleed into the next character.
                      Span.styled(".", Style.empty().withFg(Color.BLACK).withBg(Color.BLACK))));
          frame.renderWidget(paragraph, cells.get(index));
          index += 1;
        }
      }
    }
  }

  /// Formats the modifier name to mimic the upstream `format!("{modifier:11?}")` output: pad with
  /// spaces on the right to width 11.
  private static String formatModifierName(Modifier modifier) {
    String s = modifier.isEmpty() ? "NONE" : modifier.toString();
    if (s.length() < 11) {
      return s + " ".repeat(11 - s.length());
    }
    return s;
  }
}
