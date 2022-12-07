package tui
package widgets

import tui.buffer.Buffer
import tui.layout.Rect

class sparklineTests extends TuiTest {
  test("it_does_not_panic_if_max_is_zero") {
    val widget = Sparkline(data = Array(0, 0, 0))
    val area = Rect(0, 0, 3, 1);
    val buffer = Buffer.empty(area);
    widget.render(area, buffer);
  }

  test("it_does_not_panic_if_max_is_set_to_zero") {
    val widget = Sparkline(data = Array(0, 1, 2), max = Some(0))
    val area = Rect(0, 0, 3, 1);
    val buffer = Buffer.empty(area);
    widget.render(area, buffer);
  }
}
