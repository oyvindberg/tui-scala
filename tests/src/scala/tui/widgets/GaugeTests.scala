package tui
package widgets

import tui.internal.ranges

class GaugeTests extends TuiTest {

  test("widgets_gauge_renders") {
    val backend = TestBackend(40, 10)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      Layout(direction = Direction.Vertical, margin = Margin(2))(
        BlockWidget(title = Some(Spans.nostyle("Percentage")), borders = Borders.ALL)(
          GaugeWidget(
            ratio = GaugeWidget.Ratio.percent(43),
            useUnicode = true,
            style = Style(bg = Some(Color.Blue), fg = Some(Color.Red))
          )
        ),
        BlockWidget(title = Some(Spans.nostyle("Ratio")), borders = Borders.ALL)(
          GaugeWidget(
            ratio = GaugeWidget.Ratio(0.511_313_934_313_1),
            useUnicode = true,
            style = Style(bg = Some(Color.Blue), fg = Some(Color.Red))
          )
        )
      )
        .render(f.size, f.buffer)
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

    ranges.range(3, 17) { i =>
      expected
        .get(i, 3)
        .setBg(Color.Red)
        .setFg(Color.Blue)
      ()
    }
    ranges.range(17, 37) { i =>
      expected
        .get(i, 3)
        .setBg(Color.Blue)
        .setFg(Color.Red)
      ()
    }

    ranges.range(3, 20) { i =>
      expected
        .get(i, 6)
        .setBg(Color.Red)
        .setFg(Color.Blue)
      ()
    }
    ranges.range(20, 37) { i =>
      expected
        .get(i, 6)
        .setBg(Color.Blue)
        .setFg(Color.Red)
      ()
    }

    assertBuffer(backend, expected)
  }

  test("widgets_gauge_renders_no_unicode") {
    val backend = TestBackend(40, 10)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      Layout(direction = Direction.Vertical, margin = Margin(2))(
        BlockWidget(title = Some(Spans.nostyle("Percentage")), borders = Borders.ALL)(
          GaugeWidget(ratio = GaugeWidget.Ratio.percent(43))
        ),
        BlockWidget(title = Some(Spans.nostyle("Ratio")), borders = Borders.ALL)(
          GaugeWidget(ratio = GaugeWidget.Ratio(0.211_313_934_313_1))
        )
      )
        .render(f.size, f.buffer)
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
      val gauge = BlockWidget(title = Some(Spans.from(Span.styled("Test", Style.DEFAULT.fg(Color.Red)))), borders = Borders.ALL)(
        GaugeWidget(
          style = Style.DEFAULT.fg(Color.Blue).bg(Color.Red),
          ratio = GaugeWidget.Ratio.percent(43),
          label = Some(Span.styled("43%", Style(fg = Some(Color.Green), addModifier = Modifier.BOLD)))
        )
      )
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
    expected.setStyle(new Rect(1, 0, 4, 1), Style(fg = Some(Color.Red)))
    // gauge area
    expected.setStyle(
      new Rect(1, 1, 10, 3),
      Style(fg = Some(Color.Blue), bg = Some(Color.Red))
    )
    // filled area
    ranges.range(1, 4) { y =>
      expected.setStyle(
        new Rect(1, y, 4, 1),
        // filled style is invert of gauge_style
        Style(fg = Some(Color.Red), bg = Some(Color.Blue))
      );
    }
    // label (foreground and modifier from label style)
    expected.setStyle(
      new Rect(4, 2, 1, 1),
      Style(
        fg = Some(Color.Green),
        // "4" is in the filled area so background is gauge_style foreground
        bg = Some(Color.Blue),
        addModifier = Modifier.BOLD
      )
    )
    expected.setStyle(
      new Rect(5, 2, 2, 1),
      Style(
        fg = Some(Color.Green),
        // "3%" is not in the filled area so background is gauge_style background
        bg = Some(Color.Red),
        addModifier = Modifier.BOLD
      )
    )
    assertBuffer(backend, expected)
  }

  test("widgets_gauge_supports_large_labels") {
    val backend = TestBackend(10, 1)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val gauge = GaugeWidget(
        ratio = GaugeWidget.Ratio.percent(43),
        label = Some(Span.nostyle("43333333333333333333333333333%"))
      )
      f.renderWidget(gauge, f.size);
    }

    val expected = Buffer.withLines("4333333333")
    assertBuffer(backend, expected)
  }

  test("widgets_line_gauge_renders") {
    val backend = TestBackend(20, 4)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val gauge0 = LineGaugeWidget(gaugeStyle = Style.DEFAULT.fg(Color.Green).bg(Color.White), ratio = GaugeWidget.Ratio(0.43))
      f.renderWidget(gauge0, Rect(x = 0, y = 0, width = 20, height = 1))
      val gauge = BlockWidget(title = Some(Spans.nostyle("Gauge 2")), borders = Borders.ALL)(
        LineGaugeWidget(
          gaugeStyle = Style(fg = Some(Color.Green)),
          lineSet = symbols.line.THICK,
          ratio = GaugeWidget.Ratio(0.211_313_934_313_1)
        )
      )
      f.renderWidget(gauge, Rect(x = 0, y = 1, width = 20, height = 3));
    }
    val expected = Buffer.withLines(
      "43% ────────────────",
      "┌Gauge 2───────────┐",
      "│21% ━━━━━━━━━━━━━━│",
      "└──────────────────┘"
    )
    ranges.range(4, 10) { col =>
      expected.get(col, 0).setFg(Color.Green)
      ()
    }
    ranges.range(10, 20) { col =>
      expected.get(col, 0).setFg(Color.White)
      ()
    }
    ranges.range(5, 7) { col =>
      expected.get(col, 2).setFg(Color.Green)
      ()
    }
    assertBuffer(backend, expected)
  }
}
