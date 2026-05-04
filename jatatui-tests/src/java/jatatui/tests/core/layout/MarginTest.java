package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.Margin;
import org.junit.jupiter.api.Test;

public class MarginTest {

  @Test
  public void margin_to_string() {
    assertEquals("1x2", new Margin(1, 2).toString());
  }

  @Test
  public void margin_new() {
    Margin m = Margin.of(1, 2);
    assertEquals(1, m.horizontal());
    assertEquals(2, m.vertical());
  }
}
