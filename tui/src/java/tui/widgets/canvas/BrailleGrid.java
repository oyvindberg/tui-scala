package tui.widgets.canvas;

import tui.Color;
import tui.Point;
import tui.Symbols;
import tui.internal.Ranges;

public final class BrailleGrid implements Grid {
  public final int width;
  public final int height;
  public final int[] cells;
  public final Color[] colors;

  public BrailleGrid(int width, int height, int[] cells, Color[] colors) {
    this.width = width;
    this.height = height;
    this.cells = cells;
    this.colors = colors;
  }

  public static BrailleGrid create(int width, int height) {
    int length = width * height;
    int[] cells = new int[length];
    Color[] colors = new Color[length];
    for (int i = 0; i < length; i++) {
      cells[i] = Symbols.braille.BLANK;
      colors[i] = Color.Reset;
    }
    return new BrailleGrid(width, height, cells, colors);
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public Point resolution() {
    return new Point((double) width * 2.0 - 1.0, (double) height * 4.0 - 1.0);
  }

  @Override
  public Layer save() {
    int[] cps = new int[cells.length];
    System.arraycopy(cells, 0, cps, 0, cells.length);
    String s = new String(cps, 0, cps.length);
    return new Layer(s, colors.clone());
  }

  @Override
  public void reset() {
    Ranges.range(0, cells.length, i -> cells[i] = Symbols.braille.BLANK);
    Ranges.range(0, colors.length, i -> colors[i] = Color.Reset);
  }

  @Override
  public void paint(int x, int y, Color color) {
    int index = y / 4 * width + x / 2;
    if (index < cells.length) {
      int c = cells[index];
      int[] chosenDots = Symbols.braille.DOTS[y % 4];
      int chosenDot = (x % 2 == 0) ? chosenDots[0] : chosenDots[1];
      int newC = c | chosenDot;
      cells[index] = newC;
    }
    if (index < colors.length) {
      colors[index] = color;
    }
  }
}
