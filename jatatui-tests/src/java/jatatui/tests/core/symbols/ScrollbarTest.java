package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Block;
import jatatui.core.symbols.Line;
import jatatui.core.symbols.Scrollbar;
import org.junit.jupiter.api.Test;

public class ScrollbarTest {

  @Test
  public void double_vertical() {
    Scrollbar.Set s = Scrollbar.DOUBLE_VERTICAL;
    assertEquals(Line.DOUBLE_VERTICAL, s.track());
    assertEquals(Block.FULL, s.thumb());
    assertEquals("▲", s.begin());
    assertEquals("▼", s.end());
  }

  @Test
  public void double_horizontal() {
    Scrollbar.Set s = Scrollbar.DOUBLE_HORIZONTAL;
    assertEquals(Line.DOUBLE_HORIZONTAL, s.track());
    assertEquals(Block.FULL, s.thumb());
    assertEquals("◄", s.begin());
    assertEquals("►", s.end());
  }

  @Test
  public void vertical() {
    Scrollbar.Set s = Scrollbar.VERTICAL;
    assertEquals(Line.VERTICAL, s.track());
    assertEquals(Block.FULL, s.thumb());
    assertEquals("↑", s.begin());
    assertEquals("↓", s.end());
  }

  @Test
  public void horizontal() {
    Scrollbar.Set s = Scrollbar.HORIZONTAL;
    assertEquals(Line.HORIZONTAL, s.track());
    assertEquals(Block.FULL, s.thumb());
    assertEquals("←", s.begin());
    assertEquals("→", s.end());
  }
}
