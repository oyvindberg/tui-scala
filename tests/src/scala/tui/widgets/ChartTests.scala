package tui
package widgets

import tui.internal.ranges

class ChartTests extends TuiTest {
  case class LegendTestCase(
      chart_area: Rect,
      hidden_legend_constraints: (Constraint, Constraint),
      legend_area: Option[Rect]
  )

  test("it_should_hide_the_legend") {
    val data = Array(Point(0.0, 5.0), Point(1.0, 6.0), Point(3.0, 7.0))
    val cases = Array(
      LegendTestCase(
        chart_area = Rect(0, 0, 100, 100),
        hidden_legend_constraints = (Constraint.Ratio(1, 4), Constraint.Ratio(1, 4)),
        legend_area = Some(Rect(88, 0, 12, 12))
      ),
      LegendTestCase(
        chart_area = Rect(0, 0, 100, 100),
        hidden_legend_constraints = (Constraint.Ratio(1, 10), Constraint.Ratio(1, 4)),
        legend_area = None
      )
    )

    cases.foreach { c =>
      val datasets = Range(0, 10).map { i =>
        val name = s"Dataset #$i"
        ChartWidget.Dataset(name = name, data = data)
      }.toArray

      val chart = ChartWidget(
        datasets = datasets,
        x_axis = ChartWidget.Axis(title = Some(Spans.nostyle("X axis"))),
        y_axis = ChartWidget.Axis(title = Some(Spans.nostyle("Y axis"))),
        hidden_legend_constraints = c.hidden_legend_constraints
      )
      val layout = chart.layout(c.chart_area);
      assert_eq(layout.legend_area, c.legend_area);
    }
  }

  def create_labels(labels: Array[String]): Array[Span] =
    labels.map(Span.nostyle)

  def axis_test_case(width: Int, height: Int, x_axis: ChartWidget.Axis, y_axis: ChartWidget.Axis, lines: Array[String]): Unit = {
    val backend = TestBackend(width, height);
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val chart = ChartWidget(datasets = Array.empty, x_axis = x_axis, y_axis = y_axis)
      f.render_widget(chart, f.size);
    }
    val expected = Buffer.with_lines(lines);
    assert_buffer(backend, expected)
  }

  test("widgets_chart_can_render_on_small_areas") {
    def test_case(width: Int, height: Int): CompletedFrame = {
      val backend = TestBackend(width, height);
      val terminal = Terminal.init(backend)
      terminal.draw { f =>
        val datasets = Array(ChartWidget.Dataset(marker = symbols.Marker.Braille, style = Style.DEFAULT.fg(Color.Magenta), data = Array(Point.Zero)));

        val chart = ChartWidget(
          datasets = datasets,
          block = Some(BlockWidget(title = Some(Spans.nostyle("Plot")), borders = Borders.ALL)),
          x_axis = ChartWidget.Axis(bounds = Point.Zero, labels = Some(create_labels(Array("0.0", "1.0")))),
          y_axis = ChartWidget.Axis(bounds = Point.Zero, labels = Some(create_labels(Array("1.0", "0.0"))))
        )
        f.render_widget(chart, f.size);
      }
    };
    test_case(0, 0);
    test_case(0, 1);
    test_case(1, 0);
    test_case(1, 1);
    test_case(2, 2);
  }

  test("widgets_chart_handles_long_labels") {
    def test_case(x_labels: Option[(String, String)], y_labels: Option[(String, String)], x_alignment: Alignment, lines: Array[String]) = {
      val x_axis =
        x_labels.foldLeft(ChartWidget.Axis(bounds = Point(0.0, 1.0))) { case (acc, (left_label, right_label)) =>
          acc.copy(
            labels = Some(Array(Span.nostyle(left_label), Span.nostyle(right_label))),
            labels_alignment = x_alignment
          )
        };

      val y_axis =
        y_labels.foldLeft(ChartWidget.Axis(bounds = Point(0.0, 1.0))) { case (acc, (left_label, right_label)) =>
          acc.copy(labels = Some(Array(Span.nostyle(left_label), Span.nostyle(right_label))))
        };

      axis_test_case(10, 5, x_axis, y_axis, lines);
    };

    test_case(
      Some(("AAAA", "B")),
      None,
      Alignment.Left,
      Array(
        "          ",
        "          ",
        "          ",
        "   ───────",
        "AAA      B"
      )
    );
    test_case(
      Some(("A", "BBBB")),
      None,
      Alignment.Left,
      Array(
        "          ",
        "          ",
        "          ",
        " ─────────",
        "A     BBBB"
      )
    );
    test_case(
      Some(("AAAAAAAAAAA", "B")),
      None,
      Alignment.Left,
      Array(
        "          ",
        "          ",
        "          ",
        "   ───────",
        "AAA      B"
      )
    );
    test_case(
      Some(("A", "B")),
      Some(("CCCCCCC", "D")),
      Alignment.Left,
      Array(
        "D  │      ",
        "   │      ",
        "CCC│      ",
        "   └──────",
        "   A     B"
      )
    );
    test_case(
      Some(("AAAAAAAAAA", "B")),
      Some(("C", "D")),
      Alignment.Center,
      Array(
        "D  │      ",
        "   │      ",
        "C  │      ",
        "   └──────",
        "AAAAAAA  B"
      )
    );
    test_case(
      Some(("AAAAAAA", "B")),
      Some(("C", "D")),
      Alignment.Right,
      Array(
        "D│        ",
        " │        ",
        "C│        ",
        " └────────",
        " AAAAA   B"
      )
    );
    test_case(
      Some(("AAAAAAA", "BBBBBBB")),
      Some(("C", "D")),
      Alignment.Right,
      Array(
        "D│        ",
        " │        ",
        "C│        ",
        " └────────",
        " AAAAABBBB"
      )
    );
  }

  test("widgets_chart_handles_x_axis_labels_alignments") {
    def test_case(y_alignment: Alignment, lines: Array[String]) = {
      val x_axis = ChartWidget.Axis(
        labels = Some(Array(Span.nostyle("AAAA"), Span.nostyle("B"), Span.nostyle("C"))),
        labels_alignment = y_alignment
      )

      val y_axis = ChartWidget.Axis.default;

      axis_test_case(10, 5, x_axis, y_axis, lines);
    };

    test_case(
      Alignment.Left,
      Array(
        "          ",
        "          ",
        "          ",
        "   ───────",
        "AAA   B  C"
      )
    );
    test_case(
      Alignment.Center,
      Array(
        "          ",
        "          ",
        "          ",
        "  ────────",
        "AAAA B   C"
      )
    );
    test_case(
      Alignment.Right,
      Array(
        "          ",
        "          ",
        "          ",
        "──────────",
        "AAA  B   C"
      )
    );
  }

  test("widgets_chart_handles_y_axis_labels_alignments") {
    def test_case(y_alignment: Alignment, lines: Array[String]): Unit = {
      val x_axis = ChartWidget.Axis(labels = Some(create_labels(Array("AAAAA", "B"))))

      val y_axis = ChartWidget.Axis(labels = Some(create_labels(Array("C", "D"))), labels_alignment = y_alignment)

      axis_test_case(20, 5, x_axis, y_axis, lines);
    };
    test_case(
      Alignment.Left,
      Array(
        "D   │               ",
        "    │               ",
        "C   │               ",
        "    └───────────────",
        "AAAAA              B"
      )
    );
    test_case(
      Alignment.Center,
      Array(
        "  D │               ",
        "    │               ",
        "  C │               ",
        "    └───────────────",
        "AAAAA              B"
      )
    );
    test_case(
      Alignment.Right,
      Array(
        "   D│               ",
        "    │               ",
        "   C│               ",
        "    └───────────────",
        "AAAAA              B"
      )
    );
  }

  test("widgets_chart_can_have_axis_with_zero_length_bounds") {
    val backend = TestBackend(100, 100);
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val datasets = Array(
        ChartWidget.Dataset(marker = symbols.Marker.Braille, style = Style.DEFAULT.fg(Color.Magenta), data = Array(Point.Zero))
      )
      val chart = ChartWidget(
        datasets = datasets,
        block = Some(BlockWidget(title = Some(Spans.nostyle("Plot")), borders = Borders.ALL)),
        x_axis = ChartWidget.Axis(bounds = Point.Zero, labels = Option(create_labels(Array("0.0", "1.0")))),
        y_axis = ChartWidget.Axis(bounds = Point.Zero, labels = Option(create_labels(Array("0.0", "1.0"))))
      )
      f.render_widget(
        chart,
        Rect(x = 0, y = 0, width = 100, height = 100)
      )
    }
  }

  test("widgets_chart_handles_overflows") {
    val backend = TestBackend(80, 30);
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val datasets = Array(
        ChartWidget.Dataset(
          marker = symbols.Marker.Braille,
          style = Style.DEFAULT.fg(Color.Magenta),
          data = Array(Point(1_588_298_471.0, 1.0), Point(1_588_298_473.0, 0.0), Point(1_588_298_496.0, 1.0))
        )
      );

      val chart = ChartWidget(
        datasets = datasets,
        block = Some(BlockWidget(title = Some(Spans.nostyle("Plot")), borders = Borders.ALL)),
        x_axis = ChartWidget.Axis(bounds = Point(1_588_298_471.0, 1_588_992_600.0), labels = Some(create_labels(Array("1588298471.0", "1588992600.0")))),
        y_axis = ChartWidget.Axis(bounds = Point(0.0, 1.0), labels = Some(create_labels(Array("0.0", "1.0"))))
      )
      f.render_widget(chart, Rect(x = 0, y = 0, width = 80, height = 30))
    }
  }

  test("widgets_chart_can_have_empty_datasets") {
    val backend = TestBackend(100, 100);
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val datasets = Array(ChartWidget.Dataset(data = Array.empty, graph_type = ChartWidget.GraphType.Line))
      val chart = ChartWidget(
        datasets = datasets,
        block = Some(BlockWidget(title = Some(Spans.nostyle("Empty Dataset With Line")), borders = Borders.ALL)),
        x_axis = ChartWidget.Axis(bounds = Point.Zero, labels = Some(create_labels(Array("0.0", "1.0")))),
        y_axis = ChartWidget.Axis(bounds = Point(0.0, 1.0), labels = Some(create_labels(Array("0.0", "1.0"))))
      )
      f.render_widget(chart, Rect(x = 0, y = 0, width = 100, height = 100));
    }
  }

  test("widgets_chart_can_have_a_legend") {
    val backend = TestBackend(60, 30);
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val datasets = Array(
        ChartWidget.Dataset(
          name = "Dataset 1",
          style = Style.DEFAULT.fg(Color.Blue),
          data = Array(
            Point.Zero,
            Point(10.0, 1.0),
            Point(20.0, 2.0),
            Point(30.0, 3.0),
            Point(40.0, 4.0),
            Point(50.0, 5.0),
            Point(60.0, 6.0),
            Point(70.0, 7.0),
            Point(80.0, 8.0),
            Point(90.0, 9.0),
            Point(100.0, 10.0)
          ),
          graph_type = ChartWidget.GraphType.Line
        ),
        ChartWidget.Dataset(
          name = "Dataset 2",
          style = Style.DEFAULT.fg(Color.Green),
          data = Array(
            Point(0.0, 10.0),
            Point(10.0, 9.0),
            Point(20.0, 8.0),
            Point(30.0, 7.0),
            Point(40.0, 6.0),
            Point(50.0, 5.0),
            Point(60.0, 4.0),
            Point(70.0, 3.0),
            Point(80.0, 2.0),
            Point(90.0, 1.0),
            Point(100.0, 0.0)
          ),
          graph_type = ChartWidget.GraphType.Line
        )
      )
      val chart = ChartWidget(
        datasets = datasets,
        style = Style.DEFAULT.bg(Color.White),
        block = Some(BlockWidget(title = Some(Spans.nostyle("Chart Test")), borders = Borders.ALL)),
        x_axis = ChartWidget.Axis(
          bounds = Point(0.0, 100.0),
          title = Some(Spans.from(Span.styled("X Axis", Style.DEFAULT.fg(Color.Yellow)))),
          labels = Some(create_labels(Array("0.0", "50.0", "100.0")))
        ),
        y_axis = ChartWidget.Axis(bounds = Point(0.0, 10.0), title = Some(Spans.nostyle("Y Axis")), labels = Some(create_labels(Array("0.0", "5.0", "10.0"))))
      )
      f.render_widget(chart, Rect(x = 0, y = 0, width = 60, height = 30));
    }

    val expected = Buffer.with_lines(
      Array(
        "┌Chart Test────────────────────────────────────────────────┐",
        "│10.0│Y Axis                                    ┌─────────┐│",
        "│    │  ••                                      │Dataset 1││",
        "│    │    ••                                    │Dataset 2││",
        "│    │      ••                                  └─────────┘│",
        "│    │        ••                                ••         │",
        "│    │          ••                            ••           │",
        "│    │            ••                        ••             │",
        "│    │              ••                    ••               │",
        "│    │                ••                ••                 │",
        "│    │                  ••            ••                   │",
        "│    │                    ••        ••                     │",
        "│    │                      •••   ••                       │",
        "│    │                         •••                         │",
        "│5.0 │                        •• ••                        │",
        "│    │                      ••     ••                      │",
        "│    │                   •••         ••                    │",
        "│    │                 ••              ••                  │",
        "│    │               ••                  ••                │",
        "│    │             ••                      ••              │",
        "│    │           ••                          ••            │",
        "│    │         ••                              ••          │",
        "│    │       ••                                  ••        │",
        "│    │     ••                                      •••     │",
        "│    │   ••                                           ••   │",
        "│    │ ••                                               •• │",
        "│0.0 │•                                              X Axis│",
        "│    └─────────────────────────────────────────────────────│",
        "│  0.0                        50.0                    100.0│",
        "└──────────────────────────────────────────────────────────┘"
      )
    );

    // Set expected backgound color
    ranges.range(0, 30) { row =>
      ranges.range(0, 60) { col =>
        expected.get(col, row).set_bg(Color.White)
        ()
      }
    }

    // Set expected colors of the first dataset
    val line1 = Array(
      (48, 5),
      (49, 5),
      (46, 6),
      (47, 6),
      (44, 7),
      (45, 7),
      (42, 8),
      (43, 8),
      (40, 9),
      (41, 9),
      (38, 10),
      (39, 10),
      (36, 11),
      (37, 11),
      (34, 12),
      (35, 12),
      (33, 13),
      (30, 14),
      (31, 14),
      (28, 15),
      (29, 15),
      (25, 16),
      (26, 16),
      (27, 16),
      (23, 17),
      (24, 17),
      (21, 18),
      (22, 18),
      (19, 19),
      (20, 19),
      (17, 20),
      (18, 20),
      (15, 21),
      (16, 21),
      (13, 22),
      (14, 22),
      (11, 23),
      (12, 23),
      (9, 24),
      (10, 24),
      (7, 25),
      (8, 25),
      (6, 26)
    )
    val legend1 = Array(
      (49, 2),
      (50, 2),
      (51, 2),
      (52, 2),
      (53, 2),
      (54, 2),
      (55, 2),
      (56, 2),
      (57, 2)
    )
    line1.foreach { case (col, row) => expected.get(col, row).set_fg(Color.Blue); }
    legend1.foreach { case (col, row) => expected.get(col, row).set_fg(Color.Blue); }

    // Set expected colors of the second dataset
    val line2 = Array(
      (8, 2),
      (9, 2),
      (10, 3),
      (11, 3),
      (12, 4),
      (13, 4),
      (14, 5),
      (15, 5),
      (16, 6),
      (17, 6),
      (18, 7),
      (19, 7),
      (20, 8),
      (21, 8),
      (22, 9),
      (23, 9),
      (24, 10),
      (25, 10),
      (26, 11),
      (27, 11),
      (28, 12),
      (29, 12),
      (30, 12),
      (31, 13),
      (32, 13),
      (33, 14),
      (34, 14),
      (35, 15),
      (36, 15),
      (37, 16),
      (38, 16),
      (39, 17),
      (40, 17),
      (41, 18),
      (42, 18),
      (43, 19),
      (44, 19),
      (45, 20),
      (46, 20),
      (47, 21),
      (48, 21),
      (49, 22),
      (50, 22),
      (51, 23),
      (52, 23),
      (53, 23),
      (54, 24),
      (55, 24),
      (56, 25),
      (57, 25)
    )
    val legend2 = Array(
      (49, 3),
      (50, 3),
      (51, 3),
      (52, 3),
      (53, 3),
      (54, 3),
      (55, 3),
      (56, 3),
      (57, 3)
    )
    line2.foreach { case (col, row) => expected.get(col, row).set_fg(Color.Green) }
    legend2.foreach { case (col, row) => expected.get(col, row).set_fg(Color.Green) }

    // Set expected colors of the x axis
    val x_axis_title = Array((53, 26), (54, 26), (55, 26), (56, 26), (57, 26), (58, 26))
    x_axis_title.foreach { case (col, row) =>
      expected.get(col, row).set_fg(Color.Yellow);
    }
    assert_buffer(backend, expected);
  }
}
