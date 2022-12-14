package tui
package widgets

class BarchartTests extends TuiTest {
  test("widgets_barchart_not_full_below_max_value") {
    val test_case = (expected: Buffer) => {
      val backend = TestBackend(30, 10)
      val terminal = Terminal(backend)

      terminal.draw { f =>
        val barchart = BarChartWidget(
          block = Some(BlockWidget(borders = Borders.ALL)),
          bar_width = 7,
          bar_gap = 0,
          data = Array(("empty", 0), ("half", 50), ("almost", 99), ("full", 100)),
          max = Some(100)
        )
        f.render_widget(barchart, f.size);
      }
      assert_buffer(backend, expected)
    }

    // check that bars fill up correctly up to max value
    test_case(
      Buffer.with_lines(
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
