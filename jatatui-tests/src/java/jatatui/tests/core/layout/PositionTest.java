package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.Offset;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import org.junit.jupiter.api.Test;

class PositionTest {

  @Test
  void new_test() {
    Position p = Position.of(1, 2);
    assertEquals(new Position(1, 2), p);
  }

  @Test
  void from_tuple() {
    Position p = Position.of(1, 2);
    assertEquals(new Position(1, 2), p);
  }

  @Test
  void from_rect() {
    Position p = Position.fromRect(Rect.of(1, 2, 3, 4));
    assertEquals(new Position(1, 2), p);
  }

  @Test
  void to_string() {
    assertEquals("(1, 2)", Position.of(1, 2).toString());
  }

  @Test
  void offset_moves_position() {
    Position p = Position.of(2, 3).offset(Offset.of(5, 7));
    assertEquals(Position.of(7, 10), p);
  }

  @Test
  void offset_clamps_to_bounds() {
    Position p = Position.of(1, 1).offset(Offset.MAX);
    assertEquals(Position.MAX, p);
  }

  @Test
  void add_and_subtract_offset() {
    Position p = Position.of(10, 10).plus(Offset.of(-3, 4)).minus(Offset.of(5, 20));
    assertEquals(Position.of(2, 0), p);
  }
}
