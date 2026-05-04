package jatatui.widgets.canvas;

import jatatui.core.style.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// The `PatternGrid` is a grid made up of cells each containing a `W`x`H` pattern character.
///
/// This makes it possible to draw shapes with a resolution of e.g. 2x4 (Braille or unicode octant)
/// per cell.
///
/// Font support for the relevant pattern character is required. If your terminal or font does not
/// support the relevant unicode block, you will see unicode replacement characters (`�`) instead.
///
/// This grid type only supports a single foreground color for each `W`x`H` pattern character.
/// There is no way to set the individual color of each pseudo-pixel.
///
/// Mirrors the upstream `PatternGrid<const W: usize, const H: usize>`. In Java the const generic
/// parameters are passed as constructor arguments and the lookup table is supplied as a
/// `String[]` so that supplementary-plane code points (octant/sextant characters) fit. Callers
/// that have a `char[]` (Braille, Quadrant) should adapt via [#fromChars].
final class PatternGrid implements Grid {

  /// Width of a single pattern cell in dots (e.g. 2).
  private final int patternW;

  /// Height of a single pattern cell in dots (e.g. 4).
  private final int patternH;

  /// Width of the grid in number of terminal columns.
  private final int width;

  /// Height of the grid in number of terminal rows.
  private final int height;

  /// Pattern (lower 8 bits) for each cell, in row-major order.
  private final int[] patterns;

  /// Optional foreground color per cell (parallel to [#patterns]).
  private final Optional<Color>[] colors;

  /// Lookup table mapping pattern index (0..2^(W*H)-1) to a single-grapheme String.
  private final String[] charTable;

  @SuppressWarnings("unchecked")
  PatternGrid(int patternW, int patternH, int width, int height, String[] charTable) {
    if (patternW * patternH > 8) {
      throw new IllegalArgumentException(
          "PatternGrid pattern (" + patternW + "x" + patternH + ") does not fit in 8 bits");
    }
    this.patternW = patternW;
    this.patternH = patternH;
    this.width = width;
    this.height = height;
    int length = width * height;
    this.patterns = new int[length];
    this.colors = (Optional<Color>[]) new Optional<?>[length];
    for (int i = 0; i < length; i++) {
      this.colors[i] = Optional.empty();
    }
    this.charTable = charTable;
  }

  /// Build a [PatternGrid] from a `char[]` lookup table — used for Braille and Quadrant grids
  /// whose lookup characters fit in a single Java `char`.
  static PatternGrid fromChars(int patternW, int patternH, int width, int height, char[] table) {
    String[] strings = new String[table.length];
    for (int i = 0; i < table.length; i++) {
      strings[i] = String.valueOf(table[i]);
    }
    return new PatternGrid(patternW, patternH, width, height, strings);
  }

  @Override
  public Resolution resolution() {
    return new Resolution((double) width * patternW, (double) height * patternH);
  }

  @Override
  public Layer save() {
    List<LayerCell> contents = new ArrayList<>(patterns.length);
    for (int i = 0; i < patterns.length; i++) {
      int idx = patterns[i];
      Optional<String> symbol;
      if (idx == 0) {
        // Skip rendering blank patterns to allow layers underneath to show through.
        symbol = Optional.empty();
      } else {
        symbol = Optional.of(charTable[idx]);
      }
      // Patterns only affect foreground.
      contents.add(new LayerCell(symbol, colors[i], Optional.empty()));
    }
    return new Layer(contents);
  }

  @Override
  public void reset() {
    for (int i = 0; i < patterns.length; i++) {
      patterns[i] = 0;
      colors[i] = Optional.empty();
    }
  }

  @Override
  public void paint(int x, int y, Color color) {
    // saturating_div / saturating_mul / saturating_add semantics: x and y are non-negative ints
    // here. We need to guard against overflow — Java ints can overflow silently.
    int rowIdx = y / patternH;
    long indexLong = (long) rowIdx * (long) width + (long) (x / patternW);
    if (indexLong < 0 || indexLong >= patterns.length) return;
    int index = (int) indexLong;
    int bit = (x % patternW) + patternW * (y % patternH);
    patterns[index] |= (1 << bit);
    colors[index] = Optional.of(color);
  }
}
