package jatatui.tests.widgets.mascot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import jatatui.core.style.Color;
import jatatui.widgets.RatatuiMascot;
import jatatui.widgets.RatatuiMascot.MascotEyeColor;
import org.junit.jupiter.api.Test;

public class RatatuiMascotTest {

  @Test
  public void new_mascot() {
    RatatuiMascot mascot = RatatuiMascot.newMascot();
    assertEquals(MascotEyeColor.Default, mascot.eyeState);
  }

  @Test
  public void set_eye_color() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 32, 16));
    RatatuiMascot mascot = RatatuiMascot.newMascot().withEye(MascotEyeColor.Red);
    mascot.render(buf.area(), buf);
    assertEquals(MascotEyeColor.Red, mascot.eyeState);
    assertEquals(new Color.Indexed(196), buf.cellAt(21, 5).bg);
  }

  @Test
  public void render_mascot() {
    RatatuiMascot mascot = RatatuiMascot.newMascot();
    Buffer buf = Buffer.empty(new Rect(0, 0, 32, 16));
    mascot.render(buf.area(), buf);
    assertEquals(new Size(32, 16), buf.area().asSize());
    assertEquals(new Color.Indexed(236), buf.cellAt(21, 5).bg);
    StringBuilder sb = new StringBuilder();
    for (Cell c : buf.content()) {
      sb.append(c.symbol());
    }
    Buffer expected =
        Buffer.withLines(
            "             ▄▄███              ",
            "           ▄███████             ",
            "         ▄█████████             ",
            "        ████████████            ",
            "        ▀███████████▀   ▄▄██████",
            "              ▀███▀▄█▀▀████████ ",
            "            ▄▄▄▄▀▄████████████  ",
            "           ████████████████     ",
            "           ▀███▀██████████      ",
            "         ▄▀▀▄   █████████       ",
            "       ▄▀ ▄  ▀▄▀█████████       ",
            "     ▄▀  ▀▀    ▀▄▀███████       ",
            "   ▄▀      ▄▄    ▀▄▀█████████   ",
            " ▄▀         ▀▀     ▀▄▀██▀  ███  ",
            "█                    ▀▄▀  ▄██   ",
            " ▀▄                    ▀▄▀█     ");
    StringBuilder expSb = new StringBuilder();
    for (Cell c : expected.content()) {
      expSb.append(c.symbol());
    }
    assertEquals(expSb.toString(), sb.toString());
  }

  @Test
  public void render_in_minimal_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    RatatuiMascot mascot = RatatuiMascot.newMascot();
    // Should not throw.
    mascot.render(buf.area(), buf);
    Buffer expected = Buffer.withLines(" ");
    assertEquals(expected, buf);
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    RatatuiMascot mascot = RatatuiMascot.newMascot();
    // Should not throw.
    mascot.render(buf.area(), buf);
  }
}
