package jatatui.tests.core.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.layout.solver.Either;
import jatatui.core.style.Color;
import jatatui.core.style.ParseColorError;
import org.junit.jupiter.api.Test;

public class ColorTest {

  @Test
  public void from_u32() {
    assertEquals(new Color.Rgb(0, 0, 0), Color.fromU32(0x000000));
    assertEquals(new Color.Rgb(255, 0, 0), Color.fromU32(0xFF0000));
    assertEquals(new Color.Rgb(0, 255, 0), Color.fromU32(0x00FF00));
    assertEquals(new Color.Rgb(0, 0, 255), Color.fromU32(0x0000FF));
    assertEquals(new Color.Rgb(255, 255, 255), Color.fromU32(0xFFFFFF));
  }

  @Test
  public void from_rgb_color() {
    Color color = Color.fromString("#FF0000").unwrap();
    assertEquals(new Color.Rgb(255, 0, 0), color);
  }

  @Test
  public void from_indexed_color() {
    Color color = Color.fromString("10").unwrap();
    assertEquals(new Color.Indexed(10), color);
  }

  @Test
  public void from_ansi_color() {
    assertEquals(Color.RESET, Color.fromString("reset").unwrap());
    assertEquals(Color.BLACK, Color.fromString("black").unwrap());
    assertEquals(Color.RED, Color.fromString("red").unwrap());
    assertEquals(Color.GREEN, Color.fromString("green").unwrap());
    assertEquals(Color.YELLOW, Color.fromString("yellow").unwrap());
    assertEquals(Color.BLUE, Color.fromString("blue").unwrap());
    assertEquals(Color.MAGENTA, Color.fromString("magenta").unwrap());
    assertEquals(Color.CYAN, Color.fromString("cyan").unwrap());
    assertEquals(Color.GRAY, Color.fromString("gray").unwrap());
    assertEquals(Color.DARK_GRAY, Color.fromString("darkgray").unwrap());
    assertEquals(Color.LIGHT_RED, Color.fromString("lightred").unwrap());
    assertEquals(Color.LIGHT_GREEN, Color.fromString("lightgreen").unwrap());
    assertEquals(Color.LIGHT_YELLOW, Color.fromString("lightyellow").unwrap());
    assertEquals(Color.LIGHT_BLUE, Color.fromString("lightblue").unwrap());
    assertEquals(Color.LIGHT_MAGENTA, Color.fromString("lightmagenta").unwrap());
    assertEquals(Color.LIGHT_CYAN, Color.fromString("lightcyan").unwrap());
    assertEquals(Color.WHITE, Color.fromString("white").unwrap());

    // aliases
    assertEquals(Color.DARK_GRAY, Color.fromString("lightblack").unwrap());
    assertEquals(Color.WHITE, Color.fromString("lightwhite").unwrap());
    assertEquals(Color.WHITE, Color.fromString("lightgray").unwrap());

    // silver = grey = gray
    assertEquals(Color.GRAY, Color.fromString("grey").unwrap());
    assertEquals(Color.GRAY, Color.fromString("silver").unwrap());

    // spaces are ignored
    assertEquals(Color.DARK_GRAY, Color.fromString("light black").unwrap());
    assertEquals(Color.WHITE, Color.fromString("light white").unwrap());
    assertEquals(Color.WHITE, Color.fromString("light gray").unwrap());

    // dashes are ignored
    assertEquals(Color.DARK_GRAY, Color.fromString("light-black").unwrap());
    assertEquals(Color.WHITE, Color.fromString("light-white").unwrap());
    assertEquals(Color.WHITE, Color.fromString("light-gray").unwrap());

    // underscores are ignored
    assertEquals(Color.DARK_GRAY, Color.fromString("light_black").unwrap());
    assertEquals(Color.WHITE, Color.fromString("light_white").unwrap());
    assertEquals(Color.WHITE, Color.fromString("light_gray").unwrap());

    // bright = light
    assertEquals(Color.DARK_GRAY, Color.fromString("bright-black").unwrap());
    assertEquals(Color.WHITE, Color.fromString("bright-white").unwrap());

    // bright = light
    assertEquals(Color.DARK_GRAY, Color.fromString("brightblack").unwrap());
    assertEquals(Color.WHITE, Color.fromString("brightwhite").unwrap());
  }

  @Test
  public void from_invalid_colors() {
    String[] badColors = {
      "invalid_color", // not a color string
      "abcdef0", // 7 chars is not a color
      " bcdefa", // doesn't start with a '#'
      "#abcdef00", // too many chars
      "#1🦀2", // len 7 in chars (surrogate pair) but on char boundaries shouldn't blow up
      "resets", // typo
      "lightblackk" // typo
    };
    for (String bad : badColors) {
      Either<ParseColorError, Color> result = Color.fromString(bad);
      assertTrue(result.isLeft(), "bad color: '" + bad + "'");
    }
  }

  @Test
  public void display() {
    assertEquals("Black", Color.BLACK.toString());
    assertEquals("Red", Color.RED.toString());
    assertEquals("Green", Color.GREEN.toString());
    assertEquals("Yellow", Color.YELLOW.toString());
    assertEquals("Blue", Color.BLUE.toString());
    assertEquals("Magenta", Color.MAGENTA.toString());
    assertEquals("Cyan", Color.CYAN.toString());
    assertEquals("Gray", Color.GRAY.toString());
    assertEquals("DarkGray", Color.DARK_GRAY.toString());
    assertEquals("LightRed", Color.LIGHT_RED.toString());
    assertEquals("LightGreen", Color.LIGHT_GREEN.toString());
    assertEquals("LightYellow", Color.LIGHT_YELLOW.toString());
    assertEquals("LightBlue", Color.LIGHT_BLUE.toString());
    assertEquals("LightMagenta", Color.LIGHT_MAGENTA.toString());
    assertEquals("LightCyan", Color.LIGHT_CYAN.toString());
    assertEquals("White", Color.WHITE.toString());
    assertEquals("10", new Color.Indexed(10).toString());
    assertEquals("#FF0000", new Color.Rgb(255, 0, 0).toString());
    assertEquals("Reset", Color.RESET.toString());
  }

  @Test
  public void test_from_array_and_tuple_conversions() {
    Color fromArray3 = Color.fromRgbArray(new int[] {123, 45, 67});
    assertEquals(new Color.Rgb(123, 45, 67), fromArray3);

    // Tuple variant: Java has no tuples, use Rgb directly.
    Color fromTuple3 = new Color.Rgb(89, 76, 54);
    assertEquals(new Color.Rgb(89, 76, 54), fromTuple3);

    Color fromArray4 = Color.fromRgbaArray(new int[] {10, 20, 30, 255});
    assertEquals(new Color.Rgb(10, 20, 30), fromArray4);

    // Tuple4 variant: same — Rgb directly.
    Color fromTuple4 = new Color.Rgb(200, 150, 100);
    assertEquals(new Color.Rgb(200, 150, 100), fromTuple4);
  }
}
