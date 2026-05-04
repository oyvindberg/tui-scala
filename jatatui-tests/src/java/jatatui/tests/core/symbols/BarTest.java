package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Bar;
import org.junit.jupiter.api.Test;

public class BarTest {

  @Test
  public void default_set_is_nine_levels() {
    assertEquals(Bar.NINE_LEVELS, Bar.Set.defaultSet());
  }

  @Test
  public void three_levels() {
    Bar.Set s = Bar.THREE_LEVELS;
    assertEquals(Bar.FULL, s.full());
    assertEquals(Bar.FULL, s.sevenEighths());
    assertEquals(Bar.HALF, s.threeQuarters());
    assertEquals(Bar.HALF, s.fiveEighths());
    assertEquals(Bar.HALF, s.half());
    assertEquals(Bar.HALF, s.threeEighths());
    assertEquals(Bar.HALF, s.oneQuarter());
    assertEquals(" ", s.oneEighth());
    assertEquals(" ", s.empty());
  }

  @Test
  public void nine_levels() {
    Bar.Set s = Bar.NINE_LEVELS;
    assertEquals(Bar.FULL, s.full());
    assertEquals(Bar.SEVEN_EIGHTHS, s.sevenEighths());
    assertEquals(Bar.THREE_QUARTERS, s.threeQuarters());
    assertEquals(Bar.FIVE_EIGHTHS, s.fiveEighths());
    assertEquals(Bar.HALF, s.half());
    assertEquals(Bar.THREE_EIGHTHS, s.threeEighths());
    assertEquals(Bar.ONE_QUARTER, s.oneQuarter());
    assertEquals(Bar.ONE_EIGHTH, s.oneEighth());
    assertEquals(" ", s.empty());
  }
}
