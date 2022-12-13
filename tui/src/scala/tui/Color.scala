package tui

//#[derive(Debug, Clone, Copy, PartialEq, Eq)]
//#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
sealed trait Color

object Color {
  case object Reset extends Color

  case object Black extends Color

  case object Red extends Color

  case object Green extends Color

  case object Yellow extends Color

  case object Blue extends Color

  case object Magenta extends Color

  case object Cyan extends Color

  case object Gray extends Color

  case object DarkGray extends Color

  case object LightRed extends Color

  case object LightGreen extends Color

  case object LightYellow extends Color

  case object LightBlue extends Color

  case object LightMagenta extends Color

  case object LightCyan extends Color

  case object White extends Color

  case class Rgb(r: Byte, g: Byte, b: Byte) extends Color

  case class Indexed(byte: Byte) extends Color
}
