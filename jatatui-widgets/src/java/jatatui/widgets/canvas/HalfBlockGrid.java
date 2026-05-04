package jatatui.widgets.canvas;

import jatatui.core.style.Color;
import jatatui.core.symbols.HalfBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// The `HalfBlockGrid` is a grid made up of cells each containing a half block character.
///
/// In terminals, each character is usually twice as tall as it is wide. Unicode has a couple of
/// vertical half block characters, the upper half block `▀` and lower half block `▄` which take up
/// half the height of a normal character but the full width. Together with an empty space ` ` and a
/// full block `█`, we can effectively double the resolution of a single cell. In addition, because
/// each character can have a foreground and background color, we can control the color of the
/// upper and lower half of each cell. This allows us to draw shapes with a resolution of 1x2
/// "pixels" per cell.
///
/// Mirrors the upstream `HalfBlockGrid` struct.
final class HalfBlockGrid implements Grid {

  private final int width;
  private final int height;

  /// Represents a single color for each "pixel" arranged in column, row order. Outer dimension is
  /// `height * 2`, inner dimension is `width`.
  private final Optional<Color>[][] pixels;

  @SuppressWarnings("unchecked")
  HalfBlockGrid(int width, int height) {
    this.width = width;
    this.height = height;
    int rows = height * 2;
    this.pixels = (Optional<Color>[][]) new Optional<?>[rows][];
    for (int r = 0; r < rows; r++) {
      Optional<Color>[] row = (Optional<Color>[]) new Optional<?>[width];
      for (int c = 0; c < width; c++) {
        row[c] = Optional.empty();
      }
      this.pixels[r] = row;
    }
  }

  @Override
  public Resolution resolution() {
    return new Resolution(width, (double) height * 2.0);
  }

  @Override
  public Layer save() {
    // Iterate vertical pairs (upper_row, lower_row) — flatten to one cell per pair-element.
    int rows = pixels.length;
    int pairs = rows / 2;
    List<LayerCell> contents = new ArrayList<>(pairs * width);
    for (int p = 0; p < pairs; p++) {
      Optional<Color>[] upperRow = pixels[p * 2];
      Optional<Color>[] lowerRow = pixels[p * 2 + 1];
      for (int x = 0; x < width; x++) {
        Optional<Color> upper = upperRow[x];
        Optional<Color> lower = lowerRow[x];
        Optional<String> symbol;
        Optional<Color> fg;
        Optional<Color> bg;
        if (upper.isEmpty() && lower.isEmpty()) {
          symbol = Optional.empty();
          fg = Optional.empty();
          bg = Optional.empty();
        } else if (upper.isEmpty()) {
          symbol = Optional.of(String.valueOf(HalfBlock.LOWER));
          fg = lower;
          bg = Optional.empty();
        } else if (lower.isEmpty()) {
          symbol = Optional.of(String.valueOf(HalfBlock.UPPER));
          fg = upper;
          bg = Optional.empty();
        } else if (upper.get().equals(lower.get())) {
          symbol = Optional.of(String.valueOf(HalfBlock.FULL));
          fg = upper;
          bg = lower;
        } else {
          symbol = Optional.of(String.valueOf(HalfBlock.UPPER));
          fg = upper;
          bg = lower;
        }
        contents.add(new LayerCell(symbol, fg, bg));
      }
    }
    return new Layer(contents);
  }

  @Override
  public void reset() {
    for (Optional<Color>[] row : pixels) {
      for (int c = 0; c < row.length; c++) {
        row[c] = Optional.empty();
      }
    }
  }

  @Override
  public void paint(int x, int y, Color color) {
    if (y < 0 || y >= pixels.length || x < 0 || x >= width) return;
    pixels[y][x] = Optional.of(color);
  }
}
