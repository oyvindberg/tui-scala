package tui
package widgets

import tui.internal.Ranges

import java.util.Optional

class ChartTests extends TuiTest {
  case class LegendTestCase(
      chart_area: Rect,
      hidden_legend_constraints: ChartWidget.HiddenLegendConstraints,
      legend_area: Optional[Rect]
  )

  test("it_should_hide_the_legend") {
    val data = Array(new Point(0.0, 5.0), new Point(1.0, 6.0), new Point(3.0, 7.0))
    val cases = Array(
      LegendTestCase(
        chart_area = new Rect(0, 0, 100, 100),
        hidden_legend_constraints = new ChartWidget.HiddenLegendConstraints(new Constraint.Ratio(1, 4), new Constraint.Ratio(1, 4)),
        legend_area = Optional.of(new Rect(88, 0, 12, 12))
      ),
      LegendTestCase(
        chart_area = new Rect(0, 0, 100, 100),
        hidden_legend_constraints = new ChartWidget.HiddenLegendConstraints(new Constraint.Ratio(1, 10), new Constraint.Ratio(1, 4)),
        legend_area = Optional.empty[Rect]()
      )
    )

    cases.foreach { c =>
      val datasets = Range(0, 10).map { i =>
        val name = s"Dataset #$i"
        ChartWidget.Dataset.empty().withName(name).withData(data)
      }.toArray

      val chart = ChartWidget
        .empty(datasets.asInstanceOf[Array[ChartWidget.Dataset]])
        .withXAxis(ChartWidget.Axis.empty().withTitle(Spans.nostyle("X axis")))
        .withYAxis(ChartWidget.Axis.empty().withTitle(Spans.nostyle("Y axis")))
        .withHiddenLegendConstraints(c.hidden_legend_constraints)
      val layout = chart.layout(c.chart_area)
      assertEq(layout.legendArea, c.legend_area);
    }
  }

  def create_labels(labels: Array[String]): Array[Span] =
    labels.map(Span.nostyle)

  def axis_test_case(width: Int, height: Int, x_axis: ChartWidget.Axis, y_axis: ChartWidget.Axis, lines: String*): Unit = {
    val backend = TestBackend(width, height)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val chart = ChartWidget
        .empty(Array.empty[ChartWidget.Dataset])
        .withXAxis(x_axis)
        .withYAxis(y_axis)
      f.renderWidget(chart, f.size);
    }
    val expected = Buffer.withLines(lines.toArray: _*)
    assertBuffer(backend, expected)
  }

  test("widgets_chart_can_render_on_small_areas") {
    def test_case(width: Int, height: Int): CompletedFrame = {
      val backend = TestBackend(width, height)
      val terminal = Terminal.init(backend)
      terminal.draw { f =>
        val datasets = Array(
          ChartWidget.Dataset
            .empty()
            .withMarker(Symbols.Marker.Braille)
            .withStyle(Style.DEFAULT.withFg(Color.Magenta))
            .withData(Array(Point.Zero))
        )

        val chart = ChartWidget
          .empty(datasets)
          .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Plot")).withBorders(Borders.ALL))
          .withXAxis(ChartWidget.Axis.empty().withBounds(Point.Zero).withLabels(create_labels(Array("0.0", "1.0"))))
          .withYAxis(ChartWidget.Axis.empty().withBounds(Point.Zero).withLabels(create_labels(Array("1.0", "0.0"))))
        f.renderWidget(chart, f.size);
      }
    }

    test_case(0, 0)
    test_case(0, 1)
    test_case(1, 0)
    test_case(1, 1)
    test_case(2, 2)
  }

  test("widgets_chart_handles_long_labels") {
    def test_case(x_labels: Option[(String, String)], y_labels: Option[(String, String)], x_alignment: Alignment, lines: String*): Unit = {
      val x_axis =
        x_labels.foldLeft(ChartWidget.Axis.empty().withBounds(new Point(0.0, 1.0))) { case (acc, (left_label, right_label)) =>
          acc
            .withLabels(Array(Span.nostyle(left_label), Span.nostyle(right_label)))
            .withLabelsAlignment(x_alignment)
        }

      val y_axis =
        y_labels.foldLeft(ChartWidget.Axis.empty().withBounds(new Point(0.0, 1.0))) { case (acc, (left_label, right_label)) =>
          acc.withLabels(Array(Span.nostyle(left_label), Span.nostyle(right_label)))
        }

      axis_test_case(10, 5, x_axis, y_axis, lines: _*)
    }

    test_case(
      Some(("AAAA", "B")),
      None,
      Alignment.Left,
      "          ",
      "          ",
      "          ",
      "   ───────",
      "AAA      B"
    )
    test_case(
      Some(("A", "BBBB")),
      None,
      Alignment.Left,
      "          ",
      "          ",
      "          ",
      " ─────────",
      "A     BBBB"
    )
    test_case(
      Some(("AAAAAAAAAAA", "B")),
      None,
      Alignment.Left,
      "          ",
      "          ",
      "          ",
      "   ───────",
      "AAA      B"
    )
    test_case(
      Some(("A", "B")),
      Some(("CCCCCCC", "D")),
      Alignment.Left,
      "D  │      ",
      "   │      ",
      "CCC│      ",
      "   └──────",
      "   A     B"
    )
    test_case(
      Some(("AAAAAAAAAA", "B")),
      Some(("C", "D")),
      Alignment.Center,
      "D  │      ",
      "   │      ",
      "C  │      ",
      "   └──────",
      "AAAAAAA  B"
    )
    test_case(
      Some(("AAAAAAA", "B")),
      Some(("C", "D")),
      Alignment.Right,
      "D│        ",
      " │        ",
      "C│        ",
      " └────────",
      " AAAAA   B"
    )
    test_case(
      Some(("AAAAAAA", "BBBBBBB")),
      Some(("C", "D")),
      Alignment.Right,
      "D│        ",
      " │        ",
      "C│        ",
      " └────────",
      " AAAAABBBB"
    )
  }

  test("widgets_chart_handles_x_axis_labels_alignments") {
    def test_case(y_alignment: Alignment, lines: String*): Unit = {
      val x_axis = ChartWidget.Axis
        .empty()
        .withLabels(Array(Span.nostyle("AAAA"), Span.nostyle("B"), Span.nostyle("C")))
        .withLabelsAlignment(y_alignment)

      val y_axis = ChartWidget.Axis.empty()

      axis_test_case(10, 5, x_axis, y_axis, lines: _*)
    }

    test_case(
      Alignment.Left,
      "          ",
      "          ",
      "          ",
      "   ───────",
      "AAA   B  C"
    )
    test_case(
      Alignment.Center,
      "          ",
      "          ",
      "          ",
      "  ────────",
      "AAAA B   C"
    )
    test_case(
      Alignment.Right,
      "          ",
      "          ",
      "          ",
      "──────────",
      "AAA  B   C"
    )
  }

  test("widgets_chart_handles_y_axis_labels_alignments") {
    def test_case(y_alignment: Alignment, lines: String*): Unit = {
      val x_axis = ChartWidget.Axis.empty().withLabels(create_labels(Array("AAAAA", "B")))

      val y_axis = ChartWidget.Axis.empty().withLabels(create_labels(Array("C", "D"))).withLabelsAlignment(y_alignment)

      axis_test_case(20, 5, x_axis, y_axis, lines: _*)
    }

    test_case(
      Alignment.Left,
      "D   │               ",
      "    │               ",
      "C   │               ",
      "    └───────────────",
      "AAAAA              B"
    )
    test_case(
      Alignment.Center,
      "  D │               ",
      "    │               ",
      "  C │               ",
      "    └───────────────",
      "AAAAA              B"
    )
    test_case(
      Alignment.Right,
      "   D│               ",
      "    │               ",
      "   C│               ",
      "    └───────────────",
      "AAAAA              B"
    )
  }

  test("widgets_chart_can_have_axis_with_zero_length_bounds") {
    val backend = TestBackend(100, 100)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val datasets = Array(
        ChartWidget.Dataset
          .empty()
          .withMarker(Symbols.Marker.Braille)
          .withStyle(Style.DEFAULT.withFg(Color.Magenta))
          .withData(Array(Point.Zero))
      )
      val chart = ChartWidget
        .empty(datasets)
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Plot")).withBorders(Borders.ALL))
        .withXAxis(ChartWidget.Axis.empty().withBounds(Point.Zero).withLabels(create_labels(Array("0.0", "1.0"))))
        .withYAxis(ChartWidget.Axis.empty().withBounds(Point.Zero).withLabels(create_labels(Array("0.0", "1.0"))))
      f.renderWidget(
        chart,
        new Rect(0, 0, 100, 100)
      )
    }
  }

  test("widgets_chart_handles_overflows") {
    val backend = TestBackend(80, 30)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val datasets = Array(
        ChartWidget.Dataset
          .empty()
          .withMarker(Symbols.Marker.Braille)
          .withStyle(Style.DEFAULT.withFg(Color.Magenta))
          .withData(Array(new Point(1_588_298_471.0, 1.0), new Point(1_588_298_473.0, 0.0), new Point(1_588_298_496.0, 1.0)))
      )

      val chart = ChartWidget
        .empty(datasets)
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Plot")).withBorders(Borders.ALL))
        .withXAxis(
          ChartWidget.Axis
            .empty()
            .withBounds(new Point(1_588_298_471.0, 1_588_992_600.0))
            .withLabels(create_labels(Array("1588298471.0", "1588992600.0")))
        )
        .withYAxis(ChartWidget.Axis.empty().withBounds(new Point(0.0, 1.0)).withLabels(create_labels(Array("0.0", "1.0"))))
      f.renderWidget(chart, new Rect(0, 0, 80, 30))
    }
  }

  test("widgets_chart_can_have_empty_datasets") {
    val backend = TestBackend(100, 100)
    val terminal = Terminal.init(backend)

    terminal.draw { f =>
      val datasets = Array(
        ChartWidget.Dataset
          .empty()
          .withData(Array.empty[Point])
          .withGraphType(ChartWidget.GraphType.Line)
      )
      val chart = ChartWidget
        .empty(datasets)
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Empty Dataset With Line")).withBorders(Borders.ALL))
        .withXAxis(ChartWidget.Axis.empty().withBounds(Point.Zero).withLabels(create_labels(Array("0.0", "1.0"))))
        .withYAxis(ChartWidget.Axis.empty().withBounds(new Point(0.0, 1.0)).withLabels(create_labels(Array("0.0", "1.0"))))
      f.renderWidget(chart, new Rect(0, 0, 100, 100));
    }
  }

  test("widgets_chart_can_have_a_legend") {
    val backend = TestBackend(60, 30)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val datasets = Array(
        ChartWidget.Dataset
          .empty()
          .withName("Dataset 1")
          .withStyle(Style.DEFAULT.withFg(Color.Blue))
          .withData(
            Array(
              Point.Zero,
              new Point(10.0, 1.0),
              new Point(20.0, 2.0),
              new Point(30.0, 3.0),
              new Point(40.0, 4.0),
              new Point(50.0, 5.0),
              new Point(60.0, 6.0),
              new Point(70.0, 7.0),
              new Point(80.0, 8.0),
              new Point(90.0, 9.0),
              new Point(100.0, 10.0)
            )
          )
          .withGraphType(ChartWidget.GraphType.Line),
        ChartWidget.Dataset
          .empty()
          .withName("Dataset 2")
          .withStyle(Style.DEFAULT.withFg(Color.Green))
          .withData(
            Array(
              new Point(0.0, 10.0),
              new Point(10.0, 9.0),
              new Point(20.0, 8.0),
              new Point(30.0, 7.0),
              new Point(40.0, 6.0),
              new Point(50.0, 5.0),
              new Point(60.0, 4.0),
              new Point(70.0, 3.0),
              new Point(80.0, 2.0),
              new Point(90.0, 1.0),
              new Point(100.0, 0.0)
            )
          )
          .withGraphType(ChartWidget.GraphType.Line)
      )
      val chart = ChartWidget
        .empty(datasets)
        .withStyle(Style.DEFAULT.withBg(Color.White))
        .withBlock(BlockWidget.empty().withTitle(Spans.nostyle("Chart Test")).withBorders(Borders.ALL))
        .withXAxis(
          ChartWidget.Axis
            .empty()
            .withBounds(new Point(0.0, 100.0))
            .withTitle(Spans.from(Span.styled("X Axis", Style.DEFAULT.withFg(Color.Yellow))))
            .withLabels(create_labels(Array("0.0", "50.0", "100.0")))
        )
        .withYAxis(
          ChartWidget.Axis
            .empty()
            .withBounds(new Point(0.0, 10.0))
            .withTitle(Spans.nostyle("Y Axis"))
            .withLabels(create_labels(Array("0.0", "5.0", "10.0")))
        )
      f.renderWidget(chart, new Rect(0, 0, 60, 30));
    }

    val expected = Buffer.withLines(
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

    // Set expected backgound color
    Ranges.range(0, 30, (row: Int) => {
      Ranges.range(0, 60, (col: Int) => {
        expected.get(col, row).setBg(Color.White)
        ()
      })
    })

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
    line1.foreach { case (col, row) => expected.get(col, row).setFg(Color.Blue); }
    legend1.foreach { case (col, row) => expected.get(col, row).setFg(Color.Blue); }

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
    line2.foreach { case (col, row) => expected.get(col, row).setFg(Color.Green) }
    legend2.foreach { case (col, row) => expected.get(col, row).setFg(Color.Green) }

    // Set expected colors of the x axis
    val x_axis_title = Array((53, 26), (54, 26), (55, 26), (56, 26), (57, 26), (58, 26))
    x_axis_title.foreach { case (col, row) =>
      expected.get(col, row).setFg(Color.Yellow);
    }
    assertBuffer(backend, expected)
  }
}
