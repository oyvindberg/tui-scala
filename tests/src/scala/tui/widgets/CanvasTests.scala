package tui
package widgets

import tui.Symbols
import tui.internal.Ranges
import tui.widgets.canvas.{CanvasWidget, Context, Line}

class CanvasTests extends TuiTest {
  // Helper: render a horizontal + vertical Line through the origin with the given marker.
  private def testMarker(marker: Symbols.Marker, expected: Array[String]): Unit = {
    val area = new Rect(0, 0, 5, 5)
    val cell = Cell.empty()
    cell.setChar('x')
    val buf = Buffer.filled(area, cell)
    val canvas = CanvasWidget
      .empty((ctx: Context) => {
        ctx.draw(new Line(0.0, 0.0, 0.0, 10.0, Color.Reset))
        ctx.draw(new Line(0.0, 0.0, 10.0, 0.0, Color.Reset))
      })
      .withMarker(marker)
      .withXBounds(new Point(0.0, 10.0))
      .withYBounds(new Point(0.0, 10.0))
    canvas.render(area, buf)
    assertBufferEq(buf, Buffer.withLines(expected: _*))
  }

  test("test_dot_marker") {
    testMarker(Symbols.Marker.Dot, Array("•xxxx", "•xxxx", "•xxxx", "•xxxx", "•••••"))
  }

  test("test_block_marker") {
    testMarker(Symbols.Marker.Block, Array("█xxxx", "█xxxx", "█xxxx", "█xxxx", "█████"))
  }

  test("test_bar_marker") {
    testMarker(Symbols.Marker.Bar, Array("▄xxxx", "▄xxxx", "▄xxxx", "▄xxxx", "▄▄▄▄▄"))
  }

  test("test_braille_marker") {
    testMarker(Symbols.Marker.Braille, Array("⡇xxxx", "⡇xxxx", "⡇xxxx", "⡇xxxx", "⣇⣀⣀⣀⣀"))
  }

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
