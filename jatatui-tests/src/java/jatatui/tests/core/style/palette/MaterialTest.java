package jatatui.tests.core.style.palette;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Color;
import jatatui.core.style.palette.Material;
import org.junit.jupiter.api.Test;

/// Sanity checks for the [Material] palette constants.
///
/// Upstream `style/palette/material.rs` has no `#[cfg(test)] mod tests` block; the only
/// behavioral assertion is the `Example` doctest at the top:
///
/// ```rust
/// assert_eq!(RED.c500, Color::Rgb(244, 67, 54));
/// assert_eq!(BLUE.c500, Color::Rgb(33, 150, 243));
/// ```
///
/// We promote that doctest to a real test, plus a few spot checks on accent and unaccented
/// palette construction.
public class MaterialTest {

  @Test
  public void doctest_example() {
    assertEquals(new Color.Rgb(244, 67, 54), Material.RED.c500());
    assertEquals(new Color.Rgb(33, 150, 243), Material.BLUE.c500());
  }

  @Test
  public void red_palette_full() {
    assertEquals(new Color.Rgb(0xFF, 0xEB, 0xEE), Material.RED.c50());
    assertEquals(new Color.Rgb(0xB7, 0x1C, 0x1C), Material.RED.c900());
    assertEquals(new Color.Rgb(0xFF, 0x8A, 0x80), Material.RED.a100());
    assertEquals(new Color.Rgb(0xD5, 0x00, 0x00), Material.RED.a700());
  }

  @Test
  public void unaccented_palette_brown() {
    assertEquals(new Color.Rgb(0xEF, 0xEB, 0xE9), Material.BROWN.c50());
    assertEquals(new Color.Rgb(0x3E, 0x27, 0x23), Material.BROWN.c900());
  }

  @Test
  public void black_and_white_constants() {
    assertEquals(new Color.Rgb(0, 0, 0), Material.BLACK);
    assertEquals(new Color.Rgb(0xFF, 0xFF, 0xFF), Material.WHITE);
  }
}
