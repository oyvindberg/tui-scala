package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.layout.Margin;
import jatatui.core.layout.Offset;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import org.junit.jupiter.api.Test;

class RectTest {

  @Test
  void to_string() {
    assertEquals("3x4+1+2", Rect.of(1, 2, 3, 4).toString());
  }

  @Test
  void new_test() {
    Rect r = Rect.of(1, 2, 3, 4);
    assertEquals(1, r.x());
    assertEquals(2, r.y());
    assertEquals(3, r.width());
    assertEquals(4, r.height());
  }

  @Test
  void area() {
    assertEquals(12L, Rect.of(0, 0, 3, 4).area());
  }

  @Test
  void is_empty() {
    assertFalse(Rect.of(0, 0, 1, 1).isEmpty());
    assertTrue(Rect.of(0, 0, 0, 1).isEmpty());
    assertTrue(Rect.of(0, 0, 1, 0).isEmpty());
  }

  @Test
  void left() {
    assertEquals(1, Rect.of(1, 2, 3, 4).left());
  }

  @Test
  void right() {
    assertEquals(4, Rect.of(1, 2, 3, 4).right());
  }

  @Test
  void top() {
    assertEquals(2, Rect.of(1, 2, 3, 4).top());
  }

  @Test
  void bottom() {
    assertEquals(6, Rect.of(1, 2, 3, 4).bottom());
  }

  @Test
  void inner() {
    Rect r = Rect.of(1, 2, 10, 10).inner(new Margin(1, 2));
    assertEquals(Rect.of(2, 4, 8, 6), r);
  }

  @Test
  void offset() {
    Rect r = Rect.of(1, 2, 3, 4).offset(Offset.of(5, 6));
    assertEquals(Rect.of(6, 8, 3, 4), r);
  }

  @Test
  void negative_offset() {
    Rect r = Rect.of(4, 3, 10, 10).offset(Offset.of(-2, -1));
    assertEquals(Rect.of(2, 2, 10, 10), r);
  }

  @Test
  void negative_offset_saturate() {
    Rect r = Rect.of(1, 2, 3, 4).offset(Offset.of(-5, -6));
    assertEquals(Rect.of(0, 0, 3, 4), r);
  }

  @Test
  void union() {
    Rect a = Rect.of(1, 2, 3, 4);
    Rect b = Rect.of(2, 3, 4, 5);
    assertEquals(Rect.of(1, 2, 5, 6), a.union(b));
  }

  @Test
  void intersection() {
    Rect a = Rect.of(1, 2, 3, 4);
    Rect b = Rect.of(2, 3, 4, 5);
    assertEquals(Rect.of(2, 3, 2, 3), a.intersection(b));
  }

  @Test
  void intersection_underflow() {
    // Rects don't overlap — width/height clamp to zero (no underflow).
    Rect a = Rect.of(1, 1, 2, 2);
    Rect b = Rect.of(4, 4, 2, 2);
    assertEquals(Rect.of(4, 4, 0, 0), a.intersection(b));
  }

  @Test
  void intersects() {
    assertTrue(Rect.of(1, 2, 3, 4).intersects(Rect.of(2, 3, 4, 5)));
    assertFalse(Rect.of(1, 2, 3, 4).intersects(Rect.of(5, 6, 1, 1)));
  }

  @Test
  void contains() {
    Rect r = Rect.of(0, 0, 10, 10);
    assertTrue(r.contains(new Position(0, 0)));
    assertTrue(r.contains(new Position(9, 9)));
    assertFalse(r.contains(new Position(10, 9))); // right edge is exclusive
    assertFalse(r.contains(new Position(9, 10))); // bottom edge is exclusive
  }

  @Test
  void resize_updates_size() {
    Rect r = Rect.of(1, 2, 3, 4).resize(new Size(10, 20));
    assertEquals(Rect.of(1, 2, 10, 20), r);
  }

  @Test
  void as_position() {
    assertEquals(new Position(1, 2), Rect.of(1, 2, 3, 4).asPosition());
  }

  @Test
  void as_size() {
    assertEquals(new Size(3, 4), Rect.of(1, 2, 3, 4).asSize());
  }

  @Test
  void from_position_and_size() {
    Rect r = Rect.fromPositionAndSize(new Position(1, 2), new Size(3, 4));
    assertEquals(Rect.of(1, 2, 3, 4), r);
  }

  @Test
  void from_size() {
    Rect r = Rect.fromSize(new Size(3, 4));
    assertEquals(Rect.of(0, 0, 3, 4), r);
  }
}
