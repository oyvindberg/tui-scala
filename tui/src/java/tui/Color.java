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
}
