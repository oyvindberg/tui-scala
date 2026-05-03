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
import tui.Text;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.ParagraphWidget;

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
      terminal.draw(BlockExample::ui);
      Event ev = jni.read();
      if (ev instanceof Event.Key key) {
        if (key.keyEvent().code() instanceof KeyCode.Char c && c.c() == 'q') {
          return;
        }
      }
    }
  }

  public static void ui(Frame frame) {
    Layout outer =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {new Constraint.Length(1), new Constraint.Min(0)},
            true);
    Rect[] outerChunks = outer.split(frame.size);
    Rect titleArea = outerChunks[0];

    // 6 rows of pairs of cells.
    Constraint[] rowConstraints = {
      new Constraint.Max(4),
      new Constraint.Max(4),
      new Constraint.Max(4),
      new Constraint.Max(4),
      new Constraint.Max(4),
      new Constraint.Max(4),
      new Constraint.Min(0)
    };
    Layout rowsLayout =
        new Layout(Direction.Vertical, Margin.of(0), rowConstraints, true);
    Rect[] rows = rowsLayout.split(outerChunks[1]);

    Rect[][] grid = new Rect[6][];
    for (int i = 0; i < 6; i++) {
      Layout cells =
          new Layout(
              Direction.Horizontal,
              Margin.of(0),
              new Constraint[] {new Constraint.Percentage(50), new Constraint.Percentage(50)},
              true);
      grid[i] = cells.split(rows[i]);
    }

    renderTitle(frame, titleArea);

    ParagraphWidget paragraph = placeholderParagraph();

    renderBorders(frame, paragraph, Borders.ALL, grid[0][0]);
    renderBorders(frame, paragraph, Borders.NONE, grid[0][1]);
    renderBorders(frame, paragraph, Borders.LEFT, grid[1][0]);
    renderBorders(frame, paragraph, Borders.RIGHT, grid[1][1]);
    renderBorders(frame, paragraph, Borders.TOP, grid[2][0]);
    renderBorders(frame, paragraph, Borders.BOTTOM, grid[2][1]);

    renderBorderType(frame, paragraph, BlockWidget.BorderType.Plain, grid[3][0]);
    renderBorderType(frame, paragraph, BlockWidget.BorderType.Rounded, grid[3][1]);
    renderBorderType(frame, paragraph, BlockWidget.BorderType.Double, grid[4][0]);
    renderBorderType(frame, paragraph, BlockWidget.BorderType.Thick, grid[4][1]);

    renderStyledBlock(frame, paragraph, grid[5][0]);
    renderStyledBorders(frame, paragraph, grid[5][1]);
  }

  private static void renderTitle(Frame frame, Rect area) {
    ParagraphWidget title =
        ParagraphWidget.empty(Text.nostyle("Block example. Press q to quit"))
            .withStyle(Style.empty().withFg(Color.DarkGray))
            .withAlignment(Alignment.Center);
    frame.renderWidget(title, area);
  }

  private static ParagraphWidget placeholderParagraph() {
    String text =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor"
            + " incididunt ut labore et dolore magna aliqua.";
    return ParagraphWidget.empty(
            Text.from(Span.styled(text, Style.empty().withFg(Color.DarkGray))))
        .withWrap(new ParagraphWidget.Wrap(true));
  }

  private static void renderBorders(
      Frame frame, ParagraphWidget paragraph, Borders borders, Rect area) {
    BlockWidget block =
        BlockWidget.empty().withBorders(borders).withTitle(Spans.nostyle("Borders::" + bordersName(borders)));
    frame.renderWidget(paragraph.withBlock(block), area);
  }

  private static void renderBorderType(
      Frame frame, ParagraphWidget paragraph, BlockWidget.BorderType type, Rect area) {
    BlockWidget block =
        BlockWidget.empty()
            .withBorders(Borders.ALL)
            .withBorderType(type)
            .withTitle(Spans.nostyle("BorderType::" + type.name()));
    frame.renderWidget(paragraph.withBlock(block), area);
  }

  private static void renderStyledBlock(Frame frame, ParagraphWidget paragraph, Rect area) {
    BlockWidget block =
        BlockWidget.empty()
            .withBorders(Borders.ALL)
            .withStyle(
                Style.empty()
                    .withFg(Color.Blue)
                    .withBg(Color.White)
                    .withAddModifier(Modifier.BOLD.or(Modifier.ITALIC)))
            .withTitle(Spans.nostyle("Styled block"));
    frame.renderWidget(paragraph.withBlock(block), area);
  }

  private static void renderStyledBorders(Frame frame, ParagraphWidget paragraph, Rect area) {
    BlockWidget block =
        BlockWidget.empty()
            .withBorders(Borders.ALL)
            .withBorderStyle(
                Style.empty()
                    .withFg(Color.Blue)
                    .withBg(Color.White)
                    .withAddModifier(Modifier.BOLD.or(Modifier.ITALIC)))
            .withTitle(Spans.nostyle("Styled borders"));
    frame.renderWidget(paragraph.withBlock(block), area);
  }

  private static String bordersName(Borders borders) {
    if (borders.equals(Borders.NONE)) return "NONE";
    if (borders.equals(Borders.ALL)) return "ALL";
    StringBuilder sb = new StringBuilder();
    if (borders.intersects(Borders.LEFT)) appendBit(sb, "LEFT");
    if (borders.intersects(Borders.RIGHT)) appendBit(sb, "RIGHT");
    if (borders.intersects(Borders.TOP)) appendBit(sb, "TOP");
    if (borders.intersects(Borders.BOTTOM)) appendBit(sb, "BOTTOM");
    return sb.toString();
  }

  private static void appendBit(StringBuilder sb, String name) {
    if (sb.length() > 0) sb.append(" | ");
    sb.append(name);
  }
}
