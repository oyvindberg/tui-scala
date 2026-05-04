package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import org.junit.jupiter.api.Test;

class SizeTest {

  @Test
  void new_test() {
    Size s = new Size(10, 20);
    assertEquals(10, s.width());
    assertEquals(20, s.height());
  }

  @Test
  void from_rect() {
    Size s = Size.fromRect(Rect.of(0, 0, 10, 20));
    assertEquals(10, s.width());
    assertEquals(20, s.height());
  }

  @Test
  void display() {
    assertEquals("10x20", new Size(10, 20).toString());
  }

  @Test
  void area() {
    assertEquals(200L, new Size(10, 20).area());
    assertEquals(0L, new Size(0, 0).area());
    assertEquals(4_294_836_225L, new Size(Position.U16_MAX, Position.U16_MAX).area());
  }
}
