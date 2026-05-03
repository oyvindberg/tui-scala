package tui.widgets.canvas;

import tui.Color;
import tui.Point;

public interface Grid {
  int width();

  int height();

  Point resolution();

  void paint(int x, int y, Color color);

  Layer save();

  void reset();
}
