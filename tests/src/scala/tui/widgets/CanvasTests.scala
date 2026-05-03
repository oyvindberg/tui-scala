package tui
package widgets

import tui.internal.Ranges
import tui.widgets.canvas.{CanvasWidget, Context}

class CanvasTests extends TuiTest {
  test("widgets_canvas_draw_labels") {
    val backend = TestBackend(5, 5)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val label = "test"
      val canvas = CanvasWidget
        .empty((ctx: Context) => {
          ctx.print(0.0, 0.0, Spans.from(Span.styled(label, Style.empty().withFg(Color.Blue))))
        })
        .withBackgroundColor(Color.Yellow)
        .withXBounds(new Point(0.0, 5.0))
        .withYBounds(new Point(0.0, 5.0))
      f.renderWidget(canvas, f.size);
    }

    val expected = Buffer.withLines("    ", "    ", "     ", "     ", "test ")
    Ranges.range(0, 5, (row: Int) => {
      Ranges.range(0, 5, (col: Int) => {
        expected.get(col, row).setBg(Color.Yellow)
        ()
      })
    })
    Ranges.range(0, 4, (col: Int) => {
      expected.get(col, 4).setFg(Color.Blue)
      ()
    })
    assertBuffer(backend, expected)
  }
}
