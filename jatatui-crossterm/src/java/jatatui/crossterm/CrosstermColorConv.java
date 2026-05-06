package jatatui.crossterm;

import jatatui.core.style.Color;

/// Bidirectional conversion between [jatatui.core.style.Color] (jatatui's color sealed interface,
/// modeled on ratatui's `Color` enum) and [tui.crossterm.Color] (the local JNI binding's color
/// enum).
///
/// Mirrors the `IntoCrossterm<CrosstermColor> for Color` and `FromCrossterm<CrosstermColor> for
/// Color` impls in upstream `ratatui-crossterm/src/lib.rs`.
public final class CrosstermColorConv {

  private CrosstermColorConv() {}

  /// Convert a jatatui [Color] to a [tui.crossterm.Color]. Mirrors upstream
  /// `IntoCrossterm<CrosstermColor> for Color`.
  public static tui.crossterm.Color toCrossterm(Color color) {
    return switch (color) {
      case Color.Reset r -> new tui.crossterm.Color.Reset();
      case Color.Black b -> new tui.crossterm.Color.Black();
      case Color.Red r -> new tui.crossterm.Color.DarkRed();
      case Color.Green g -> new tui.crossterm.Color.DarkGreen();
      case Color.Yellow y -> new tui.crossterm.Color.DarkYellow();
      case Color.Blue b -> new tui.crossterm.Color.DarkBlue();
      case Color.Magenta m -> new tui.crossterm.Color.DarkMagenta();
      case Color.Cyan c -> new tui.crossterm.Color.DarkCyan();
      case Color.Gray g -> new tui.crossterm.Color.Grey();
      case Color.DarkGray dg -> new tui.crossterm.Color.DarkGrey();
      case Color.LightRed lr -> new tui.crossterm.Color.Red();
      case Color.LightGreen lg -> new tui.crossterm.Color.Green();
      case Color.LightBlue lb -> new tui.crossterm.Color.Blue();
      case Color.LightYellow ly -> new tui.crossterm.Color.Yellow();
      case Color.LightMagenta lm -> new tui.crossterm.Color.Magenta();
      case Color.LightCyan lc -> new tui.crossterm.Color.Cyan();
      case Color.White w -> new tui.crossterm.Color.White();
      case Color.Indexed i -> new tui.crossterm.Color.AnsiValue(i.i() & 0xFF);
      case Color.Rgb rgb -> new tui.crossterm.Color.Rgb(rgb.r(), rgb.g(), rgb.b());
    };
  }

  /// Convert a [tui.crossterm.Color] to a jatatui [Color]. Mirrors upstream
  /// `FromCrossterm<CrosstermColor> for Color`.
  public static Color fromCrossterm(tui.crossterm.Color value) {
    return switch (value) {
      case tui.crossterm.Color.Reset r -> Color.RESET;
      case tui.crossterm.Color.Black b -> Color.BLACK;
      case tui.crossterm.Color.DarkRed dr -> Color.RED;
      case tui.crossterm.Color.DarkGreen dg -> Color.GREEN;
      case tui.crossterm.Color.DarkYellow dy -> Color.YELLOW;
      case tui.crossterm.Color.DarkBlue db -> Color.BLUE;
      case tui.crossterm.Color.DarkMagenta dm -> Color.MAGENTA;
      case tui.crossterm.Color.DarkCyan dc -> Color.CYAN;
      case tui.crossterm.Color.Grey g -> Color.GRAY;
      case tui.crossterm.Color.DarkGrey dg -> Color.DARK_GRAY;
      case tui.crossterm.Color.Red r -> Color.LIGHT_RED;
      case tui.crossterm.Color.Green g -> Color.LIGHT_GREEN;
      case tui.crossterm.Color.Blue b -> Color.LIGHT_BLUE;
      case tui.crossterm.Color.Yellow y -> Color.LIGHT_YELLOW;
      case tui.crossterm.Color.Magenta m -> Color.LIGHT_MAGENTA;
      case tui.crossterm.Color.Cyan c -> Color.LIGHT_CYAN;
      case tui.crossterm.Color.White w -> Color.WHITE;
      case tui.crossterm.Color.Rgb rgb -> new Color.Rgb(rgb.r(), rgb.g(), rgb.b());
      case tui.crossterm.Color.AnsiValue v -> new Color.Indexed(v.color());
    };
  }
}
