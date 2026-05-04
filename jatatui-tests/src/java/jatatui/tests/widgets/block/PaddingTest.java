package jatatui.tests.widgets.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.widgets.block.Padding;
import org.junit.jupiter.api.Test;

public class PaddingTest {

  @Test
  public void new_() {
    assertEquals(new Padding(1, 2, 3, 4), new Padding(1, 2, 3, 4));
  }

  @Test
  public void constructors() {
    assertEquals(new Padding(1, 1, 0, 0), Padding.horizontal(1));
    assertEquals(new Padding(0, 0, 1, 1), Padding.vertical(1));
    assertEquals(new Padding(1, 1, 1, 1), Padding.uniform(1));
    assertEquals(new Padding(2, 2, 1, 1), Padding.proportional(1));
    assertEquals(new Padding(1, 1, 2, 2), Padding.symmetric(1, 2));
    assertEquals(new Padding(1, 0, 0, 0), Padding.left(1));
    assertEquals(new Padding(0, 1, 0, 0), Padding.right(1));
    assertEquals(new Padding(0, 0, 1, 0), Padding.top(1));
    assertEquals(new Padding(0, 0, 0, 1), Padding.bottom(1));
  }

  @Test
  public void can_be_const() {
    // The Java equivalent of upstream's `const` test — these all evaluate to compile-time-stable
    // values via the `static final` / `record` constructor path.
    final Padding padding = new Padding(1, 1, 1, 1);
    final Padding uni = Padding.uniform(1);
    final Padding horizontal = Padding.horizontal(1);
    final Padding vertical = Padding.vertical(1);
    final Padding proportional = Padding.proportional(1);
    final Padding symmetric = Padding.symmetric(1, 1);
    final Padding left = Padding.left(1);
    final Padding right = Padding.right(1);
    final Padding top = Padding.top(1);
    final Padding bottom = Padding.bottom(1);

    assertEquals(new Padding(1, 1, 1, 1), padding);
    assertEquals(new Padding(1, 1, 1, 1), uni);
    assertEquals(new Padding(1, 1, 0, 0), horizontal);
    assertEquals(new Padding(0, 0, 1, 1), vertical);
    assertEquals(new Padding(2, 2, 1, 1), proportional);
    assertEquals(new Padding(1, 1, 1, 1), symmetric);
    assertEquals(new Padding(1, 0, 0, 0), left);
    assertEquals(new Padding(0, 1, 0, 0), right);
    assertEquals(new Padding(0, 0, 1, 0), top);
    assertEquals(new Padding(0, 0, 0, 1), bottom);
  }

  @Test
  public void zero_constant_and_factory() {
    assertEquals(new Padding(0, 0, 0, 0), Padding.ZERO);
    assertEquals(Padding.ZERO, Padding.zero());
  }
}
