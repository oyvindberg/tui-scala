package tui
package widgets

import tui.internal.Ranges

class GaugeTests extends TuiTest {

  test("widgets_gauge_renders") {
    val backend = TestBackend(40, 10)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val chunks = new Layout(
        Direction.Vertical,
        Margin.of(2),
        Array[Constraint](new Constraint.Percentage(50), new Constraint.Percentage(50)),
        false
      )
        .split(f.size)

      val gauge0 = GaugeWidget
        .empty()
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Percentage")).withBorders(Borders.ALL))
        .withGaugeStyle(Style.empty().withBg(Color.Blue).withFg(Color.Red))
        .withUseUnicode(true)
        .withRatio(GaugeWidget.Ratio.percent(43))
      f.renderWidget(gauge0, chunks(0))
      val gauge = GaugeWidget
        .empty()
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Ratio")).withBorders(Borders.ALL))
        .withGaugeStyle(Style.empty().withBg(Color.Blue).withFg(Color.Red))
        .withUseUnicode(true)
        .withRatio(new GaugeWidget.Ratio(0.511_313_934_313_1))
      f.renderWidget(gauge, chunks(1));
    }
    val expected = Buffer.withLines(
      "                                        ",
      "                                        ",
      "  ┌Percentage────────────────────────┐  ",
      "  │██████████████▋43%                │  ",
      "  └──────────────────────────────────┘  ",
      "  ┌Ratio─────────────────────────────┐  ",
      "  │███████████████51%                │  ",
      "  └──────────────────────────────────┘  ",
      "                                        ",
      "                                        "
    )
    // First gauge (43% with unicode): whole row gets gauge_style applied.
    expected.setStyle(new Rect(3, 3, 34, 1), Style.empty().withFg(Color.Red).withBg(Color.Blue))
    // Second gauge (51% with unicode): the "5" and "1" of the label happen to fall inside the
    // filled portion, so they have their fg/bg swapped; the rest of the row is plain gauge style.
    expected.setStyle(new Rect(3, 6, 15, 1), Style.empty().withFg(Color.Red).withBg(Color.Blue))
    expected.setStyle(new Rect(18, 6, 2, 1), Style.empty().withFg(Color.Blue).withBg(Color.Red))
    expected.setStyle(new Rect(20, 6, 17, 1), Style.empty().withFg(Color.Red).withBg(Color.Blue))

    assertBuffer(backend, expected)
  }

  test("widgets_gauge_renders_no_unicode") {
    val backend = TestBackend(40, 10)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val chunks = new Layout(
        Direction.Vertical,
        Margin.of(2),
        Array[Constraint](new Constraint.Percentage(50), new Constraint.Percentage(50)),
        false
      ).split(f.size)

      val gauge0 = GaugeWidget
        .empty()
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Percentage")).withBorders(Borders.ALL))
        .withRatio(GaugeWidget.Ratio.percent(43))
      f.renderWidget(gauge0, chunks(0))
      val gauge = GaugeWidget
        .empty()
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Ratio")).withBorders(Borders.ALL))
        .withRatio(new GaugeWidget.Ratio(0.211_313_934_313_1))
      f.renderWidget(gauge, chunks(1));
    }

    val expected = Buffer.withLines(
      "                                        ",
      "                                        ",
      "  ┌Percentage────────────────────────┐  ",
      "  │███████████████43%                │  ",
      "  └──────────────────────────────────┘  ",
      "  ┌Ratio─────────────────────────────┐  ",
      "  │███████        21%                │  ",
      "  └──────────────────────────────────┘  ",
      "                                        ",
      "                                        "
    )
    assertBuffer(backend, expected)
  }

  test("widgets_gauge_applies_styles") {
    val backend = TestBackend(12, 5)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val gauge = GaugeWidget
        .empty()
        .withBlock(
          BlockWidget
            .empty()
            .withTitle(Spans.from(Span.styled("Test", Style.DEFAULT.withFg(Color.Red))))
            .withBorders(Borders.ALL)
        )
        .withGaugeStyle(Style.DEFAULT.withFg(Color.Blue).withBg(Color.Red))
        .withRatio(GaugeWidget.Ratio.percent(43))
        .withLabel(Span.styled("43%", Style.empty().withFg(Color.Green).withAddModifier(Modifier.BOLD)))
      f.renderWidget(gauge, f.size);
    }

    val expected = Buffer.withLines(
      "┌Test──────┐",
      "│████      │",
      "│███43%    │",
      "│████      │",
      "└──────────┘"
    )
    // title
    expected.setStyle(new Rect(1, 0, 4, 1), Style.empty().withFg(Color.Red))
    // gauge area
    expected.setStyle(
      new Rect(1, 1, 10, 3),
      Style.empty().withFg(Color.Blue).withBg(Color.Red)
    )
    // filled area: with the v0.24.0 fix, the filled cells now use the FULL block character
    // and keep the gauge_style colors as-is (no fg/bg swap).
    expected.setStyle(
      new Rect(1, 1, 4, 3),
      Style.empty().withFg(Color.Blue).withBg(Color.Red)
    )
    // label (foreground and modifier from label style)
    expected.setStyle(
      new Rect(4, 2, 1, 1),
      Style.empty()
        .withFg(Color.Green)
        // "4" is in the filled area so background is gauge_style foreground
        .withBg(Color.Blue)
        .withAddModifier(Modifier.BOLD)
    )
    expected.setStyle(
      new Rect(5, 2, 2, 1),
      Style.empty()
        .withFg(Color.Green)
        // "3%" is not in the filled area so background is gauge_style background
        .withBg(Color.Red)
        .withAddModifier(Modifier.BOLD)
    )
    assertBuffer(backend, expected)
  }

  test("widgets_gauge_supports_large_labels") {
    val backend = TestBackend(10, 1)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val gauge = GaugeWidget
        .empty()
        .withRatio(GaugeWidget.Ratio.percent(43))
        .withLabel(Span.nostyle("43333333333333333333333333333%"))
      f.renderWidget(gauge, f.size);
    }

    val expected = Buffer.withLines("4333333333")
    assertBuffer(backend, expected)
  }

  test("widgets_line_gauge_renders") {
    val backend = TestBackend(20, 4)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val gauge0 = LineGaugeWidget
        .empty()
        .withGaugeStyle(Style.DEFAULT.withFg(Color.Green).withBg(Color.White))
        .withRatio(new GaugeWidget.Ratio(0.43))
      f.renderWidget(gauge0, new Rect(0, 0, 20, 1))
      val gauge = LineGaugeWidget
        .empty()
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Gauge 2")).withBorders(Borders.ALL))
        .withGaugeStyle(Style.empty().withFg(Color.Green))
        .withLineSet(Symbols.line.THICK)
        .withRatio(new GaugeWidget.Ratio(0.211_313_934_313_1))
      f.renderWidget(gauge, new Rect(0, 1, 20, 3));
    }
    val expected = Buffer.withLines(
      "43% ────────────────",
      "┌Gauge 2───────────┐",
      "│21% ━━━━━━━━━━━━━━│",
      "└──────────────────┘"
    )
    Ranges.range(4, 10, (col: Int) => {
      expected.get(col, 0).setFg(Color.Green)
      ()
    })
    Ranges.range(10, 20, (col: Int) => {
      expected.get(col, 0).setFg(Color.White)
      ()
    })
    Ranges.range(5, 7, (col: Int) => {
      expected.get(col, 2).setFg(Color.Green)
      ()
    })
    assertBuffer(backend, expected)
  }
}
