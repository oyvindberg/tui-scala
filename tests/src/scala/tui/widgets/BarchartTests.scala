package tui
package widgets

class BarchartTests extends TuiTest {
  test("widgets_barchart_not_full_below_max_value") {
    val test_case = (expected: Buffer) => {
      val backend = TestBackend(30, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val barchart = BarChartWidget(
          block = Some(BlockWidget(borders = Borders.ALL)),
          barWidth = 7,
          barGap = 0,
          data = Array(("empty", 0), ("half", 50), ("almost", 99), ("full", 100)),
          max = Some(100)
        )
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
