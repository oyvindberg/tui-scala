package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Offset;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import jatatui.core.layout.solver.Either;
import org.junit.jupiter.api.Assertions;
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

  // --- Layout-dependent methods (ported from rect.rs) ---------------------

  @Test
  void centered_horizontally() {
    Rect rect = Rect.of(0, 0, 5, 5);
    assertEquals(Rect.of(1, 0, 3, 5), rect.centeredHorizontally(new Constraint.Length(3)));
  }

  @Test
  void centered_vertically() {
    Rect rect = Rect.of(0, 0, 5, 5);
    assertEquals(Rect.of(0, 2, 5, 1), rect.centeredVertically(new Constraint.Length(1)));
  }

  @Test
  void centered() {
    Rect rect = Rect.of(0, 0, 5, 5);
    assertEquals(
        Rect.of(1, 2, 3, 1),
        rect.centered(new Constraint.Length(3), new Constraint.Length(1)));
  }

  @Test
  void layout() {
    Layout layout = Layout.horizontal(new Constraint.Length(3), new Constraint.Min(0));

    Rect[] ab = Rect.of(0, 0, 10, 10).layout(layout);
    assertEquals(Rect.of(0, 0, 3, 10), ab[0]);
    assertEquals(Rect.of(3, 0, 7, 10), ab[1]);

    Rect[] areas = Rect.of(0, 0, 10, 10).layout(layout, 2);
    assertEquals(Rect.of(0, 0, 3, 10), areas[0]);
    assertEquals(Rect.of(3, 0, 7, 10), areas[1]);
  }

  @Test
  void layout_invalid_number_of_rects() {
    Layout layout = Layout.horizontal(new Constraint.Length(1));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> Rect.of(0, 0, 10, 10).layout(layout, 3));
  }

  @Test
  void layout_vec() {
    Layout layout = Layout.horizontal(new Constraint.Length(3), new Constraint.Min(0));
    var areas = Rect.of(0, 0, 10, 10).layoutVec(layout);
    assertEquals(Rect.of(0, 0, 3, 10), areas.get(0));
    assertEquals(Rect.of(3, 0, 7, 10), areas.get(1));
  }

  @Test
  void try_layout() {
    Layout layout = Layout.horizontal(new Constraint.Length(3), new Constraint.Min(0));

    Either<String, Rect[]> result = Rect.of(0, 0, 10, 10).tryLayout(layout, 2);
    assertTrue(result instanceof Either.Right<?, ?>);
    Rect[] ab = ((Either.Right<String, Rect[]>) result).value();
    assertEquals(Rect.of(0, 0, 3, 10), ab[0]);
    assertEquals(Rect.of(3, 0, 7, 10), ab[1]);
  }

  @Test
  void try_layout_invalid_number_of_rects() {
    Layout layout = Layout.horizontal(new Constraint.Length(1));
    Either<String, Rect[]> result = Rect.of(0, 0, 10, 10).tryLayout(layout, 3);
    assertTrue(result instanceof Either.Left<?, ?>);
  }
}
