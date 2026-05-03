package tuiexamples;

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
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;

public final class BlockExample {
  private BlockExample() {}

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      runApp(terminal, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, CrosstermJni jni) {
    while (true) {
      terminal.draw(f -> ui(f));
      Event ev = jni.read();
      if (ev instanceof Event.Key key) {
        if (key.keyEvent().code() instanceof KeyCode.Char c && c.c() == 'q') {
          return;
        }
      }
    }
  }

  public static void ui(Frame f) {
    Rect size = f.size;

    BlockWidget block0 =
        BlockWidget.empty()
            .withBorders(Borders.ALL)
            .withTitle(Spans.nostyle("Main block with round corners"))
            .withTitleAlignment(Alignment.Center)
            .withBorderType(BlockWidget.BorderType.Rounded);
    f.renderWidget(block0, size);

    Layout outerLayout =
        new Layout(
            Direction.Vertical,
            Margin.of(4),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)}, true);
    Rect[] chunks = outerLayout.split(f.size);

    Layout topLayout =
        new Layout(
            Direction.Horizontal,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)}, true);
    Rect[] topChunks = topLayout.split(chunks[0]);

    BlockWidget blockTop0 =
        BlockWidget.empty()
            .withTitle(
                Spans.from(
                    Span.styled("With", Style.DEFAULT.withFg(Color.Yellow)),
                    Span.nostyle(" background")))
            .withStyle(Style.DEFAULT.withBg(Color.Green));
    f.renderWidget(blockTop0, topChunks[0]);

    BlockWidget blockTop1 =
        BlockWidget.empty()
            .withTitle(
                Spans.from(
                    Span.styled(
                        "Styled title",
                        Style.empty()
                            .withFg(Color.White)
                            .withBg(Color.Red)
                            .withAddModifier(Modifier.BOLD))))
            .withTitleAlignment(Alignment.Right);
    f.renderWidget(blockTop1, topChunks[1]);

    Layout bottomLayout =
        new Layout(
            Direction.Horizontal,
            new Margin(0, 0),
            new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)}, true);
    Rect[] bottomChunks = bottomLayout.split(chunks[1]);

    BlockWidget blockBottom0 =
        BlockWidget.empty().withTitle(Spans.nostyle("With borders")).withBorders(Borders.ALL);
    f.renderWidget(blockBottom0, bottomChunks[0]);

    BlockWidget blockBottom1 =
        BlockWidget.empty()
            .withTitle(Spans.nostyle("With styled borders and doubled borders"))
            .withBorderStyle(Style.DEFAULT.withFg(Color.Cyan))
            .withBorders(Borders.LEFT.or(Borders.RIGHT))
            .withBorderType(BlockWidget.BorderType.Double);
    f.renderWidget(blockBottom1, bottomChunks[1]);
  }
}
