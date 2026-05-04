package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Ports the inline tests in `submodules/ratatui/ratatui-core/src/layout/rect/iter.rs`.
public class RectIterTest {

  @Test
  public void rows() {
    Rect rect = Rect.of(0, 0, 2, 3);
    Rect.Rows rows = rect.rows();
    assertEquals(3, rows.remaining());
    assertEquals(Rect.of(0, 0, 2, 1), rows.next());
    assertEquals(2, rows.remaining());
    assertEquals(Rect.of(0, 1, 2, 1), rows.next());
    assertEquals(1, rows.remaining());
    assertEquals(Rect.of(0, 2, 2, 1), rows.next());
    assertEquals(0, rows.remaining());
    assertFalse(rows.hasNext());
    assertEquals(0, rows.remaining());
    assertEquals(Optional.empty(), rows.nextBack());
    assertEquals(0, rows.remaining());
  }

  @Test
  public void rows_back() {
    Rect rect = Rect.of(0, 0, 2, 3);
    Rect.Rows rows = rect.rows();
    assertEquals(3, rows.remaining());
    assertEquals(Optional.of(Rect.of(0, 2, 2, 1)), rows.nextBack());
    assertEquals(2, rows.remaining());
    assertEquals(Optional.of(Rect.of(0, 1, 2, 1)), rows.nextBack());
    assertEquals(1, rows.remaining());
    assertEquals(Optional.of(Rect.of(0, 0, 2, 1)), rows.nextBack());
    assertEquals(0, rows.remaining());
    assertEquals(Optional.empty(), rows.nextBack());
    assertEquals(0, rows.remaining());
    assertFalse(rows.hasNext());
    assertEquals(0, rows.remaining());
  }

  @Test
  public void rows_meet_in_the_middle() {
    Rect rect = Rect.of(0, 0, 2, 4);
    Rect.Rows rows = rect.rows();
    assertEquals(4, rows.remaining());
    assertEquals(Rect.of(0, 0, 2, 1), rows.next());
    assertEquals(3, rows.remaining());
    assertEquals(Optional.of(Rect.of(0, 3, 2, 1)), rows.nextBack());
    assertEquals(2, rows.remaining());
    assertEquals(Rect.of(0, 1, 2, 1), rows.next());
    assertEquals(1, rows.remaining());
    assertEquals(Optional.of(Rect.of(0, 2, 2, 1)), rows.nextBack());
    assertEquals(0, rows.remaining());
    assertFalse(rows.hasNext());
    assertEquals(0, rows.remaining());
    assertEquals(Optional.empty(), rows.nextBack());
    assertEquals(0, rows.remaining());
  }

  @Test
  public void columns() {
    Rect rect = Rect.of(0, 0, 3, 2);
    Rect.Columns columns = rect.columns();
    assertEquals(3, columns.remaining());
    assertEquals(Rect.of(0, 0, 1, 2), columns.next());
    assertEquals(2, columns.remaining());
    assertEquals(Rect.of(1, 0, 1, 2), columns.next());
    assertEquals(1, columns.remaining());
    assertEquals(Rect.of(2, 0, 1, 2), columns.next());
    assertEquals(0, columns.remaining());
    assertFalse(columns.hasNext());
    assertEquals(0, columns.remaining());
    assertEquals(Optional.empty(), columns.nextBack());
    assertEquals(0, columns.remaining());
  }

  @Test
  public void columns_back() {
    Rect rect = Rect.of(0, 0, 3, 2);
    Rect.Columns columns = rect.columns();
    assertEquals(3, columns.remaining());
    assertEquals(Optional.of(Rect.of(2, 0, 1, 2)), columns.nextBack());
    assertEquals(2, columns.remaining());
    assertEquals(Optional.of(Rect.of(1, 0, 1, 2)), columns.nextBack());
    assertEquals(1, columns.remaining());
    assertEquals(Optional.of(Rect.of(0, 0, 1, 2)), columns.nextBack());
    assertEquals(0, columns.remaining());
    assertEquals(Optional.empty(), columns.nextBack());
    assertEquals(0, columns.remaining());
    assertFalse(columns.hasNext());
    assertEquals(0, columns.remaining());
  }

  @Test
  public void columns_meet_in_the_middle() {
    Rect rect = Rect.of(0, 0, 4, 2);
    Rect.Columns columns = rect.columns();
    assertEquals(4, columns.remaining());
    assertEquals(Rect.of(0, 0, 1, 2), columns.next());
    assertEquals(3, columns.remaining());
    assertEquals(Optional.of(Rect.of(3, 0, 1, 2)), columns.nextBack());
    assertEquals(2, columns.remaining());
    assertEquals(Rect.of(1, 0, 1, 2), columns.next());
    assertEquals(1, columns.remaining());
    assertEquals(Optional.of(Rect.of(2, 0, 1, 2)), columns.nextBack());
    assertEquals(0, columns.remaining());
    assertFalse(columns.hasNext());
    assertEquals(0, columns.remaining());
    assertEquals(Optional.empty(), columns.nextBack());
    assertEquals(0, columns.remaining());
  }

  /// We allow a total of `65536` columns in the range `(0..=65535)`. In this test we iterate
  /// forward and skip the first `65534` columns, and expect the next column to be `65535` and
  /// the subsequent columns to be exhausted.
  @Test
  public void columns_max() {
    Rect rect = Rect.of(0, 0, Position.U16_MAX, 1);
    Rect.Columns columns = rect.columns();
    int skip = Position.U16_MAX - 1;
    for (int i = 0; i < skip; i++) columns.next();
    assertTrue(columns.hasNext());
    assertEquals(Rect.of(Position.U16_MAX - 1, 0, 1, 1), columns.next());
    assertFalse(columns.hasNext());
  }

  /// Backward variant of `columns_max`.
  @Test
  public void columns_min() {
    Rect rect = Rect.of(0, 0, Position.U16_MAX, 1);
    Rect.Columns columns = rect.columns();
    int skip = Position.U16_MAX - 1;
    for (int i = 0; i < skip; i++) columns.nextBack();
    Optional<Rect> next = columns.nextBack();
    assertEquals(Optional.of(Rect.of(0, 0, 1, 1)), next);
    assertEquals(Optional.empty(), columns.nextBack());
    assertEquals(Optional.empty(), columns.nextBack());
  }

  @Test
  public void positions() {
    Rect rect = Rect.of(0, 0, 2, 2);
    Rect.Positions positions = rect.positions();
    assertEquals(4, positions.remaining());
    assertEquals(new Position(0, 0), positions.next());
    assertEquals(3, positions.remaining());
    assertEquals(new Position(1, 0), positions.next());
    assertEquals(2, positions.remaining());
    assertEquals(new Position(0, 1), positions.next());
    assertEquals(1, positions.remaining());
    assertEquals(new Position(1, 1), positions.next());
    assertEquals(0, positions.remaining());
    assertFalse(positions.hasNext());
    assertEquals(0, positions.remaining());
  }

  @Test
  public void positions_zero_width() {
    Rect rect = Rect.of(0, 0, 0, 1);
    Rect.Positions positions = rect.positions();
    assertEquals(0, positions.remaining());
    assertFalse(positions.hasNext());
    assertEquals(0, positions.remaining());
  }

  @Test
  public void positions_zero_height() {
    Rect rect = Rect.of(0, 0, 1, 0);
    Rect.Positions positions = rect.positions();
    assertEquals(0, positions.remaining());
    assertFalse(positions.hasNext());
    assertEquals(0, positions.remaining());
  }

  @Test
  public void positions_zero_by_zero() {
    Rect rect = Rect.of(0, 0, 0, 0);
    Rect.Positions positions = rect.positions();
    assertEquals(0, positions.remaining());
    assertFalse(positions.hasNext());
    assertEquals(0, positions.remaining());
  }
}
