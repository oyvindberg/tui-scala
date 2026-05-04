package jatatui.examples.userinput;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.list.List;
import jatatui.widgets.list.ListItem;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.util.ArrayList;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to handle user input.
///
/// An input box is always focused. Every character you type is registered there. An entered
/// character is inserted at the cursor position. Backspace erases the character to the left of the
/// cursor. Enter pushes the current input into the message history.
///
/// Java port of `examples/apps/user-input/src/main.rs` from ratatui v0.30. Unicode characters are
/// unsupported (mirroring upstream's caveat).
public final class UserInputExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private UserInputExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  enum InputMode {
    Normal,
    Editing
  }

  /// App holds the state of the application.
  static final class App {
    /// Current value of the input box.
    String input = "";
    /// Position of the cursor (in characters) in the editor area.
    int characterIndex = 0;
    /// Current input mode.
    InputMode inputMode = InputMode.Normal;
    /// History of recorded messages.
    final java.util.List<String> messages = new ArrayList<>();

    void moveCursorLeft() {
      int moved = Math.max(0, characterIndex - 1);
      characterIndex = clampCursor(moved);
    }

    void moveCursorRight() {
      int moved = characterIndex + 1;
      characterIndex = clampCursor(moved);
    }

    void enterChar(char newChar) {
      int index = byteIndex();
      input = input.substring(0, index) + newChar + input.substring(index);
      moveCursorRight();
    }

    /// Returns the byte index based on the character position.
    ///
    /// Mirrors upstream's helper for working with UTF-8 byte boundaries. In Java, `String` is
    /// UTF-16 — each `char` is a 16-bit code unit. We mimic upstream's per-`char` semantics by
    /// using `String.offsetByCodePoints` over the input length.
    int byteIndex() {
      int n = input.length();
      int idx = 0;
      int count = 0;
      while (idx < n && count < characterIndex) {
        int cp = input.codePointAt(idx);
        idx += Character.charCount(cp);
        count++;
      }
      return idx;
    }

    void deleteChar() {
      boolean isNotCursorLeftmost = characterIndex != 0;
      if (!isNotCursorLeftmost) return;
      int currentIndex = characterIndex;
      int fromLeftToCurrentIndex = currentIndex - 1;
      // Build a new string by collecting code points before and after the deleted one.
      StringBuilder before = new StringBuilder();
      StringBuilder after = new StringBuilder();
      int idx = 0;
      int count = 0;
      int n = input.length();
      while (idx < n) {
        int cp = input.codePointAt(idx);
        int sz = Character.charCount(cp);
        if (count < fromLeftToCurrentIndex) {
          before.appendCodePoint(cp);
        } else if (count >= currentIndex) {
          after.appendCodePoint(cp);
        }
        // else: this is the character being deleted — skip it
        idx += sz;
        count++;
      }
      input = before.toString() + after;
      moveCursorLeft();
    }

    int clampCursor(int newCursorPos) {
      int charCount = input.codePointCount(0, input.length());
      return Math.max(0, Math.min(newCursorPos, charCount));
    }

    void resetCursor() {
      characterIndex = 0;
    }

    void submitMessage() {
      messages.add(input);
      input = "";
      resetCursor();
    }

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      while (true) {
        terminal.draw(this::render);
        Event event = JNI.read();
        if (!(event instanceof Event.Key keyEvt)) continue;
        KeyEvent ke = keyEvt.keyEvent();
        switch (inputMode) {
          case Normal -> {
            if (ke.kind() != KeyEventKind.Press) {
              continue;
            }
            KeyCode code = ke.code();
            if (code instanceof KeyCode.Char ch) {
              if (ch.c() == 'e') {
                inputMode = InputMode.Editing;
              } else if (ch.c() == 'q') {
                return;
              }
            }
          }
          case Editing -> {
            if (ke.kind() != KeyEventKind.Press) {
              continue;
            }
            KeyCode code = ke.code();
            if (code instanceof KeyCode.Enter) {
              submitMessage();
            } else if (code instanceof KeyCode.Char ch) {
              enterChar(ch.c());
            } else if (code instanceof KeyCode.Backspace) {
              deleteChar();
            } else if (code instanceof KeyCode.Left) {
              moveCursorLeft();
            } else if (code instanceof KeyCode.Right) {
              moveCursorRight();
            } else if (code instanceof KeyCode.Esc) {
              inputMode = InputMode.Normal;
            }
          }
        }
      }
    }

    void render(Frame frame) {
      Layout layout =
          Layout.vertical(
              new Constraint.Length(1),
              new Constraint.Length(3),
              new Constraint.Min(1));
      Rect[] split = frame.area().layout(layout, 3);
      Rect helpArea = split[0];
      Rect inputArea = split[1];
      Rect messagesArea = split[2];

      MessageAndStyle ms = helpMessage();
      Text text = Text.from(Line.from(ms.spans.toArray(new Span[0]))).patchStyle(ms.style);
      Paragraph helpMessage = Paragraph.of(text);
      frame.renderWidget(helpMessage, helpArea);

      Style inputStyle =
          switch (inputMode) {
            case Normal -> Style.empty();
            case Editing -> Style.empty().withFg(Color.YELLOW);
          };
      Paragraph input =
          Paragraph.of(this.input)
              .withStyle(inputStyle)
              .withBlock(Block.bordered().withTitle("Input"));
      frame.renderWidget(input, inputArea);
      switch (inputMode) {
        case Normal -> {
          // Hide the cursor. `Frame` does this by default, so we don't need to do anything here.
        }
        case Editing -> frame.setCursorPosition(
            new Position(
                // Draw the cursor at the current position in the input field.
                inputArea.x() + this.characterIndex + 1,
                // Move one line down, from the border to the input line.
                inputArea.y() + 1));
      }

      java.util.List<ListItem> items = new ArrayList<>(messages.size());
      for (int i = 0; i < messages.size(); i++) {
        String m = messages.get(i);
        Line content = Line.from(Span.raw(i + ": " + m));
        items.add(ListItem.of(content));
      }
      List messagesList = List.of(items).withBlock(Block.bordered().withTitle("Messages"));
      frame.renderWidget(messagesList, messagesArea);
    }

    private MessageAndStyle helpMessage() {
      java.util.List<Span> spans = new ArrayList<>();
      Style style;
      switch (inputMode) {
        case Normal -> {
          spans.add(Span.raw("Press "));
          spans.add(Span.styled("q", Style.empty().bold()));
          spans.add(Span.raw(" to exit, "));
          spans.add(Span.styled("e", Style.empty().bold()));
          spans.add(Span.styled(" to start editing.", Style.empty().bold()));
          style = Style.empty().withAddModifier(Modifier.RAPID_BLINK);
        }
        case Editing -> {
          spans.add(Span.raw("Press "));
          spans.add(Span.styled("Esc", Style.empty().bold()));
          spans.add(Span.raw(" to stop editing, "));
          spans.add(Span.styled("Enter", Style.empty().bold()));
          spans.add(Span.raw(" to record the message"));
          style = Style.empty();
        }
        default -> throw new IllegalStateException("Unknown input mode: " + inputMode);
      }
      return new MessageAndStyle(spans, style);
    }
  }

  /// Pair of (spans, style) returned by [App#helpMessage()].
  private record MessageAndStyle(java.util.List<Span> spans, Style style) {}
}
