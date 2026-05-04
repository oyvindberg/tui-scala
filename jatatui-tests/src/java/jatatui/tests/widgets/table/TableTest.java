package jatatui.tests.widgets.table;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.widgets.block.Block;
import jatatui.widgets.table.HighlightSpacing;
import jatatui.widgets.table.Row;
import jatatui.widgets.table.Table;
import jatatui.widgets.table.TableCell;
import jatatui.widgets.table.TableState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Inline tests from `ratatui_widgets::table` (the body of `table.rs`).
///
/// Mapped tests:
/// - `new`, `default`, `collect`, `widths`, `rows`, `column_spacing`, `block`, `header`, `footer`,
///   `row_highlight_style`, `column_highlight_style`, `cell_highlight_style`, `highlight_symbol`,
///   `highlight_spacing`, `table_invalid_percentages`, `widths_conversions`,
///   `column_count` (parameterised), `stylize`, `render_in_minimal_buffer`,
///   `render_in_zero_size_buffer`.
/// - `state::test_list_state_empty_list`, `state::test_list_state_single_item`.
/// - `render::*` (all of them).
/// - `column_widths::*` (all of them).
///
/// Skipped:
/// - upstream `highlight_style` (deprecated): N/A. Java does not carry the deprecated
///   `highlight_style` setter — only `withRowHighlightStyle` (the replacement) exists.
public class TableTest {

  // ---- Builder / constructor tests ----------------------------------------

  @Test
  public void new_() {
    List<Row> rows = List.of(Row.of(TableCell.of("")));
    List<Constraint> widths = List.of(new Constraint.Percentage(100));
    Table table = Table.of(rows, widths);
    assertEquals(rows, table.rows());
    assertEquals(Optional.empty(), table.header());
    assertEquals(Optional.empty(), table.footer());
    assertEquals(widths, table.widths());
    assertEquals(1, table.columnSpacing());
    assertEquals(Optional.empty(), table.block());
    assertEquals(Style.empty(), table.style());
    assertEquals(Style.empty(), table.rowHighlightStyle());
    assertEquals(Text.empty(), table.highlightSymbol());
    assertEquals(HighlightSpacing.WhenSelected, table.highlightSpacing());
    assertEquals(Flex.Start, table.flex());
  }

  @Test
  public void default_() {
    Table table = Table.empty();
    assertEquals(List.of(), table.rows());
    assertEquals(Optional.empty(), table.header());
    assertEquals(Optional.empty(), table.footer());
    assertEquals(List.of(), table.widths());
    assertEquals(1, table.columnSpacing());
    assertEquals(Optional.empty(), table.block());
    assertEquals(Style.empty(), table.style());
    assertEquals(Style.empty(), table.rowHighlightStyle());
    assertEquals(Text.empty(), table.highlightSymbol());
    assertEquals(HighlightSpacing.WhenSelected, table.highlightSpacing());
    assertEquals(Flex.Start, table.flex());
  }

  @Test
  public void collect() {
    List<Row> rows = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      List<TableCell> cells = new ArrayList<>();
      for (int j = 0; j < 4; j++) {
        cells.add(TableCell.of(i + "*" + j + " = " + (i * j)));
      }
      rows.add(Row.of(cells));
    }
    List<Constraint> widths =
        List.of(
            new Constraint.Percentage(25),
            new Constraint.Percentage(25),
            new Constraint.Percentage(25),
            new Constraint.Percentage(25));
    Table table = Table.of(rows, widths);

    List<Row> expectedRows =
        List.of(
            Row.ofStrings("0*0 = 0", "0*1 = 0", "0*2 = 0", "0*3 = 0"),
            Row.ofStrings("1*0 = 0", "1*1 = 1", "1*2 = 2", "1*3 = 3"),
            Row.ofStrings("2*0 = 0", "2*1 = 2", "2*2 = 4", "2*3 = 6"),
            Row.ofStrings("3*0 = 0", "3*1 = 3", "3*2 = 6", "3*3 = 9"));
    assertEquals(expectedRows, table.rows());
    assertEquals(widths, table.widths());
  }

  @Test
  public void widths() {
    Table table = Table.empty().withWidths(List.of(new Constraint.Length(100)));
    assertEquals(List.of(new Constraint.Length(100)), table.widths());
  }

  @Test
  public void rows() {
    List<Row> rows = List.of(Row.of(TableCell.of("")));
    Table table = Table.empty().withRows(rows);
    assertEquals(rows, table.rows());
  }

  @Test
  public void column_spacing() {
    Table table = Table.empty().withColumnSpacing(2);
    assertEquals(2, table.columnSpacing());
  }

  @Test
  public void block() {
    Block block = Block.bordered().withTitle(Line.from("Table"));
    Table table = Table.empty().withBlock(block);
    assertEquals(Optional.of(block), table.block());
  }

  @Test
  public void header() {
    Row header = Row.of(TableCell.of(""));
    Table table = Table.empty().withHeader(header);
    assertEquals(Optional.of(header), table.header());
  }

  @Test
  public void footer() {
    Row footer = Row.of(TableCell.of(""));
    Table table = Table.empty().withFooter(footer);
    assertEquals(Optional.of(footer), table.footer());
  }

  @Test
  public void row_highlight_style() {
    Style style = Style.empty().red().italic();
    Table table = Table.empty().withRowHighlightStyle(style);
    assertEquals(style, table.rowHighlightStyle());
  }

  @Test
  public void column_highlight_style() {
    Style style = Style.empty().red().italic();
    Table table = Table.empty().withColumnHighlightStyle(style);
    assertEquals(style, table.columnHighlightStyle());
  }

  @Test
  public void cell_highlight_style() {
    Style style = Style.empty().red().italic();
    Table table = Table.empty().withCellHighlightStyle(style);
    assertEquals(style, table.cellHighlightStyle());
  }

  @Test
  public void highlight_symbol() {
    Table table = Table.empty().withHighlightSymbol(">>");
    assertEquals(Text.from(">>"), table.highlightSymbol());
  }

  @Test
  public void highlight_spacing() {
    Table table = Table.empty().withHighlightSpacing(HighlightSpacing.Always);
    assertEquals(HighlightSpacing.Always, table.highlightSpacing());
  }

  @Test
  public void table_invalid_percentages() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Table.empty().withWidths(List.of(new Constraint.Percentage(110))));
  }

  /// Java has no array/slice/vec coercions - the constructor only accepts `List<Constraint>`.
  /// This test just verifies that a single canonical form works.
  @Test
  public void widths_conversions() {
    List<Constraint> widths = List.of(new Constraint.Percentage(100));
    Table table = Table.of(List.of(), widths);
    assertEquals(widths, table.widths());
  }

  @Test
  public void stylize() {
    Table table =
        Table.of(List.of(Row.of(TableCell.of(""))), List.of(new Constraint.Percentage(100)))
            .black()
            .onWhite()
            .bold()
            .notCrossedOut();
    Style expected =
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.CROSSED_OUT);
    assertEquals(expected, table.style());
  }

  // ---- Stateful render with empty / single-item lists ---------------------

  @Test
  public void test_list_state_empty_list() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 10, 10));
    TableState state = new TableState();
    Table table = Table.of(List.of(), List.of(new Constraint.Percentage(100)));
    state.selectFirst();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.empty(), state.selected());
    assertEquals(Optional.empty(), state.selectedColumn());
  }

  @Test
  public void test_list_state_single_item() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 10, 10));
    Table table =
        Table.of(List.of(Row.ofStrings("Item 1")), List.of(new Constraint.Percentage(100)));

    TableState state = new TableState();
    state.selectFirst();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.of(0), state.selected());
    assertEquals(Optional.empty(), state.selectedColumn());

    state.selectLast();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.of(0), state.selected());
    assertEquals(Optional.empty(), state.selectedColumn());

    state.selectPrevious();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.of(0), state.selected());
    assertEquals(Optional.empty(), state.selectedColumn());

    state.selectNext();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.of(0), state.selected());
    assertEquals(Optional.empty(), state.selectedColumn());

    state = new TableState();
    state.selectFirstColumn();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.of(0), state.selectedColumn());
    assertEquals(Optional.empty(), state.selected());

    state.selectLastColumn();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.of(0), state.selectedColumn());
    assertEquals(Optional.empty(), state.selected());

    state.selectPreviousColumn();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.of(0), state.selectedColumn());
    assertEquals(Optional.empty(), state.selected());

    state.selectNextColumn();
    table.render(buf.area(), buf, state);
    assertEquals(Optional.of(0), state.selectedColumn());
    assertEquals(Optional.empty(), state.selected());
  }

  // ---- render::* tests ----------------------------------------------------

  @Test
  public void render_empty_area() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    Table table = Table.of(List.of(Row.ofStrings("Cell1", "Cell2")), lengths(5, 5));
    table.render(new Rect(0, 0, 0, 0), buf);
    assertBufferEq(buf, Buffer.empty(new Rect(0, 0, 15, 3)));
  }

  @Test
  public void render_default() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    Table.empty().render(new Rect(0, 0, 15, 3), buf);
    assertBufferEq(buf, Buffer.empty(new Rect(0, 0, 15, 3)));
  }

  @Test
  public void render_with_block() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"), Row.ofStrings("Cell3", "Cell4"));
    Block block = Block.bordered().withTitle(Line.from("Block"));
    Table table = Table.of(rows, lengths(5, 5)).withBlock(block);
    table.render(new Rect(0, 0, 15, 3), buf);
    Buffer expected =
        Buffer.withLines("┌Block────────┐",
            "│Cell1 Cell2  │",
            "└─────────────┘");
    assertBufferEq(buf, expected);
  }

  @Test
  public void render_with_header() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    Row header = Row.ofStrings("Head1", "Head2");
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"), Row.ofStrings("Cell3", "Cell4"));
    Table table = Table.of(rows, lengths(5, 5)).withHeader(header);
    table.render(new Rect(0, 0, 15, 3), buf);
    assertBufferEq(buf, Buffer.withLines("Head1 Head2    ", "Cell1 Cell2    ", "Cell3 Cell4    "));
  }

  @Test
  public void render_with_footer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    Row footer = Row.ofStrings("Foot1", "Foot2");
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"), Row.ofStrings("Cell3", "Cell4"));
    Table table = Table.of(rows, lengths(5, 5)).withFooter(footer);
    table.render(new Rect(0, 0, 15, 3), buf);
    assertBufferEq(buf, Buffer.withLines("Cell1 Cell2    ", "Cell3 Cell4    ", "Foot1 Foot2    "));
  }

  @Test
  public void render_with_header_and_footer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    Row header = Row.ofStrings("Head1", "Head2");
    Row footer = Row.ofStrings("Foot1", "Foot2");
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"));
    Table table = Table.of(rows, lengths(5, 5)).withHeader(header).withFooter(footer);
    table.render(new Rect(0, 0, 15, 3), buf);
    assertBufferEq(buf, Buffer.withLines("Head1 Head2    ", "Cell1 Cell2    ", "Foot1 Foot2    "));
  }

  @Test
  public void render_with_header_margin() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    Row header = Row.ofStrings("Head1", "Head2").withBottomMargin(1);
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"), Row.ofStrings("Cell3", "Cell4"));
    Table table = Table.of(rows, lengths(5, 5)).withHeader(header);
    table.render(new Rect(0, 0, 15, 3), buf);
    assertBufferEq(buf, Buffer.withLines("Head1 Head2    ", "               ", "Cell1 Cell2    "));
  }

  @Test
  public void render_with_footer_margin() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    Row footer = Row.ofStrings("Foot1", "Foot2").withTopMargin(1);
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"));
    Table table = Table.of(rows, lengths(5, 5)).withFooter(footer);
    table.render(new Rect(0, 0, 15, 3), buf);
    assertBufferEq(buf, Buffer.withLines("Cell1 Cell2    ", "               ", "Foot1 Foot2    "));
  }

  @Test
  public void render_with_row_margin() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    List<Row> rows =
        List.of(
            Row.ofStrings("Cell1", "Cell2").withBottomMargin(1), Row.ofStrings("Cell3", "Cell4"));
    Table table = Table.of(rows, lengths(5, 5));
    table.render(new Rect(0, 0, 15, 3), buf);
    assertBufferEq(buf, Buffer.withLines("Cell1 Cell2    ", "               ", "Cell3 Cell4    "));
  }

  @Test
  public void render_with_tall_row() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 23, 3));
    Row tall =
        Row.of(
                TableCell.of(Text.raw("Cell3-Line1\nCell3-Line2\nCell3-Line3")),
                TableCell.of(Text.raw("Cell4-Line1\nCell4-Line2\nCell4-Line3")))
            .withHeight(3);
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"), tall);
    Table table = Table.of(rows, lengths(11, 11));
    table.render(new Rect(0, 0, 23, 3), buf);
    assertBufferEq(
        buf,
        Buffer.withLines(
            "Cell1       Cell2      ",
            "Cell3-Line1 Cell4-Line1",
            "Cell3-Line2 Cell4-Line2"));
  }

  @Test
  public void render_with_alignment() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 10, 3));
    List<Row> rows =
        List.of(
            Row.of(TableCell.of(Text.from(Line.from("Left").withAlignment(HorizontalAlignment.Left)))),
            Row.of(
                TableCell.of(
                    Text.from(Line.from("Center").withAlignment(HorizontalAlignment.Center)))),
            Row.of(
                TableCell.of(
                    Text.from(Line.from("Right").withAlignment(HorizontalAlignment.Right)))));
    Table table = Table.of(rows, List.of(new Constraint.Percentage(100)));
    table.render(new Rect(0, 0, 10, 3), buf);
    assertBufferEq(buf, Buffer.withLines("Left      ", "  Center  ", "     Right"));
  }

  @Test
  public void render_with_overflow_does_not_panic() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 20, 3));
    Table table =
        Table.of(List.of(), List.of(new Constraint.Min(20)))
            .withHeader(
                Row.of(
                    TableCell.of(
                        Text.from(Line.from("").withAlignment(HorizontalAlignment.Right)))))
            .withFooter(
                Row.of(
                    TableCell.of(
                        Text.from(Line.from("").withAlignment(HorizontalAlignment.Right)))));
    table.render(new Rect(0, 0, 20, 3), buf);
  }

  @Test
  public void render_with_selected_column_and_incorrect_width_count_does_not_panic() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 20, 3));
    Table table =
        Table.of(List.of(Row.ofStrings("Row1", "Row2", "Row3")), List.of(new Constraint.Length(10)));
    TableState state = new TableState().withSelectedColumn(2);
    table.render(new Rect(0, 0, 20, 3), buf, state);
  }

  @Test
  public void render_with_selected() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"), Row.ofStrings("Cell3", "Cell4"));
    Table table =
        Table.of(rows, lengths(5, 5))
            .withRowHighlightStyle(Style.empty().red())
            .withHighlightSymbol(">>");
    TableState state = new TableState().withSelected(0);
    table.render(new Rect(0, 0, 15, 3), buf, state);
    Line redRow = Line.styled(">>Cell1 Cell2  ", Style.empty().red());
    Buffer expected =
        Buffer.withLineObjects(redRow, Line.from("  Cell3 Cell4  "), Line.from("               "));
    assertBufferEq(buf, expected);
  }

  @Test
  public void render_with_selected_column() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 3));
    List<Row> rows = List.of(Row.ofStrings("Cell1", "Cell2"), Row.ofStrings("Cell3", "Cell4"));
    Table table =
        Table.of(rows, lengths(5, 5))
            .withColumnHighlightStyle(Style.empty().blue())
            .withHighlightSymbol(">>");
    TableState state = new TableState().withSelectedColumn(1);
    table.render(new Rect(0, 0, 15, 3), buf, state);
    Style blue = Style.empty().blue();
    Buffer expected =
        Buffer.withLineObjects(
            Line.fromSpans(
                List.of(
                    Span.raw("Cell1"), Span.raw(" "), Span.styled("Cell2", blue), Span.raw("    "))),
            Line.fromSpans(
                List.of(
                    Span.raw("Cell3"), Span.raw(" "), Span.styled("Cell4", blue), Span.raw("    "))),
            Line.fromSpans(
                List.of(Span.raw("      "), Span.styled("     ", blue), Span.raw("    "))));
    assertBufferEq(buf, expected);
  }

  @Test
  public void render_with_selected_cell() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 20, 4));
    List<Row> rows =
        List.of(
            Row.ofStrings("Cell1", "Cell2", "Cell3"),
            Row.ofStrings("Cell4", "Cell5", "Cell6"),
            Row.ofStrings("Cell7", "Cell8", "Cell9"));
    Table table =
        Table.of(rows, lengths(5, 5, 5))
            .withHighlightSymbol(">>")
            .withCellHighlightStyle(Style.empty().green());
    TableState state = new TableState().withSelectedCell(1, 2);
    table.render(new Rect(0, 0, 20, 4), buf, state);
    Style green = Style.empty().green();
    Buffer expected =
        Buffer.withLineObjects(
            Line.fromSpans(List.of(Span.raw("  Cell1 "), Span.raw("Cell2 "), Span.raw("Cell3"))),
            Line.fromSpans(
                List.of(Span.raw(">>Cell4 Cell5 "), Span.styled("Cell6", green), Span.raw(" "))),
            Line.fromSpans(List.of(Span.raw("  Cell7 "), Span.raw("Cell8 "), Span.raw("Cell9"))),
            Line.fromSpans(List.of(Span.raw("                    "))));
    assertBufferEq(buf, expected);
  }

  @Test
  public void render_with_selected_row_and_column() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 20, 4));
    List<Row> rows =
        List.of(
            Row.ofStrings("Cell1", "Cell2", "Cell3"),
            Row.ofStrings("Cell4", "Cell5", "Cell6"),
            Row.ofStrings("Cell7", "Cell8", "Cell9"));
    Table table =
        Table.of(rows, lengths(5, 5, 5))
            .withHighlightSymbol(">>")
            .withRowHighlightStyle(Style.empty().red())
            .withColumnHighlightStyle(Style.empty().blue());
    TableState state = new TableState().withSelected(1).withSelectedColumn(2);
    table.render(new Rect(0, 0, 20, 4), buf, state);
    Style red = Style.empty().red();
    Style blue = Style.empty().blue();
    Buffer expected =
        Buffer.withLineObjects(
            Line.fromSpans(
                List.of(Span.raw("  Cell1 "), Span.raw("Cell2 "), Span.styled("Cell3", blue))),
            Line.fromSpans(
                List.of(
                    Span.styled(">>Cell4 Cell5 ", red),
                    Span.styled("Cell6", blue),
                    Span.styled(" ", red))),
            Line.fromSpans(
                List.of(Span.raw("  Cell7 "), Span.raw("Cell8 "), Span.styled("Cell9", blue))),
            Line.fromSpans(
                List.of(Span.raw("              "), Span.styled("     ", blue), Span.raw(" "))));
    assertBufferEq(buf, expected);
  }

  @Test
  public void render_with_selected_row_and_column_and_cell() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 20, 4));
    List<Row> rows =
        List.of(
            Row.ofStrings("Cell1", "Cell2", "Cell3"),
            Row.ofStrings("Cell4", "Cell5", "Cell6"),
            Row.ofStrings("Cell7", "Cell8", "Cell9"));
    Table table =
        Table.of(rows, lengths(5, 5, 5))
            .withHighlightSymbol(">>")
            .withRowHighlightStyle(Style.empty().red())
            .withColumnHighlightStyle(Style.empty().blue())
            .withCellHighlightStyle(Style.empty().green());
    TableState state = new TableState().withSelected(1).withSelectedColumn(2);
    table.render(new Rect(0, 0, 20, 4), buf, state);
    Style red = Style.empty().red();
    Style blue = Style.empty().blue();
    Style green = Style.empty().green();
    Buffer expected =
        Buffer.withLineObjects(
            Line.fromSpans(
                List.of(Span.raw("  Cell1 "), Span.raw("Cell2 "), Span.styled("Cell3", blue))),
            Line.fromSpans(
                List.of(
                    Span.styled(">>Cell4 Cell5 ", red),
                    Span.styled("Cell6", green),
                    Span.styled(" ", red))),
            Line.fromSpans(
                List.of(Span.raw("  Cell7 "), Span.raw("Cell8 "), Span.styled("Cell9", blue))),
            Line.fromSpans(
                List.of(Span.raw("              "), Span.styled("     ", blue), Span.raw(" "))));
    assertBufferEq(buf, expected);
  }

  @ParameterizedTest
  @MethodSource("render_with_selection_and_offset_cases")
  public void render_with_selection_and_offset(
      Optional<Integer> selectedRow, int expectedOffset, String[] expectedItems) {
    List<Row> rows = new ArrayList<>(100);
    for (int i = 0; i < 100; i++) {
      rows.add(Row.ofStrings(String.valueOf(i)));
    }
    Table table = Table.of(rows, List.of(new Constraint.Length(2)));
    Buffer buf = Buffer.empty(new Rect(0, 0, 2, 5));
    TableState state = new TableState().withOffset(50).withSelected(selectedRow);
    table.render(new Rect(0, 0, 5, 5), buf, state);
    assertBufferEq(buf, Buffer.withLines(expectedItems));
    assertEquals(expectedOffset, state.offset());
  }

  static Stream<Arguments> render_with_selection_and_offset_cases() {
    return Stream.of(
        // case::no_selection
        Arguments.of(Optional.<Integer>empty(), 50, new String[] {"50", "51", "52", "53", "54"}),
        // case::selection_before_offset
        Arguments.of(Optional.of(20), 20, new String[] {"20", "21", "22", "23", "24"}),
        // case::selection_immediately_before_offset
        Arguments.of(Optional.of(49), 49, new String[] {"49", "50", "51", "52", "53"}),
        // case::selection_at_start_of_offset
        Arguments.of(Optional.of(50), 50, new String[] {"50", "51", "52", "53", "54"}),
        // case::selection_at_end_of_offset
        Arguments.of(Optional.of(54), 50, new String[] {"50", "51", "52", "53", "54"}),
        // case::selection_immediately_after_offset
        Arguments.of(Optional.of(55), 51, new String[] {"51", "52", "53", "54", "55"}),
        // case::selection_after_offset
        Arguments.of(Optional.of(80), 76, new String[] {"76", "77", "78", "79", "80"}));
  }

  // ---- column_widths::* tests --------------------------------------------

  @Test
  public void length_constraint() {
    // without selection, more than needed width
    Table table = Table.empty().withWidths(lengths(4, 4));
    assertEquals(xws(0, 4, 5, 4), table.getColumnWidths(20, 0, 0));

    // with selection, more than needed width
    table = Table.empty().withWidths(lengths(4, 4));
    assertEquals(xws(3, 4, 8, 4), table.getColumnWidths(20, 3, 0));

    // without selection, less than needed width
    table = Table.empty().withWidths(lengths(4, 4));
    assertEquals(xws(0, 3, 4, 3), table.getColumnWidths(7, 0, 0));

    // with selection, less than needed width
    table = Table.empty().withWidths(lengths(4, 4));
    assertEquals(xws(3, 2, 6, 1), table.getColumnWidths(7, 3, 0));
  }

  @Test
  public void max_constraint() {
    Table table = Table.empty().withWidths(maxes(4, 4));
    assertEquals(xws(0, 4, 5, 4), table.getColumnWidths(20, 0, 0));

    table = Table.empty().withWidths(maxes(4, 4));
    assertEquals(xws(3, 4, 8, 4), table.getColumnWidths(20, 3, 0));

    table = Table.empty().withWidths(maxes(4, 4));
    assertEquals(xws(0, 3, 4, 3), table.getColumnWidths(7, 0, 0));

    table = Table.empty().withWidths(maxes(4, 4));
    assertEquals(xws(3, 2, 6, 1), table.getColumnWidths(7, 3, 0));
  }

  @Test
  public void min_constraint() {
    Table table = Table.empty().withWidths(mins(4, 4));
    assertEquals(xws(0, 10, 11, 9), table.getColumnWidths(20, 0, 0));

    table = Table.empty().withWidths(mins(4, 4));
    assertEquals(xws(3, 8, 12, 8), table.getColumnWidths(20, 3, 0));

    table = Table.empty().withWidths(mins(4, 4));
    assertEquals(xws(0, 3, 4, 3), table.getColumnWidths(7, 0, 0));

    table = Table.empty().withWidths(mins(4, 4));
    assertEquals(xws(3, 2, 6, 1), table.getColumnWidths(7, 3, 0));
  }

  @Test
  public void percentage_constraint() {
    Table table = Table.empty().withWidths(percentages(30, 30));
    assertEquals(xws(0, 6, 7, 6), table.getColumnWidths(20, 0, 0));

    table = Table.empty().withWidths(percentages(30, 30));
    assertEquals(xws(3, 5, 9, 5), table.getColumnWidths(20, 3, 0));

    table = Table.empty().withWidths(percentages(30, 30));
    assertEquals(xws(0, 2, 3, 2), table.getColumnWidths(7, 0, 0));

    table = Table.empty().withWidths(percentages(30, 30));
    assertEquals(xws(3, 1, 5, 1), table.getColumnWidths(7, 3, 0));
  }

  @Test
  public void ratio_constraint() {
    Table table =
        Table.empty().withWidths(List.of(new Constraint.Ratio(1, 3), new Constraint.Ratio(1, 3)));
    assertEquals(xws(0, 7, 8, 6), table.getColumnWidths(20, 0, 0));

    table = Table.empty().withWidths(List.of(new Constraint.Ratio(1, 3), new Constraint.Ratio(1, 3)));
    assertEquals(xws(3, 6, 10, 5), table.getColumnWidths(20, 3, 0));

    table = Table.empty().withWidths(List.of(new Constraint.Ratio(1, 3), new Constraint.Ratio(1, 3)));
    assertEquals(xws(0, 2, 3, 3), table.getColumnWidths(7, 0, 0));

    table = Table.empty().withWidths(List.of(new Constraint.Ratio(1, 3), new Constraint.Ratio(1, 3)));
    assertEquals(xws(3, 1, 5, 2), table.getColumnWidths(7, 3, 0));
  }

  @Test
  public void underconstrained_flex() {
    Table table = Table.empty().withWidths(mins(10, 10, 1));
    assertEquals(xws(0, 20, 21, 20, 42, 20), table.getColumnWidths(62, 0, 0));

    table = Table.empty().withWidths(mins(10, 10, 1)).withFlex(Flex.Legacy);
    assertEquals(xws(0, 10, 11, 10, 22, 40), table.getColumnWidths(62, 0, 0));

    table = Table.empty().withWidths(mins(10, 10, 1)).withFlex(Flex.SpaceBetween);
    assertEquals(xws(0, 20, 21, 20, 42, 20), table.getColumnWidths(62, 0, 0));
  }

  @Test
  public void underconstrained_segment_size() {
    Table table = Table.empty().withWidths(mins(10, 10, 1));
    assertEquals(xws(0, 20, 21, 20, 42, 20), table.getColumnWidths(62, 0, 0));

    table = Table.empty().withWidths(mins(10, 10, 1)).withFlex(Flex.Legacy);
    assertEquals(xws(0, 10, 11, 10, 22, 40), table.getColumnWidths(62, 0, 0));
  }

  @Test
  public void no_constraint_with_rows() {
    Table table =
        Table.empty()
            .withRows(List.of(Row.ofStrings("a", "b"), Row.ofStrings("c", "d", "e")))
            .withHeader(Row.ofStrings("f", "g"))
            .withFooter(Row.ofStrings("h", "i"))
            .withColumnSpacing(0);
    assertEquals(xws(0, 10, 10, 10, 20, 10), table.getColumnWidths(30, 0, 3));
  }

  @Test
  public void no_constraint_with_header() {
    Table table =
        Table.empty()
            .withRows(List.of())
            .withHeader(Row.ofStrings("f", "g"))
            .withColumnSpacing(0);
    assertEquals(xws(0, 5, 5, 5), table.getColumnWidths(10, 0, 2));
  }

  @Test
  public void no_constraint_with_footer() {
    Table table =
        Table.empty()
            .withRows(List.of())
            .withFooter(Row.ofStrings("h", "i"))
            .withColumnSpacing(0);
    assertEquals(xws(0, 5, 5, 5), table.getColumnWidths(10, 0, 2));
  }

  // ---- highlight_symbol / column_spacing render specs --------------------

  /// Helper: builds a single-row table with two cells, given highlight spacing and selection,
  /// renders into a `(columns x 3)` buffer, and asserts the buffer matches the expected lines.
  private static void test_table_with_selection(
      HighlightSpacing highlightSpacing,
      int columns,
      int spacing,
      Optional<Integer> selection,
      String[] expected) {
    Table table =
        Table.empty()
            .withRows(List.of(Row.ofStrings("ABCDE", "12345")))
            .withHighlightSpacing(highlightSpacing)
            .withHighlightSymbol(">>>")
            .withColumnSpacing(spacing);
    Rect area = new Rect(0, 0, columns, 3);
    Buffer buf = Buffer.empty(area);
    TableState state = new TableState().withSelected(selection);
    table.render(area, buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  @Test
  public void excess_area_highlight_symbol_and_column_spacing_allocation() {
    test_table_with_selection(
        HighlightSpacing.Never,
        15,
        0,
        Optional.<Integer>empty(),
        new String[] {"ABCDE  12345   ", "               ", "               "});

    Table table =
        Table.empty()
            .withRows(List.of(Row.ofStrings("ABCDE", "12345")))
            .withWidths(lengths(5, 5))
            .withColumnSpacing(0);
    Rect area = new Rect(0, 0, 15, 3);
    Buffer buf = Buffer.empty(area);
    table.render(area, buf);
    assertBufferEq(buf, Buffer.withLines("ABCDE12345     ", "               ", "               "));

    test_table_with_selection(
        HighlightSpacing.Never,
        15,
        0,
        Optional.of(0),
        new String[] {"ABCDE  12345   ", "               ", "               "});

    test_table_with_selection(
        HighlightSpacing.WhenSelected,
        15,
        0,
        Optional.<Integer>empty(),
        new String[] {"ABCDE  12345   ", "               ", "               "});

    test_table_with_selection(
        HighlightSpacing.WhenSelected,
        15,
        0,
        Optional.of(0),
        new String[] {">>>ABCDE 12345 ", "               ", "               "});

    test_table_with_selection(
        HighlightSpacing.Always,
        15,
        0,
        Optional.<Integer>empty(),
        new String[] {"   ABCDE 12345 ", "               ", "               "});

    test_table_with_selection(
        HighlightSpacing.Always,
        15,
        0,
        Optional.of(0),
        new String[] {">>>ABCDE 12345 ", "               ", "               "});
  }

  @Test
  public void insufficient_area_highlight_symbol_and_column_spacing_allocation() {
    test_table_with_selection(
        HighlightSpacing.Never,
        10,
        1,
        Optional.<Integer>empty(),
        new String[] {"ABCDE 1234", "          ", "          "});

    test_table_with_selection(
        HighlightSpacing.WhenSelected,
        10,
        1,
        Optional.<Integer>empty(),
        new String[] {"ABCDE 1234", "          ", "          "});

    test_table_with_selection(
        HighlightSpacing.Always,
        10,
        1,
        Optional.<Integer>empty(),
        new String[] {"   ABC 123", "          ", "          "});

    test_table_with_selection(
        HighlightSpacing.Always,
        9,
        1,
        Optional.<Integer>empty(),
        new String[] {"   ABC 12", "         ", "         "});

    test_table_with_selection(
        HighlightSpacing.Always,
        8,
        1,
        Optional.<Integer>empty(),
        new String[] {"   AB 12", "        ", "        "});

    test_table_with_selection(
        HighlightSpacing.Always,
        7,
        1,
        Optional.<Integer>empty(),
        new String[] {"   AB 1", "       ", "       "});

    Table tableLegacy =
        Table.empty()
            .withRows(List.of(Row.ofStrings("ABCDE", "12345")))
            .withHighlightSpacing(HighlightSpacing.Always)
            .withFlex(Flex.Legacy)
            .withHighlightSymbol(">>>")
            .withColumnSpacing(1);
    Rect area = new Rect(0, 0, 10, 3);
    Buffer buf = Buffer.empty(area);
    tableLegacy.render(area, buf);
    assertBufferEq(buf, Buffer.withLines("   ABCDE 1", "          ", "          "));

    Table tableStart =
        Table.empty()
            .withRows(List.of(Row.ofStrings("ABCDE", "12345")))
            .withHighlightSpacing(HighlightSpacing.Always)
            .withFlex(Flex.Start)
            .withHighlightSymbol(">>>")
            .withColumnSpacing(1);
    Buffer buf2 = Buffer.empty(area);
    tableStart.render(area, buf2);
    assertBufferEq(buf2, Buffer.withLines("   ABC 123", "          ", "          "));

    test_table_with_selection(
        HighlightSpacing.Never,
        10,
        1,
        Optional.of(0),
        new String[] {"ABCDE 1234", "          ", "          "});

    test_table_with_selection(
        HighlightSpacing.WhenSelected,
        10,
        1,
        Optional.of(0),
        new String[] {">>>ABC 123", "          ", "          "});

    test_table_with_selection(
        HighlightSpacing.Always,
        10,
        1,
        Optional.of(0),
        new String[] {">>>ABC 123", "          ", "          "});
  }

  @Test
  public void insufficient_area_highlight_symbol_allocation_with_no_column_spacing() {
    test_table_with_selection(
        HighlightSpacing.Never,
        10,
        0,
        Optional.<Integer>empty(),
        new String[] {"ABCDE12345", "          ", "          "});
    test_table_with_selection(
        HighlightSpacing.WhenSelected,
        10,
        0,
        Optional.<Integer>empty(),
        new String[] {"ABCDE12345", "          ", "          "});
    test_table_with_selection(
        HighlightSpacing.Always,
        10,
        0,
        Optional.<Integer>empty(),
        new String[] {"   ABCD123", "          ", "          "});
    test_table_with_selection(
        HighlightSpacing.Never,
        10,
        0,
        Optional.of(0),
        new String[] {"ABCDE12345", "          ", "          "});
    test_table_with_selection(
        HighlightSpacing.WhenSelected,
        10,
        0,
        Optional.of(0),
        new String[] {">>>ABCD123", "          ", "          "});
    test_table_with_selection(
        HighlightSpacing.Always,
        10,
        0,
        Optional.of(0),
        new String[] {">>>ABCD123", "          ", "          "});
  }

  // ---- column_count parameterised test -----------------------------------

  @ParameterizedTest
  @MethodSource("column_count_cases")
  public void column_count(
      List<String> headerCells, List<List<String>> rowCells, List<String> footerCells, int expected) {
    Row header = Row.ofStrings(headerCells);
    Row footer = Row.ofStrings(footerCells);
    List<Row> rows = new ArrayList<>(rowCells.size());
    for (List<String> rc : rowCells) rows.add(Row.ofStrings(rc));
    Table table = Table.of(rows, List.<Constraint>of()).withHeader(header).withFooter(footer);
    assertEquals(expected, table.columnCount());
  }

  static Stream<Arguments> column_count_cases() {
    return Stream.of(
        Arguments.of(List.<String>of(), List.<List<String>>of(), List.<String>of(), 0),
        Arguments.of(List.of("H1", "H2"), List.<List<String>>of(), List.<String>of(), 2),
        Arguments.of(
            List.<String>of(),
            List.of(List.of("C1", "C2"), List.of("C1", "C2", "C3")),
            List.<String>of(),
            3),
        Arguments.of(List.<String>of(), List.<List<String>>of(), List.of("F1", "F2", "F3", "F4"), 4),
        Arguments.of(
            List.of("H1", "H2", "H3", "H4"),
            List.of(List.of("C1", "C2"), List.of("C1", "C2", "C3")),
            List.of("F1", "F2"),
            4),
        Arguments.of(
            List.of("H1", "H2"),
            List.of(List.of("C1", "C2"), List.of("C1", "C2", "C3", "C4")),
            List.of("F1", "F2"),
            4),
        Arguments.of(
            List.of("H1", "H2"),
            List.of(List.of("C1", "C2"), List.of("C1", "C2", "C3")),
            List.of("F1", "F2", "F3", "F4"),
            4));
  }

  // ---- Buffer-edge regression tests --------------------------------------

  @Test
  public void render_in_minimal_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 1, 1));
    List<Row> rows =
        List.of(Row.ofStrings("Cell1", "Cell2", "Cell3"), Row.ofStrings("Cell4", "Cell5", "Cell6"));
    Table table =
        Table.of(rows, lengths(10, 10, 10))
            .withHeader(Row.ofStrings("Header1", "Header2", "Header3"))
            .withFooter(Row.ofStrings("Footer1", "Footer2", "Footer3"));
    table.render(buffer.area(), buffer);
    assertBufferEq(buffer, Buffer.withLines(" "));
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 0));
    List<Row> rows =
        List.of(Row.ofStrings("Cell1", "Cell2", "Cell3"), Row.ofStrings("Cell4", "Cell5", "Cell6"));
    Table table =
        Table.of(rows, lengths(10, 10, 10))
            .withHeader(Row.ofStrings("Header1", "Header2", "Header3"))
            .withFooter(Row.ofStrings("Footer1", "Footer2", "Footer3"));
    table.render(buffer.area(), buffer);
  }

  // ---- Helpers ------------------------------------------------------------

  private static List<Constraint> lengths(int... values) {
    List<Constraint> out = new ArrayList<>(values.length);
    for (int v : values) out.add(new Constraint.Length(v));
    return out;
  }

  private static List<Constraint> maxes(int... values) {
    List<Constraint> out = new ArrayList<>(values.length);
    for (int v : values) out.add(new Constraint.Max(v));
    return out;
  }

  private static List<Constraint> mins(int... values) {
    List<Constraint> out = new ArrayList<>(values.length);
    for (int v : values) out.add(new Constraint.Min(v));
    return out;
  }

  private static List<Constraint> percentages(int... values) {
    List<Constraint> out = new ArrayList<>(values.length);
    for (int v : values) out.add(new Constraint.Percentage(v));
    return out;
  }

  /// Build a list of `(x, width)` pairs from a flat sequence of integers.
  private static List<Table.XAndWidth> xws(int... values) {
    if ((values.length & 1) != 0) {
      throw new IllegalArgumentException("expected even number of values");
    }
    List<Table.XAndWidth> out = new ArrayList<>(values.length / 2);
    for (int i = 0; i < values.length; i += 2) {
      out.add(new Table.XAndWidth(values[i], values[i + 1]));
    }
    return out;
  }
}
