package tui
package widgets

import tui.internal.ranges

class GaugeTests extends TuiTest {

  test("widgets_gauge_renders") {
    val backend = TestBackend(40, 10)
    val terminal = Terminal(backend)
    terminal.draw { f =>
      val chunks = Layout(
        direction = Direction.Vertical,
        margin = Margin(2),
        constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))
      )
        .split(f.size)

      val gauge0 = GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Percentage")), borders = Borders.ALL)),
        gauge_style = Style(bg = Some(Color.Blue), fg = Some(Color.Red)),
        use_unicode = true,
        ratio = GaugeWidget.Ratio.percent(43)
      )
      f.render_widget(gauge0, chunks(0))
      val gauge = GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Ratio")), borders = Borders.ALL)),
        gauge_style = Style(bg = Some(Color.Blue), fg = Some(Color.Red)),
        use_unicode = true,
        ratio = GaugeWidget.Ratio(0.511_313_934_313_1)
      )
      f.render_widget(gauge, chunks(1));
    }
    val expected = Buffer.with_lines(
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
      expected.update(i, 3)(_.withBg(Color.Red).withFg(Color.Blue))
    }
    ranges.range(17, 37) { i =>
      expected.update(i, 3)(_.withBg(Color.Blue).withFg(Color.Red))
    }

    ranges.range(3, 20) { i =>
      expected.update(i, 6)(_.withBg(Color.Red).withFg(Color.Blue))
    }
    ranges.range(20, 37) { i =>
      expected.update(i, 6)(_.withBg(Color.Blue).withFg(Color.Red))
    }

    assert_buffer(backend, expected)
  }

  test("widgets_gauge_renders_no_unicode") {
    val backend = TestBackend(40, 10)
    val terminal = Terminal(backend)

    terminal.draw { f =>
      val chunks = Layout(direction = Direction.Vertical, margin = Margin(2), constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50)))
        .split(f.size)

      val gauge0 = GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Percentage")), borders = Borders.ALL)),
        ratio = GaugeWidget.Ratio.percent(43)
      )
      f.render_widget(gauge0, chunks(0))
      val gauge = GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Ratio")), borders = Borders.ALL)),
        ratio = GaugeWidget.Ratio(0.211_313_934_313_1)
      )
      f.render_widget(gauge, chunks(1));
    }

    val expected = Buffer.with_lines(
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
    assert_buffer(backend, expected)
  }

  test("widgets_gauge_applies_styles") {
    val backend = TestBackend(12, 5)
    val terminal = Terminal(backend)

    terminal.draw { f =>
      val gauge = GaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.from(Span.styled("Test", Style.DEFAULT.fg(Color.Red)))), borders = Borders.ALL)),
        ratio = GaugeWidget.Ratio.percent(43),
        label = Some(Span.styled("43%", Style(fg = Some(Color.Green), add_modifier = Modifier.BOLD))),
        gauge_style = Style.DEFAULT.fg(Color.Blue).bg(Color.Red)
      )
      f.render_widget(gauge, f.size);
    }

    val expected = Buffer.with_lines(
      "┌Test──────┐",
      "│          │",
      "│   43%    │",
      "│          │",
      "└──────────┘"
    )
    // title
    expected.update_style(new Rect(1, 0, 4, 1), Style(fg = Some(Color.Red)))
    // gauge area
    expected.update_style(
      new Rect(1, 1, 10, 3),
      Style(fg = Some(Color.Blue), bg = Some(Color.Red))
    )
    // filled area
    ranges.range(1, 4) { y =>
      expected.update_style(
        new Rect(1, y, 4, 1),
        // filled style is invert of gauge_style
        Style(fg = Some(Color.Red), bg = Some(Color.Blue))
      );
    }
    // label (foreground and modifier from label style)
    expected.update_style(
      new Rect(4, 2, 1, 1),
      Style(
        fg = Some(Color.Green),
        // "4" is in the filled area so background is gauge_style foreground
        bg = Some(Color.Blue),
        add_modifier = Modifier.BOLD
      )
    )
    expected.update_style(
      new Rect(5, 2, 2, 1),
      Style(
        fg = Some(Color.Green),
        // "3%" is not in the filled area so background is gauge_style background
        bg = Some(Color.Red),
        add_modifier = Modifier.BOLD
      )
    )
    assert_buffer(backend, expected)
  }

  test("widgets_gauge_supports_large_labels") {
    val backend = TestBackend(10, 1)
    val terminal = Terminal(backend)

    terminal.draw { f =>
      val gauge = GaugeWidget(
        ratio = GaugeWidget.Ratio.percent(43),
        label = Some(Span.nostyle("43333333333333333333333333333%"))
      )
      f.render_widget(gauge, f.size);
    }

    val expected = Buffer.with_lines("4333333333")
    assert_buffer(backend, expected)
  }

  test("widgets_line_gauge_renders") {
    val backend = TestBackend(20, 4)
    val terminal = Terminal(backend)
    terminal.draw { f =>
      val gauge0 = LineGaugeWidget(gauge_style = Style.DEFAULT.fg(Color.Green).bg(Color.White), ratio = GaugeWidget.Ratio(0.43))
      f.render_widget(gauge0, Rect(x = 0, y = 0, width = 20, height = 1))
      val gauge = LineGaugeWidget(
        block = Some(BlockWidget(title = Some(Spans.nostyle("Gauge 2")), borders = Borders.ALL)),
        gauge_style = Style(fg = Some(Color.Green)),
        line_set = symbols.line.THICK,
        ratio = GaugeWidget.Ratio(0.211_313_934_313_1)
      )
      f.render_widget(gauge, Rect(x = 0, y = 1, width = 20, height = 3));
    }
    val expected = Buffer.with_lines(
      "43% ────────────────",
      "┌Gauge 2───────────┐",
      "│21% ━━━━━━━━━━━━━━│",
      "└──────────────────┘"
    )
    ranges.range(4, 10) { col =>
      expected.update(col, 0)(_.withFg(Color.Green))
    }
    ranges.range(10, 20) { col =>
      expected.update(col, 0)(_.withFg(Color.White))
      ()
    }
    ranges.range(5, 7) { col =>
      expected.update(col, 2)(_.withFg(Color.Green))
      ()
    }
    assert_buffer(backend, expected)
  }
}
