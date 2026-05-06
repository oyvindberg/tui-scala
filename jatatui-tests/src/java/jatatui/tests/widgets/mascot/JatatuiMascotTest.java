package jatatui.tests.widgets.mascot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import jatatui.core.style.Color;
import jatatui.widgets.JatatuiMascot;
import org.junit.jupiter.api.Test;

public class JatatuiMascotTest {

  @Test
  public void new_mascot_defaults() {
    JatatuiMascot m = JatatuiMascot.newMascot();
    assertEquals(new Color.Indexed(252), m.cupColor);
    assertEquals(new Color.Indexed(231), m.steamColor);
    assertEquals(new Color.Indexed(196), m.logoColor);
  }

  @Test
  public void with_cup_color() {
    Color yellow = new Color.Yellow();
    JatatuiMascot m = JatatuiMascot.newMascot().withCupColor(yellow);
    assertEquals(yellow, m.cupColor);
  }

  @Test
  public void with_steam_color() {
    Color cyan = new Color.Cyan();
    JatatuiMascot m = JatatuiMascot.newMascot().withSteamColor(cyan);
    assertEquals(cyan, m.steamColor);
  }

  @Test
  public void with_logo_color() {
    Color green = new Color.Green();
    JatatuiMascot m = JatatuiMascot.newMascot().withLogoColor(green);
    assertEquals(green, m.logoColor);
  }

  @Test
  public void render_mascot() {
    JatatuiMascot mascot = JatatuiMascot.newMascot();
    Buffer buf = Buffer.empty(new Rect(0, 0, 32, 16));
    mascot.render(buf.area(), buf);
    assertEquals(new Size(32, 16), buf.area().asSize());

    // Cup body cell at (4, 5) — top-left of cup rim — must be cup-colored.
    assertEquals(new Color.Indexed(252), buf.cellAt(4, 5).fg);
    // Steam cell at (14, 1) must be steam-colored.
    assertEquals(new Color.Indexed(231), buf.cellAt(14, 1).fg);
    // Logo cell at (7, 7) — first column of the J's top bar — must be logo-colored.
    assertEquals(new Color.Indexed(196), buf.cellAt(7, 7).fg);

    StringBuilder sb = new StringBuilder();
    for (Cell c : buf.content()) {
      sb.append(c.symbol());
    }
    Buffer expected =
        Buffer.withLines(
            "                                ",
            "              █    █            ",
            "               █  █             ",
            "                ██              ",
            "                                ",
            "    ████████████████            ",
            "    █              █            ",
            "    █  ████████    █            ",
            "    █        █     █            ",
            "    █        █     █            ",
            "    █  █     █     █            ",
            "    █   █████      █            ",
            "    █              █            ",
            "     █            █             ",
            "       ██████████               ",
            "                                ");
    StringBuilder expSb = new StringBuilder();
    for (Cell c : expected.content()) {
      expSb.append(c.symbol());
    }
    assertEquals(expSb.toString(), sb.toString());
  }

  @Test
  public void render_in_minimal_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    JatatuiMascot mascot = JatatuiMascot.newMascot();
    // Should not throw.
    mascot.render(buf.area(), buf);
    Buffer expected = Buffer.withLines(" ");
    assertEquals(expected, buf);
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    JatatuiMascot mascot = JatatuiMascot.newMascot();
    // Should not throw.
    mascot.render(buf.area(), buf);
  }
}
