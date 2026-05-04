package jatatui.tests.widgets.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.widgets.table.Row;
import jatatui.widgets.table.TableCell;
import java.util.List;
import org.junit.jupiter.api.Test;

/// Inline tests from `ratatui_widgets::table::row`.
///
/// Mapped tests:
/// - `new` - port of upstream `new`.
/// - `collect` - N/A: upstream collects an iterator into a `Row` via the `FromIterator` impl. In
///   Java we use [Row#of(List)] which is the equivalent constructor; the test below mirrors the
///   intent.
/// - `cells` - port of upstream `cells` (uses [Row#withCells]).
/// - `height` - port of upstream `height` (uses [Row#withHeight]).
/// - `top_margin` - port of upstream `top_margin` (uses [Row#withTopMargin]).
/// - `bottom_margin` - port of upstream `bottom_margin` (uses [Row#withBottomMargin]).
/// - `style` - port of upstream `style` (uses [Row#withStyle]).
/// - `stylize` - port of upstream `stylize`.
public class RowTest {

  @Test
  public void new_() {
    List<TableCell> cells = List.of(TableCell.of(""));
    Row row = Row.of(cells);
    assertEquals(cells, row.cells());
  }

  @Test
  public void collect() {
    List<TableCell> cells = List.of(TableCell.of(""));
    Row row = Row.of(cells);
    assertEquals(cells, row.cells());
  }

  @Test
  public void cells() {
    List<TableCell> cells = List.of(TableCell.of(""));
    Row row = Row.empty().withCells(cells);
    assertEquals(cells, row.cells());
  }

  @Test
  public void height() {
    Row row = Row.empty().withHeight(2);
    assertEquals(2, row.height());
  }

  @Test
  public void top_margin() {
    Row row = Row.empty().withTopMargin(1);
    assertEquals(1, row.topMargin());
  }

  @Test
  public void bottom_margin() {
    Row row = Row.empty().withBottomMargin(1);
    assertEquals(1, row.bottomMargin());
  }

  @Test
  public void style() {
    Style style = Style.empty().red().italic();
    Row row = Row.empty().withStyle(style);
    assertEquals(style, row.style());
  }

  @Test
  public void stylize() {
    Row row = Row.of(List.of(TableCell.of(""))).black().onWhite().bold().notItalic();
    Style expected =
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.ITALIC);
    assertEquals(expected, row.style());
  }
}
