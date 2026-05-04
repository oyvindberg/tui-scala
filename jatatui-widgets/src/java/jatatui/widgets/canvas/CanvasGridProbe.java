package jatatui.widgets.canvas;

import jatatui.core.style.Color;
import jatatui.core.symbols.Braille;

/// Test-only helper exposing the package-private grids so that integer-overflow sanity tests
/// (mirroring upstream `check_canvas_paint_max` / `check_canvas_paint_overflow`) can construct
/// arbitrarily large grids and try to paint at extreme coordinates without panicking.
///
/// Not part of the public API.
public final class CanvasGridProbe {

  private CanvasGridProbe() {}

  /// Build a [PatternGrid] of `width` x `height` cells using a 2x4 Braille lookup table and call
  /// `paint(x, y, Color.RED)` on it. Returns silently — the contract is "no exception".
  public static void paintPatternMax(
      int width, int height, int patternW, int patternH, int x, int y) {
    PatternGrid grid;
    if (patternW == 2 && patternH == 4) {
      grid = PatternGrid.fromChars(2, 4, width, height, Braille.BRAILLE);
    } else {
      // Fallback: build with a dummy lookup of the right size (2^(W*H) entries).
      int size = 1 << (patternW * patternH);
      String[] table = new String[size];
      for (int i = 0; i < size; i++) {
        table[i] = "?";
      }
      grid = new PatternGrid(patternW, patternH, width, height, table);
    }
    grid.paint(x, y, Color.RED);
  }

  /// Build a [CharGrid] of `width` x `height` cells with cell character `'d'` and call
  /// `paint(x, y, Color.RED)` on it.
  public static void paintCharMax(int width, int height, int x, int y) {
    CharGrid grid = CharGrid.plain(width, height, "d");
    grid.paint(x, y, Color.RED);
  }
}
