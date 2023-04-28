package tui
package widgets

import tui.internal.ranges

/** A widget to clear/reset a certain area to allow overdrawing (e.g. for popups).
  *
  * This widget *cannot be used to clear the terminal on the first render* as `tui` assumes the render area is empty. Use `Terminal.clear` instead.
  */
case object ClearWidget extends Widget {
  override def render(area: Rect, buf: Buffer): Unit =
    ranges.range(area.left, area.right) { x =>
      ranges.range(area.top, area.bottom) { y =>
        buf.get(x, y).reset()
      }
    }
}
