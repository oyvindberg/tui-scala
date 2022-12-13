package tui
package widgets

import test.TestBackend
import tui.internal.ranges

class gaugeTests extends TuiTest {

  test("widgets_gauge_renders") {
    val backend = TestBackend(40, 10)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val chunks = Layout(
        direction = Direction.Vertical,
        margin = Margin(2),
        constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))
      )
        .split(f.size)

      val gauge0 = Gauge(
        block = Some(Block(title = Some(Spans.from("Percentage")), borders = Borders.ALL)),
        gauge_style = Style(bg = Some(Color.Blue), fg = Some(Color.Red)),
        use_unicode = true,
        ratio = Ratio.percent(43)
      )
      f.render_widget(gauge0, chunks(0))
      val gauge = Gauge(
        block = Some(Block(title = Some(Spans.from("Ratio")), borders = Borders.ALL)),
        gauge_style = Style(bg = Some(Color.Blue), fg = Some(Color.Red)),
        use_unicode = true,
        ratio = Ratio(0.511_313_934_313_1)
      )
      f.render_widget(gauge, chunks(1));
    }
    val expected = Buffer.with_lines(
      Array(
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
    )

    ranges.range(3, 17) { i =>
      expected
        .get(i, 3)
        .set_bg(Color.Red)
        .set_fg(Color.Blue);
      ()
    }
    ranges.range(17, 37) { i =>
      expected
        .get(i, 3)
        .set_bg(Color.Blue)
        .set_fg(Color.Red);
      ()
    }

    ranges.range(3, 20) { i =>
      expected
        .get(i, 6)
        .set_bg(Color.Red)
        .set_fg(Color.Blue);
      ()
    }
    ranges.range(20, 37) { i =>
      expected
        .get(i, 6)
        .set_bg(Color.Blue)
        .set_fg(Color.Red);
      ()
    }

    assert_buffer(backend, expected)
  }

  test("widgets_gauge_renders_no_unicode") {
    val backend = TestBackend(40, 10)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val chunks = Layout(direction = Direction.Vertical, margin = Margin(2), constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50)))
        .split(f.size)

      val gauge0 = Gauge(
        block = Some(Block(title = Some(Spans.from("Percentage")), borders = Borders.ALL)),
        ratio = Ratio.percent(43)
      )
      f.render_widget(gauge0, chunks(0))
      val gauge = Gauge(
        block = Some(Block(title = Some(Spans.from("Ratio")), borders = Borders.ALL)),
        ratio = Ratio(0.211_313_934_313_1)
      )
      f.render_widget(gauge, chunks(1));
    }

    val expected = Buffer.with_lines(
      Array(
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
    )
    assert_buffer(backend, expected)
  }

  test("widgets_gauge_applies_styles") {
    val backend = TestBackend(12, 5)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val gauge = Gauge(
        block = Some(Block(title = Some(Spans.from(Span.styled("Test", Style.DEFAULT.fg(Color.Red)))), borders = Borders.ALL)),
        gauge_style = Style.DEFAULT.fg(Color.Blue).bg(Color.Red),
        ratio = Ratio.percent(43),
        label = Some(Span.styled("43%", Style(fg = Some(Color.Green), add_modifier = Modifier.BOLD)))
      )
      f.render_widget(gauge, f.size);
    }

    val expected = Buffer.with_lines(
      Array(
        "┌Test──────┐",
        "│          │",
        "│   43%    │",
        "│          │",
        "└──────────┘"
      )
    )
    // title
    expected.set_style(new Rect(1, 0, 4, 1), Style(fg = Some(Color.Red)))
    // gauge area
    expected.set_style(
      new Rect(1, 1, 10, 3),
      Style(fg = Some(Color.Blue), bg = Some(Color.Red))
    )
    // filled area
    ranges.range(1, 4) { y =>
      expected.set_style(
        new Rect(1, y, 4, 1),
        // filled style is invert of gauge_style
        Style(fg = Some(Color.Red), bg = Some(Color.Blue))
      );
    }
    // label (foreground and modifier from label style)
    expected.set_style(
      new Rect(4, 2, 1, 1),
      Style(
        fg = Some(Color.Green),
        // "4" is in the filled area so background is gauge_style foreground
        bg = Some(Color.Blue),
        add_modifier = Modifier.BOLD
      )
    )
    expected.set_style(
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
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val gauge = Gauge(
        ratio = Ratio.percent(43),
        label = Some(Span.from("43333333333333333333333333333%"))
      )
      f.render_widget(gauge, f.size);
    }

    val expected = Buffer.with_lines(Array("4333333333"))
    assert_buffer(backend, expected)
  }

  test("widgets_line_gauge_renders") {
    val backend = TestBackend(20, 4)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val gauge0 = LineGauge(gauge_style = Style.DEFAULT.fg(Color.Green).bg(Color.White), ratio = Ratio(0.43))
      f.render_widget(gauge0, Rect(x = 0, y = 0, width = 20, height = 1))
      val gauge = LineGauge(
        block = Some(Block(title = Some(Spans.from("Gauge 2")), borders = Borders.ALL)),
        gauge_style = Style(fg = Some(Color.Green)),
        line_set = symbols.line.THICK,
        ratio = Ratio(0.211_313_934_313_1)
      )
      f.render_widget(gauge, Rect(x = 0, y = 1, width = 20, height = 3));
    }
    val expected = Buffer.with_lines(
      Array(
        "43% ────────────────",
        "┌Gauge 2───────────┐",
        "│21% ━━━━━━━━━━━━━━│",
        "└──────────────────┘"
      )
    )
    ranges.range(4, 10) { col =>
      expected.get(col, 0).set_fg(Color.Green)
      ()
    }
    ranges.range(10, 20) { col =>
      expected.get(col, 0).set_fg(Color.White)
      ()
    }
    ranges.range(5, 7) { col =>
      expected.get(col, 2).set_fg(Color.Green)
      ()
    }
    assert_buffer(backend, expected)
  }
}
