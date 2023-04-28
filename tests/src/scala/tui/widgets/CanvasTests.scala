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
      val canvas = CanvasWidget(backgroundColor = Color.Yellow, xBounds = Point(0.0, 5.0), yBounds = Point(0.0, 5.0)) { ctx =>
        ctx.print(0.0, 0.0, Spans.from(Span.styled(label, Style(fg = Some(Color.Blue)))))
      }
      f.renderWidget(canvas, f.size);
    }

    val expected = Buffer.withLines("    ", "    ", "     ", "     ", "test ")
    ranges.range(0, 5) { row =>
      ranges.range(0, 5) { col =>
        expected.get(col, row).setBg(Color.Yellow)
        ()
      }
    }
    ranges.range(0, 4) { col =>
      expected.get(col, 4).setFg(Color.Blue)
      ()
    }
    assertBuffer(backend, expected)
  }
}
