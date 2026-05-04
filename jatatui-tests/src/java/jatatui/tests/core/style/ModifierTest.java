package jatatui.tests.core.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.style.Modifier;
import org.junit.jupiter.api.Test;

public class ModifierTest {

  @Test
  public void modifier_debug_empty() {
    assertEquals("NONE", Modifier.EMPTY.toString());
  }

  @Test
  public void modifier_debug_bold() {
    assertEquals("BOLD", Modifier.BOLD.toString());
  }

  @Test
  public void modifier_debug_dim() {
    assertEquals("DIM", Modifier.DIM.toString());
  }

  @Test
  public void modifier_debug_italic() {
    assertEquals("ITALIC", Modifier.ITALIC.toString());
  }

  @Test
  public void modifier_debug_underlined() {
    assertEquals("UNDERLINED", Modifier.UNDERLINED.toString());
  }

  @Test
  public void modifier_debug_slow_blink() {
    assertEquals("SLOW_BLINK", Modifier.SLOW_BLINK.toString());
  }

  @Test
  public void modifier_debug_rapid_blink() {
    assertEquals("RAPID_BLINK", Modifier.RAPID_BLINK.toString());
  }

  @Test
  public void modifier_debug_reversed() {
    assertEquals("REVERSED", Modifier.REVERSED.toString());
  }

  @Test
  public void modifier_debug_hidden() {
    assertEquals("HIDDEN", Modifier.HIDDEN.toString());
  }

  @Test
  public void modifier_debug_crossed_out() {
    assertEquals("CROSSED_OUT", Modifier.CROSSED_OUT.toString());
  }

  @Test
  public void modifier_debug_combined() {
    assertEquals("BOLD | DIM", Modifier.BOLD.or(Modifier.DIM).toString());
  }

  @Test
  public void modifier_debug_all() {
    assertEquals(
        "BOLD | DIM | ITALIC | UNDERLINED | SLOW_BLINK | RAPID_BLINK | REVERSED | HIDDEN | CROSSED_OUT",
        Modifier.ALL.toString());
  }

  // Additional tests covering the bit-set operations (not in the upstream `style.rs` but
  // necessary for the `bitflags!` API surface that callers rely on).

  @Test
  public void contains_basic() {
    Modifier m = Modifier.BOLD.or(Modifier.ITALIC);
    assertTrue(m.contains(Modifier.BOLD));
    assertTrue(m.contains(Modifier.ITALIC));
    assertFalse(m.contains(Modifier.DIM));
  }

  @Test
  public void insert_and_remove() {
    Modifier m = Modifier.EMPTY.insert(Modifier.BOLD).insert(Modifier.DIM);
    assertTrue(m.contains(Modifier.BOLD));
    assertTrue(m.contains(Modifier.DIM));
    Modifier minusBold = m.remove(Modifier.BOLD);
    assertFalse(minusBold.contains(Modifier.BOLD));
    assertTrue(minusBold.contains(Modifier.DIM));
  }

  @Test
  public void union_difference_intersection() {
    Modifier a = Modifier.BOLD.or(Modifier.ITALIC);
    Modifier b = Modifier.ITALIC.or(Modifier.DIM);
    assertEquals(Modifier.BOLD.or(Modifier.ITALIC).or(Modifier.DIM), a.union(b));
    assertEquals(Modifier.BOLD, a.difference(b));
    assertEquals(Modifier.ITALIC, a.intersection(b));
  }

  @Test
  public void intersects() {
    Modifier a = Modifier.BOLD.or(Modifier.ITALIC);
    assertTrue(a.intersects(Modifier.BOLD));
    assertFalse(a.intersects(Modifier.DIM));
  }

  @Test
  public void empty_and_all_constants() {
    assertTrue(Modifier.EMPTY.isEmpty());
    assertFalse(Modifier.ALL.isEmpty());
    // ALL contains every individual flag
    assertTrue(Modifier.ALL.contains(Modifier.BOLD));
    assertTrue(Modifier.ALL.contains(Modifier.CROSSED_OUT));
  }
}
