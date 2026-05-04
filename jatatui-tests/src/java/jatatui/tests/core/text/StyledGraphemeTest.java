package jatatui.tests.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Style;
import jatatui.core.text.StyledGrapheme;
import org.junit.jupiter.api.Test;

public class StyledGraphemeTest {

  @Test
  public void new_() {
    Style style = Style.empty().yellow();
    StyledGrapheme sg = new StyledGrapheme("a", style);
    assertEquals("a", sg.symbol);
    assertEquals(style, sg.style);
  }

  @Test
  public void style() {
    Style style = Style.empty().yellow();
    StyledGrapheme sg = new StyledGrapheme("a", style);
    assertEquals(style, sg.style());
  }

  @Test
  public void set_style() {
    Style style = Style.empty().yellow().onRed();
    Style style2 = Style.empty().green();
    StyledGrapheme sg = new StyledGrapheme("a", style).setStyle(style2);
    assertEquals(style2, sg.style);
  }

  @Test
  public void stylize() {
    Style style = Style.empty().yellow().onRed();
    StyledGrapheme sg = new StyledGrapheme("a", style).green();
    assertEquals(Style.empty().green().onRed(), sg.style);
  }
}
