package jatatui.widgets.canvas;

import jatatui.core.style.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// The `CharGrid` is a grid made up of cells each containing a single character.
///
/// This makes it possible to draw shapes with a resolution of 1x1 dots per cell. This is useful
/// when you want to draw shapes with a low resolution.
///
/// Mirrors the upstream `CharGrid` struct.
final class CharGrid implements Grid {

  private final int width;
  private final int height;

  /// The color of each cell.
  private final Optional<Color>[] cells;

  /// The character to use for every cell — e.g. a block, dot, etc.
  private final String cellChar;

  /// If true, apply the color to the background as well as the foreground. This is used for
  /// `Marker.Block`, so that it will overwrite any previous foreground character, but also leave
  /// a background that can be overlaid with an additional foreground character.
  private final boolean applyColorToBg;

  @SuppressWarnings("unchecked")
  CharGrid(int width, int height, String cellChar, boolean applyColorToBg) {
    this.width = width;
    this.height = height;
    this.cellChar = cellChar;
    this.applyColorToBg = applyColorToBg;
    int length = width * height;
    this.cells = (Optional<Color>[]) new Optional<?>[length];
    for (int i = 0; i < length; i++) {
      this.cells[i] = Optional.empty();
    }
  }

  static CharGrid plain(int width, int height, String cellChar) {
    return new CharGrid(width, height, cellChar, false);
  }

  static CharGrid withColorToBg(int width, int height, String cellChar) {
    return new CharGrid(width, height, cellChar, true);
  }

  @Override
  public Resolution resolution() {
    return new Resolution(width, height);
  }

  @Override
  public Layer save() {
    List<LayerCell> contents = new ArrayList<>(cells.length);
    for (Optional<Color> color : cells) {
      Optional<String> symbol = color.map(c -> cellChar);
      Optional<Color> bg = applyColorToBg ? color : Optional.empty();
      contents.add(new LayerCell(symbol, color, bg));
    }
    return new Layer(contents);
  }

  @Override
  public void reset() {
    for (int i = 0; i < cells.length; i++) {
      cells[i] = Optional.empty();
    }
  }

  @Override
  public void paint(int x, int y, Color color) {
    long indexLong = (long) y * (long) width + (long) x;
    if (indexLong < 0 || indexLong >= cells.length) return;
    cells[(int) indexLong] = Optional.of(color);
  }
}
