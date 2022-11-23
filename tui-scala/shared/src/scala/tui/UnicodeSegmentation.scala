package tui

import java.text.BreakIterator
import java.util.Locale

case class Grapheme(str: String) {
  lazy val width: Int = math.max(0, str.codePoints().map(Wcwidth.of).sum())
}

object Grapheme {
  val Empty = Grapheme(" ")
}

object UnicodeSegmentation {

  def graphemes(str: String, is_extended: Boolean, locale: Locale = Locale.getDefault): Array[Grapheme] = {
    val b = Array.newBuilder[Grapheme]
    val boundary = BreakIterator.getCharacterInstance(locale)
    boundary.setText(str)

    var start = boundary.first
    var end = boundary.next
    while (end != BreakIterator.DONE) {
      val chunk = str.substring(start, end)
      b += Grapheme(chunk)
      start = end
      end = boundary.next
    }
    b.result()
  }
}
