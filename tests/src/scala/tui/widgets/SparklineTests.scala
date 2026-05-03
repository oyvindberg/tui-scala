package tui
package widgets

class SparklineTests extends TuiTest {
  // Helper to render a sparkline to a buffer pre-filled with 'x' so empty cells are visible.
  private def render(widget: SparklineWidget, width: Int): Buffer = {
    val area = new Rect(0, 0, width, 1)
    val cell = Cell.empty()
    cell.setSymbol("x")
    val buffer = Buffer.filled(area, cell)
    widget.render(area, buffer)
    buffer
  }

  test("it_does_not_panic_if_max_is_zero") {
    val widget = SparklineWidget.empty().withData(Array(0, 0, 0))
    val buffer = render(widget, 6)
    assertBufferEq(buffer, Buffer.withLines("   xxx"))
  }

  test("it_does_not_panic_if_max_is_set_to_zero") {
    val widget = SparklineWidget.empty().withData(Array(0, 1, 2)).withMax(0)
    val buffer = render(widget, 6)
    assertBufferEq(buffer, Buffer.withLines("   xxx"))
  }

  test("it_draws") {
    val widget = SparklineWidget.empty().withData(Array(0, 1, 2, 3, 4, 5, 6, 7, 8))
    val buffer = render(widget, 12)
    assertBufferEq(buffer, Buffer.withLines(" ▁▂▃▄▅▆▇█xxx"))
  }
}
