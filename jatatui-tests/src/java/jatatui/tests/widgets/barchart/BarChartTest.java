package jatatui.tests.widgets.barchart;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Direction;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Bar;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.widgets.barchart.BarChart;
import jatatui.widgets.barchart.BarGroup;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.BorderType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BarChartTest {

  // ---- Helpers ----

  private static jatatui.widgets.barchart.Bar bar(String label, long value) {
    return jatatui.widgets.barchart.Bar.withLabel(label, value);
  }

  private static BarChart withDataPairs(BarChart chart, BarGroup.LabelledValue... entries) {
    return chart.withData(entries);
  }

  // ---- Tests ----

  @Test
  void Default() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget = BarChart.empty();
    widget.render(buffer.area, buffer);
    assertBufferEq(buffer, Buffer.withLines("          ", "          ", "          "));
  }

  @Test
  void data() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget =
        BarChart.empty().withData(BarGroup.LabelledValue.of("foo", 1), BarGroup.LabelledValue.of("bar", 2));
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "  █       ",
            "1 2       ",
            "f b       ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void block() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 5));
    Block block = Block.bordered().withBorderType(BorderType.Double).withTitle("Block");
    BarChart widget =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("foo", 1), BarGroup.LabelledValue.of("bar", 2))
            .withBlock(block);
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "╔Block═══╗",
            "║  █     ║",
            "║1 2     ║",
            "║f b     ║",
            "╚════════╝");
    assertBufferEq(buffer, expected);
  }

  @Test
  void max() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart withoutMax =
        BarChart.empty()
            .withData(
                BarGroup.LabelledValue.of("foo", 1),
                BarGroup.LabelledValue.of("bar", 2),
                BarGroup.LabelledValue.of("baz", 100));
    withoutMax.render(buffer.area, buffer);
    Buffer expected1 =
        Buffer.withLines(
            "    █     ",
            "    █     ",
            "f b b     ");
    assertBufferEq(buffer, expected1);

    BarChart withMax =
        BarChart.empty()
            .withData(
                BarGroup.LabelledValue.of("foo", 1),
                BarGroup.LabelledValue.of("bar", 2),
                BarGroup.LabelledValue.of("baz", 100))
            .withMax(2);
    withMax.render(buffer.area, buffer);
    Buffer expected2 =
        Buffer.withLines(
            "  █ █     ",
            "1 2 █     ",
            "f b b     ");
    assertBufferEq(buffer, expected2);
  }

  @Test
  void bar_style() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("foo", 1), BarGroup.LabelledValue.of("bar", 2))
            .withBarStyle(Style.empty().red());
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "  █       ",
            "1 2       ",
            "f b       ");
    for (int x : new int[] {0, 2}) {
      for (int y : new int[] {0, 1}) {
        expected.cellAt(x, y).setFg(Color.RED);
      }
    }
    assertBufferEq(buffer, expected);
  }

  @Test
  void bar_width() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("foo", 1), BarGroup.LabelledValue.of("bar", 2))
            .withBarWidth(3);
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "    ███   ",
            "█1█ █2█   ",
            "foo bar   ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void bar_gap() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("foo", 1), BarGroup.LabelledValue.of("bar", 2))
            .withBarGap(2);
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "   █      ",
            "1  2      ",
            "f  b      ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void bar_set() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget =
        BarChart.empty()
            .withData(
                BarGroup.LabelledValue.of("foo", 0),
                BarGroup.LabelledValue.of("bar", 1),
                BarGroup.LabelledValue.of("baz", 3))
            .withBarSet(Bar.THREE_LEVELS);
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "    █     ",
            "  ▄ 3     ",
            "f b b     ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void bar_set_nine_levels() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 18, 3));
    BarChart widget =
        BarChart.empty()
            .withData(
                BarGroup.LabelledValue.of("a", 0),
                BarGroup.LabelledValue.of("b", 1),
                BarGroup.LabelledValue.of("c", 2),
                BarGroup.LabelledValue.of("d", 3),
                BarGroup.LabelledValue.of("e", 4),
                BarGroup.LabelledValue.of("f", 5),
                BarGroup.LabelledValue.of("g", 6),
                BarGroup.LabelledValue.of("h", 7),
                BarGroup.LabelledValue.of("i", 8))
            .withBarSet(Bar.NINE_LEVELS);
    widget.render(new Rect(0, 1, 18, 2), buffer);
    Buffer expected =
        Buffer.withLines(
            "                  ",
            "  ▁ ▂ ▃ ▄ ▅ ▆ ▇ 8 ",
            "a b c d e f g h i ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void value_style() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("foo", 1), BarGroup.LabelledValue.of("bar", 2))
            .withBarWidth(3)
            .withValueStyle(Style.empty().red());
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "    ███   ",
            "█1█ █2█   ",
            "foo bar   ");
    expected.cellAt(1, 1).setFg(Color.RED);
    expected.cellAt(5, 1).setFg(Color.RED);
    assertBufferEq(buffer, expected);
  }

  @Test
  void label_style() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("foo", 1), BarGroup.LabelledValue.of("bar", 2))
            .withLabelStyle(Style.empty().red());
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "  █       ",
            "1 2       ",
            "f b       ");
    expected.cellAt(0, 2).setFg(Color.RED);
    expected.cellAt(2, 2).setFg(Color.RED);
    assertBufferEq(buffer, expected);
  }

  @Test
  void style() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    BarChart widget =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("foo", 1), BarGroup.LabelledValue.of("bar", 2))
            .withStyle(Style.empty().red());
    widget.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "  █       ",
            "1 2       ",
            "f b       ");
    for (int x = 0; x < 10; x++) {
      for (int y = 0; y < 3; y++) {
        expected.cellAt(x, y).setFg(Color.RED);
      }
    }
    assertBufferEq(buffer, expected);
  }

  @Test
  void can_be_stylized() {
    BarChart styled = BarChart.empty().black().onWhite().bold();
    assertEquals(
        Style.empty().withFg(Color.BLACK).withBg(Color.WHITE).withAddModifier(Modifier.BOLD),
        styled.style());
  }

  @Test
  void test_empty_group() {
    BarChart chart =
        BarChart.empty()
            .withGroup(BarGroup.empty().withLabel("invisible"))
            .withGroup(
                BarGroup.empty()
                    .withLabel("G")
                    .withBars(
                        jatatui.widgets.barchart.Bar.empty().withValue(1),
                        jatatui.widgets.barchart.Bar.empty().withValue(2)));

    Buffer buffer = Buffer.empty(new Rect(0, 0, 3, 3));
    chart.render(buffer.area, buffer);
    Buffer expected = Buffer.withLines("  █", "1 2", "G  ");
    assertBufferEq(buffer, expected);
  }

  private static BarChart buildTestBarChart() {
    return BarChart.empty()
        .withGroup(
            BarGroup.empty()
                .withLabel("G1")
                .withBars(
                    jatatui.widgets.barchart.Bar.empty().withValue(2),
                    jatatui.widgets.barchart.Bar.empty().withValue(3),
                    jatatui.widgets.barchart.Bar.empty().withValue(4)))
        .withGroup(
            BarGroup.empty()
                .withLabel("G2")
                .withBars(
                    jatatui.widgets.barchart.Bar.empty().withValue(3),
                    jatatui.widgets.barchart.Bar.empty().withValue(4),
                    jatatui.widgets.barchart.Bar.empty().withValue(5)))
        .withGroupGap(1)
        .withDirection(Direction.Horizontal)
        .withBarGap(0);
  }

  @Test
  void test_horizontal_bars() {
    BarChart chart = buildTestBarChart();
    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 8));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "2█   ",
            "3██  ",
            "4███ ",
            "G1   ",
            "3██  ",
            "4███ ",
            "5████",
            "G2   ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_horizontal_bars_no_space_for_group_label() {
    BarChart chart = buildTestBarChart();
    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 7));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "2█   ",
            "3██  ",
            "4███ ",
            "G1   ",
            "3██  ",
            "4███ ",
            "5████");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_horizontal_bars_no_space_for_all_bars() {
    BarChart chart = buildTestBarChart();
    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 5));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "2█   ",
            "3██  ",
            "4███ ",
            "G1   ",
            "3██  ");
    assertBufferEq(buffer, expected);
  }

  private static void test_horizontal_bars_label_width_greater_than_bar(java.util.Optional<Color> barColor) {
    jatatui.widgets.barchart.Bar bar =
        jatatui.widgets.barchart.Bar.empty()
            .withValue(2)
            .withTextValue("label")
            .withValueStyle(Style.empty().red());
    if (barColor.isPresent()) {
      bar = bar.withStyle(Style.empty().withFg(barColor.get()));
    }
    BarChart chart =
        BarChart.empty()
            .withGroup(BarGroup.empty().withBars(bar, jatatui.widgets.barchart.Bar.empty().withValue(5)))
            .withDirection(Direction.Horizontal)
            .withBarStyle(Style.empty().yellow())
            .withValueStyle(Style.empty().italic())
            .withBarGap(0);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 2));
    chart.render(buffer.area, buffer);

    Buffer expected = Buffer.withLines("label", "5████");
    expected.cellAt(0, 1).modifier = expected.cellAt(0, 1).modifier.insert(Modifier.ITALIC);
    for (int x = 0; x < 5; x++) {
      expected.cellAt(x, 1).setFg(Color.YELLOW);
    }

    Color expectedColor = barColor.orElse(Color.YELLOW);

    expected.cellAt(0, 0).setFg(Color.RED);
    expected.cellAt(0, 0).modifier = expected.cellAt(0, 0).modifier.insert(Modifier.ITALIC);
    expected.cellAt(1, 0).setFg(Color.RED);
    expected.cellAt(1, 0).modifier = expected.cellAt(1, 0).modifier.insert(Modifier.ITALIC);
    expected.cellAt(2, 0).setFg(expectedColor);
    expected.cellAt(3, 0).setFg(expectedColor);
    expected.cellAt(4, 0).setFg(expectedColor);

    assertBufferEq(buffer, expected);
  }

  @Test
  void test_horizontal_bars_label_width_greater_than_bar_without_style() {
    test_horizontal_bars_label_width_greater_than_bar(java.util.Optional.empty());
  }

  @Test
  void test_horizontal_bars_label_width_greater_than_bar_with_style() {
    test_horizontal_bars_label_width_greater_than_bar(java.util.Optional.of(Color.WHITE));
  }

  @Test
  void test_horizontal_label() {
    BarChart chart =
        BarChart.empty()
            .withDirection(Direction.Horizontal)
            .withBarGap(0)
            .withData(
                BarGroup.LabelledValue.of("Jan", 10),
                BarGroup.LabelledValue.of("Feb", 20),
                BarGroup.LabelledValue.of("Mar", 5));

    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "Jan 10█   ",
            "Feb 20████",
            "Mar 5     ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_group_label_style() {
    BarChart chart =
        BarChart.empty()
            .withGroup(
                BarGroup.empty()
                    .withLabel(Line.from(Span.raw("G1")).red())
                    .withBars(jatatui.widgets.barchart.Bar.empty().withValue(2)))
            .withGroupGap(1)
            .withDirection(Direction.Horizontal)
            .withLabelStyle(Style.empty().bold().yellow());

    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 2));
    chart.render(buffer.area, buffer);

    Buffer expected = Buffer.withLines("2████", "G1   ");
    expected.cellAt(0, 1).setFg(Color.RED);
    expected.cellAt(0, 1).modifier = expected.cellAt(0, 1).modifier.insert(Modifier.BOLD);
    expected.cellAt(1, 1).setFg(Color.RED);
    expected.cellAt(1, 1).modifier = expected.cellAt(1, 1).modifier.insert(Modifier.BOLD);

    assertBufferEq(buffer, expected);
  }

  @Test
  void test_group_label_center() {
    BarGroup group =
        BarGroup.fromPairs(
            BarGroup.LabelledValue.of("a", 1),
            BarGroup.LabelledValue.of("b", 2),
            BarGroup.LabelledValue.of("c", 3),
            BarGroup.LabelledValue.of("c", 4));
    BarChart chart =
        BarChart.empty()
            .withGroup(group.withLabel(Line.from("G1").withAlignment(HorizontalAlignment.Center)))
            .withGroup(group.withLabel(Line.from("G2").withAlignment(HorizontalAlignment.Center)));

    Buffer buffer = Buffer.empty(new Rect(0, 0, 13, 5));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "    ▂ █     ▂",
            "  ▄ █ █   ▄ █",
            "▆ 2 3 4 ▆ 2 3",
            "a b c c a b c",
            "  G1     G2  ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_group_label_right() {
    BarChart chart =
        BarChart.empty()
            .withGroup(
                BarGroup.empty()
                    .withLabel(Line.from(Span.raw("G")).withAlignment(HorizontalAlignment.Right))
                    .withBars(
                        jatatui.widgets.barchart.Bar.empty().withValue(2),
                        jatatui.widgets.barchart.Bar.empty().withValue(5)));

    Buffer buffer = Buffer.empty(new Rect(0, 0, 3, 3));
    chart.render(buffer.area, buffer);
    Buffer expected = Buffer.withLines("  █", "▆ 5", "  G");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_unicode_as_value() {
    BarGroup group =
        BarGroup.empty()
            .withBars(
                jatatui.widgets.barchart.Bar.empty().withValue(123).withLabel("B1").withTextValue("写"),
                jatatui.widgets.barchart.Bar.empty().withValue(321).withLabel("B2").withTextValue("写"),
                jatatui.widgets.barchart.Bar.empty().withValue(333).withLabel("B2").withTextValue("写"));
    BarChart chart = BarChart.empty().withGroup(group).withBarWidth(3).withBarGap(1);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 11, 5));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "    ▆▆▆ ███",
            "    ███ ███",
            "▃▃▃ ███ ███",
            "写█ 写█ 写█",
            "B1  B2  B2 ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void handles_zero_width() {
    BarChart chart =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("A", 1))
            .withBarWidth(0)
            .withBarGap(0);
    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 10));
    chart.render(buffer.area, buffer);
    assertBufferEq(buffer, Buffer.empty(new Rect(0, 0, 0, 10)));
  }

  @Test
  void single_line() {
    BarGroup group =
        BarGroup.fromPairs(
                BarGroup.LabelledValue.of("a", 0),
                BarGroup.LabelledValue.of("b", 1),
                BarGroup.LabelledValue.of("c", 2),
                BarGroup.LabelledValue.of("d", 3),
                BarGroup.LabelledValue.of("e", 4),
                BarGroup.LabelledValue.of("f", 5),
                BarGroup.LabelledValue.of("g", 6),
                BarGroup.LabelledValue.of("h", 7),
                BarGroup.LabelledValue.of("i", 8))
            .withLabel("Group");

    BarChart chart = BarChart.empty().withGroup(group).withBarSet(Bar.NINE_LEVELS);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 17, 1));
    chart.render(buffer.area, buffer);
    assertBufferEq(buffer, Buffer.withLines("  ▁ ▂ ▃ ▄ ▅ ▆ ▇ 8"));
  }

  @Test
  void two_lines() {
    BarGroup group =
        BarGroup.fromPairs(
                BarGroup.LabelledValue.of("a", 0),
                BarGroup.LabelledValue.of("b", 1),
                BarGroup.LabelledValue.of("c", 2),
                BarGroup.LabelledValue.of("d", 3),
                BarGroup.LabelledValue.of("e", 4),
                BarGroup.LabelledValue.of("f", 5),
                BarGroup.LabelledValue.of("g", 6),
                BarGroup.LabelledValue.of("h", 7),
                BarGroup.LabelledValue.of("i", 8))
            .withLabel("Group");

    BarChart chart = BarChart.empty().withGroup(group).withBarSet(Bar.NINE_LEVELS);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 17, 3));
    chart.render(new Rect(0, 1, buffer.area.width(), 2), buffer);
    Buffer expected =
        Buffer.withLines(
            "                 ",
            "  ▁ ▂ ▃ ▄ ▅ ▆ ▇ 8",
            "a b c d e f g h i");
    assertBufferEq(buffer, expected);
  }

  @Test
  void three_lines() {
    BarGroup group =
        BarGroup.fromPairs(
                BarGroup.LabelledValue.of("a", 0),
                BarGroup.LabelledValue.of("b", 1),
                BarGroup.LabelledValue.of("c", 2),
                BarGroup.LabelledValue.of("d", 3),
                BarGroup.LabelledValue.of("e", 4),
                BarGroup.LabelledValue.of("f", 5),
                BarGroup.LabelledValue.of("g", 6),
                BarGroup.LabelledValue.of("h", 7),
                BarGroup.LabelledValue.of("i", 8))
            .withLabel(Line.from("Group").withAlignment(HorizontalAlignment.Center));

    BarChart chart = BarChart.empty().withGroup(group).withBarSet(Bar.NINE_LEVELS);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 17, 3));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "  ▁ ▂ ▃ ▄ ▅ ▆ ▇ 8",
            "a b c d e f g h i",
            "      Group      ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void three_lines_double_width() {
    BarGroup group =
        BarGroup.fromPairs(
                BarGroup.LabelledValue.of("a", 0),
                BarGroup.LabelledValue.of("b", 1),
                BarGroup.LabelledValue.of("c", 2),
                BarGroup.LabelledValue.of("d", 3),
                BarGroup.LabelledValue.of("e", 4),
                BarGroup.LabelledValue.of("f", 5),
                BarGroup.LabelledValue.of("g", 6),
                BarGroup.LabelledValue.of("h", 7),
                BarGroup.LabelledValue.of("i", 8))
            .withLabel(Line.from("Group").withAlignment(HorizontalAlignment.Center));

    BarChart chart = BarChart.empty().withGroup(group).withBarWidth(2).withBarSet(Bar.NINE_LEVELS);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 26, 3));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "   1▁ 2▂ 3▃ 4▄ 5▅ 6▆ 7▇ 8█",
            "a  b  c  d  e  f  g  h  i ",
            "          Group           ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void four_lines() {
    BarGroup group =
        BarGroup.fromPairs(
                BarGroup.LabelledValue.of("a", 0),
                BarGroup.LabelledValue.of("b", 1),
                BarGroup.LabelledValue.of("c", 2),
                BarGroup.LabelledValue.of("d", 3),
                BarGroup.LabelledValue.of("e", 4),
                BarGroup.LabelledValue.of("f", 5),
                BarGroup.LabelledValue.of("g", 6),
                BarGroup.LabelledValue.of("h", 7),
                BarGroup.LabelledValue.of("i", 8))
            .withLabel(Line.from("Group").withAlignment(HorizontalAlignment.Center));

    BarChart chart = BarChart.empty().withGroup(group).withBarSet(Bar.NINE_LEVELS);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 17, 4));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "          ▂ ▄ ▆ █",
            "  ▂ ▄ ▆ 4 5 6 7 8",
            "a b c d e f g h i",
            "      Group      ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void two_lines_without_bar_labels() {
    BarGroup group =
        BarGroup.empty()
            .withLabel(Line.from("Group").withAlignment(HorizontalAlignment.Center))
            .withBars(
                jatatui.widgets.barchart.Bar.empty().withValue(0),
                jatatui.widgets.barchart.Bar.empty().withValue(1),
                jatatui.widgets.barchart.Bar.empty().withValue(2),
                jatatui.widgets.barchart.Bar.empty().withValue(3),
                jatatui.widgets.barchart.Bar.empty().withValue(4),
                jatatui.widgets.barchart.Bar.empty().withValue(5),
                jatatui.widgets.barchart.Bar.empty().withValue(6),
                jatatui.widgets.barchart.Bar.empty().withValue(7),
                jatatui.widgets.barchart.Bar.empty().withValue(8));

    BarChart chart = BarChart.empty().withGroup(group);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 17, 3));
    chart.render(new Rect(0, 1, buffer.area.width(), 2), buffer);
    Buffer expected =
        Buffer.withLines(
            "                 ",
            "  ▁ ▂ ▃ ▄ ▅ ▆ ▇ 8",
            "      Group      ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void one_lines_with_more_bars() {
    List<jatatui.widgets.barchart.Bar> bars = new ArrayList<>();
    for (int i = 0; i < 30; i++) bars.add(jatatui.widgets.barchart.Bar.empty().withValue(i));

    BarChart chart = BarChart.empty().withGroup(BarGroup.empty().withBars(bars));

    Buffer buffer = Buffer.empty(new Rect(0, 0, 59, 1));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines("        ▁ ▁ ▁ ▁ ▂ ▂ ▂ ▃ ▃ ▃ ▃ ▄ ▄ ▄ ▄ ▅ ▅ ▅ ▆ ▆ ▆ ▆ ▇ ▇ ▇ █");
    assertBufferEq(buffer, expected);
  }

  @Test
  void first_bar_of_the_group_is_half_outside_view() {
    BarChart chart =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("a", 1), BarGroup.LabelledValue.of("b", 2))
            .withData(BarGroup.LabelledValue.of("a", 1), BarGroup.LabelledValue.of("b", 2))
            .withBarWidth(2);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 7, 6));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "   ██  ",
            "   ██  ",
            "▄▄ ██  ",
            "██ ██  ",
            "1█ 2█  ",
            "a  b   ");
    assertBufferEq(buffer, expected);
  }

  @Test
  void test_barchart_new() {
    List<jatatui.widgets.barchart.Bar> bars =
        List.of(
            jatatui.widgets.barchart.Bar.withLabel("Red", 1),
            jatatui.widgets.barchart.Bar.withLabel("Green", 2));

    BarChart chart = BarChart.of(bars);
    assertEquals(1, chart.data().size());
    assertEquals(bars, chart.data().get(0).bars);

    BarChart updatedChart = chart.withData(BarGroup.LabelledValue.of("Blue", 3));
    assertEquals(2, updatedChart.data().size());
    assertEquals(
        List.of(jatatui.widgets.barchart.Bar.withLabel("Blue", 3)),
        updatedChart.data().get(1).bars);
  }

  @Test
  void regression_1928() {
    String textValue = " ";
    List<jatatui.widgets.barchart.Bar> bars =
        List.of(
            jatatui.widgets.barchart.Bar.empty().withTextValue(textValue).withValue(0),
            jatatui.widgets.barchart.Bar.empty().withTextValue(textValue).withValue(1),
            jatatui.widgets.barchart.Bar.empty().withTextValue(textValue).withValue(2),
            jatatui.widgets.barchart.Bar.empty().withTextValue(textValue).withValue(3),
            jatatui.widgets.barchart.Bar.empty().withTextValue(textValue).withValue(4));
    BarChart chart =
        BarChart.empty()
            .withGroup(BarGroup.empty().withBars(bars))
            .withBarGap(0)
            .withDirection(Direction.Horizontal);
    Buffer buffer = Buffer.empty(new Rect(0, 0, 4, 5));
    chart.render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "    ",
            "    ",
            " █  ",
            " ██ ",
            " ███");
    assertBufferEq(buffer, expected);
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  void render_in_minimal_buffer(Direction direction) {
    BarChart chart =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("A", 1), BarGroup.LabelledValue.of("B", 2))
            .withBarWidth(3)
            .withBarGap(1)
            .withDirection(direction);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 1, 1));
    // This should not panic, even if the buffer is too small to render the chart.
    chart.render(buffer.area, buffer);
    assertBufferEq(buffer, Buffer.withLines(" "));
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  void render_in_zero_size_buffer(Direction direction) {
    BarChart chart =
        BarChart.empty()
            .withData(BarGroup.LabelledValue.of("A", 1), BarGroup.LabelledValue.of("B", 2))
            .withBarWidth(3)
            .withBarGap(1)
            .withDirection(direction);

    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 0));
    // This should not panic, even if the buffer has zero size.
    chart.render(buffer.area, buffer);
  }
}
