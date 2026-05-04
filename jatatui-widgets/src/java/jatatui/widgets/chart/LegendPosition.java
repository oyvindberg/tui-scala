package jatatui.widgets.chart;

import jatatui.core.layout.Rect;
import java.util.Optional;

/// Allows users to specify the position of a legend in a [Chart].
public enum LegendPosition {
  /// Legend is centered on top.
  Top,
  /// Legend is in the top-right corner. This is the **default**.
  TopRight,
  /// Legend is in the top-left corner.
  TopLeft,
  /// Legend is centered on the left.
  Left,
  /// Legend is centered on the right.
  Right,
  /// Legend is centered on the bottom.
  Bottom,
  /// Legend is in the bottom-right corner.
  BottomRight,
  /// Legend is in the bottom-left corner.
  BottomLeft;

  /// Returns the default legend position ([LegendPosition#TopRight]).
  public static LegendPosition defaultPosition() {
    return TopRight;
  }

  /// Computes a [Rect] for the legend, given the chart graph area and the lengths of the X / Y
  /// axis titles. Returns [Optional#empty()] when the legend doesn't fit (i.e. the height is
  /// strictly less than `legendHeight + (xTitleWidth?1:0) + (yTitleWidth?1:0)`).
  Optional<Rect> layout(
      Rect area, int legendWidth, int legendHeight, int xTitleWidth, int yTitleWidth) {
    int heightMargin = area.height() - legendHeight;
    if (xTitleWidth != 0) heightMargin -= 1;
    if (yTitleWidth != 0) heightMargin -= 1;
    if (heightMargin < 0) return Optional.empty();

    int x;
    int y;
    switch (this) {
      case TopRight -> {
        if (legendWidth + yTitleWidth > area.width()) {
          x = area.right() - legendWidth;
          y = area.top() + 1;
        } else {
          x = area.right() - legendWidth;
          y = area.top();
        }
      }
      case TopLeft -> {
        if (yTitleWidth != 0) {
          x = area.left();
          y = area.top() + 1;
        } else {
          x = area.left();
          y = area.top();
        }
      }
      case Top -> {
        int dx = (area.width() - legendWidth) / 2;
        if (area.left() + yTitleWidth > dx) {
          x = area.left() + dx;
          y = area.top() + 1;
        } else {
          x = area.left() + dx;
          y = area.top();
        }
      }
      case Left -> {
        int dy = (area.height() - legendHeight) / 2;
        if (yTitleWidth != 0) dy += 1;
        if (xTitleWidth != 0) dy = Math.max(0, dy - 1);
        x = area.left();
        y = area.top() + dy;
      }
      case Right -> {
        int dy = (area.height() - legendHeight) / 2;
        if (yTitleWidth != 0) dy += 1;
        if (xTitleWidth != 0) dy = Math.max(0, dy - 1);
        x = area.right() - legendWidth;
        y = area.top() + dy;
      }
      case BottomLeft -> {
        if (xTitleWidth + legendWidth > area.width()) {
          x = area.left();
          y = area.bottom() - legendHeight - 1;
        } else {
          x = area.left();
          y = area.bottom() - legendHeight;
        }
      }
      case BottomRight -> {
        if (xTitleWidth != 0) {
          x = area.right() - legendWidth;
          y = area.bottom() - legendHeight - 1;
        } else {
          x = area.right() - legendWidth;
          y = area.bottom() - legendHeight;
        }
      }
      case Bottom -> {
        int dx2 = area.left() + (area.width() - legendWidth) / 2;
        if (dx2 + legendWidth > area.right() - xTitleWidth) {
          x = dx2;
          y = area.bottom() - legendHeight - 1;
        } else {
          x = dx2;
          y = area.bottom() - legendHeight;
        }
      }
      default -> throw new IllegalStateException("unreachable");
    }

    return Optional.of(new Rect(x, y, legendWidth, legendHeight));
  }
}
