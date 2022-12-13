package tui

import tui.internal.Wcwidth

case class Grapheme(str: String) {
  lazy val width: Int = math.max(0, str.codePoints().map(Wcwidth.of).sum())
}

object Grapheme {
  val Empty: Grapheme = Grapheme(" ")
}
