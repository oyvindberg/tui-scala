package tui
package widgets

class SparklineTests extends TuiTest {
  test("it_does_not_panic_if_max_is_zero") {
    val widget = SparklineWidget.empty().withData(Array(0, 0, 0))
    val area = new Rect(0, 0, 3, 1)
    val buffer = Buffer.empty(area)
    widget.render(area, buffer)
  }

  test("it_does_not_panic_if_max_is_set_to_zero") {
    val widget = SparklineWidget.empty().withData(Array(0, 1, 2)).withMax(0)
    val area = new Rect(0, 0, 3, 1)
    val buffer = Buffer.empty(area)
    widget.render(area, buffer)
  }
}
