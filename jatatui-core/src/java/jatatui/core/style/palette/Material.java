package jatatui.core.style.palette;

import jatatui.core.style.Color;

/// Material design color palettes.
///
/// Represents the colors from the 2014 [Material design color palettes][palettes] by Google.
///
/// [palettes]: https://m2.material.io/design/color/the-color-system.html#tools-for-picking-colors
///
/// There are 16 palettes with accent colors, and 3 palettes without accent colors. Each palette
/// has 10 colors, with variants from 50 to 900. The accent palettes also have 4 accent colors
/// with variants from 100 to 700. Black and White are also included for completeness.
///
/// ## Example
///
/// ```
/// import jatatui.core.style.Color;
/// import jatatui.core.style.palette.Material;
///
/// assert Material.RED.c500.equals(new Color.Rgb(244, 67, 54));
/// assert Material.BLUE.c500.equals(new Color.Rgb(33, 150, 243));
/// ```
public final class Material {
  private Material() {}

  /// A palette of colors for use in Material design with accent colors.
  ///
  /// Contains 10 base colors (`c50` through `c900`) and 4 accent colors (`a100`, `a200`, `a400`,
  /// `a700`).
  public record AccentedPalette(
      Color c50,
      Color c100,
      Color c200,
      Color c300,
      Color c400,
      Color c500,
      Color c600,
      Color c700,
      Color c800,
      Color c900,
      Color a100,
      Color a200,
      Color a400,
      Color a700) {
    /// Create a new `AccentedPalette` from 14 `0x00RRGGBB` variants.
    public static AccentedPalette fromVariants(int[] variants) {
      if (variants.length != 14) {
        throw new IllegalArgumentException("expected 14 variants, got " + variants.length);
      }
      return new AccentedPalette(
          Color.fromU32(variants[0]),
          Color.fromU32(variants[1]),
          Color.fromU32(variants[2]),
          Color.fromU32(variants[3]),
          Color.fromU32(variants[4]),
          Color.fromU32(variants[5]),
          Color.fromU32(variants[6]),
          Color.fromU32(variants[7]),
          Color.fromU32(variants[8]),
          Color.fromU32(variants[9]),
          Color.fromU32(variants[10]),
          Color.fromU32(variants[11]),
          Color.fromU32(variants[12]),
          Color.fromU32(variants[13]));
    }
  }

  /// A palette of colors for use in Material design without accent colors.
  ///
  /// Contains 10 base colors (`c50` through `c900`).
  public record NonAccentedPalette(
      Color c50,
      Color c100,
      Color c200,
      Color c300,
      Color c400,
      Color c500,
      Color c600,
      Color c700,
      Color c800,
      Color c900) {
    /// Create a new `NonAccentedPalette` from 10 `0x00RRGGBB` variants.
    public static NonAccentedPalette fromVariants(int[] variants) {
      if (variants.length != 10) {
        throw new IllegalArgumentException("expected 10 variants, got " + variants.length);
      }
      return new NonAccentedPalette(
          Color.fromU32(variants[0]),
          Color.fromU32(variants[1]),
          Color.fromU32(variants[2]),
          Color.fromU32(variants[3]),
          Color.fromU32(variants[4]),
          Color.fromU32(variants[5]),
          Color.fromU32(variants[6]),
          Color.fromU32(variants[7]),
          Color.fromU32(variants[8]),
          Color.fromU32(variants[9]));
    }
  }

  // ---- Variant data ----

  private static final int[] RED_VARIANTS = {
    0xFFEBEE, 0xFFCDD2, 0xEF9A9A, 0xE57373, 0xEF5350, 0xF44336, 0xE53935, 0xD32F2F, 0xC62828,
    0xB71C1C, 0xFF8A80, 0xFF5252, 0xFF1744, 0xD50000
  };

  private static final int[] PINK_VARIANTS = {
    0xFCE4EC, 0xF8BBD0, 0xF48FB1, 0xF06292, 0xEC407A, 0xE91E63, 0xD81B60, 0xC2185B, 0xAD1457,
    0x880E4F, 0xFF80AB, 0xFF4081, 0xF50057, 0xC51162
  };

  private static final int[] PURPLE_VARIANTS = {
    0xF3E5F5, 0xE1BEE7, 0xCE93D8, 0xBA68C8, 0xAB47BC, 0x9C27B0, 0x8E24AA, 0x7B1FA2, 0x6A1B9A,
    0x4A148C, 0xEA80FC, 0xE040FB, 0xD500F9, 0xAA00FF
  };

  private static final int[] DEEP_PURPLE_VARIANTS = {
    0xEDE7F6, 0xD1C4E9, 0xB39DDB, 0x9575CD, 0x7E57C2, 0x673AB7, 0x5E35B1, 0x512DA8, 0x4527A0,
    0x311B92, 0xB388FF, 0x7C4DFF, 0x651FFF, 0x6200EA
  };

  private static final int[] INDIGO_VARIANTS = {
    0xE8EAF6, 0xC5CAE9, 0x9FA8DA, 0x7986CB, 0x5C6BC0, 0x3F51B5, 0x3949AB, 0x303F9F, 0x283593,
    0x1A237E, 0x8C9EFF, 0x536DFE, 0x3D5AFE, 0x304FFE
  };

  private static final int[] BLUE_VARIANTS = {
    0xE3F2FD, 0xBBDEFB, 0x90CAF9, 0x64B5F6, 0x42A5F5, 0x2196F3, 0x1E88E5, 0x1976D2, 0x1565C0,
    0x0D47A1, 0x82B1FF, 0x448AFF, 0x2979FF, 0x2962FF
  };

  private static final int[] LIGHT_BLUE_VARIANTS = {
    0xE1F5FE, 0xB3E5FC, 0x81D4FA, 0x4FC3F7, 0x29B6F6, 0x03A9F4, 0x039BE5, 0x0288D1, 0x0277BD,
    0x01579B, 0x80D8FF, 0x40C4FF, 0x00B0FF, 0x0091EA
  };

  private static final int[] CYAN_VARIANTS = {
    0xE0F7FA, 0xB2EBF2, 0x80DEEA, 0x4DD0E1, 0x26C6DA, 0x00BCD4, 0x00ACC1, 0x0097A7, 0x00838F,
    0x006064, 0x84FFFF, 0x18FFFF, 0x00E5FF, 0x00B8D4
  };

  private static final int[] TEAL_VARIANTS = {
    0xE0F2F1, 0xB2DFDB, 0x80CBC4, 0x4DB6AC, 0x26A69A, 0x009688, 0x00897B, 0x00796B, 0x00695C,
    0x004D40, 0xA7FFEB, 0x64FFDA, 0x1DE9B6, 0x00BFA5
  };

  private static final int[] GREEN_VARIANTS = {
    0xE8F5E9, 0xC8E6C9, 0xA5D6A7, 0x81C784, 0x66BB6A, 0x4CAF50, 0x43A047, 0x388E3C, 0x2E7D32,
    0x1B5E20, 0xB9F6CA, 0x69F0AE, 0x00E676, 0x00C853
  };

  private static final int[] LIGHT_GREEN_VARIANTS = {
    0xF1F8E9, 0xDCEDC8, 0xC5E1A5, 0xAED581, 0x9CCC65, 0x8BC34A, 0x7CB342, 0x689F38, 0x558B2F,
    0x33691E, 0xCCFF90, 0xB2FF59, 0x76FF03, 0x64DD17
  };

  private static final int[] LIME_VARIANTS = {
    0xF9FBE7, 0xF0F4C3, 0xE6EE9C, 0xDCE775, 0xD4E157, 0xCDDC39, 0xC0CA33, 0xAFB42B, 0x9E9D24,
    0x827717, 0xF4FF81, 0xEEFF41, 0xC6FF00, 0xAEEA00
  };

  private static final int[] YELLOW_VARIANTS = {
    0xFFFDE7, 0xFFF9C4, 0xFFF59D, 0xFFF176, 0xFFEE58, 0xFFEB3B, 0xFDD835, 0xFBC02D, 0xF9A825,
    0xF57F17, 0xFFFF8D, 0xFFFF00, 0xFFEA00, 0xFFD600
  };

  private static final int[] AMBER_VARIANTS = {
    0xFFF8E1, 0xFFECB3, 0xFFE082, 0xFFD54F, 0xFFCA28, 0xFFC107, 0xFFB300, 0xFFA000, 0xFF8F00,
    0xFF6F00, 0xFFE57F, 0xFFD740, 0xFFC400, 0xFFAB00
  };

  private static final int[] ORANGE_VARIANTS = {
    0xFFF3E0, 0xFFE0B2, 0xFFCC80, 0xFFB74D, 0xFFA726, 0xFF9800, 0xFB8C00, 0xF57C00, 0xEF6C00,
    0xE65100, 0xFFD180, 0xFFAB40, 0xFF9100, 0xFF6D00
  };

  private static final int[] DEEP_ORANGE_VARIANTS = {
    0xFBE9E7, 0xFFCCBC, 0xFFAB91, 0xFF8A65, 0xFF7043, 0xFF5722, 0xF4511E, 0xE64A19, 0xD84315,
    0xBF360C, 0xFF9E80, 0xFF6E40, 0xFF3D00, 0xDD2C00
  };

  private static final int[] BROWN_VARIANTS = {
    0xEFEBE9, 0xD7CCC8, 0xBCAAA4, 0xA1887F, 0x8D6E63, 0x795548, 0x6D4C41, 0x5D4037, 0x4E342E,
    0x3E2723
  };

  private static final int[] GRAY_VARIANTS = {
    0xFAFAFA, 0xF5F5F5, 0xEEEEEE, 0xE0E0E0, 0xBDBDBD, 0x9E9E9E, 0x757575, 0x616161, 0x424242,
    0x212121
  };

  private static final int[] BLUE_GRAY_VARIANTS = {
    0xECEFF1, 0xCFD8DC, 0xB0BEC5, 0x90A4AE, 0x78909C, 0x607D8B, 0x546E7A, 0x455A64, 0x37474F,
    0x263238
  };

  // ---- Accented palettes ----

  public static final AccentedPalette RED = AccentedPalette.fromVariants(RED_VARIANTS);
  public static final AccentedPalette PINK = AccentedPalette.fromVariants(PINK_VARIANTS);
  public static final AccentedPalette PURPLE = AccentedPalette.fromVariants(PURPLE_VARIANTS);
  public static final AccentedPalette DEEP_PURPLE =
      AccentedPalette.fromVariants(DEEP_PURPLE_VARIANTS);
  public static final AccentedPalette INDIGO = AccentedPalette.fromVariants(INDIGO_VARIANTS);
  public static final AccentedPalette BLUE = AccentedPalette.fromVariants(BLUE_VARIANTS);
  public static final AccentedPalette LIGHT_BLUE =
      AccentedPalette.fromVariants(LIGHT_BLUE_VARIANTS);
  public static final AccentedPalette CYAN = AccentedPalette.fromVariants(CYAN_VARIANTS);
  public static final AccentedPalette TEAL = AccentedPalette.fromVariants(TEAL_VARIANTS);
  public static final AccentedPalette GREEN = AccentedPalette.fromVariants(GREEN_VARIANTS);
  public static final AccentedPalette LIGHT_GREEN =
      AccentedPalette.fromVariants(LIGHT_GREEN_VARIANTS);
  public static final AccentedPalette LIME = AccentedPalette.fromVariants(LIME_VARIANTS);
  public static final AccentedPalette YELLOW = AccentedPalette.fromVariants(YELLOW_VARIANTS);
  public static final AccentedPalette AMBER = AccentedPalette.fromVariants(AMBER_VARIANTS);
  public static final AccentedPalette ORANGE = AccentedPalette.fromVariants(ORANGE_VARIANTS);
  public static final AccentedPalette DEEP_ORANGE =
      AccentedPalette.fromVariants(DEEP_ORANGE_VARIANTS);

  // ---- Unaccented palettes ----

  public static final NonAccentedPalette BROWN = NonAccentedPalette.fromVariants(BROWN_VARIANTS);
  public static final NonAccentedPalette GRAY = NonAccentedPalette.fromVariants(GRAY_VARIANTS);
  public static final NonAccentedPalette BLUE_GRAY =
      NonAccentedPalette.fromVariants(BLUE_GRAY_VARIANTS);

  // ---- Black and white ----

  public static final Color BLACK = Color.fromU32(0x000000);
  public static final Color WHITE = Color.fromU32(0xFFFFFF);
}
