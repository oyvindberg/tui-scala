package jatatui.tests.widgets.chart;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Rect;
import jatatui.core.layout.solver.Either;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.widgets.chart.Axis;
import jatatui.widgets.chart.Chart;
import jatatui.widgets.chart.Dataset;
import jatatui.widgets.chart.GraphType;
import jatatui.widgets.chart.HiddenLegendConstraints;
import jatatui.widgets.chart.LegendPosition;
import jatatui.widgets.chart.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ChartTest {

  private record LegendTestCase(
      Rect chartArea, HiddenLegendConstraints hiddenLegendConstraints, Optional<Rect> legendArea) {}

  @Test
  void it_should_hide_the_legend() {
    List<Point> data = List.of(new Point(0.0, 5.0), new Point(1.0, 6.0), new Point(3.0, 7.0));
    LegendTestCase[] cases =
        new LegendTestCase[] {
          new LegendTestCase(
              new Rect(0, 0, 100, 100),
              new HiddenLegendConstraints(new Constraint.Ratio(1, 4), new Constraint.Ratio(1, 4)),
              Optional.of(new Rect(88, 0, 12, 12))),
          new LegendTestCase(
              new Rect(0, 0, 100, 100),
              new HiddenLegendConstraints(new Constraint.Ratio(1, 10), new Constraint.Ratio(1, 4)),
              Optional.empty()),
        };
    for (LegendTestCase tc : cases) {
      List<Dataset> datasets = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        String name = "Dataset #" + i;
        datasets.add(Dataset.empty().withName(name).withData(data));
      }
      Chart chart =
          Chart.of(datasets)
              .withXAxis(Axis.empty().withTitle("X axis"))
              .withYAxis(Axis.empty().withTitle("Y axis"))
              .withHiddenLegendConstraints(tc.hiddenLegendConstraints());
      Chart.ChartLayout layout = chart.layout(tc.chartArea()).orElseThrow();
      assertEquals(tc.legendArea(), layout.legendArea);
    }
  }

  @Test
  void axis_can_be_stylized() {
    Axis styled = Axis.empty().black().onWhite().bold().notDim();
    assertEquals(
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM),
        styled.style());
  }

  @Test
  void dataset_can_be_stylized() {
    Dataset styled = Dataset.empty().black().onWhite().bold().notDim();
    assertEquals(
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM),
        styled.style());
  }

  @Test
  void chart_can_be_stylized() {
    Chart styled = Chart.empty().black().onWhite().bold().notDim();
    assertEquals(
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM),
        styled.style());
  }

  @Test
  void graph_type_to_string() {
    assertEquals("Scatter", GraphType.Scatter.toString());
    assertEquals("Line", GraphType.Line.toString());
    assertEquals("Bar", GraphType.Bar.toString());
  }

  @Test
  void graph_type_from_str() {
    assertEquals(Either.right(GraphType.Scatter), GraphType.fromString("Scatter"));
    assertEquals(Either.right(GraphType.Line), GraphType.fromString("Line"));
    assertEquals(Either.right(GraphType.Bar), GraphType.fromString("Bar"));
    assertEquals(Either.left("VariantNotFound"), GraphType.fromString(""));
  }

  @Test
  void it_does_not_panic_if_title_is_wider_than_buffer() {
    Chart widget =
        Chart.empty()
            .withYAxis(Axis.empty().withTitle("xxxxxxxxxxxxxxxx"))
            .withXAxis(Axis.empty().withTitle("xxxxxxxxxxxxxxxx"));
    Buffer buffer = Buffer.empty(new Rect(0, 0, 8, 4));
    widget.render(buffer.area, buffer);
    assertBufferEq(buffer, Buffer.withLines("        ", "        ", "        ", "        "));
  }

  @Test
  void datasets_without_name_dont_contribute_to_legend_height() {
    Dataset dataNamed1 = Dataset.empty().withName("data1");
    Dataset dataNamed2 = Dataset.empty().withName("");
    Dataset dataUnnamed = Dataset.empty();
    Chart widget = Chart.of(List.of(dataNamed1, dataUnnamed, dataNamed2));
    Buffer buffer = Buffer.empty(new Rect(0, 0, 50, 25));
    Chart.ChartLayout layout = widget.layout(buffer.area).orElseThrow();

    assertTrue(layout.legendArea.isPresent());
    assertEquals(4, layout.legendArea.get().height());
  }

  @Test
  void no_legend_if_no_named_datasets() {
    Dataset dataset = Dataset.empty();
    Chart widget = Chart.of(List.of(dataset, dataset, dataset));
    Buffer buffer = Buffer.empty(new Rect(0, 0, 50, 25));
    Chart.ChartLayout layout = widget.layout(buffer.area).orElseThrow();

    assertFalse(layout.legendArea.isPresent());
  }

  @Test
  void dataset_legend_style_is_patched() {
    Dataset longDatasetName = Dataset.empty().withName("Very long name");
    Dataset shortDataset =
        Dataset.empty().withName(Line.from("Short name").withAlignment(HorizontalAlignment.Right));
    Chart widget =
        Chart.of(List.of(longDatasetName, shortDataset))
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Length(100), new Constraint.Length(100)));
    Buffer buffer = Buffer.empty(new Rect(0, 0, 20, 5));
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "    ┌──────────────┐",
            "    │Very long name│",
            "    │    Short name│",
            "    └──────────────┘",
            "                    ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_chart_have_a_topleft_legend() {
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName("Ds1")))
            .withLegendPosition(LegendPosition.TopLeft);
    Rect area = new Rect(0, 0, 30, 20);
    Buffer buffer = Buffer.empty(area);
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┌───┐                         ",
            "│Ds1│                         ",
            "└───┘                         ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_chart_have_a_long_y_axis_title_overlapping_legend() {
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName("Ds1")))
            .withYAxis(Axis.empty().withTitle("The title overlap a legend."));
    Rect area = new Rect(0, 0, 30, 20);
    Buffer buffer = Buffer.empty(area);
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "The title overlap a legend.   ",
            "                         ┌───┐",
            "                         │Ds1│",
            "                         └───┘",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ",
            "                              ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_chart_have_overflowed_y_axis() {
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName("Ds1")))
            .withYAxis(Axis.empty().withTitle("The title overlap a legend."));
    Rect area = new Rect(0, 0, 10, 10);
    Buffer buffer = Buffer.empty(area);
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "          ",
            "          ",
            "          ",
            "          ",
            "          ",
            "          ",
            "          ",
            "          ",
            "          ",
            "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_legend_area_can_fit_same_chart_area() {
    String name = "Data";
    Chart base =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    Rect area = new Rect(0, 0, name.length() + 2, 3);
    Buffer buffer = Buffer.empty(area);
    LegendPosition[] positions = {
      LegendPosition.TopLeft,
      LegendPosition.Top,
      LegendPosition.TopRight,
      LegendPosition.Left,
      LegendPosition.Right,
      LegendPosition.Bottom,
      LegendPosition.BottomLeft,
      LegendPosition.BottomRight,
    };
    for (LegendPosition position : positions) {
      Chart chart = base.withLegendPosition(position);
      buffer.reset();
      chart.render(buffer.area, buffer);
      Buffer expected = Buffer.withLines("┌────┐", "│Data│", "└────┘");
      assertBufferEq(buffer, expected);
    }
  }

  @Test
  void test_legend_topleft_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(LegendPosition.TopLeft)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            "┌────┐   ", "│Data│   ", "└────┘   ", "         ", "         ", "         "));
  }

  @Test
  void test_legend_top_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(LegendPosition.Top)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            " ┌────┐  ", " │Data│  ", " └────┘  ", "         ", "         ", "         "));
  }

  @Test
  void test_legend_topright_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(LegendPosition.TopRight)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            "   ┌────┐", "   │Data│", "   └────┘", "         ", "         ", "         "));
  }

  @Test
  void test_legend_left_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(LegendPosition.Left)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            "         ", "┌────┐   ", "│Data│   ", "└────┘   ", "         ", "         "));
  }

  @Test
  void test_legend_right_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(LegendPosition.Right)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            "         ", "   ┌────┐", "   │Data│", "   └────┘", "         ", "         "));
  }

  @Test
  void test_legend_bottomleft_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(LegendPosition.BottomLeft)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            "         ", "         ", "         ", "┌────┐   ", "│Data│   ", "└────┘   "));
  }

  @Test
  void test_legend_bottom_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(LegendPosition.Bottom)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            "         ", "         ", "         ", " ┌────┐  ", " │Data│  ", " └────┘  "));
  }

  @Test
  void test_legend_bottomright_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(LegendPosition.BottomRight)
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            "         ", "         ", "         ", "   ┌────┐", "   │Data│", "   └────┘"));
  }

  @Test
  void test_legend_none_with_odd_margin() {
    String name = "Data";
    Rect area = new Rect(0, 0, name.length() + 2 + 3, 3 + 3);
    Buffer buffer = Buffer.empty(area);
    Chart chart =
        Chart.of(List.of(Dataset.empty().withName(name)))
            .withLegendPosition(Optional.empty())
            .withHiddenLegendConstraints(
                new HiddenLegendConstraints(
                    new Constraint.Percentage(100), new Constraint.Percentage(100)));
    chart.render(buffer.area, buffer);
    assertBufferEq(
        buffer,
        Buffer.withLines(
            "         ", "         ", "         ", "         ", "         ", "         "));
  }

  @Test
  void render_in_minimal_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 1, 1));
    Chart chart =
        Chart.of(List.of(Dataset.empty().withData(new Point(0.0, 0.0), new Point(1.0, 1.0))))
            .withXAxis(Axis.empty().withBounds(0.0, 1.0))
            .withYAxis(Axis.empty().withBounds(0.0, 1.0));
    chart.render(buffer.area, buffer);
    assertBufferEq(buffer, Buffer.withLines("•"));
  }

  @Test
  void render_in_zero_size_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 0));
    Chart chart =
        Chart.of(List.of(Dataset.empty().withData(new Point(0.0, 0.0), new Point(1.0, 1.0))))
            .withXAxis(Axis.empty().withBounds(0.0, 1.0))
            .withYAxis(Axis.empty().withBounds(0.0, 1.0));
    // This should not panic, even if the buffer has zero size.
    chart.render(buffer.area, buffer);
  }

  /// N/A: `bar_chart` and `overlapping_lines` upstream tests verify Canvas drawing primitives at
  /// pixel granularity (Marker.Dot/Braille/Block). Those are exercised by the canvas widget tests
  /// in `jatatui.tests.widgets.canvas`. We keep simpler smoke tests here
  // (`render_in_minimal_buffer`,
  /// `render_in_zero_size_buffer`) since they're sufficient to confirm Chart drives the Canvas
  /// pipeline correctly without duplicating canvas tests.
  @Test
  void bar_chart_smoke() {
    List<Point> data =
        List.of(
            new Point(0.0, 0.0),
            new Point(2.0, 1.0),
            new Point(4.0, 4.0),
            new Point(6.0, 8.0),
            new Point(8.0, 9.0),
            new Point(10.0, 10.0));
    Chart chart =
        Chart.of(
                List.of(
                    Dataset.empty()
                        .withData(data)
                        .withMarker(jatatui.core.symbols.Marker.Dot)
                        .withGraphType(GraphType.Bar)))
            .withXAxis(Axis.empty().withBounds(0.0, 10.0))
            .withYAxis(Axis.empty().withBounds(0.0, 10.0));
    Rect area = new Rect(0, 0, 11, 11);
    Buffer buffer = Buffer.empty(area);
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "          •",
            "        • •",
            "      • • •",
            "      • • •",
            "      • • •",
            "      • • •",
            "    • • • •",
            "    • • • •",
            "    • • • •",
            "  • • • • •",
            "• • • • • •");
    assertBufferEq(buffer, expected);
  }
}
