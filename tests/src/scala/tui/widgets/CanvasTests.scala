package tui
package widgets

import tui.internal.ranges
import tui.widgets.canvas.CanvasWidget

class CanvasTests extends TuiTest {
  test("widgets_canvas_draw_labels") {
    val backend = TestBackend(5, 5)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val label = "test"
      val canvas = CanvasWidget(
        background_color = Color.Yellow,
        x_bounds = Point(0.0, 5.0),
        y_bounds = Point(0.0, 5.0),
        painter = Some(_.print(0.0, 0.0, Spans.from(Span.styled(label, Style(fg = Some(Color.Blue))))))
      )
      f.render_widget(canvas, f.size);
    }

    val expected = Buffer.with_lines("    ", "    ", "     ", "     ", "test ")
    ranges.range(0, 5) { row =>
      ranges.range(0, 5) { col =>
        expected.get(col, row).set_bg(Color.Yellow)
        ()
      }
    }
    ranges.range(0, 4) { col =>
      expected.get(col, 4).set_fg(Color.Blue)
      ()
    }
    assert_buffer(backend, expected)
  }
}
