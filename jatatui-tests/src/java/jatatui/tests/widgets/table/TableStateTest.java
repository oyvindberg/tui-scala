package jatatui.tests.widgets.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.widgets.table.TableState;
import jatatui.widgets.table.TableState.RowAndColumn;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Inline tests from `ratatui_widgets::table::state`.
///
/// Mapped tests (one-to-one with upstream method names):
/// - `new`, `with_offset`, `with_selected`, `with_selected_column`, `with_selected_cell_none`,
///   `offset`, `offset_mut`, `selected`, `selected_column`, `selected_cell`, `selected_mut`,
///   `selected_column_mut`, `select`, `select_none`, `select_column`, `select_column_none`,
///   `select_cell`, `select_cell_none`, `test_table_state_navigation`.
///
/// `offset_mut` / `selected_mut` / `selected_column_mut` upstream return `&mut` references; in
/// Java we expose `setOffset` / `setSelected` / `setSelectedColumn` setters with the same effect.
public class TableStateTest {

  @Test
  public void new_() {
    TableState state = new TableState();
    assertEquals(0, state.offset());
    assertEquals(Optional.empty(), state.selected());
    assertEquals(Optional.empty(), state.selectedColumn());
  }

  @Test
  public void with_offset() {
    TableState state = new TableState().withOffset(1);
    assertEquals(1, state.offset());
  }

  @Test
  public void with_selected() {
    TableState state = new TableState().withSelected(1);
    assertEquals(Optional.of(1), state.selected());
  }

  @Test
  public void with_selected_column() {
    TableState state = new TableState().withSelectedColumn(1);
    assertEquals(Optional.of(1), state.selectedColumn());
  }

  @Test
  public void with_selected_cell_none() {
    TableState state = new TableState().withSelectedCell(Optional.empty());
    assertEquals(Optional.empty(), state.selected());
    assertEquals(Optional.empty(), state.selectedColumn());
  }

  @Test
  public void offset() {
    TableState state = new TableState();
    assertEquals(0, state.offset());
  }

  @Test
  public void offset_mut() {
    TableState state = new TableState();
    state.setOffset(1);
    assertEquals(1, state.offset());
  }

  @Test
  public void selected() {
    TableState state = new TableState();
    assertEquals(Optional.empty(), state.selected());
  }

  @Test
  public void selected_column() {
    TableState state = new TableState();
    assertEquals(Optional.empty(), state.selectedColumn());
  }

  @Test
  public void selected_cell() {
    TableState state = new TableState();
    assertEquals(Optional.empty(), state.selectedCell());
  }

  @Test
  public void selected_mut() {
    TableState state = new TableState();
    state.setSelected(Optional.of(1));
    assertEquals(Optional.of(1), state.selected());
  }

  @Test
  public void selected_column_mut() {
    TableState state = new TableState();
    state.setSelectedColumn(Optional.of(1));
    assertEquals(Optional.of(1), state.selectedColumn());
  }

  @Test
  public void select() {
    TableState state = new TableState();
    state.select(1);
    assertEquals(Optional.of(1), state.selected());
  }

  @Test
  public void select_none() {
    TableState state = new TableState().withSelected(1);
    state.select(Optional.empty());
    assertEquals(Optional.empty(), state.selected());
  }

  @Test
  public void select_column() {
    TableState state = new TableState();
    state.selectColumn(1);
    assertEquals(Optional.of(1), state.selectedColumn());
  }

  @Test
  public void select_column_none() {
    TableState state = new TableState().withSelectedColumn(1);
    state.selectColumn(Optional.empty());
    assertEquals(Optional.empty(), state.selectedColumn());
  }

  @Test
  public void select_cell() {
    TableState state = new TableState();
    state.selectCell(Optional.of(new RowAndColumn(1, 5)));
    assertEquals(Optional.of(new RowAndColumn(1, 5)), state.selectedCell());
  }

  @Test
  public void select_cell_none() {
    TableState state = new TableState().withSelectedCell(1, 5);
    state.selectCell(Optional.empty());
    assertEquals(Optional.empty(), state.selected());
    assertEquals(Optional.empty(), state.selectedColumn());
    assertEquals(Optional.empty(), state.selectedCell());
  }

  @Test
  public void test_table_state_navigation() {
    TableState state = new TableState();
    state.selectFirst();
    assertEquals(Optional.of(0), state.selected());

    state.selectPrevious(); // should not go below 0
    assertEquals(Optional.of(0), state.selected());

    state.selectNext();
    assertEquals(Optional.of(1), state.selected());

    state.selectPrevious();
    assertEquals(Optional.of(0), state.selected());

    state.selectLast();
    assertEquals(Optional.of(TableState.MAX_INDEX), state.selected());

    state.selectNext(); // should not go above MAX_INDEX
    assertEquals(Optional.of(TableState.MAX_INDEX), state.selected());

    state.selectPrevious();
    assertEquals(Optional.of(TableState.MAX_INDEX - 1), state.selected());

    state.selectNext();
    assertEquals(Optional.of(TableState.MAX_INDEX), state.selected());

    state = new TableState();
    state.selectNext();
    assertEquals(Optional.of(0), state.selected());

    state = new TableState();
    state.selectPrevious();
    assertEquals(Optional.of(TableState.MAX_INDEX), state.selected());

    state = new TableState();
    state.select(2);
    state.scrollDownBy(4);
    assertEquals(Optional.of(6), state.selected());

    state = new TableState();
    state.scrollUpBy(3);
    assertEquals(Optional.of(0), state.selected());

    state.select(6);
    state.scrollUpBy(4);
    assertEquals(Optional.of(2), state.selected());

    state.scrollUpBy(4);
    assertEquals(Optional.of(0), state.selected());

    state = new TableState();
    state.selectFirstColumn();
    assertEquals(Optional.of(0), state.selectedColumn());

    state.selectPreviousColumn();
    assertEquals(Optional.of(0), state.selectedColumn());

    state.selectNextColumn();
    assertEquals(Optional.of(1), state.selectedColumn());

    state.selectPreviousColumn();
    assertEquals(Optional.of(0), state.selectedColumn());

    state.selectLastColumn();
    assertEquals(Optional.of(TableState.MAX_INDEX), state.selectedColumn());

    state.selectPreviousColumn();
    assertEquals(Optional.of(TableState.MAX_INDEX - 1), state.selectedColumn());

    state = new TableState().withSelectedColumn(12);
    state.scrollRightBy(4);
    assertEquals(Optional.of(16), state.selectedColumn());

    state.scrollLeftBy(20);
    assertEquals(Optional.of(0), state.selectedColumn());

    state.scrollRightBy(100);
    assertEquals(Optional.of(100), state.selectedColumn());

    state.scrollLeftBy(20);
    assertEquals(Optional.of(80), state.selectedColumn());
  }
}
