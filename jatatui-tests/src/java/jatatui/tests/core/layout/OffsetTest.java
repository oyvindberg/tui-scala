package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.Offset;
import org.junit.jupiter.api.Test;

public class OffsetTest {

  @Test
  public void new_sets_components() {
    assertEquals(new Offset(-3, 7), Offset.of(-3, 7));
  }

  @Test
  public void constants_match_expected_values() {
    assertEquals(new Offset(0, 0), Offset.ZERO);
    assertEquals(new Offset(Integer.MIN_VALUE, Integer.MIN_VALUE), Offset.MIN);
    assertEquals(new Offset(Integer.MAX_VALUE, Integer.MAX_VALUE), Offset.MAX);
  }

  @Test
  public void negate() {
    assertEquals(new Offset(-3, 7), new Offset(3, -7).negate());
  }
}
