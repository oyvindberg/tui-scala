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
      "  │              ▋43%                │  ",
      "  └──────────────────────────────────┘  ",
      "  ┌Ratio─────────────────────────────┐  ",
      "  │               51%                │  ",
      "  └──────────────────────────────────┘  ",
      "                                        ",
      "                                        "
    )

    Ranges.range(3, 17, (i: Int) => {
      expected
        .get(i, 3)
        .setBg(Color.Red)
        .setFg(Color.Blue)
      ()
    })
    Ranges.range(17, 37, (i: Int) => {
      expected
        .get(i, 3)
        .setBg(Color.Blue)
        .setFg(Color.Red)
      ()
    })

    Ranges.range(3, 20, (i: Int) => {
      expected
        .get(i, 6)
        .setBg(Color.Red)
        .setFg(Color.Blue)
      ()
    })
    Ranges.range(20, 37, (i: Int) => {
      expected
        .get(i, 6)
        .setBg(Color.Blue)
        .setFg(Color.Red)
      ()
    })

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
      "  │               43%                │  ",
      "  └──────────────────────────────────┘  ",
      "  ┌Ratio─────────────────────────────┐  ",
      "  │               21%                │  ",
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
      "│          │",
      "│   43%    │",
      "│          │",
      "└──────────┘"
    )
    // title
    expected.setStyle(new Rect(1, 0, 4, 1), Style.empty().withFg(Color.Red))
    // gauge area
    expected.setStyle(
      new Rect(1, 1, 10, 3),
      Style.empty().withFg(Color.Blue).withBg(Color.Red)
    )
    // filled area
    Ranges.range(1, 4, (y: Int) => {
      expected.setStyle(
        new Rect(1, y, 4, 1),
        // filled style is invert of gauge_style
        Style.empty().withFg(Color.Red).withBg(Color.Blue)
      );
    })
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
