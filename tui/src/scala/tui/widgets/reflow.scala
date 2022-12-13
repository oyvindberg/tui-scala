package tui
package widgets

import tui.internal.breakableForeach._
import tui.internal.{breakableForeach, UnicodeSegmentation}

import scala.collection.mutable

object reflow {
  val NBSP: String = "\u00a0"

  /// A state machine to pack styled symbols into lines.
  /// Cannot implement it as Iterator since it yields slices of the internal buffer (need streaming
  /// iterators for that).
  trait LineComposer {
    def next_line(): Option[(Array[StyledGrapheme], Int)]
  }

  /// A state machine that wraps lines on word boundaries.
  case class WordWrapper(
      symbols: Iterator[StyledGrapheme],
      max_line_width: Int,
      /// Removes the leading whitespace from lines
      trim: Boolean
  ) extends LineComposer {
    var current_line: mutable.ArrayBuffer[StyledGrapheme] = mutable.ArrayBuffer.empty
    var next_line_var: mutable.ArrayBuffer[StyledGrapheme] = mutable.ArrayBuffer.empty

    def next_line(): Option[(Array[StyledGrapheme], Int)] = {
      if (max_line_width == 0) {
        return None
      }

      val tmp = current_line
      current_line = next_line_var
      next_line_var = tmp
      next_line_var.clear()

      var current_line_width = current_line.iterator.map { case StyledGrapheme(symbol, _) => symbol.width }.sum

      var symbols_to_last_word_end: Int = 0
      var width_to_last_word_end: Int = 0
      var prev_whitespace = false
      var symbols_exhausted = true

      def handleSymbol(sg: StyledGrapheme): breakableForeach.Res = {
        symbols_exhausted = false
        val symbol_whitespace = sg.symbol.str.forall(_.isWhitespace) && sg.symbol.str != NBSP

        // Ignore characters wider that the total max width.
        if (
          sg.symbol.width > max_line_width ||
          // Skip leading whitespace when trim is enabled.
          trim && symbol_whitespace && sg.symbol.str != "\n" && current_line_width == 0
        ) {
          return breakableForeach.Continue
        }

        // Break on newline and discard it.
        if (sg.symbol.str == "\n") {
          if (prev_whitespace) {
            current_line_width = width_to_last_word_end
            current_line.takeInPlace(symbols_to_last_word_end)
          }
          return breakableForeach.Break;
        }

        // Mark the previous symbol as word end.
        if (symbol_whitespace && !prev_whitespace) {
          symbols_to_last_word_end = current_line.length
          width_to_last_word_end = current_line_width
        }

        current_line.append(StyledGrapheme(sg.symbol, sg.style))
        current_line_width += sg.symbol.width

        if (current_line_width > max_line_width) {
          // If there was no word break in the text, wrap at the end of the line.
          val (truncate_at, truncated_width) = if (symbols_to_last_word_end != 0) {
            (symbols_to_last_word_end, width_to_last_word_end)
          } else {
            (current_line.length - 1, max_line_width)
          }

          // Push the remainder to the next line but strip leading whitespace:
          {
            val remainder = current_line.drop(truncate_at)

            remainder.indexWhere { case StyledGrapheme(symbol, _) => !symbol.str.forall(_.isWhitespace) } match {
              case -1 => ()
              case remainder_nonwhite =>
                next_line_var ++= remainder.drop(remainder_nonwhite)
            }
          }
          current_line.takeInPlace(truncate_at)
          current_line_width = truncated_width
          breakableForeach.Break
        } else {
          prev_whitespace = symbol_whitespace
          breakableForeach.Continue
        }
      }

      symbols.breakableForeach(handleSymbol)

      // Even if the iterator is exhausted, pass the previous remainder.
      if (symbols_exhausted && current_line.isEmpty) {
        None
      } else {
        Some((current_line.toArray, current_line_width))
      }
    }
  }

  /// A state machine that truncates overhanging lines.
  case class LineTruncator(
      symbols: Iterator[StyledGrapheme],
      max_line_width: Int,
      current_line: mutable.ArrayBuffer[StyledGrapheme] = mutable.ArrayBuffer.empty,
      /// Record the offet to skip render
      horizontal_offset: Int = 0
  ) extends LineComposer {
    def next_line(): Option[(Array[StyledGrapheme], Int)] = {

      if (max_line_width == 0) {
        return None
      }

      current_line.clear()
      var current_line_width = 0

      var skip_rest = false
      var symbols_exhausted = true
      var horizontal_offset = this.horizontal_offset

      def handleSymbol(sg: StyledGrapheme): breakableForeach.Res = {
        val symbol = sg.symbol
        symbols_exhausted = false

        // Ignore characters wider that the total max width.
        if (symbol.width > max_line_width) {
          return breakableForeach.Continue
        }

        // Break on newline and discard it.
        if (symbol.str == "\n") {
          return breakableForeach.Break
        }

        if (current_line_width + symbol.width > max_line_width) {
          // Exhaust the remainder of the line.
          skip_rest = true
          return breakableForeach.Break
        }

        val symbol1: Grapheme = if (horizontal_offset == 0) {
          symbol
        } else {
          val w = symbol.width
          if (w > horizontal_offset) {
            val t = trim_offset(symbol.str, horizontal_offset)
            horizontal_offset = 0
            Grapheme(t)
          } else {
            horizontal_offset -= w
            Grapheme("")
          }
        }
        current_line_width += symbol1.width
        this.current_line.addOne(StyledGrapheme(symbol1, sg.style))
        breakableForeach.Continue
      }

      symbols.breakableForeach(handleSymbol)

      if (skip_rest) {
        symbols.breakableForeach { case StyledGrapheme(symbol, _) =>
          if (symbol.str == "\n") breakableForeach.Break else breakableForeach.Continue
        }
      }

      if (symbols_exhausted && current_line.isEmpty) {
        None
      } else {
        Some((current_line.toArray, current_line_width))
      }
    }
  }

  /// This function will return a str slice which start at specified offset.
  /// As src is a unicode str, start offset has to be calculated with each character.
  def trim_offset(src: String, offset0: Int): String = {
    var offset = offset0
    var start = 0
    // todo: the only usage of trim_offset unpacks `src` from a Grapheme, so this is likely unnecessary
    UnicodeSegmentation.graphemes(src, true).breakableForeach { (c, _) =>
      if (c.width <= offset) {
        offset -= c.width
        start += c.str.length
        breakableForeach.Continue
      } else {
        breakableForeach.Break
      }
    }
    src.substring(start)
  }
}
