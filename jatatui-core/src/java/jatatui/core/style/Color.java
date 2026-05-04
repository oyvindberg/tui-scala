package jatatui.core.style;

import jatatui.core.layout.solver.Either;

/// ANSI Color.
///
/// All colors from the [ANSI color table] are supported (though some names are not exactly the
/// same).
///
/// | Color Name     | Color                   | Foreground | Background |
/// |----------------|-------------------------|------------|------------|
/// | `black`        | [Color.Black]           | 30         | 40         |
/// | `red`          | [Color.Red]             | 31         | 41         |
/// | `green`        | [Color.Green]           | 32         | 42         |
/// | `yellow`       | [Color.Yellow]          | 33         | 43         |
/// | `blue`         | [Color.Blue]            | 34         | 44         |
/// | `magenta`      | [Color.Magenta]         | 35         | 45         |
/// | `cyan`         | [Color.Cyan]            | 36         | 46         |
/// | `gray`         | [Color.Gray]            | 37         | 47         |
/// | `darkgray`     | [Color.DarkGray]        | 90         | 100        |
/// | `lightred`     | [Color.LightRed]        | 91         | 101        |
/// | `lightgreen`   | [Color.LightGreen]      | 92         | 102        |
/// | `lightyellow`  | [Color.LightYellow]     | 93         | 103        |
/// | `lightblue`    | [Color.LightBlue]       | 94         | 104        |
/// | `lightmagenta` | [Color.LightMagenta]    | 95         | 105        |
/// | `lightcyan`    | [Color.LightCyan]       | 96         | 106        |
/// | `white`        | [Color.White]           | 97         | 107        |
///
/// - `gray` is sometimes called `silver` - this is supported
/// - `darkgray` is sometimes called `light black` or `bright black` (both are supported)
/// - `white` is sometimes called `light white` or `bright white` (both are supported)
/// - we support `bright` and `light` prefixes for all colors
/// - we support `-`, `_`, and ` ` as separators for all colors
/// - we support both `gray` and `grey` spellings
///
/// [ANSI color table]: https://en.wikipedia.org/wiki/ANSI_escape_code#Colors
public sealed interface Color
    permits Color.Reset,
        Color.Black,
        Color.Red,
        Color.Green,
        Color.Yellow,
        Color.Blue,
        Color.Magenta,
        Color.Cyan,
        Color.Gray,
        Color.DarkGray,
        Color.LightRed,
        Color.LightGreen,
        Color.LightYellow,
        Color.LightBlue,
        Color.LightMagenta,
        Color.LightCyan,
        Color.White,
        Color.Rgb,
        Color.Indexed {

  /// Resets the foreground or background color.
  record Reset() implements Color {
    @Override
    public String toString() {
      return "Reset";
    }
  }

  /// ANSI Color: Black. Foreground: 30, Background: 40.
  record Black() implements Color {
    @Override
    public String toString() {
      return "Black";
    }
  }

  /// ANSI Color: Red. Foreground: 31, Background: 41.
  record Red() implements Color {
    @Override
    public String toString() {
      return "Red";
    }
  }

  /// ANSI Color: Green. Foreground: 32, Background: 42.
  record Green() implements Color {
    @Override
    public String toString() {
      return "Green";
    }
  }

  /// ANSI Color: Yellow. Foreground: 33, Background: 43.
  record Yellow() implements Color {
    @Override
    public String toString() {
      return "Yellow";
    }
  }

  /// ANSI Color: Blue. Foreground: 34, Background: 44.
  record Blue() implements Color {
    @Override
    public String toString() {
      return "Blue";
    }
  }

  /// ANSI Color: Magenta. Foreground: 35, Background: 45.
  record Magenta() implements Color {
    @Override
    public String toString() {
      return "Magenta";
    }
  }

  /// ANSI Color: Cyan. Foreground: 36, Background: 46.
  record Cyan() implements Color {
    @Override
    public String toString() {
      return "Cyan";
    }
  }

  /// ANSI Color: White. Foreground: 37, Background: 47.
  ///
  /// Note that this is sometimes called `silver` or `white` but we use `white` for bright white.
  record Gray() implements Color {
    @Override
    public String toString() {
      return "Gray";
    }
  }

  /// ANSI Color: Bright Black. Foreground: 90, Background: 100.
  ///
  /// Note that this is sometimes called `light black` or `bright black` but we use `dark gray`.
  record DarkGray() implements Color {
    @Override
    public String toString() {
      return "DarkGray";
    }
  }

  /// ANSI Color: Bright Red. Foreground: 91, Background: 101.
  record LightRed() implements Color {
    @Override
    public String toString() {
      return "LightRed";
    }
  }

  /// ANSI Color: Bright Green. Foreground: 92, Background: 102.
  record LightGreen() implements Color {
    @Override
    public String toString() {
      return "LightGreen";
    }
  }

  /// ANSI Color: Bright Yellow. Foreground: 93, Background: 103.
  record LightYellow() implements Color {
    @Override
    public String toString() {
      return "LightYellow";
    }
  }

  /// ANSI Color: Bright Blue. Foreground: 94, Background: 104.
  record LightBlue() implements Color {
    @Override
    public String toString() {
      return "LightBlue";
    }
  }

  /// ANSI Color: Bright Magenta. Foreground: 95, Background: 105.
  record LightMagenta() implements Color {
    @Override
    public String toString() {
      return "LightMagenta";
    }
  }

  /// ANSI Color: Bright Cyan. Foreground: 96, Background: 106.
  record LightCyan() implements Color {
    @Override
    public String toString() {
      return "LightCyan";
    }
  }

  /// ANSI Color: Bright White. Foreground: 97, Background: 107.
  /// Sometimes called `bright white` or `light white` in some terminals.
  record White() implements Color {
    @Override
    public String toString() {
      return "White";
    }
  }

  /// An RGB color (24-bit true color).
  ///
  /// Components are unsigned 8-bit values (0..255). Stored as `int` since Java has no `u8`; values
  /// outside the 0..255 range are masked to a single byte during formatting.
  record Rgb(int r, int g, int b) implements Color {
    @Override
    public String toString() {
      return String.format("#%02X%02X%02X", r & 0xFF, g & 0xFF, b & 0xFF);
    }
  }

  /// An 8-bit 256 color.
  record Indexed(int i) implements Color {
    @Override
    public String toString() {
      return Integer.toString(i & 0xFF);
    }
  }

  // ---- Singleton constants for the variants without payload ----

  Color RESET = new Reset();
  Color BLACK = new Black();
  Color RED = new Red();
  Color GREEN = new Green();
  Color YELLOW = new Yellow();
  Color BLUE = new Blue();
  Color MAGENTA = new Magenta();
  Color CYAN = new Cyan();
  Color GRAY = new Gray();
  Color DARK_GRAY = new DarkGray();
  Color LIGHT_RED = new LightRed();
  Color LIGHT_GREEN = new LightGreen();
  Color LIGHT_YELLOW = new LightYellow();
  Color LIGHT_BLUE = new LightBlue();
  Color LIGHT_MAGENTA = new LightMagenta();
  Color LIGHT_CYAN = new LightCyan();
  Color WHITE = new White();

  /// Convert a u32 (0x00RRGGBB) to a Color.
  static Color fromU32(int u) {
    int r = (u >> 16) & 0xFF;
    int g = (u >> 8) & 0xFF;
    int b = u & 0xFF;
    return new Rgb(r, g, b);
  }

  /// Construct a `Color.Rgb` from an array of 3 components `[r, g, b]`.
  ///
  /// Mirrors `From<[u8; 3]> for Color`.
  static Color fromRgbArray(int[] rgb) {
    if (rgb.length < 3) {
      throw new IllegalArgumentException("expected at least 3 components, got " + rgb.length);
    }
    return new Rgb(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
  }

  /// Construct a `Color.Rgb` from an array of 4 components `[r, g, b, _]`, ignoring the alpha.
  ///
  /// Mirrors `From<[u8; 4]> for Color`.
  static Color fromRgbaArray(int[] rgba) {
    if (rgba.length < 4) {
      throw new IllegalArgumentException("expected at least 4 components, got " + rgba.length);
    }
    return new Rgb(rgba[0] & 0xFF, rgba[1] & 0xFF, rgba[2] & 0xFF);
  }

  /// Parse a string into a `Color`. Returns `Either.left(ParseColorError)` on failure.
  ///
  /// Supports named colors (with `bright`/`light` prefix synonyms, `grey`/`silver` aliases),
  /// `#RRGGBB` hex, and a decimal index.
  static Either<ParseColorError, Color> fromString(String s) {
    String normalized =
        s.toLowerCase()
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "")
            .replace("bright", "light")
            .replace("grey", "gray")
            .replace("silver", "gray")
            .replace("lightblack", "darkgray")
            .replace("lightwhite", "white")
            .replace("lightgray", "white");
    return switch (normalized) {
      case "reset" -> Either.right(RESET);
      case "black" -> Either.right(BLACK);
      case "red" -> Either.right(RED);
      case "green" -> Either.right(GREEN);
      case "yellow" -> Either.right(YELLOW);
      case "blue" -> Either.right(BLUE);
      case "magenta" -> Either.right(MAGENTA);
      case "cyan" -> Either.right(CYAN);
      case "gray" -> Either.right(GRAY);
      case "darkgray" -> Either.right(DARK_GRAY);
      case "lightred" -> Either.right(LIGHT_RED);
      case "lightgreen" -> Either.right(LIGHT_GREEN);
      case "lightyellow" -> Either.right(LIGHT_YELLOW);
      case "lightblue" -> Either.right(LIGHT_BLUE);
      case "lightmagenta" -> Either.right(LIGHT_MAGENTA);
      case "lightcyan" -> Either.right(LIGHT_CYAN);
      case "white" -> Either.right(WHITE);
      default -> {
        // Try parse as decimal `u8` index
        try {
          int idx = Integer.parseInt(s);
          if (idx >= 0 && idx <= 255) {
            yield Either.right(new Indexed(idx));
          }
        } catch (NumberFormatException ignored) {
          // fall through
        }
        // Try parse as `#RRGGBB`
        Rgb hex = parseHexColor(s);
        if (hex != null) {
          yield Either.right(hex);
        }
        yield Either.left(new ParseColorError());
      }
    };
  }

  /// Parses a `#RRGGBB` string. Returns `null` on failure (internal helper, never escapes).
  private static Rgb parseHexColor(String input) {
    if (input.length() != 7 || input.charAt(0) != '#') {
      return null;
    }
    // Reject if not all ASCII (mirrors Rust's `s.get(1..3)?` byte-boundary safety).
    for (int i = 1; i < 7; i++) {
      char c = input.charAt(i);
      if (c > 127) {
        return null;
      }
    }
    try {
      int r = Integer.parseInt(input.substring(1, 3), 16);
      int g = Integer.parseInt(input.substring(3, 5), 16);
      int b = Integer.parseInt(input.substring(5, 7), 16);
      return new Rgb(r, g, b);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
