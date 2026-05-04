package jatatui.tests.widgets.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Text;
import jatatui.widgets.table.TableCell;
import org.junit.jupiter.api.Test;

/// Inline tests from `ratatui_widgets::table::cell`.
///
/// Mapped tests:
/// - `new` - port of upstream `new`.
/// - `content` - port of upstream `content` (uses [TableCell#withContent] in Java).
/// - `style` - port of upstream `style`.
/// - `stylize` - port of upstream `stylize`.
public class TableCellTest {

  @Test
  public void new_() {
    TableCell cell = TableCell.of("");
    assertEquals(Text.from(""), cell.content());
  }

  @Test
  public void content() {
    TableCell cell = TableCell.empty().withContent("");
    assertEquals(Text.from(""), cell.content());
  }

  @Test
  public void style() {
    Style style = Style.empty().red().italic();
    TableCell cell = TableCell.empty().withStyle(style);
    assertEquals(style, cell.style());
  }

  @Test
  public void stylize() {
    TableCell cell = TableCell.of("").black().onWhite().bold().notDim();
    Style expected =
        Style.empty()
            .withFg(jatatui.core.style.Color.BLACK)
            .withBg(jatatui.core.style.Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM);
    assertEquals(expected, cell.style());
  }
}
