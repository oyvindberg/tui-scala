package jatatui.examples.customwidget;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import tui.crossterm.Command;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;
import tui.crossterm.MouseButton;
import tui.crossterm.MouseEvent;
import tui.crossterm.MouseEventKind;

/// A jatatui example that demonstrates how to create a custom widget that can be interacted with
/// using the mouse.
///
/// Java port of `examples/apps/custom-widget/src/main.rs` from ratatui v0.30.
///
/// Three button widgets — Red, Green, Blue — can be navigated with the arrow keys (h/l) and
/// activated with Space. Mouse motion updates the selected button; left-clicking activates it.
/// Press 'q' to quit.
public final class CustomWidgetExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private CustomWidgetExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> {
      JNI.execute(new Command.EnableMouseCapture());
      try {
        run(terminal);
      } finally {
        try {
          JNI.execute(new Command.DisableMouseCapture());
        } catch (RuntimeException e) {
          System.err.println("Error disabling mouse capture: " + e.getMessage());
        }
      }
    });
  }

  /// Possible visual states of a [Button] widget.
  enum State {
    Normal,
    Selected,
    Active
  }

  /// A color theme for a [Button] widget.
  record Theme(Color text, Color background, Color highlight, Color shadow) {}

  static final Theme BLUE =
      new Theme(
          new Color.Rgb(16, 24, 48),
          new Color.Rgb(48, 72, 144),
          new Color.Rgb(64, 96, 192),
          new Color.Rgb(32, 48, 96));

  static final Theme RED =
      new Theme(
          new Color.Rgb(48, 16, 16),
          new Color.Rgb(144, 48, 48),
          new Color.Rgb(192, 64, 64),
          new Color.Rgb(96, 32, 32));

  static final Theme GREEN =
      new Theme(
          new Color.Rgb(16, 48, 16),
          new Color.Rgb(48, 144, 48),
          new Color.Rgb(64, 192, 64),
          new Color.Rgb(32, 96, 32));

  /// Pair of color tuples returned by [Button#colors()] — `(background, text, shadow, highlight)`.
  record ButtonColors(Color background, Color text, Color shadow, Color highlight) {}

  /// A custom widget that renders a button with a label, theme and state.
  static final class Button implements Widget {
    private final Line label;
    private final Theme theme;
    private final State state;

    Button(Line label, Theme theme, State state) {
      this.label = label;
      this.theme = theme;
      this.state = state;
    }

    /// Creates a new button with the given label, the [#BLUE] theme, and [State#Normal] state.
    static Button of(String label) {
      return new Button(Line.from(label), BLUE, State.Normal);
    }

    /// Returns a copy of this button with the given theme.
    Button withTheme(Theme theme) {
      return new Button(label, theme, state);
    }

    /// Returns a copy of this button with the given state.
    Button withState(State state) {
      return new Button(label, theme, state);
    }

    /// Returns the colors used to render this button at its current state.
    ButtonColors colors() {
      return switch (state) {
        case Normal -> new ButtonColors(theme.background(), theme.text(), theme.shadow(), theme.highlight());
        case Selected -> new ButtonColors(theme.highlight(), theme.text(), theme.shadow(), theme.highlight());
        case Active -> new ButtonColors(theme.background(), theme.text(), theme.highlight(), theme.shadow());
      };
    }

    @Override
    public void render(Rect area, Buffer buf) {
      ButtonColors c = colors();
      buf.setStyle(area, Style.empty().withBg(c.background()).withFg(c.text()));

      // Top line if there's enough space.
      if (area.height() > 2) {
        buf.setString(
            area.x(),
            area.y(),
            "▔".repeat(area.width()),
            Style.empty().withFg(c.highlight()).withBg(c.background()));
      }
      // Bottom line if there's enough space.
      if (area.height() > 1) {
        buf.setString(
            area.x(),
            area.y() + area.height() - 1,
            "▁".repeat(area.width()),
            Style.empty().withFg(c.shadow()).withBg(c.background()));
      }
      // Centered label.
      int labelWidth = label.width();
      int xOffset = saturatingSub(area.width(), labelWidth) / 2;
      int yOffset = saturatingSub(area.height(), 1) / 2;
      buf.setLine(area.x() + xOffset, area.y() + yOffset, label, area.width());
    }

    private static int saturatingSub(int a, int b) {
      return Math.max(0, a - b);
    }
  }

  private static void run(Terminal<CrosstermBackend> terminal) throws IOException {
    int[] selectedButton = {0};
    State[] buttonStates = {State.Selected, State.Normal, State.Normal};
    while (true) {
      State[] snapshot = buttonStates.clone();
      terminal.draw(frame -> render(frame, snapshot));
      if (!JNI.poll(new Duration(0, 100_000_000))) {
        continue;
      }
      Event event = JNI.read();
      switch (event) {
        case Event.Key key -> {
          if (handleKeyEvent(key.keyEvent(), buttonStates, selectedButton)) {
            return;
          }
        }
        case Event.Mouse mouse -> handleMouseEvent(mouse.mouseEvent(), buttonStates, selectedButton);
        default -> {}
      }
    }
  }

  private static void render(Frame frame, State[] states) {
    Rect[] split =
        frame.area().layout(
            Layout.vertical(
                new Constraint.Length(1),
                new Constraint.Max(3),
                new Constraint.Length(1),
                new Constraint.Min(0)),
            4);
    Rect title = split[0];
    Rect buttons = split[1];
    Rect help = split[2];

    frame.renderWidget(Paragraph.of("Custom Widget Example (mouse enabled)"), title);
    renderButtons(frame, buttons, states);
    frame.renderWidget(Paragraph.of("←/→: select, Space: toggle, q: quit"), help);
  }

  private static void renderButtons(Frame frame, Rect area, State[] states) {
    Rect[] cols =
        area.layout(
            Layout.horizontal(
                    new Constraint.Length(15),
                    new Constraint.Length(15),
                    new Constraint.Length(15))
                .withFlex(Flex.Start),
            3);

    frame.renderWidget(Button.of("Red").withTheme(RED).withState(states[0]), cols[0]);
    frame.renderWidget(Button.of("Green").withTheme(GREEN).withState(states[1]), cols[1]);
    frame.renderWidget(Button.of("Blue").withTheme(BLUE).withState(states[2]), cols[2]);
  }

  /// Handle a single key event. Returns `true` if the application should quit.
  private static boolean handleKeyEvent(KeyEvent key, State[] buttonStates, int[] selectedButton) {
    if (key.kind() != KeyEventKind.Press) {
      return false;
    }
    KeyCode code = key.code();
    if (code instanceof KeyCode.Char ch && ch.c() == 'q') {
      return true;
    }
    if (code instanceof KeyCode.Left || (code instanceof KeyCode.Char ch && ch.c() == 'h')) {
      buttonStates[selectedButton[0]] = State.Normal;
      selectedButton[0] = Math.max(0, selectedButton[0] - 1);
      buttonStates[selectedButton[0]] = State.Selected;
    } else if (code instanceof KeyCode.Right || (code instanceof KeyCode.Char ch2 && ch2.c() == 'l')) {
      buttonStates[selectedButton[0]] = State.Normal;
      selectedButton[0] = Math.min(2, selectedButton[0] + 1);
      buttonStates[selectedButton[0]] = State.Selected;
    } else if (code instanceof KeyCode.Char ch3 && ch3.c() == ' ') {
      if (buttonStates[selectedButton[0]] == State.Active) {
        buttonStates[selectedButton[0]] = State.Normal;
      } else {
        buttonStates[selectedButton[0]] = State.Active;
      }
    }
    return false;
  }

  private static void handleMouseEvent(MouseEvent mouse, State[] buttonStates, int[] selectedButton) {
    MouseEventKind kind = mouse.kind();
    if (kind instanceof MouseEventKind.Moved) {
      int oldSelected = selectedButton[0];
      int column = mouse.column();
      int newSelected;
      if (column < 15) {
        newSelected = 0;
      } else if (column < 30) {
        newSelected = 1;
      } else {
        newSelected = 2;
      }
      selectedButton[0] = newSelected;
      if (oldSelected != newSelected) {
        if (buttonStates[oldSelected] != State.Active) {
          buttonStates[oldSelected] = State.Normal;
        }
        if (buttonStates[newSelected] != State.Active) {
          buttonStates[newSelected] = State.Selected;
        }
      }
    } else if (kind instanceof MouseEventKind.Down down && down.mouseButton() == MouseButton.Left) {
      if (buttonStates[selectedButton[0]] == State.Active) {
        buttonStates[selectedButton[0]] = State.Normal;
      } else {
        buttonStates[selectedButton[0]] = State.Active;
      }
    }
  }

}
