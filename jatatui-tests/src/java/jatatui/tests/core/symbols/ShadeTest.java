package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Shade;
import org.junit.jupiter.api.Test;

public class ShadeTest {

  @Test
  public void constants() {
    assertEquals(" ", Shade.EMPTY);
    assertEquals("░", Shade.LIGHT);
    assertEquals("▒", Shade.MEDIUM);
    assertEquals("▓", Shade.DARK);
    assertEquals("█", Shade.FULL);
  }
}
