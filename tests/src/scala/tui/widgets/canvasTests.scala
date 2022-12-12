package tui
package widgets

import tui.backend.test.TestBackend
import tui.buffer.Buffer
import tui.internal.ranges
import tui.terminal.Terminal
import tui.text.{Span, Spans}
import tui.widgets.canvas.Canvas

class canvasTests extends TuiTest {
  test("widgets_canvas_draw_labels") {
    val backend = TestBackend(5, 5);
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val label = "test";
      val canvas = Canvas(
        background_color = Color.Yellow,
        x_bounds = Point(0.0, 5.0),
        y_bounds = Point(0.0, 5.0),
        painter = Some(_.print(0.0, 0.0, Spans.from(Span.styled(label, Style(fg = Some(Color.Blue))))))
      )
      f.render_widget(canvas, f.size);
    }

    val expected = Buffer.with_lines(Array("    ", "    ", "     ", "     ", "test "));
    ranges.range(0, 5) { row =>
      ranges.range(0, 5) { col =>
        expected.get(col, row).set_bg(Color.Yellow);
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
