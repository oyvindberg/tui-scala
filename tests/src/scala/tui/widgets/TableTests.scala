package tui
package widgets

import tui.internal.Ranges
import tui.widgets.TableWidget.Row

import java.util.Optional

class TableTests extends TuiTest {
  def cell(str: String): TableWidget.Cell = new TableWidget.Cell(Text.nostyle(str), Style.DEFAULT)

  /// Build a Row with given cells (height=1, no style, no bottomMargin).
  def row(cells: Array[TableWidget.Cell]): Row = new Row(cells, 1, Style.DEFAULT, 0)

  /// Build a Row with given cells and given height.
  def row(cells: Array[TableWidget.Cell], height: Int): Row = new Row(cells, height, Style.DEFAULT, 0)

  /// Build a Row with given cells and bottom margin.
  def rowWithBottomMargin(cells: Array[TableWidget.Cell], bottomMargin: Int): Row =
    new Row(cells, 1, Style.DEFAULT, bottomMargin)

  /// Build a Row with given cells and given style.
  def rowWithStyle(cells: Array[TableWidget.Cell], style: Style): Row =
    new Row(cells, 1, style, 0)

  test("widgets_table_column_spacing_can_be_changed") {
    def test_case(column_spacing: Int, expected: Buffer): Unit = {
      val backend = TestBackend(30, 10)
      val terminal = Terminal.init(backend)

      terminal.draw { f =>
        val table = TableWidget
          .empty(
            Array(
              row(Array("Row11", "Row12", "Row13").map(cell)),
              row(Array("Row21", "Row22", "Row23").map(cell)),
              row(Array("Row31", "Row32", "Row33").map(cell)),
              row(Array("Row41", "Row42", "Row43").map(cell))
            )
          )
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withWidths(Array(new Constraint.Length(5), new Constraint.Length(5), new Constraint.Length(5)))
          .withColumnSpacing(column_spacing)
          .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
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
        val table = TableWidget
          .empty(
            Array(
              row(Array("Row11", "Row12", "Row13").map(cell)),
              row(Array("Row21", "Row22", "Row23").map(cell)),
              row(Array("Row31", "Row32", "Row33").map(cell)),
              row(Array("Row41", "Row42", "Row43").map(cell))
            )
          )
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withWidths(widths)
          .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
        f.renderWidget(table, f.size)
      }
      assertBuffer(backend, expected)
    }

    // columns of zero width show nothing
    test_case(
      Array(new Constraint.Length(0), new Constraint.Length(0), new Constraint.Length(0)),
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
      Array(new Constraint.Length(1), new Constraint.Length(1), new Constraint.Length(1)),
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
      Array(new Constraint.Length(8), new Constraint.Length(8), new Constraint.Length(8)),
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
        val table = TableWidget
          .empty(
            Array(
              row(Array("Row11", "Row12", "Row13").map(cell)),
              row(Array("Row21", "Row22", "Row23").map(cell)),
              row(Array("Row31", "Row32", "Row33").map(cell)),
              row(Array("Row41", "Row42", "Row43").map(cell))
            )
          )
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withWidths(widths)
          .withColumnSpacing(0)
          .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
        f.renderWidget(table, f.size)
      }
      assertBuffer(backend, expected)
    }

    // columns of zero width show nothing
    test_case(
      Array(new Constraint.Percentage(0), new Constraint.Percentage(0), new Constraint.Percentage(0)),
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
      Array(new Constraint.Percentage(11), new Constraint.Percentage(11), new Constraint.Percentage(11)),
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
      Array(new Constraint.Percentage(33), new Constraint.Percentage(33), new Constraint.Percentage(33)),
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
      Array(new Constraint.Percentage(50), new Constraint.Percentage(50)),
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
        val table = TableWidget
          .empty(
            Array(
              row(Array("Row11", "Row12", "Row13").map(cell)),
              row(Array("Row21", "Row22", "Row23").map(cell)),
              row(Array("Row31", "Row32", "Row33").map(cell)),
              row(Array("Row41", "Row42", "Row43").map(cell))
            )
          )
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withWidths(widths)
          .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
        f.renderWidget(table, f.size)
      }

      assertBuffer(backend, expected)
    }

    // columns of zero width show nothing
    test_case(
      Array(new Constraint.Percentage(0), new Constraint.Length(0), new Constraint.Percentage(0)),
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
      Array(new Constraint.Percentage(11), new Constraint.Length(20), new Constraint.Percentage(11)),
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
      Array(new Constraint.Percentage(33), new Constraint.Length(10), new Constraint.Percentage(33)),
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
      Array(new Constraint.Percentage(60), new Constraint.Length(10), new Constraint.Percentage(60)),
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
        val table = TableWidget
          .empty(
            Array(
              row(Array("Row11", "Row12", "Row13").map(cell)),
              row(Array("Row21", "Row22", "Row23").map(cell)),
              row(Array("Row31", "Row32", "Row33").map(cell)),
              row(Array("Row41", "Row42", "Row43").map(cell))
            )
          )
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withWidths(widths)
          .withColumnSpacing(0)
          .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
        f.renderWidget(table, f.size)
      }
      assertBuffer(backend, expected)
    }

    // columns of zero width show nothing
    test_case(
      Array(new Constraint.Ratio(0, 1), new Constraint.Ratio(0, 1), new Constraint.Ratio(0, 1)),
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
      Array(new Constraint.Ratio(1, 9), new Constraint.Ratio(1, 9), new Constraint.Ratio(1, 9)),
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
      Array(new Constraint.Ratio(1, 3), new Constraint.Ratio(1, 3), new Constraint.Ratio(1, 3)),
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
      Array(new Constraint.Ratio(1, 2), new Constraint.Ratio(1, 2)),
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
        val table = TableWidget
          .empty(
            Array(
              row(Array("Row11", "Row12", "Row13").map(cell)),
              row(Array("Row21", "Row22", "Row23").map(cell), 2),
              row(Array("Row31", "Row32", "Row33").map(cell)),
              row(Array("Row41", "Row42", "Row43").map(cell), 2)
            )
          )
          .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
          .withWidths(Array(new Constraint.Length(5), new Constraint.Length(5), new Constraint.Length(5)))
          .withHighlightSymbol(">> ")
          .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
        f.renderStatefulWidget(table, f.size, state)
      }
      assertBuffer(backend, expected)
    }

    val state = TableWidget.State.empty()
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
    state.select(Optional.of(Integer.valueOf(0)))
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
    state.select(Optional.of(Integer.valueOf(1)))
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
    state.select(Optional.of(Integer.valueOf(3)))
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
    val state = TableWidget.State.empty()
    state.select(Optional.of(Integer.valueOf(0)))
    terminal.draw { f =>
      val table = TableWidget
        .empty(
          Array(
            rowWithStyle(Array("Row11", "Row12", "Row13").map(cell), Style.empty().withFg(Color.Green)),
            rowWithStyle(
              Array(
                new TableWidget.Cell(Text.nostyle("Row21"), Style.DEFAULT),
                new TableWidget.Cell(Text.nostyle("Row22"), Style.empty().withFg(Color.Yellow)),
                new TableWidget.Cell(
                  Text.fromSpans(Span.nostyle("Row"), Span.styled("23", Style.DEFAULT.withFg(Color.Blue))),
                  Style.empty().withFg(Color.Red)
                )
              ),
              Style.empty().withFg(Color.LightGreen)
            )
          )
        )
        .withBlock(BlockWidget.empty().withBorders(Borders.LEFT.or(Borders.RIGHT)))
        .withWidths(Array(new Constraint.Length(6), new Constraint.Length(6), new Constraint.Length(6)))
        .withHighlightStyle(Style.empty().withAddModifier(Modifier.BOLD))
        .withHighlightSymbol(">> ")
        .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
      f.renderStatefulWidget(table, f.size, state)
    }

    val expected = Buffer.withLines(
      "│   Head1  Head2  Head3      │",
      "│                            │",
      "│>> Row11  Row12  Row13      │",
      "│   Row21  Row22  Row23      │"
    )
    // First row = row color + highlight style
    Ranges.range(1, 29, (col: Int) => {
      expected.get(col, 2).setStyle(Style.DEFAULT.withFg(Color.Green).withAddModifier(Modifier.BOLD))
      ()
    })
    // Second row:
    // 1. row color
    Ranges.range(1, 29, (col: Int) => {
      expected.get(col, 3).setStyle(Style.DEFAULT.withFg(Color.LightGreen))
      ()
    })
    // 2. cell color
    Ranges.range(11, 17, (col: Int) => {
      expected.get(col, 3).setStyle(Style.DEFAULT.withFg(Color.Yellow))
      ()
    })
    Ranges.range(18, 24, (col: Int) => {
      expected.get(col, 3).setStyle(Style.DEFAULT.withFg(Color.Red))
      ()
    })
    // 3. text color
    Ranges.range(21, 23, (col: Int) => {
      expected.get(col, 3).setStyle(Style.DEFAULT.withFg(Color.Blue))
      ()
    })
    assertBuffer(backend, expected)
  }

  test("widgets_table_should_render_even_if_empty") {
    val backend = TestBackend(30, 4)
    val terminal = Terminal.init(backend)
    terminal.draw { f =>
      val table = TableWidget
        .empty(Array.empty[Row])
        .withBlock(BlockWidget.empty().withBorders(Borders.LEFT.or(Borders.RIGHT)))
        .withWidths(Array(new Constraint.Length(6), new Constraint.Length(6), new Constraint.Length(6)))
        .withHeader(row(Array("Head1", "Head2", "Head3").map(cell)))
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
        f.renderStatefulWidget(table, f.size, state)
      }
      ()
    }

    // based on https://github.com/fdehau/tui-rs/issues/470#issuecomment-852562848
    val table1_width = 98
    val table1 = TableWidget
      .empty(Array(row(Array("r1", "r2", "r3", "r4").map(cell))))
      .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
      .withWidths(
        Array(
          new Constraint.Percentage(15),
          new Constraint.Percentage(15),
          new Constraint.Percentage(25),
          new Constraint.Percentage(45)
        )
      )
      .withHighlightSymbol(">> ")
      .withHeader(row(Array("h1", "h2", "h3", "h4").map(cell)))

    val state = TableWidget.State.empty()

    // select first, which would cause a panic before fix
    state.select(Optional.of(Integer.valueOf(0)))
    test_case(state, table1, table1_width)
  }

  ignore("widgets_table_should_clamp_offset_if_rows_are_removed") {
    val backend = TestBackend(30, 8)
    val terminal = Terminal.init(backend)
    val state = TableWidget.State.empty()

    // render with 6 items => offset will be at 2
    state.select(Optional.of(Integer.valueOf(5)))
    terminal.draw { f =>
      val table = TableWidget
        .empty(
          Array(
            row(Array("Row01", "Row02", "Row03").map(cell)),
            row(Array("Row11", "Row12", "Row13").map(cell)),
            row(Array("Row21", "Row22", "Row23").map(cell)),
            row(Array("Row31", "Row32", "Row33").map(cell)),
            row(Array("Row41", "Row42", "Row43").map(cell)),
            row(Array("Row51", "Row52", "Row53").map(cell))
          )
        )
        .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
        .withWidths(Array(new Constraint.Length(5), new Constraint.Length(5), new Constraint.Length(5)))
        .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
      f.renderStatefulWidget(table, f.size, state)
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
    state.select(Optional.of(Integer.valueOf(1)))
    terminal.draw { f =>
      val table = TableWidget
        .empty(Array(row(Array("Row31", "Row32", "Row33").map(cell))))
        .withBlock(BlockWidget.empty().withBorders(Borders.ALL))
        .withWidths(Array(new Constraint.Length(5), new Constraint.Length(5), new Constraint.Length(5)))
        .withHeader(rowWithBottomMargin(Array("Head1", "Head2", "Head3").map(cell), 1))
      f.renderStatefulWidget(table, f.size, state)
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
