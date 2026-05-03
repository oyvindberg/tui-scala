package tui.widgets.canvas;

import tui.Color;
import tui.Point;
import tui.internal.Ranges;

public final class CharGrid implements Grid {
  public final int width;
  public final int height;
  public final char[] cells;
  public final Color[] colors;
  public final char cellChar;

  public CharGrid(int width, int height, char[] cells, Color[] colors, char cellChar) {
    this.width = width;
    this.height = height;
    this.cells = cells;
    this.colors = colors;
    this.cellChar = cellChar;
  }

  public static CharGrid create(int width, int height, char cellChar) {
    int length = width * height;
    char[] cells = new char[length];
    Color[] colors = new Color[length];
    for (int i = 0; i < length; i++) {
      cells[i] = ' ';
      colors[i] = Color.Reset;
    }
    return new CharGrid(width, height, cells, colors, cellChar);
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
    return new Point((double) width - 1.0, (double) height - 1.0);
  }

  @Override
  public Layer save() {
    return new Layer(new String(cells), colors.clone());
  }

  @Override
  public void reset() {
    Ranges.range(0, cells.length, i -> cells[i] = ' ');
    Ranges.range(0, colors.length, i -> colors[i] = Color.Reset);
  }

  @Override
  public void paint(int x, int y, Color color) {
    int index = y * width + x;
    if (index < cells.length) {
      cells[index] = cellChar;
    }
    if (index < colors.length) {
      colors[index] = color;
    }
  }
}
