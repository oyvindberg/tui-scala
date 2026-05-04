package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.VerticalAlignment;
import org.junit.jupiter.api.Test;

public class AlignmentTest {

  @Test
  public void alignment_to_string() {
    assertEquals("Left", HorizontalAlignment.Left.toString());
    assertEquals("Center", HorizontalAlignment.Center.toString());
    assertEquals("Right", HorizontalAlignment.Right.toString());
  }

  @Test
  public void alignment_from_str() {
    assertEquals(HorizontalAlignment.Left, HorizontalAlignment.valueOf("Left"));
    assertEquals(HorizontalAlignment.Center, HorizontalAlignment.valueOf("Center"));
    assertEquals(HorizontalAlignment.Right, HorizontalAlignment.valueOf("Right"));
  }

  @Test
  public void vertical_alignment_to_string() {
    assertEquals("Top", VerticalAlignment.Top.toString());
    assertEquals("Center", VerticalAlignment.Center.toString());
    assertEquals("Bottom", VerticalAlignment.Bottom.toString());
  }

  @Test
  public void vertical_alignment_from_str() {
    assertEquals(VerticalAlignment.Top, VerticalAlignment.valueOf("Top"));
    assertEquals(VerticalAlignment.Center, VerticalAlignment.valueOf("Center"));
    assertEquals(VerticalAlignment.Bottom, VerticalAlignment.valueOf("Bottom"));
  }
}
