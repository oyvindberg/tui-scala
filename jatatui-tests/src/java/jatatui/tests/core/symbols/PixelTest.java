package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Pixel;
import org.junit.jupiter.api.Test;

public class PixelTest {

  @Test
  public void quadrants_has_16_entries() {
    assertEquals(16, Pixel.QUADRANTS.length);
    assertEquals(' ', Pixel.QUADRANTS[0]);
    assertEquals('█', Pixel.QUADRANTS[15]);
  }

  @Test
  public void sextants_has_64_entries() {
    assertEquals(64, Pixel.SEXTANTS.length);
    assertEquals(" ", Pixel.SEXTANTS[0]);
    assertEquals("█", Pixel.SEXTANTS[63]);
  }

  @Test
  public void octants_has_256_entries() {
    assertEquals(256, Pixel.OCTANTS.length);
    assertEquals(" ", Pixel.OCTANTS[0]);
    assertEquals("█", Pixel.OCTANTS[255]);
  }
}
