package tuiexamples;

import tui.Buffer;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Rect;
import tui.Spans;
import tui.Style;
import tui.Terminal;
import tui.Text;
import tui.Widget;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.ParagraphWidget;

public final class CustomWidgetExample {
  private CustomWidgetExample() {}

  public enum State {
    Normal,
    Selected,
    Active
  }

  public record Theme(Color text, Color background, Color highlight, Color shadow) {}

  public static final Theme BLUE =
      new Theme(
          new Color.Rgb(16, 24, 48),
          new Color.Rgb(48, 72, 144),
          new Color.Rgb(64, 96, 192),
          new Color.Rgb(32, 48, 96));

  public static final Theme RED =
      new Theme(
          new Color.Rgb(48, 16, 16),
          new Color.Rgb(144, 48, 48),
          new Color.Rgb(192, 64, 64),
          new Color.Rgb(96, 32, 32));

  public static final Theme GREEN =
      new Theme(
          new Color.Rgb(16, 48, 16),
          new Color.Rgb(48, 144, 48),
          new Color.Rgb(64, 192, 64),
          new Color.Rgb(32, 96, 32));

  /// A custom widget that renders a button with a label, theme and state.
  public static final class Button implements Widget {
    public final Spans label;
    public final Theme theme;
    public final State state;

    public Button(Spans label, Theme theme, State state) {
      this.label = label;
      this.theme = theme;
      this.state = state;
    }

    public static Button create(String label) {
      return new Button(Spans.nostyle(label), BLUE, State.Normal);
    }

    public Button withTheme(Theme t) {
      return new Button(label, t, state);
    }

    public Button withState(State s) {
      return new Button(label, theme, s);
    }

    private Color[] colors() {
      return switch (state) {
        case Normal ->
            new Color[] {theme.background(), theme.text(), theme.shadow(), theme.highlight()};
        case Selected ->
            new Color[] {theme.highlight(), theme.text(), theme.shadow(), theme.highlight()};
        case Active ->
            new Color[] {theme.background(), theme.text(), theme.highlight(), theme.shadow()};
      };
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Color[] cs = colors();
      Color background = cs[0];
      Color text = cs[1];
      Color shadow = cs[2];
      Color highlight = cs[3];

      buf.setStyle(area, Style.empty().withBg(background).withFg(text));

      // top highlight line
      if (area.height() > 2) {
        buf.setString(
            area.x(),
            area.y(),
            "▔".repeat(area.width()),
            Style.empty().withFg(highlight).withBg(background));
      }
      // bottom shadow line
      if (area.height() > 1) {
        buf.setString(
            area.x(),
            area.y() + area.height() - 1,
            "▁".repeat(area.width()),
            Style.empty().withFg(shadow).withBg(background));
      }
      // label centered
      int labelWidth = label.width();
      int dx = Math.max(0, area.width() - labelWidth) / 2;
      int dy = Math.max(0, area.height() - 1) / 2;
      buf.setSpans(area.x() + dx, area.y() + dy, label, area.width());
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      runApp(terminal, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, CrosstermJni jni) {
    int[] selectedButton = {0};
    State[] buttonStates = {State.Selected, State.Normal, State.Normal};
    while (true) {
      terminal.draw(f -> ui(f, buttonStates));
      tui.crossterm.Duration timeout = new tui.crossterm.Duration(0, 100_000_000);
      if (!jni.poll(timeout)) {
        continue;
      }
      Event ev = jni.read();
      if (ev instanceof Event.Key key) {
        tui.crossterm.KeyCode code = key.keyEvent().code();
        if (code instanceof KeyCode.Char c) {
          if (c.c() == 'q') {
            return;
          } else if (c.c() == ' ') {
            buttonStates[selectedButton[0]] =
                buttonStates[selectedButton[0]] == State.Active ? State.Normal : State.Active;
          }
        } else if (code instanceof KeyCode.Left) {
          buttonStates[selectedButton[0]] = State.Normal;
          selectedButton[0] = Math.max(0, selectedButton[0] - 1);
          buttonStates[selectedButton[0]] = State.Selected;
        } else if (code instanceof KeyCode.Right) {
          buttonStates[selectedButton[0]] = State.Normal;
          selectedButton[0] = Math.min(2, selectedButton[0] + 1);
          buttonStates[selectedButton[0]] = State.Selected;
        }
      }
    }
  }

  public static void ui(Frame f, State[] states) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {
              new Constraint.Length(1),
              new Constraint.Max(3),
              new Constraint.Length(1),
              new Constraint.Min(0)
            },
            true);
    Rect[] chunks = layout.split(f.size);

    f.renderWidget(
        ParagraphWidget.empty(Text.nostyle("Custom Widget Example")), chunks[0]);
    renderButtons(f, chunks[1], states);
    f.renderWidget(
        ParagraphWidget.empty(Text.nostyle("←/→: select, Space: toggle, q: quit")), chunks[2]);
  }

  private static void renderButtons(Frame f, Rect area, State[] states) {
    Layout layout =
        new Layout(
            Direction.Horizontal,
            Margin.of(0),
            new Constraint[] {
              new Constraint.Length(15),
              new Constraint.Length(15),
              new Constraint.Length(15),
              new Constraint.Min(0)
            },
            true);
    Rect[] chunks = layout.split(area);
    f.renderWidget(Button.create("Red").withTheme(RED).withState(states[0]), chunks[0]);
    f.renderWidget(Button.create("Green").withTheme(GREEN).withState(states[1]), chunks[1]);
    f.renderWidget(Button.create("Blue").withTheme(BLUE).withState(states[2]), chunks[2]);
  }
}
