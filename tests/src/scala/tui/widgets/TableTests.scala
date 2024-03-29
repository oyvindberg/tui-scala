package tui
package widgets

import tui.internal.ranges
import tui.widgets.TableWidget.Row

class TableTests extends TuiTest {
  def cell(str: String): TableWidget.Cell = TableWidget.Cell(Text.nostyle(str))

  test("widgets_table_column_spacing_can_be_changed") {
    def test_case(column_spacing: Int, expected: Buffer): Unit = {
      val backend = TestBackend(30, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val table = TableWidget(
          block = Some(BlockWidget(borders = Borders.ALL)),
          widths = Array(Constraint.Length(5), Constraint.Length(5), Constraint.Length(5)),
          columnSpacing = column_spacing,
          header = Some(Row(cells = Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
          rows = Array(
            Row(Array("Row11", "Row12", "Row13").map(cell)),
            Row(Array("Row21", "Row22", "Row23").map(cell)),
            Row(Array("Row31", "Row32", "Row33").map(cell)),
            Row(Array("Row41", "Row42", "Row43").map(cell))
          )
        )
        f.renderWidget(table, f.size);
      }
      assertBuffer(backend, expected)
    }

    // no space between columns
    test_case(
      0,
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1Head2Head3             │",
        "│                            │",
        "│Row11Row12Row13             │",
        "│Row21Row22Row23             │",
        "│Row31Row32Row33             │",
        "│Row41Row42Row43             │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // one space between columns
    test_case(
      1,
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1 Head2 Head3           │",
        "│                            │",
        "│Row11 Row12 Row13           │",
        "│Row21 Row22 Row23           │",
        "│Row31 Row32 Row33           │",
        "│Row41 Row42 Row43           │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // enough space to just not hide the third column
    test_case(
      6,
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1      Head2      Head3 │",
        "│                            │",
        "│Row11      Row12      Row13 │",
        "│Row21      Row22      Row23 │",
        "│Row31      Row32      Row33 │",
        "│Row41      Row42      Row43 │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // enough space to hide part of the third column
    test_case(
      7,
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1       Head2       Head│",
        "│                            │",
        "│Row11       Row12       Row1│",
        "│Row21       Row22       Row2│",
        "│Row31       Row32       Row3│",
        "│Row41       Row42       Row4│",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )
  }

  test("widgets_table_columns_widths_can_use_fixed_length_constraints") {
    def test_case(widths: Array[Constraint], expected: Buffer): Unit = {
      val backend = TestBackend(30, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val table = TableWidget(
          block = Some(BlockWidget(borders = Borders.ALL)),
          widths = widths,
          header = Some(Row(Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
          rows = Array(
            Row(Array("Row11", "Row12", "Row13").map(cell)),
            Row(Array("Row21", "Row22", "Row23").map(cell)),
            Row(Array("Row31", "Row32", "Row33").map(cell)),
            Row(Array("Row41", "Row42", "Row43").map(cell))
          )
        )
        f.renderWidget(table, f.size)
      }
      assertBuffer(backend, expected)
    }

    // columns of zero width show nothing
    test_case(
      Array(Constraint.Length(0), Constraint.Length(0), Constraint.Length(0)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of 1 width trim
    test_case(
      Array(Constraint.Length(1), Constraint.Length(1), Constraint.Length(1)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│H H H                       │",
        "│                            │",
        "│R R R                       │",
        "│R R R                       │",
        "│R R R                       │",
        "│R R R                       │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of large width just before pushing a column off
    test_case(
      Array(Constraint.Length(8), Constraint.Length(8), Constraint.Length(8)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1    Head2    Head3     │",
        "│                            │",
        "│Row11    Row12    Row13     │",
        "│Row21    Row22    Row23     │",
        "│Row31    Row32    Row33     │",
        "│Row41    Row42    Row43     │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )
  }

  test("widgets_table_columns_widths_can_use_percentage_constraints") {
    def test_case(widths: Array[Constraint], expected: Buffer): Unit = {
      val backend = TestBackend(30, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val table = TableWidget(
          block = Some(BlockWidget(borders = Borders.ALL)),
          widths = widths,
          columnSpacing = 0,
          header = Some(Row(cells = Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
          rows = Array(
            Row(Array("Row11", "Row12", "Row13").map(cell)),
            Row(Array("Row21", "Row22", "Row23").map(cell)),
            Row(Array("Row31", "Row32", "Row33").map(cell)),
            Row(Array("Row41", "Row42", "Row43").map(cell))
          )
        )
        f.renderWidget(table, f.size)
      }
      assertBuffer(backend, expected)
    }

    // columns of zero width show nothing
    test_case(
      Array(Constraint.Percentage(0), Constraint.Percentage(0), Constraint.Percentage(0)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of not enough width trims the data
    test_case(
      Array(Constraint.Percentage(11), Constraint.Percentage(11), Constraint.Percentage(11)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│HeaHeaHea                   │",
        "│                            │",
        "│RowRowRow                   │",
        "│RowRowRow                   │",
        "│RowRowRow                   │",
        "│RowRowRow                   │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of large width just before pushing a column off
    test_case(
      Array(Constraint.Percentage(33), Constraint.Percentage(33), Constraint.Percentage(33)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1    Head2    Head3     │",
        "│                            │",
        "│Row11    Row12    Row13     │",
        "│Row21    Row22    Row23     │",
        "│Row31    Row32    Row33     │",
        "│Row41    Row42    Row43     │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // percentages summing to 100 should give equal widths
    test_case(
      Array(Constraint.Percentage(50), Constraint.Percentage(50)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1         Head2         │",
        "│                            │",
        "│Row11         Row12         │",
        "│Row21         Row22         │",
        "│Row31         Row32         │",
        "│Row41         Row42         │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )
  }

  test("widgets_table_columns_widths_can_use_mixed_constraints") {
    def test_case(widths: Array[Constraint], expected: Buffer): Unit = {
      val backend = TestBackend(30, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val table = TableWidget(
          block = Some(BlockWidget(borders = Borders.ALL)),
          widths = widths,
          header = Some(Row(cells = Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
          rows = Array(
            Row(Array("Row11", "Row12", "Row13").map(cell)),
            Row(Array("Row21", "Row22", "Row23").map(cell)),
            Row(Array("Row31", "Row32", "Row33").map(cell)),
            Row(Array("Row41", "Row42", "Row43").map(cell))
          )
        )
        f.renderWidget(table, f.size)
      }

      assertBuffer(backend, expected)
    }

    // columns of zero width show nothing
    test_case(
      Array(Constraint.Percentage(0), Constraint.Length(0), Constraint.Percentage(0)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of not enough width trims the data
    test_case(
      Array(Constraint.Percentage(11), Constraint.Length(20), Constraint.Percentage(11)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Hea Head2                He │",
        "│                            │",
        "│Row Row12                Ro │",
        "│Row Row22                Ro │",
        "│Row Row32                Ro │",
        "│Row Row42                Ro │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of large width just before pushing a column off
    test_case(
      Array(Constraint.Percentage(33), Constraint.Length(10), Constraint.Percentage(33)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1     Head2      Head3  │",
        "│                            │",
        "│Row11     Row12      Row13  │",
        "│Row21     Row22      Row23  │",
        "│Row31     Row32      Row33  │",
        "│Row41     Row42      Row43  │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of large size (>100% total) hide the last column
    test_case(
      Array(Constraint.Percentage(60), Constraint.Length(10), Constraint.Percentage(60)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1            Head2      │",
        "│                            │",
        "│Row11            Row12      │",
        "│Row21            Row22      │",
        "│Row31            Row32      │",
        "│Row41            Row42      │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )
  }

  test("widgets_table_columns_widths_can_use_ratio_constraints") {
    def test_case(widths: Array[Constraint], expected: Buffer): Unit = {
      val backend = TestBackend(30, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val table = TableWidget(
          block = Some(BlockWidget(borders = Borders.ALL)),
          widths = widths,
          columnSpacing = 0,
          header = Some(Row(cells = Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
          rows = Array(
            Row(Array("Row11", "Row12", "Row13").map(cell)),
            Row(Array("Row21", "Row22", "Row23").map(cell)),
            Row(Array("Row31", "Row32", "Row33").map(cell)),
            Row(Array("Row41", "Row42", "Row43").map(cell))
          )
        )
        f.renderWidget(table, f.size)
      }
      assertBuffer(backend, expected)
    }

    // columns of zero width show nothing
    test_case(
      Array(Constraint.Ratio(0, 1), Constraint.Ratio(0, 1), Constraint.Ratio(0, 1)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of not enough width trims the data
    test_case(
      Array(Constraint.Ratio(1, 9), Constraint.Ratio(1, 9), Constraint.Ratio(1, 9)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│HeaHeaHea                   │",
        "│                            │",
        "│RowRowRow                   │",
        "│RowRowRow                   │",
        "│RowRowRow                   │",
        "│RowRowRow                   │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // columns of large width just before pushing a column off
    test_case(
      Array(Constraint.Ratio(1, 3), Constraint.Ratio(1, 3), Constraint.Ratio(1, 3)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1    Head2    Head3     │",
        "│                            │",
        "│Row11    Row12    Row13     │",
        "│Row21    Row22    Row23     │",
        "│Row31    Row32    Row33     │",
        "│Row41    Row42    Row43     │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )

    // percentages summing to 100 should give equal widths
    test_case(
      Array(Constraint.Ratio(1, 2), Constraint.Ratio(1, 2)),
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1         Head2         │",
        "│                            │",
        "│Row11         Row12         │",
        "│Row21         Row22         │",
        "│Row31         Row32         │",
        "│Row41         Row42         │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )
  }

  test("widgets_table_can_have_rows_with_multi_lines") {
    def test_case(state: TableWidget.State, expected: Buffer): Unit = {
      val backend = TestBackend(30, 8)
      val terminal = Terminal.init(backend)
      terminal.draw { f =>
        val table = TableWidget(
          block = Some(BlockWidget(borders = Borders.ALL)),
          widths = Array(Constraint.Length(5), Constraint.Length(5), Constraint.Length(5)),
          highlightSymbol = Some(">> "),
          header = Some(Row(cells = Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
          rows = Array(
            Row(cells = Array("Row11", "Row12", "Row13").map(cell)),
            Row(cells = Array("Row21", "Row22", "Row23").map(cell), height = 2),
            Row(cells = Array("Row31", "Row32", "Row33").map(cell)),
            Row(cells = Array("Row41", "Row42", "Row43").map(cell), height = 2)
          )
        )
        f.renderStatefulWidget(table, f.size)(state)
      }
      assertBuffer(backend, expected)
    }

    val state = TableWidget.State()
    // no selection
    test_case(
      state,
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│Head1 Head2 Head3           │",
        "│                            │",
        "│Row11 Row12 Row13           │",
        "│Row21 Row22 Row23           │",
        "│                            │",
        "│Row31 Row32 Row33           │",
        "└────────────────────────────┘"
      )
    )

    // select first
    state.select(Some(0))
    test_case(
      state,
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│   Head1 Head2 Head3        │",
        "│                            │",
        "│>> Row11 Row12 Row13        │",
        "│   Row21 Row22 Row23        │",
        "│                            │",
        "│   Row31 Row32 Row33        │",
        "└────────────────────────────┘"
      )
    )

    // select second (we don't show partially the 4th row)
    state.select(Some(1))
    test_case(
      state,
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│   Head1 Head2 Head3        │",
        "│                            │",
        "│   Row11 Row12 Row13        │",
        "│>> Row21 Row22 Row23        │",
        "│                            │",
        "│   Row31 Row32 Row33        │",
        "└────────────────────────────┘"
      )
    )

    // select 4th (we don't show partially the 1st row)
    state.select(Some(3))
    test_case(
      state,
      Buffer.withLines(
        "┌────────────────────────────┐",
        "│   Head1 Head2 Head3        │",
        "│                            │",
        "│   Row31 Row32 Row33        │",
        "│>> Row41 Row42 Row43        │",
        "│                            │",
        "│                            │",
        "└────────────────────────────┘"
      )
    )
  }

  test("widgets_table_can_have_elements_styled_individually") {
    val backend = TestBackend(30, 4)
    val terminal = Terminal.init(backend)
    val state = TableWidget.State()
    state.select(Some(0))
    terminal.draw { f =>
      val table = TableWidget(
        block = Some(BlockWidget(borders = Borders.LEFT | Borders.RIGHT)),
        widths = Array(Constraint.Length(6), Constraint.Length(6), Constraint.Length(6)),
        highlightStyle = Style(addModifier = Modifier.BOLD),
        highlightSymbol = Some(">> "),
        header = Some(Row(Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
        rows = Array(
          Row(cells = Array("Row11", "Row12", "Row13").map(cell), style = Style(fg = Some(Color.Green))),
          Row(
            cells = Array(
              TableWidget.Cell(content = Text.nostyle("Row21")),
              TableWidget.Cell(content = Text.nostyle("Row22"), style = Style(fg = Some(Color.Yellow))),
              TableWidget.Cell(
                content = Text.from(Span.nostyle("Row"), Span.styled("23", Style.DEFAULT.fg(Color.Blue))),
                style = Style(fg = Some(Color.Red))
              )
            ),
            style = Style(fg = Some(Color.LightGreen))
          )
        )
      )
      f.renderStatefulWidget(table, f.size)(state)
    }

    val expected = Buffer.withLines(
      "│   Head1  Head2  Head3      │",
      "│                            │",
      "│>> Row11  Row12  Row13      │",
      "│   Row21  Row22  Row23      │"
    )
    // First row = row color + highlight style
    ranges.range(1, 29) { col =>
      expected.get(col, 2).setStyle(Style.DEFAULT.fg(Color.Green).addModifier(Modifier.BOLD))
      ()
    }
    // Second row:
    // 1. row color
    internal.ranges.range(1, 29) { col =>
      expected.get(col, 3).setStyle(Style.DEFAULT.fg(Color.LightGreen))
      ()
    }
    // 2. cell color
    internal.ranges.range(11, 17) { col =>
      expected.get(col, 3).setStyle(Style.DEFAULT.fg(Color.Yellow))
      ()
    }
    internal.ranges.range(18, 24) { col =>
      expected.get(col, 3).setStyle(Style.DEFAULT.fg(Color.Red))
      ()
    }
    // 3. text color
    internal.ranges.range(21, 23) { col =>
      expected.get(col, 3).setStyle(Style.DEFAULT.fg(Color.Blue))
      ()
    }
    assertBuffer(backend, expected)
  }

  test("widgets_table_should_render_even_if_empty") {
    val backend = TestBackend(30, 4)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val table = TableWidget(
        block = Some(BlockWidget(borders = Borders.LEFT | Borders.RIGHT)),
        widths = Array(Constraint.Length(6), Constraint.Length(6), Constraint.Length(6)),
        header = Some(Row(Array("Head1", "Head2", "Head3").map(cell))),
        rows = Array()
      )
      f.renderWidget(table, f.size)
    }

    val expected = Buffer.withLines(
      "│Head1  Head2  Head3         │",
      "│                            │",
      "│                            │",
      "│                            │"
    )

    assertBuffer(backend, expected)
  }

  test("widgets_table_columns_dont_panic") {
    def test_case(state: TableWidget.State, table: TableWidget, width: Int): Unit = {
      val backend = TestBackend(width, 8)
      val terminal = Terminal.init(backend)
      terminal.draw { f =>
        f.renderStatefulWidget(table, f.size)(state)
      }
      ()
    }

    // based on https://github.com/fdehau/tui-rs/issues/470#issuecomment-852562848
    val table1_width = 98
    val table1 = TableWidget(
      block = Some(BlockWidget(borders = Borders.ALL)),
      widths = Array(Constraint.Percentage(15), Constraint.Percentage(15), Constraint.Percentage(25), Constraint.Percentage(45)),
      highlightSymbol = Some(">> "),
      header = Some(Row(Array("h1", "h2", "h3", "h4").map(cell))),
      rows = Array(Row(Array("r1", "r2", "r3", "r4").map(cell)))
    )

    val state = TableWidget.State()

    // select first, which would cause a panic before fix
    state.select(Some(0))
    test_case(state, table1, table1_width)
  }

  ignore("widgets_table_should_clamp_offset_if_rows_are_removed") {
    val backend = TestBackend(30, 8)
    val terminal = Terminal.init(backend)
    val state = TableWidget.State()

    // render with 6 items => offset will be at 2
    state.select(Some(5))
    terminal.draw { f =>
      val table = TableWidget(
        block = Some(BlockWidget(borders = Borders.ALL)),
        widths = Array(Constraint.Length(5), Constraint.Length(5), Constraint.Length(5)),
        header = Some(Row(cells = Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
        rows = Array(
          Row(Array("Row01", "Row02", "Row03").map(cell)),
          Row(Array("Row11", "Row12", "Row13").map(cell)),
          Row(Array("Row21", "Row22", "Row23").map(cell)),
          Row(Array("Row31", "Row32", "Row33").map(cell)),
          Row(Array("Row41", "Row42", "Row43").map(cell)),
          Row(Array("Row51", "Row52", "Row53").map(cell))
        )
      )
      f.renderStatefulWidget(table, f.size)(state)
    }
    val expected0 = Buffer.withLines(
      "┌────────────────────────────┐",
      "│Head1 Head2 Head3           │",
      "│                            │",
      "│Row21 Row22 Row23           │",
      "│Row31 Row32 Row33           │",
      "│Row41 Row42 Row43           │",
      "│Row51 Row52 Row53           │",
      "└────────────────────────────┘"
    )
    assertBuffer(backend, expected0)

    // render with 1 item => offset will be at 1
    state.select(Some(1))
    terminal.draw { f =>
      val table = TableWidget(
        block = Some(BlockWidget(borders = Borders.ALL)),
        widths = Array(Constraint.Length(5), Constraint.Length(5), Constraint.Length(5)),
        header = Some(Row(Array("Head1", "Head2", "Head3").map(cell), bottomMargin = 1)),
        rows = Array(Row(Array("Row31", "Row32", "Row33").map(cell)))
      )
      f.renderStatefulWidget(table, f.size)(state)
    }
    val expected1 = Buffer.withLines(
      "┌────────────────────────────┐",
      "│Head1 Head2 Head3           │",
      "│                            │",
      "│Row31 Row32 Row33           │",
      "│                            │",
      "│                            │",
      "│                            │",
      "└────────────────────────────┘"
    )
    assertBuffer(backend, expected1)
  }
}
