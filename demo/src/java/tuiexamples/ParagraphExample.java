package tuiexamples;

import java.time.Duration;
import java.time.Instant;
import tui.Alignment;
import tui.Borders;
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
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.ParagraphWidget;

public final class ParagraphExample {
  private ParagraphExample() {}

  public static final class App {
    public int scroll;

    public App(int scroll) {
      this.scroll = scroll;
    }

    public void onTick() {
      scroll += 1;
      scroll %= 10;
    }
  }

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      Duration tickRate = Duration.ofMillis(250);
      App app = new App(0);
      runApp(terminal, app, tickRate, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, App app, Duration tickRate, CrosstermJni jni) {
    Instant[] lastTick = {Instant.now()};

    while (true) {
      terminal.draw(f -> ui(f, app));

      Duration elapsed = Duration.between(lastTick[0], Instant.now());
      Duration remaining = tickRate.minus(elapsed);
      tui.crossterm.Duration timeout =
          new tui.crossterm.Duration(remaining.toSeconds(), remaining.getNano());
      if (jni.poll(timeout)) {
        Event ev = jni.read();
        if (ev instanceof Event.Key key) {
          if (key.keyEvent().code() instanceof KeyCode.Char c && c.c() == 'q') {
            return;
          }
        }
      }
      Duration elapsed2 = Duration.between(lastTick[0], Instant.now());
      if (elapsed2.compareTo(tickRate) >= 0) {
        app.onTick();
        lastTick[0] = Instant.now();
      }
    }
  }

  public static void ui(Frame f, App app) {
    String s = "Veeeeeeeeeeeeeeeery    loooooooooooooooooong   striiiiiiiiiiiiiiiiiiiiiiiiiing.   ";
    String longLine = s.repeat(f.size.width() / s.length() + 4) + "\n";

    BlockWidget block =
        BlockWidget.empty().withStyle(Style.empty().withBg(Color.White).withFg(Color.Black));
    f.renderWidget(block, f.size);

    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(5),
            new Constraint[] {
              new Constraint.Percentage(25),
              new Constraint.Percentage(25),
              new Constraint.Percentage(25),
              new Constraint.Percentage(25)
            }, true);
    Rect[] chunks = layout.split(f.size);

    Text text =
        Text.fromMany(
            Spans.nostyle("This is a line "),
            Spans.styled("This is a line   ", Style.DEFAULT.withFg(Color.Red)),
            Spans.styled("This is a line", Style.DEFAULT.withBg(Color.Blue)),
            Spans.styled(
                "This is a longer line", Style.DEFAULT.withAddModifier(Modifier.CROSSED_OUT)),
            Spans.styled(longLine, Style.DEFAULT.withBg(Color.Green)),
            Spans.styled(
                "This is a line",
                Style.DEFAULT.withFg(Color.Green).withAddModifier(Modifier.ITALIC)));

    ParagraphWidget paragraph0 =
        ParagraphWidget.empty(text)
            .withStyle(Style.empty().withBg(Color.White).withFg(Color.Black))
            .withBlock(createBlock("Left, no wrap"))
            .withAlignment(Alignment.Left);
    f.renderWidget(paragraph0, chunks[0]);

    ParagraphWidget paragraph1 =
        ParagraphWidget.empty(text)
            .withStyle(Style.empty().withBg(Color.White).withFg(Color.Black))
            .withBlock(createBlock("Left, wrap"))
            .withAlignment(Alignment.Left)
            .withWrap(new ParagraphWidget.Wrap(true));
    f.renderWidget(paragraph1, chunks[1]);

    ParagraphWidget paragraph2 =
        ParagraphWidget.empty(text)
            .withStyle(Style.empty().withBg(Color.White).withFg(Color.Black))
            .withBlock(createBlock("Center, wrap"))
            .withAlignment(Alignment.Center)
            .withWrap(new ParagraphWidget.Wrap(true))
            .withScroll(new ParagraphWidget.Scroll(app.scroll, 0));
    f.renderWidget(paragraph2, chunks[2]);

    ParagraphWidget paragraph3 =
        ParagraphWidget.empty(text)
            .withStyle(Style.empty().withBg(Color.White).withFg(Color.Black))
            .withBlock(createBlock("Right, wrap"))
            .withAlignment(Alignment.Right)
            .withWrap(new ParagraphWidget.Wrap(true));
    f.renderWidget(paragraph3, chunks[3]);
  }

  private static BlockWidget createBlock(String title) {
    return BlockWidget.empty()
        .withBorders(Borders.ALL)
        .withStyle(Style.empty().withBg(Color.White).withFg(Color.Black))
        .withTitle(
            Spans.from(Span.styled(title, Style.DEFAULT.withAddModifier(Modifier.BOLD))));
  }
}
