package tui;

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

  Reset Reset = new Reset();
  Black Black = new Black();
  Red Red = new Red();
  Green Green = new Green();
  Yellow Yellow = new Yellow();
  Blue Blue = new Blue();
  Magenta Magenta = new Magenta();
  Cyan Cyan = new Cyan();
  Gray Gray = new Gray();
  DarkGray DarkGray = new DarkGray();
  LightRed LightRed = new LightRed();
  LightGreen LightGreen = new LightGreen();
  LightYellow LightYellow = new LightYellow();
  LightBlue LightBlue = new LightBlue();
  LightMagenta LightMagenta = new LightMagenta();
  LightCyan LightCyan = new LightCyan();
  White White = new White();

  record Reset() implements Color {}

  record Black() implements Color {}

  record Red() implements Color {}

  record Green() implements Color {}

  record Yellow() implements Color {}

  record Blue() implements Color {}

  record Magenta() implements Color {}

  record Cyan() implements Color {}

  record Gray() implements Color {}

  record DarkGray() implements Color {}

  record LightRed() implements Color {}

  record LightGreen() implements Color {}

  record LightYellow() implements Color {}

  record LightBlue() implements Color {}

  record LightMagenta() implements Color {}

  record LightCyan() implements Color {}

  record White() implements Color {}

  record Rgb(int r, int g, int b) implements Color {}

  record Indexed(int index) implements Color {}

  /// Parses a string representation into a Color.
  ///
  /// Supports many spellings and formats:
  ///   * named colors: "blue", "lightblue", "light blue", "light-blue", "light_blue", "lightBlue"
  ///   * "bright" prefix is treated as "light": "bright red" == "lightred"
  ///   * "grey" == "gray", "silver" == "gray", "lightblack" == "darkgray", "lightwhite" == "white"
  ///   * `#RRGGBB` hex RGB
  ///   * 0..255 indexed value as a decimal string
  /// Returns empty Optional if the string does not match any known form.
  static java.util.Optional<Color> fromString(String s) {
    String normalized = s
        .toLowerCase(java.util.Locale.ROOT)
        .replace(" ", "")
        .replace("-", "")
        .replace("_", "")
        .replace("bright", "light")
        .replace("grey", "gray")
        .replace("silver", "gray")
        .replace("lightblack", "darkgray")
        .replace("lightwhite", "white")
        .replace("lightgray", "white");
    Color named = switch (normalized) {
      case "reset" -> Reset;
      case "black" -> Black;
      case "red" -> Red;
      case "green" -> Green;
      case "yellow" -> Yellow;
      case "blue" -> Blue;
      case "magenta" -> Magenta;
      case "cyan" -> Cyan;
      case "gray" -> Gray;
      case "darkgray" -> DarkGray;
      case "lightred" -> LightRed;
      case "lightgreen" -> LightGreen;
      case "lightyellow" -> LightYellow;
      case "lightblue" -> LightBlue;
      case "lightmagenta" -> LightMagenta;
      case "lightcyan" -> LightCyan;
      case "white" -> White;
      default -> null;
    };
    if (named != null) {
      return java.util.Optional.of(named);
    }
    try {
      int idx = Integer.parseInt(s);
      if (idx >= 0 && idx <= 255) {
        return java.util.Optional.of(new Indexed(idx));
      }
    } catch (NumberFormatException ignored) {
      // not an index, try hex
    }
    if (s.startsWith("#") && s.length() == 7) {
      try {
        int r = Integer.parseInt(s.substring(1, 3), 16);
        int g = Integer.parseInt(s.substring(3, 5), 16);
        int b = Integer.parseInt(s.substring(5, 7), 16);
        return java.util.Optional.of(new Rgb(r, g, b));
      } catch (NumberFormatException ignored) {
        // fall through
      }
    }
    return java.util.Optional.empty();
  }
}
