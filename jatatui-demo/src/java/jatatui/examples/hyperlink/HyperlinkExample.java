package jatatui.examples.hyperlink;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyEventKind;

/// A jatatui example showing how to create hyperlinks in the terminal using OSC 8.
///
/// Mirrors `examples/apps/hyperlink/src/main.rs`. Renders the hyperlink and waits for any key
/// press to exit.
public final class HyperlinkExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private HyperlinkExample() {}

  public static void main(String[] args) throws IOException {
    Line text = Line.from(Span.raw("Example "), Span.raw("hyperlink").blue());
    Hyperlink hyperlink = new Hyperlink(Text.from(text), "https://example.com");

    Jatatui.runIo(terminal -> run(terminal, hyperlink));
  }

  private static void run(Terminal<CrosstermBackend> terminal, Hyperlink hyperlink)
      throws IOException {
    while (true) {
      terminal.draw(frame -> hyperlink.render(frame.area(), frame.bufferMut()));
      Event event = JNI.read();
      if (event instanceof Event.Key key
          && key.keyEvent().kind() == KeyEventKind.Press) {
        return;
      }
    }
  }

  /// A hyperlink widget that renders a hyperlink in the terminal using OSC 8.
  ///
  /// This is a hacky workaround for ratatui issue #902, a bug in the terminal code that
  /// incorrectly calculates the width of ANSI escape sequences. It works by rendering the
  /// hyperlink as a series of 2-character chunks, which is the calculated width of the hyperlink
  /// text.
  static final class Hyperlink implements Widget {
    /// ESC (\u001B) — start of the OSC 8 sequence.
    private static final String ESC = "\u001B";

    /// BEL (\u0007) — terminator of the OSC 8 sequence.
    private static final String BEL = "\u0007";

    private final Text text;
    private final String url;

    Hyperlink(Text text, String url) {
      this.text = text;
      this.url = url;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
      // Render the underlying text first so the styled spans land in the buffer.
      Paragraph.of(text).render(area, buffer);

      // Then walk the rendered string in 2-character chunks and overwrite each pair of cells
      // with the OSC-8 escape sequence wrapping the literal characters.
      String content = textAsString(text);
      int charCount = content.length();
      int i = 0;
      int chunkIndex = 0;
      while (i < charCount) {
        int end = Math.min(i + 2, charCount);
        String twoChars = content.substring(i, end);
        String hyperlink = ESC + "]8;;" + url + BEL + twoChars + ESC + "]8;;" + BEL;
        int x = area.x() + chunkIndex * 2;
        int y = area.y();
        if (x < buffer.area().right() && y < buffer.area().bottom()) {
          buffer.cellAt(x, y).setSymbol(hyperlink);
        }
        i = end;
        chunkIndex += 1;
      }
    }

    /// Concatenates the content of every span on every line of `t`, joining lines with `\n`.
    /// Equivalent to upstream's `text.to_string()`.
    private static String textAsString(Text t) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (Line line : t) {
        if (!first) sb.append('\n');
        first = false;
        for (Span span : line) {
          sb.append(span.content);
        }
      }
      return sb.toString();
    }
  }
}
