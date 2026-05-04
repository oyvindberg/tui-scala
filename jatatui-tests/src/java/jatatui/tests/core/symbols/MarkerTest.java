package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jatatui.core.symbols.Marker;
import org.junit.jupiter.api.Test;

public class MarkerTest {

  @Test
  public void marker_tostring() {
    assertEquals("Dot", Marker.Dot.toString());
    assertEquals("Block", Marker.Block.toString());
    assertEquals("Bar", Marker.Bar.toString());
    assertEquals("Braille", Marker.Braille.toString());
  }

  @Test
  public void marker_from_str() {
    assertEquals(Marker.Dot, Marker.valueOf("Dot"));
    assertEquals(Marker.Block, Marker.valueOf("Block"));
    assertEquals(Marker.Bar, Marker.valueOf("Bar"));
    assertEquals(Marker.Braille, Marker.valueOf("Braille"));
    assertThrows(IllegalArgumentException.class, () -> Marker.valueOf(""));
  }

  @Test
  public void dot_constant() {
    assertEquals("•", Marker.DOT);
  }

  @Test
  public void default_marker() {
    assertEquals(Marker.Dot, Marker.defaultMarker());
  }
}
