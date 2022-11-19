package tui

import java.text.BreakIterator
import java.util.Locale

object UnicodeSegmentation {
  def graphemes(str: String, unknown: Boolean, locale: Locale = Locale.getDefault): Array[String] = {
    val b = Array.newBuilder[String]
    val boundary = BreakIterator.getCharacterInstance(locale)
    boundary.setText(str)

    var start = boundary.first
    var end = boundary.next
    while (end != BreakIterator.DONE) {
      val chunk = str.substring(start, end)
      b += chunk
      start = end
      end = boundary.next
    }
    b.result()
  }
}
