package jatatui.tests.widgets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.widgets.Borders;
import org.junit.jupiter.api.Test;

public class BordersTest {

  @Test
  public void test_borders_debug() {
    assertEquals("NONE", new Borders(0).toString());
    assertEquals("NONE", Borders.NONE.toString());
    assertEquals("TOP", Borders.TOP.toString());
    assertEquals("BOTTOM", Borders.BOTTOM.toString());
    assertEquals("LEFT", Borders.LEFT.toString());
    assertEquals("RIGHT", Borders.RIGHT.toString());
    assertEquals("ALL", Borders.ALL.toString());
    assertEquals("TOP | BOTTOM", Borders.TOP.or(Borders.BOTTOM).toString());
  }

  @Test
  public void can_be_const() {
    // Java equivalent of the upstream `const` test: `static final` usage works.
    final Borders nothing = Borders.NONE;
    final Borders justTop = Borders.TOP;
    final Borders topBottom = Borders.TOP.or(Borders.BOTTOM);
    final Borders rightOpen = Borders.TOP.or(Borders.LEFT).or(Borders.BOTTOM);

    assertEquals(Borders.NONE, nothing);
    assertEquals(Borders.TOP, justTop);
    assertEquals(Borders.TOP.or(Borders.BOTTOM), topBottom);
    assertEquals(Borders.TOP.or(Borders.LEFT).or(Borders.BOTTOM), rightOpen);
  }

  @Test
  public void border_empty() {
    assertEquals(Borders.NONE, new Borders(0));
  }

  @Test
  public void border_all() {
    Borders all = Borders.ALL;
    assertEquals(all, Borders.TOP.or(Borders.BOTTOM).or(Borders.LEFT).or(Borders.RIGHT));
  }

  @Test
  public void border_left_right() {
    Borders leftRight = new Borders(Borders.LEFT.bits() | Borders.RIGHT.bits());
    assertEquals(Borders.RIGHT.or(Borders.LEFT), leftRight);
  }

  // Additional tests for the bitflag operations.

  @Test
  public void contains_basic() {
    Borders b = Borders.TOP.or(Borders.BOTTOM);
    assertTrue(b.contains(Borders.TOP));
    assertTrue(b.contains(Borders.BOTTOM));
    assertFalse(b.contains(Borders.LEFT));
    // empty set is contained in any set
    assertTrue(b.contains(Borders.NONE));
  }

  @Test
  public void intersects_basic() {
    Borders b = Borders.TOP.or(Borders.BOTTOM);
    assertTrue(b.intersects(Borders.TOP));
    assertTrue(b.intersects(Borders.BOTTOM));
    assertFalse(b.intersects(Borders.LEFT));
  }

  @Test
  public void insert_and_remove() {
    Borders b = Borders.NONE.insert(Borders.TOP).insert(Borders.BOTTOM);
    assertTrue(b.contains(Borders.TOP));
    assertTrue(b.contains(Borders.BOTTOM));
    Borders minusTop = b.remove(Borders.TOP);
    assertFalse(minusTop.contains(Borders.TOP));
    assertTrue(minusTop.contains(Borders.BOTTOM));
  }

  @Test
  public void union_difference_intersection() {
    Borders a = Borders.TOP.or(Borders.BOTTOM);
    Borders b = Borders.BOTTOM.or(Borders.LEFT);
    assertEquals(Borders.TOP.or(Borders.BOTTOM).or(Borders.LEFT), a.union(b));
    assertEquals(Borders.TOP, a.difference(b));
    assertEquals(Borders.BOTTOM, a.intersection(b));
  }

  @Test
  public void empty_and_all_constants() {
    assertTrue(Borders.NONE.isEmpty());
    assertTrue(Borders.ALL.isAll());
    assertFalse(Borders.NONE.isAll());
    assertFalse(Borders.ALL.isEmpty());
    assertTrue(Borders.ALL.contains(Borders.TOP));
    assertTrue(Borders.ALL.contains(Borders.LEFT));
  }
}
