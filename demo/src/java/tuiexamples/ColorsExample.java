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
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.ParagraphWidget;

/// Renders a grid of named and indexed colors so you can verify what your terminal supports.
public final class ColorsExample {
  private ColorsExample() {}

  private static final Color[] NAMED_COLORS = {
    Color.Black, Color.Red, Color.Green, Color.Yellow,
    Color.Blue, Color.Magenta, Color.Cyan, Color.Gray,
    Color.DarkGray, Color.LightRed, Color.LightGreen, Color.LightYellow,
    Color.LightBlue, Color.LightMagenta, Color.LightCyan, Color.White
  };

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      runApp(terminal, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, CrosstermJni jni) {
    while (true) {
      terminal.draw(ColorsExample::ui);
      Duration timeout = new Duration(0, 250_000_000);
      if (jni.poll(timeout)) {
        Event ev = jni.read();
        if (ev instanceof Event.Key key
            && key.keyEvent().code() instanceof KeyCode.Char c
            && c.c() == 'q') {
          return;
        }
      }
    }
  }

  public static void ui(Frame frame) {
    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {
              new Constraint.Length(30),
              new Constraint.Length(17),
              new Constraint.Length(2)
            },
            true);
    Rect[] chunks = layout.split(frame.size);

    renderNamedColors(frame, chunks[0]);
    renderIndexedColors(frame, chunks[1]);
    renderIndexedGrayscale(frame, chunks[2]);
  }

  private static void renderNamedColors(Frame frame, Rect area) {
    Constraint[] rows = new Constraint[10];
    for (int i = 0; i < rows.length; i++) rows[i] = new Constraint.Length(3);
    Layout layout = new Layout(Direction.Vertical, Margin.of(0), rows, true);
    Rect[] rowAreas = layout.split(area);

    Color[] backgrounds = {Color.Reset, Color.Black, Color.DarkGray, Color.Gray, Color.White};
    for (int i = 0; i < backgrounds.length; i++) {
      renderFgNamedColors(frame, backgrounds[i], rowAreas[i]);
    }
    for (int i = 0; i < backgrounds.length; i++) {
      renderBgNamedColors(frame, backgrounds[i], rowAreas[i + backgrounds.length]);
    }
  }

  private static void renderFgNamedColors(Frame frame, Color bg, Rect area) {
    BlockWidget block = titleBlock("Foreground colors on " + colorName(bg) + " background");
    Rect inner = block.inner(area);
    frame.renderWidget(block, area);

    Rect[] cells = colorGrid(inner);
    for (int i = 0; i < NAMED_COLORS.length; i++) {
      Color fg = NAMED_COLORS[i];
      ParagraphWidget p =
          ParagraphWidget.empty(Text.nostyle(colorName(fg)))
              .withStyle(Style.empty().withFg(fg).withBg(bg));
      frame.renderWidget(p, cells[i]);
    }
  }

  private static void renderBgNamedColors(Frame frame, Color fg, Rect area) {
    BlockWidget block = titleBlock("Background colors with " + colorName(fg) + " foreground");
    Rect inner = block.inner(area);
    frame.renderWidget(block, area);

    Rect[] cells = colorGrid(inner);
    for (int i = 0; i < NAMED_COLORS.length; i++) {
      Color bg = NAMED_COLORS[i];
      ParagraphWidget p =
          ParagraphWidget.empty(Text.nostyle(colorName(bg)))
              .withStyle(Style.empty().withFg(fg).withBg(bg));
      frame.renderWidget(p, cells[i]);
    }
  }

  private static Rect[] colorGrid(Rect inner) {
    Layout vertical =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {new Constraint.Length(1), new Constraint.Length(1)},
            true);
    Rect[] rowAreas = vertical.split(inner);
    Constraint[] eight = new Constraint[8];
    for (int i = 0; i < 8; i++) eight[i] = new Constraint.Ratio(1, 8);
    Rect[] cells = new Rect[16];
    for (int r = 0; r < 2; r++) {
      Layout horizontal = new Layout(Direction.Horizontal, Margin.of(0), eight, true);
      Rect[] cs = horizontal.split(rowAreas[r]);
      System.arraycopy(cs, 0, cells, r * 8, 8);
    }
    return cells;
  }

  private static void renderIndexedColors(Frame frame, Rect area) {
    BlockWidget block = titleBlock("Indexed colors");
    Rect inner = block.inner(area);
    frame.renderWidget(block, area);

    Layout layout =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {
              new Constraint.Length(1), // 0..16
              new Constraint.Length(1), // blank
              new Constraint.Min(6), // 16..124
              new Constraint.Length(1), // blank
              new Constraint.Min(6), // 124..232
              new Constraint.Length(1) // blank
            },
            true);
    Rect[] sections = layout.split(inner);

    // First row: 0..16
    Constraint[] sixteen = new Constraint[16];
    for (int i = 0; i < 16; i++) sixteen[i] = new Constraint.Length(5);
    Rect[] firstRow =
        new Layout(Direction.Horizontal, Margin.of(0), sixteen, true).split(sections[0]);
    for (int i = 0; i < 16; i++) {
      Color color = new Color.Indexed(i);
      Color bg = i < 1 ? Color.DarkGray : Color.Black;
      String idx = String.format("%02d", i);
      Spans line =
          Spans.from(
              Span.styled(idx, Style.empty().withFg(color).withBg(bg)),
              Span.styled("██", Style.empty().withBg(color).withFg(color)));
      frame.renderWidget(ParagraphWidget.empty(Text.from(line)), firstRow[i]);
    }

    // 16..232 split across two row sections (sections[2], sections[4]), each 3 columns of 6x6 cells.
    Rect[][] indexLayout = build216Layout(new Rect[] {sections[2], sections[4]});
    int n = indexLayout.length;
    int cellsTotal = 0;
    for (Rect[] row : indexLayout) cellsTotal += row.length;
    Rect[] flat = new Rect[cellsTotal];
    int k = 0;
    for (Rect[] row : indexLayout) {
      for (Rect r : row) {
        flat[k++] = r;
      }
    }

    for (int i = 16; i <= 231 && (i - 16) < flat.length; i++) {
      Color color = new Color.Indexed(i);
      Spans line =
          Spans.from(
              Span.styled(String.format("%03d", i), Style.empty().withFg(color).withBg(Color.Reset)),
              Span.styled(".", Style.empty().withBg(color).withFg(color)),
              Span.styled("███", Style.empty().withAddModifier(Modifier.REVERSED)));
      frame.renderWidget(ParagraphWidget.empty(Text.from(line)), flat[i - 16]);
    }
  }

  private static Rect[][] build216Layout(Rect[] sections) {
    // For each section, split into 3 horizontal columns of width 27,
    // each into 6 vertical rows of 1 line, each into 6 horizontal cells of min 4.
    java.util.List<Rect> rowsList = new java.util.ArrayList<>();
    Constraint[] threeCols = new Constraint[] {
        new Constraint.Length(27), new Constraint.Length(27), new Constraint.Length(27)
    };
    Constraint[] sixRows = new Constraint[6];
    for (int i = 0; i < 6; i++) sixRows[i] = new Constraint.Length(1);
    Constraint[] sixCells = new Constraint[6];
    for (int i = 0; i < 6; i++) sixCells[i] = new Constraint.Min(4);

    for (Rect section : sections) {
      Rect[] cols =
          new Layout(Direction.Horizontal, Margin.of(0), threeCols, true).split(section);
      for (Rect col : cols) {
        Rect[] rows =
            new Layout(Direction.Vertical, Margin.of(0), sixRows, true).split(col);
        for (Rect row : rows) {
          Rect[] cells =
              new Layout(Direction.Horizontal, Margin.of(0), sixCells, true).split(row);
          rowsList.add(cells[0]);
          rowsList.add(cells[1]);
          rowsList.add(cells[2]);
          rowsList.add(cells[3]);
          rowsList.add(cells[4]);
          rowsList.add(cells[5]);
        }
      }
    }
    Rect[] arr = rowsList.toArray(new Rect[0]);
    return new Rect[][] {arr};
  }

  private static void renderIndexedGrayscale(Frame frame, Rect area) {
    Constraint[] rows = new Constraint[2];
    for (int i = 0; i < 2; i++) rows[i] = new Constraint.Length(1);
    Rect[] rowAreas =
        new Layout(Direction.Vertical, Margin.of(0), rows, true).split(area);

    Constraint[] cols = new Constraint[12];
    for (int i = 0; i < 12; i++) cols[i] = new Constraint.Length(6);

    Rect[] cells = new Rect[24];
    for (int r = 0; r < 2; r++) {
      Rect[] cs =
          new Layout(Direction.Horizontal, Margin.of(0), cols, true).split(rowAreas[r]);
      System.arraycopy(cs, 0, cells, r * 12, 12);
    }

    for (int i = 232; i <= 255; i++) {
      Color color = new Color.Indexed(i);
      Color bg = i < 244 ? Color.Gray : Color.Black;
      Spans line =
          Spans.from(
              Span.styled(String.format("%03d", i), Style.empty().withFg(color).withBg(bg)),
              Span.styled("██", Style.empty().withBg(color).withFg(color)));
      frame.renderWidget(ParagraphWidget.empty(Text.from(line)), cells[i - 232]);
    }
  }

  private static BlockWidget titleBlock(String title) {
    return BlockWidget.empty()
        .withBorders(Borders.TOP)
        .withBorderStyle(Style.empty().withFg(Color.DarkGray))
        .withTitle(Spans.nostyle(title))
        .withTitleAlignment(Alignment.Center);
  }

  private static String colorName(Color c) {
    return switch (c) {
      case Color.Reset r -> "Reset";
      case Color.Black b -> "Black";
      case Color.Red r -> "Red";
      case Color.Green g -> "Green";
      case Color.Yellow y -> "Yellow";
      case Color.Blue b -> "Blue";
      case Color.Magenta m -> "Magenta";
      case Color.Cyan c2 -> "Cyan";
      case Color.Gray g -> "Gray";
      case Color.DarkGray d -> "DarkGray";
      case Color.LightRed l -> "LightRed";
      case Color.LightGreen l -> "LightGreen";
      case Color.LightYellow l -> "LightYellow";
      case Color.LightBlue l -> "LightBlue";
      case Color.LightMagenta l -> "LightMagenta";
      case Color.LightCyan l -> "LightCyan";
      case Color.White w -> "White";
      case Color.Rgb rgb -> String.format("#%02X%02X%02X", rgb.r(), rgb.g(), rgb.b());
      case Color.Indexed ix -> Integer.toString(ix.index());
    };
  }
}
