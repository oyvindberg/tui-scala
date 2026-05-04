package jatatui.react.examples.scrollbar;

import static jatatui.react.Components.*;

import jatatui.core.layout.Margin;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.ReactApp;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Scroll;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.crossterm.KeyCode;

/// Runnable demo of the React-style scrollbar component.
///
/// Renders 50 fake content lines on the left, with a controlled vertical scrollbar on the right.
/// Scroll position lives in `useState<Integer>` and is moved via Up/Down (single line) and
/// PgUp/PgDn (jump by `PAGE`). Esc / Ctrl-C quit (handled by [ReactApp]).
public final class ScrollbarExample {

  private static final int CONTENT_LENGTH = 50;
  private static final int PAGE = 10;

  public static void main(String[] args) throws IOException {
    ReactApp.run(app());
  }

  static Element app() {
    return component(
        ctx -> {
          var position = ctx.useState(() -> 0);
          boolean focused = ctx.useFocus(Optional.of("scrollbar-demo"), true);

          if (focused) {
            ctx.onKey(new KeyCode.Up(), () -> position.update(ScrollbarExample::dec));
            ctx.onKey(new KeyCode.Down(), () -> position.update(ScrollbarExample::inc));
            ctx.onKey(new KeyCode.PageUp(), () -> position.update(p -> clamp(p - PAGE)));
            ctx.onKey(new KeyCode.PageDown(), () -> position.update(p -> clamp(p + PAGE)));
            ctx.onKey(new KeyCode.Home(), () -> position.set(0));
            ctx.onKey(new KeyCode.End(), () -> position.set(CONTENT_LENGTH - 1));
          }

          int pos = position.get();
          String title = " Scrollable content (line " + (pos + 1) + " / " + CONTENT_LENGTH + ") ";

          return column(
              fill(1, scrollableArea(title, pos)),
              length(
                  1,
                  text(
                      "Up/Down to scroll, PgUp/PgDn to jump by " + PAGE + ", Home/End, Esc to quit",
                      Style.empty().withFg(Color.GRAY))))
              .withMargin(new Margin(1, 1));
        });
  }

  /// The content area + the scrollbar. Two columns inside a bordered block: a wide left column
  /// holds the (clipped) paragraph; the right-most single-cell column holds the scrollbar.
  static Element scrollableArea(String title, int position) {
    return component(
        ctx ->
            box(
                title,
                Borders.ALL,
                row(
                    fill(1, contentParagraph(position)),
                    length(
                        1,
                        jatatui.components.scrollbar.Components.scrollbar(
                            position, CONTENT_LENGTH)))));
  }

  /// Wraps a vertically-scrolled [Paragraph] as a React Element. Using the `widget(...)` escape
  /// hatch keeps this example single-file and avoids inventing a new Paragraph component.
  static Element contentParagraph(int position) {
    String content = String.join("\n", lines());
    Paragraph p =
        Paragraph.of(content)
            .withStyle(Style.empty().withFg(Color.WHITE))
            .withScroll(new Scroll(position, 0))
            .withBlock(Block.empty());
    return widget(p);
  }

  private static List<String> lines() {
    List<String> out = new ArrayList<>(CONTENT_LENGTH);
    for (int i = 0; i < CONTENT_LENGTH; i++) {
      out.add("Line " + (i + 1) + " — the quick brown fox jumps over the lazy dog");
    }
    return out;
  }

  private static int inc(int n) {
    return clamp(n + 1);
  }

  private static int dec(int n) {
    return clamp(n - 1);
  }

  private static int clamp(int n) {
    if (n < 0) return 0;
    if (n > CONTENT_LENGTH - 1) return CONTENT_LENGTH - 1;
    return n;
  }
}
