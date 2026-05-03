package tui
package widgets

class BarchartTests extends TuiTest {
  test("widgets_barchart_not_full_below_max_value") {
    val test_case = (expected: Buffer) => {
      val backend = TestBackend(30, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val barchart = BarChartWidget
          .empty()
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withBarWidth(7)
          .withBarGap(0)
          .withData(
            Array(
              new BarChartWidget.LabelValue("empty", 0),
              new BarChartWidget.LabelValue("half", 50),
              new BarChartWidget.LabelValue("almost", 99),
              new BarChartWidget.LabelValue("full", 100)
            )
          )
          .withMax(100)
        f.renderWidget(barchart, f.size);
      }
      assertBuffer(backend, expected)
    }

    // check that bars fill up correctly up to max value
    test_case(
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│              ▇▇▇▇▇▇▇███████│",
        "│              ██████████████│",
        "│              ██████████████│",
        "│       ▄▄▄▄▄▄▄██████████████│",
        "│       █████████████████████│",
        "│       █████████████████████│",
        "│       ██50█████99█████100██│",
        "│empty  half   almost full   │",
        "└────────────────────────────┘"
      )
    )
  }
}
