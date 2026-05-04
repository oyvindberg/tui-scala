package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.HalfBlock;
import org.junit.jupiter.api.Test;

public class HalfBlockTest {

  @Test
  public void constants() {
    assertEquals('▀', HalfBlock.UPPER);
    assertEquals('▄', HalfBlock.LOWER);
    assertEquals('█', HalfBlock.FULL);
  }
}
