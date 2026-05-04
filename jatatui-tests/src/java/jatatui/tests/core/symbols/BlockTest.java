package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Block;
import org.junit.jupiter.api.Test;

public class BlockTest {

  @Test
  public void default_set_is_nine_levels() {
    assertEquals(Block.NINE_LEVELS, Block.Set.defaultSet());
  }

  @Test
  public void three_levels() {
    Block.Set s = Block.THREE_LEVELS;
    assertEquals(Block.FULL, s.full());
    assertEquals(Block.FULL, s.sevenEighths());
    assertEquals(Block.HALF, s.threeQuarters());
    assertEquals(Block.HALF, s.fiveEighths());
    assertEquals(Block.HALF, s.half());
    assertEquals(Block.HALF, s.threeEighths());
    assertEquals(Block.HALF, s.oneQuarter());
    assertEquals(" ", s.oneEighth());
    assertEquals(" ", s.empty());
  }

  @Test
  public void nine_levels() {
    Block.Set s = Block.NINE_LEVELS;
    assertEquals(Block.FULL, s.full());
    assertEquals(Block.SEVEN_EIGHTHS, s.sevenEighths());
    assertEquals(Block.THREE_QUARTERS, s.threeQuarters());
    assertEquals(Block.FIVE_EIGHTHS, s.fiveEighths());
    assertEquals(Block.HALF, s.half());
    assertEquals(Block.THREE_EIGHTHS, s.threeEighths());
    assertEquals(Block.ONE_QUARTER, s.oneQuarter());
    assertEquals(Block.ONE_EIGHTH, s.oneEighth());
    assertEquals(" ", s.empty());
  }
}
