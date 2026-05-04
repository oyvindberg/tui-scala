package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.Direction;
import org.junit.jupiter.api.Test;

public class DirectionTest {

  @Test
  public void direction_to_string() {
    assertEquals("Horizontal", Direction.Horizontal.toString());
    assertEquals("Vertical", Direction.Vertical.toString());
  }

  @Test
  public void direction_from_str() {
    assertEquals(Direction.Horizontal, Direction.valueOf("Horizontal"));
    assertEquals(Direction.Vertical, Direction.valueOf("Vertical"));
  }

  @Test
  public void perpendicular() {
    assertEquals(Direction.Vertical, Direction.Horizontal.perpendicular());
    assertEquals(Direction.Horizontal, Direction.Vertical.perpendicular());
  }
}
