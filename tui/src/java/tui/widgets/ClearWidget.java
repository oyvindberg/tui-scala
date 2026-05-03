package tui.widgets;

import tui.Buffer;
import tui.Rect;
import tui.Widget;
import tui.internal.Ranges;

/// A widget to clear/reset a certain area to allow overdrawing (e.g. for popups).
public final class ClearWidget implements Widget {
  public static final ClearWidget INSTANCE = new ClearWidget();

  private ClearWidget() {}

  @Override
  public void render(Rect area, Buffer buf) {
    Ranges.range(area.left(), area.right(), x ->
        Ranges.range(area.top(), area.bottom(), y ->
            buf.get(x, y).reset()));
  }
}
