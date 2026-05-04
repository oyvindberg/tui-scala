package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Braille;
import org.junit.jupiter.api.Test;

public class BrailleTest {

  @Test
  public void braille_has_256_entries() {
    assertEquals(256, Braille.BRAILLE.length);
  }

  @Test
  public void first_and_last() {
    assertEquals('⠀', Braille.BRAILLE[0]);
    assertEquals('⣿', Braille.BRAILLE[255]);
  }
}
