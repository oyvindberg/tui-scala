package jatatui.examples.inputform;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Offset;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.util.Optional;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to handle input form focus.
///
/// This example demonstrates how to handle cursor and input focus between multiple fields in a
/// form. You can navigate between fields using the Tab key. Press Enter to submit, Esc to cancel.
///
/// Java port of `examples/apps/input-form/src/main.rs` from ratatui v0.30.
public final class InputFormExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private InputFormExample() {}

  public static void main(String[] args) throws IOException {
    Optional<InputForm> result = run();
    result.ifPresentOrElse(
        form -> System.out.println(form.toJsonPretty()), () -> System.out.println("Canceled"));
  }

  private static Optional<InputForm> run() throws IOException {
    App[] holder = new App[] {new App()};
    Jatatui.runIo(terminal -> loop(terminal, holder[0]));
    App finished = holder[0];
    return switch (finished.state) {
      case Cancelled -> Optional.empty();
      case Submitted -> Optional.of(finished.form);
      case Running -> throw new IllegalStateException("loop exited while still running");
    };
  }

  private static void loop(Terminal<CrosstermBackend> terminal, App app) throws IOException {
    while (app.state == AppState.Running) {
      terminal.draw(app::render);
      handleEvents(app);
    }
  }

  private static void handleEvents(App app) {
    Event event = JNI.read();
    if (!(event instanceof Event.Key keyEvt)) return;
    KeyEvent ke = keyEvt.keyEvent();
    if (ke.kind() != KeyEventKind.Press) return;
    KeyCode code = ke.code();
    if (code instanceof KeyCode.Esc) {
      app.state = AppState.Cancelled;
    } else if (code instanceof KeyCode.Enter) {
      app.state = AppState.Submitted;
    } else {
      app.form.onKeyPress(ke);
    }
  }

  // ---- App state ----

  enum AppState {
    Running,
    Cancelled,
    Submitted
  }

  static final class App {
    AppState state = AppState.Running;
    InputForm form = new InputForm();

    void render(Frame frame) {
      form.render(frame);
    }
  }

  // ---- Input form ----

  static final class InputForm {
    Focus focus = Focus.FirstName;
    final StringField firstName = new StringField("First Name");
    final StringField lastName = new StringField("Last Name");
    final AgeField age = new AgeField("Age");

    /// Handle focus navigation or pass the event to the focused field.
    void onKeyPress(KeyEvent event) {
      if (event.code() instanceof KeyCode.Tab) {
        focus = focus.next();
        return;
      }
      switch (focus) {
        case FirstName -> firstName.onKeyPress(event);
        case LastName -> lastName.onKeyPress(event);
        case Age -> age.onKeyPress(event);
      }
    }

    /// Render the form with the current focus.
    ///
    /// The cursor is placed at the end of the focused field.
    void render(Frame frame) {
      Layout layout = Layout.vertical(Constraint.fromLengths(1, 1, 1));
      Rect[] split = frame.area().layout(layout, 3);
      Rect firstNameArea = split[0];
      Rect lastNameArea = split[1];
      Rect ageArea = split[2];

      frame.renderWidget(firstName, firstNameArea);
      frame.renderWidget(lastName, lastNameArea);
      frame.renderWidget(age, ageArea);

      Rect cursorArea =
          switch (focus) {
            case FirstName -> firstNameArea.plus(firstName.cursorOffset());
            case LastName -> lastNameArea.plus(lastName.cursorOffset());
            case Age -> ageArea.plus(age.cursorOffset());
          };
      frame.setCursorPosition(new Position(cursorArea.x(), cursorArea.y()));
    }

    String toJsonPretty() {
      // Simple, dependency-free JSON formatter mirroring upstream's `serde_json::to_string_pretty`.
      StringBuilder sb = new StringBuilder();
      sb.append("{\n");
      sb.append("  \"first_name\": \"").append(escapeJson(firstName.value)).append("\",\n");
      sb.append("  \"last_name\": \"").append(escapeJson(lastName.value)).append("\",\n");
      sb.append("  \"age\": ").append(age.value).append("\n");
      sb.append("}");
      return sb.toString();
    }

    private static String escapeJson(String s) {
      StringBuilder out = new StringBuilder(s.length());
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        switch (c) {
          case '"' -> out.append("\\\"");
          case '\\' -> out.append("\\\\");
          case '\n' -> out.append("\\n");
          case '\r' -> out.append("\\r");
          case '\t' -> out.append("\\t");
          default -> {
            if (c < 0x20) {
              out.append(String.format("\\u%04x", (int) c));
            } else {
              out.append(c);
            }
          }
        }
      }
      return out.toString();
    }
  }

  enum Focus {
    FirstName,
    LastName,
    Age;

    /// Round-robin focus order.
    Focus next() {
      return switch (this) {
        case FirstName -> LastName;
        case LastName -> Age;
        case Age -> FirstName;
      };
    }
  }

  /// A new-type representing a string field with a label.
  static final class StringField implements Widget {
    final String label;
    String value = "";

    StringField(String label) {
      this.label = label;
    }

    /// Handle input events for the string input.
    void onKeyPress(KeyEvent event) {
      KeyCode code = event.code();
      if (code instanceof KeyCode.Char ch) {
        value = value + ch.c();
      } else if (code instanceof KeyCode.Backspace) {
        if (!value.isEmpty()) {
          value = value.substring(0, value.length() - 1);
        }
      }
    }

    Offset cursorOffset() {
      int x = label.length() + value.length() + 2;
      return new Offset(x, 0);
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Layout layout =
          Layout.horizontal(new Constraint.Length(label.length() + 2), new Constraint.Fill(1));
      Rect[] split = area.layout(layout, 2);
      Rect labelArea = split[0];
      Rect valueArea = split[1];
      Line lbl = Line.fromIter(java.util.List.of(label, ": ")).bold();
      Paragraph.of(lbl).render(labelArea, buf);
      Paragraph.of(value).render(valueArea, buf);
    }
  }

  /// A new-type representing a person's age in years (0-130).
  static final class AgeField implements Widget {
    static final int MAX = 130;

    final String label;
    int value = 0;

    AgeField(String label) {
      this.label = label;
    }

    /// Handle input events for the age input.
    ///
    /// Digits are accepted as input, with any input which would exceed the maximum age being
    /// ignored. The up/down arrow keys and 'j'/'k' keys can be used to increment/decrement the
    /// age.
    void onKeyPress(KeyEvent event) {
      KeyCode code = event.code();
      if (code instanceof KeyCode.Char ch) {
        char c = ch.c();
        if (c >= '0' && c <= '9') {
          // Mirror upstream's `saturating_mul(10).saturating_add(digit - '0')` clamped to MAX.
          long candidate = (long) value * 10L + (long) (c - '0');
          if (candidate > MAX) {
            // value stays unchanged
          } else {
            value = (int) candidate;
          }
        } else if (c == 'k') {
          increment();
        } else if (c == 'j') {
          decrement();
        }
      } else if (code instanceof KeyCode.Backspace) {
        value = value / 10;
      } else if (code instanceof KeyCode.Up) {
        increment();
      } else if (code instanceof KeyCode.Down) {
        decrement();
      }
    }

    void increment() {
      value = Math.min(MAX, value + 1);
    }

    void decrement() {
      value = Math.max(0, value - 1);
    }

    Offset cursorOffset() {
      int x = label.length() + Integer.toString(value).length() + 2;
      return new Offset(x, 0);
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Layout layout =
          Layout.horizontal(new Constraint.Length(label.length() + 2), new Constraint.Fill(1));
      Rect[] split = area.layout(layout, 2);
      Rect labelArea = split[0];
      Rect valueArea = split[1];
      Line lbl = Line.fromIter(java.util.List.of(label, ": ")).bold();
      Paragraph.of(lbl).render(labelArea, buf);
      Paragraph.of(Integer.toString(value)).render(valueArea, buf);
    }
  }
}
