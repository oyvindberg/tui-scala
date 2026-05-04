package jatatui.tests.core.style.palette;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Color;
import jatatui.core.style.palette.Tailwind;
import org.junit.jupiter.api.Test;

/// Sanity checks for the [Tailwind] palette constants.
///
/// Upstream `style/palette/tailwind.rs` has no `#[cfg(test)] mod tests` block; the only
/// behavioral assertion is the `Example` doctest at the top:
///
/// ```rust
/// assert_eq!(RED.c500, Color::Rgb(239, 68, 68));
/// assert_eq!(BLUE.c500, Color::Rgb(59, 130, 246));
/// ```
public class TailwindTest {

  @Test
  public void doctest_example() {
    assertEquals(new Color.Rgb(239, 68, 68), Tailwind.RED.c500());
    assertEquals(new Color.Rgb(59, 130, 246), Tailwind.BLUE.c500());
  }

  @Test
  public void slate_palette_full() {
    assertEquals(new Color.Rgb(0xf8, 0xfa, 0xfc), Tailwind.SLATE.c50());
    assertEquals(new Color.Rgb(0x0f, 0x17, 0x2a), Tailwind.SLATE.c900());
    assertEquals(new Color.Rgb(0x02, 0x06, 0x17), Tailwind.SLATE.c950());
  }

  @Test
  public void rose_c950() {
    // Spot check the last palette
    assertEquals(new Color.Rgb(0x4c, 0x05, 0x19), Tailwind.ROSE.c950());
  }

  @Test
  public void black_and_white_constants() {
    assertEquals(new Color.Rgb(0, 0, 0), Tailwind.BLACK);
    assertEquals(new Color.Rgb(0xff, 0xff, 0xff), Tailwind.WHITE);
  }
}
