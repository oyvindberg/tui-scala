package jatatui.examples.colorexplorer;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to handle colors.
///
/// This example shows all the colors supported by jatatui. It will render a grid of foreground
/// and background colors with their names and indexes.
///
/// Java port of `examples/apps/color-explorer/src/main.rs` from ratatui v0.30.
public final class ColorExplorerExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private static final Color[] NAMED_COLORS = {
      Color.BLACK,
      Color.RED,
      Color.GREEN,
      Color.YELLOW,
      Color.BLUE,
      Color.MAGENTA,
      Color.CYAN,
      Color.GRAY,
      Color.DARK_GRAY,
      Color.LIGHT_RED,
      Color.LIGHT_GREEN,
      Color.LIGHT_YELLOW,
      Color.LIGHT_BLUE,
      Color.LIGHT_MAGENTA,
      Color.LIGHT_CYAN,
      Color.WHITE,
  };

  private ColorExplorerExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> {
      while (true) {
        terminal.draw(ColorExplorerExample::render);
        Event event = JNI.read();
        if (isKeyPress(event)) {
          return;
        }
      }
    });
  }

  private static boolean isKeyPress(Event event) {
    if (event instanceof Event.Key key) {
      return key.keyEvent().kind() == KeyEventKind.Press;
    }
    return false;
  }

  private static void render(Frame frame) {
    Rect[] rows = Layout.vertical(
        new Constraint.Length(30),
        new Constraint.Length(17),
        new Constraint.Length(2)).split(frame.area());
    renderNamedColors(frame, rows[0]);
    renderIndexedColors(frame, rows[1]);
    renderIndexedGrayscale(frame, rows[2]);
  }

  private static void renderNamedColors(Frame frame, Rect area) {
    Rect[] layout = repeatedLengthVertical(3, 10).split(area);
    renderFgNamedColors(frame, Color.RESET, layout[0]);
    renderFgNamedColors(frame, Color.BLACK, layout[1]);
    renderFgNamedColors(frame, Color.DARK_GRAY, layout[2]);
    renderFgNamedColors(frame, Color.GRAY, layout[3]);
    renderFgNamedColors(frame, Color.WHITE, layout[4]);

    renderBgNamedColors(frame, Color.RESET, layout[5]);
    renderBgNamedColors(frame, Color.BLACK, layout[6]);
    renderBgNamedColors(frame, Color.DARK_GRAY, layout[7]);
    renderBgNamedColors(frame, Color.GRAY, layout[8]);
    renderBgNamedColors(frame, Color.WHITE, layout[9]);
  }

  private static void renderFgNamedColors(Frame frame, Color bg, Rect area) {
    Block block = titleBlock("Foreground colors on " + bg + " background");
    Rect inner = block.inner(area);
    frame.renderWidget(block, area);

    List<Rect> areas = colorGridAreas(inner);
    for (int i = 0; i < NAMED_COLORS.length && i < areas.size(); i++) {
      Color fg = NAMED_COLORS[i];
      String colorName = fg.toString();
      Paragraph paragraph =
          Paragraph.of(colorName).withStyle(Style.empty().withFg(fg).withBg(bg));
      frame.renderWidget(paragraph, areas.get(i));
    }
  }

  private static void renderBgNamedColors(Frame frame, Color fg, Rect area) {
    Block block = titleBlock("Background colors with " + fg + " foreground");
    Rect inner = block.inner(area);
    frame.renderWidget(block, area);

    List<Rect> areas = colorGridAreas(inner);
    for (int i = 0; i < NAMED_COLORS.length && i < areas.size(); i++) {
      Color bg = NAMED_COLORS[i];
      String colorName = bg.toString();
      Paragraph paragraph =
          Paragraph.of(colorName).withStyle(Style.empty().withFg(fg).withBg(bg));
      frame.renderWidget(paragraph, areas.get(i));
    }
  }

  /// Returns a 2-row × 8-column grid of cells covering `inner` — mirrors the upstream
  /// `vertical([Length(1); 2])` / `horizontal([Ratio(1, 8); 8])` flat_map combination.
  private static List<Rect> colorGridAreas(Rect inner) {
    Layout vertical = repeatedLengthVertical(1, 2);
    Layout horizontal =
        Layout.horizontal(
            new Constraint.Ratio(1, 8),
            new Constraint.Ratio(1, 8),
            new Constraint.Ratio(1, 8),
            new Constraint.Ratio(1, 8),
            new Constraint.Ratio(1, 8),
            new Constraint.Ratio(1, 8),
            new Constraint.Ratio(1, 8),
            new Constraint.Ratio(1, 8));
    List<Rect> out = new ArrayList<>(16);
    for (Rect row : vertical.splitVec(inner)) {
      out.addAll(horizontal.splitVec(row));
    }
    return out;
  }

  private static void renderIndexedColors(Frame frame, Rect area) {
    Block block = titleBlock("Indexed colors");
    Rect inner = block.inner(area);
    frame.renderWidget(block, area);

    Rect[] layout = Layout.vertical(
        new Constraint.Length(1), // 0 - 15
        new Constraint.Length(1), // blank
        new Constraint.Min(6),    // 16 - 123
        new Constraint.Length(1), // blank
        new Constraint.Min(6),    // 124 - 231
        new Constraint.Length(1)  // blank
    ).split(inner);

    Rect[] colorLayout = repeatedLengthHorizontal(5, 16).split(layout[0]);
    for (int i = 0; i < 16; i++) {
      Color color = new Color.Indexed(i);
      String colorIndex = String.format("%02d", i);
      Color bg = i < 1 ? Color.DARK_GRAY : Color.BLACK;
      Line line = Line.from(
          Span.styled(colorIndex, Style.empty().withFg(color).withBg(bg)),
          Span.styled("██", Style.empty().withFg(color).withBg(color)));
      Paragraph paragraph = Paragraph.of(line);
      frame.renderWidget(paragraph, colorLayout[i]);
    }

    // Build the index layout: from layout[2] and layout[4], two rows of three width-27 columns,
    // each split into six height-1 rows, each split into six width-min-4 columns.
    List<Rect> indexLayout = new ArrayList<>(216);
    Rect[] sourceRows = new Rect[] {layout[2], layout[4]};
    Layout colCols =
        Layout.horizontal(
            new Constraint.Length(27),
            new Constraint.Length(27),
            new Constraint.Length(27));
    Layout sixRows = repeatedLengthVertical(1, 6);
    Layout sixCols =
        Layout.horizontal(
            new Constraint.Min(4),
            new Constraint.Min(4),
            new Constraint.Min(4),
            new Constraint.Min(4),
            new Constraint.Min(4),
            new Constraint.Min(4));
    for (Rect row : sourceRows) {
      for (Rect col : colCols.splitVec(row)) {
        for (Rect cellRow : sixRows.splitVec(col)) {
          indexLayout.addAll(sixCols.splitVec(cellRow));
        }
      }
    }

    for (int i = 16; i <= 231; i++) {
      Color color = new Color.Indexed(i);
      String colorIndex = String.format("%03d", i);
      int idx = i - 16;
      if (idx >= indexLayout.size()) {
        break;
      }
      Line line = Line.from(
          Span.styled(colorIndex, Style.empty().withFg(color).withBg(Color.RESET)),
          Span.styled(".", Style.empty().withBg(color).withFg(color)),
          // VHS background-bleed workaround from upstream.
          Span.styled("███", Style.empty().withAddModifier(jatatui.core.style.Modifier.REVERSED)));
      Paragraph paragraph = Paragraph.of(line);
      frame.renderWidget(paragraph, indexLayout.get(idx));
    }
  }

  private static Block titleBlock(String title) {
    return Block.empty()
        .withBorders(Borders.TOP)
        .withTitleAlignment(HorizontalAlignment.Center)
        .withBorderStyle(Style.empty().darkGray())
        .withTitleStyle(Style.reset())
        .withTitle(title);
  }

  private static void renderIndexedGrayscale(Frame frame, Rect area) {
    Rect[] rows = Layout.vertical(
        new Constraint.Length(1), // 232 - 243
        new Constraint.Length(1)  // 244 - 255
    ).split(area);
    Layout twelveCols = repeatedLengthHorizontal(6, 12);
    List<Rect> layout = new ArrayList<>(24);
    for (Rect row : rows) {
      layout.addAll(twelveCols.splitVec(row));
    }

    for (int i = 232; i <= 255; i++) {
      Color color = new Color.Indexed(i);
      String colorIndex = String.format("%03d", i);
      Color bg = i < 244 ? Color.GRAY : Color.BLACK;
      int idx = i - 232;
      if (idx >= layout.size()) {
        break;
      }
      Line line = Line.from(
          Span.styled(colorIndex, Style.empty().withFg(color).withBg(bg)),
          Span.styled("██", Style.empty().withBg(color).withFg(color)),
          // VHS background-bleed workaround from upstream.
          Span.styled("███████",
              Style.empty().withAddModifier(jatatui.core.style.Modifier.REVERSED)));
      Paragraph paragraph = Paragraph.of(line);
      frame.renderWidget(paragraph, layout.get(idx));
    }
  }

  // ---- Helpers ----

  private static Layout repeatedLengthVertical(int length, int count) {
    List<Constraint> cs = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      cs.add(new Constraint.Length(length));
    }
    return Layout.vertical(cs);
  }

  private static Layout repeatedLengthHorizontal(int length, int count) {
    List<Constraint> cs = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      cs.add(new Constraint.Length(length));
    }
    return Layout.horizontal(cs);
  }

}
