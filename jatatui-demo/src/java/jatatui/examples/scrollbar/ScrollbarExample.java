package jatatui.examples.scrollbar;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.text.Masked;
import jatatui.core.text.Span;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Scroll;
import jatatui.widgets.scrollbar.Scrollbar;
import jatatui.widgets.scrollbar.ScrollbarOrientation;
import jatatui.widgets.scrollbar.ScrollbarState;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to handle scrollbars.
///
/// Draws various types of vertical and horizontal scrollbars with different styles.
///
/// Java port of `examples/apps/scrollbar/src/main.rs` from ratatui v0.30.
public final class ScrollbarExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private ScrollbarExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  static final class App {
    final ScrollbarState verticalScrollState = ScrollbarState.empty();
    final ScrollbarState horizontalScrollState = ScrollbarState.empty();
    int verticalScroll = 0;
    int horizontalScroll = 0;

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      // tick rate: 250 ms — mirrors upstream's `Duration::from_millis(250)`.
      Duration tickRate = new Duration(0, 250_000_000);
      long lastTickNanos = System.nanoTime();
      while (true) {
        terminal.draw(this::render);
        long now = System.nanoTime();
        long elapsed = now - lastTickNanos;
        long tickNanos = tickRate.secs() * 1_000_000_000L + tickRate.nanos();
        long timeoutNanos = Math.max(0L, tickNanos - elapsed);
        Duration timeout =
            new Duration(timeoutNanos / 1_000_000_000L, (int) (timeoutNanos % 1_000_000_000L));
        if (!JNI.poll(timeout)) {
          lastTickNanos = System.nanoTime();
          continue;
        }
        Event event = JNI.read();
        if (!(event instanceof Event.Key keyEvt)) continue;
        if (keyEvt.keyEvent().kind() != KeyEventKind.Press) continue;
        KeyCode code = keyEvt.keyEvent().code();
        if (code instanceof KeyCode.Char ch) {
          switch (ch.c()) {
            case 'q' -> {
              return;
            }
            case 'j' -> scrollDown();
            case 'k' -> scrollUp();
            case 'h' -> scrollLeft();
            case 'l' -> scrollRight();
            default -> {
              // ignore
            }
          }
        } else if (code instanceof KeyCode.Down) {
          scrollDown();
        } else if (code instanceof KeyCode.Up) {
          scrollUp();
        } else if (code instanceof KeyCode.Left) {
          scrollLeft();
        } else if (code instanceof KeyCode.Right) {
          scrollRight();
        }
      }
    }

    void scrollDown() {
      verticalScroll = saturatingAdd(verticalScroll, 1);
      verticalScrollState.withPosition(verticalScroll);
    }

    void scrollUp() {
      verticalScroll = Math.max(0, verticalScroll - 1);
      verticalScrollState.withPosition(verticalScroll);
    }

    void scrollLeft() {
      horizontalScroll = Math.max(0, horizontalScroll - 1);
      horizontalScrollState.withPosition(horizontalScroll);
    }

    void scrollRight() {
      horizontalScroll = saturatingAdd(horizontalScroll, 1);
      horizontalScrollState.withPosition(horizontalScroll);
    }

    private static int saturatingAdd(int a, int b) {
      long r = (long) a + (long) b;
      return r > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) r;
    }

    void render(Frame frame) {
      Rect area = frame.area();

      // Words made "loooong" to demonstrate line breaking.
      String s =
          "Veeeeeeeeeeeeeeeery    loooooooooooooooooong   striiiiiiiiiiiiiiiiiiiiiiiiiing.   ";
      int repeatCount = (area.width() / s.length()) + 4;
      StringBuilder longBuilder = new StringBuilder(s.length() * repeatCount + 1);
      for (int i = 0; i < repeatCount; i++) {
        longBuilder.append(s);
      }
      longBuilder.append('\n');
      String longLine = longBuilder.toString();

      Rect[] chunks =
          Layout.vertical(
                  new Constraint.Min(1),
                  new Constraint.Percentage(25),
                  new Constraint.Percentage(25),
                  new Constraint.Percentage(25),
                  new Constraint.Percentage(25))
              .split(area);

      java.util.List<Line> text = new ArrayList<>();
      text.add(Line.from("This is a line "));
      text.add(Line.from("This is a line   ").red());
      text.add(Line.from("This is a line").onDarkGray());
      text.add(Line.from("This is a longer line").crossedOut());
      text.add(Line.from(longLine));
      text.add(Line.from("This is a line").reset());
      text.add(
          Line.from(
              Span.raw("Masked text: "),
              Span.styled(Masked.of("password", '*').value(), Style.empty().withFg(Color.RED))));
      text.add(Line.from("This is a line "));
      text.add(Line.from("This is a line   ").red());
      text.add(Line.from("This is a line").onDarkGray());
      text.add(Line.from("This is a longer line").crossedOut());
      text.add(Line.from(longLine));
      text.add(Line.from("This is a line").reset());
      text.add(
          Line.from(
              Span.raw("Masked text: "),
              Span.styled(Masked.of("password", '*').value(), Style.empty().withFg(Color.RED))));

      verticalScrollState.withContentLength(text.size());
      horizontalScrollState.withContentLength(longLine.length());

      Function<String, Block> createBlock =
          title -> Block.bordered().gray().withTitle(Line.from(Span.raw(title)).bold());

      Block titleBlock =
          Block.empty()
              .withTitleAlignment(HorizontalAlignment.Center)
              .withTitle(Line.from(Span.raw("Use h j k l or ◄ ▲ ▼ ► to scroll ")).bold());
      frame.renderWidget(titleBlock, chunks[0]);

      Paragraph paragraph1 =
          Paragraph.of(java.util.List.copyOf(text))
              .gray()
              .withBlock(createBlock.apply("Vertical scrollbar with arrows"))
              .withScroll(new Scroll(verticalScroll, 0));
      frame.renderWidget(paragraph1, chunks[1]);
      frame.renderStatefulWidget(
          Scrollbar.of(ScrollbarOrientation.VerticalRight)
              .withBeginSymbol(Optional.of("↑"))
              .withEndSymbol(Optional.of("↓")),
          chunks[1],
          verticalScrollState);

      Paragraph paragraph2 =
          Paragraph.of(java.util.List.copyOf(text))
              .gray()
              .withBlock(
                  createBlock.apply(
                      "Vertical scrollbar without arrows, without track symbol and mirrored"))
              .withScroll(new Scroll(verticalScroll, 0));
      frame.renderWidget(paragraph2, chunks[2]);
      frame.renderStatefulWidget(
          Scrollbar.of(ScrollbarOrientation.VerticalLeft)
              .withSymbols(jatatui.core.symbols.Scrollbar.VERTICAL)
              .withBeginSymbol(Optional.empty())
              .withTrackSymbol(Optional.empty())
              .withEndSymbol(Optional.empty()),
          chunks[2].inner(new Margin(0, 1)),
          verticalScrollState);

      Paragraph paragraph3 =
          Paragraph.of(java.util.List.copyOf(text))
              .gray()
              .withBlock(
                  createBlock.apply(
                      "Horizontal scrollbar with only begin arrow & custom thumb symbol"))
              .withScroll(new Scroll(0, horizontalScroll));
      frame.renderWidget(paragraph3, chunks[3]);
      frame.renderStatefulWidget(
          Scrollbar.of(ScrollbarOrientation.HorizontalBottom)
              .withThumbSymbol("🬋")
              .withEndSymbol(Optional.empty()),
          chunks[3].inner(new Margin(1, 0)),
          horizontalScrollState);

      Paragraph paragraph4 =
          Paragraph.of(java.util.List.copyOf(text))
              .gray()
              .withBlock(
                  createBlock.apply(
                      "Horizontal scrollbar without arrows & custom thumb and track symbol"))
              .withScroll(new Scroll(0, horizontalScroll));
      frame.renderWidget(paragraph4, chunks[4]);
      frame.renderStatefulWidget(
          Scrollbar.of(ScrollbarOrientation.HorizontalBottom)
              .withThumbSymbol("░")
              .withTrackSymbol(Optional.of("─")),
          chunks[4].inner(new Margin(1, 0)),
          horizontalScrollState);
    }
  }
}
