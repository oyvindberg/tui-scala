package tui
package internal

import java.text.BreakIterator
import java.util.Locale

object UnicodeSegmentation {

  def graphemes(str: String, isExtended: Boolean, locale: Locale = Locale.getDefault): Array[tui.Grapheme] = {
    val b = Array.newBuilder[tui.Grapheme]
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
