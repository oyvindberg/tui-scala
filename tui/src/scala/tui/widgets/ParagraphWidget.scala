package tui
package widgets

import tui.internal.saturating._
import tui.internal.reflow.{LineComposer, LineTruncator, WordWrapper}

/** A widget to display some text.
  *
  * @param text
  *   The text to display
  * @param block
  *   A block to wrap the widget in
  * @param style
  *   Widget style
  * @param wrap
  *   How to wrap the text
  * @param scroll
  *   Scroll
  * @param alignment
  *   Alignment of the text
  */
case class ParagraphWidget(
    text: Text,
    style: Style = Style.DEFAULT,
    wrap: Option[ParagraphWidget.Wrap] = None,
    scroll: (Int, Int) = (0, 0),
    alignment: Alignment = Alignment.Left
) extends Widget {

  def render(area: Rect, buf: Buffer): Unit = {
    buf.setStyle(area, this.style)
    if (area.height < 1) {
      return
    }

    val styled: Array[StyledGrapheme] =
      text.lines.flatMap { case Spans(spans: Array[Span]) =>
        spans.flatMap(span => span.styledGraphemes(this.style)) :+ StyledGrapheme(Grapheme("\n"), this.style)
      }

    val line_composer: LineComposer =
      wrap match {
        case Some(ParagraphWidget.Wrap(trim)) => WordWrapper(styled.iterator, area.width, trim)
        case None =>
          val line_composer = LineTruncator(styled.iterator, area.width)
          alignment match {
            case Alignment.Left => line_composer.copy(horizontal_offset = this.scroll._2)
            case _              => line_composer
          }
      }

    var y = 0
    var continue = true
    while (continue)
      line_composer.next_line() match {
        case None => continue = false
        case Some((current_line, current_line_width)) =>
          if (y >= this.scroll._1) {
            var x = ParagraphWidget.getLineOffset(current_line_width, area.width, this.alignment)
            current_line.foreach { case StyledGrapheme(symbol, style) =>
              // If the symbol is empty, the last char which rendered last time will
              // leave on the line. It's a quick fix.
              val newSymbol = if (symbol.str.isEmpty) " " else symbol.str

              buf
                .get(area.left + x, area.top + y - this.scroll._1)
                .setSymbol(newSymbol)
                .setStyle(style)

              x += symbol.width
            }
          }
          y += 1
          if (y >= area.height + this.scroll._1) {
            continue = false
          }
      }
  }
}
object ParagraphWidget {
  def getLineOffset(lineWidth: Int, textAreaWidth: Int, alignment: Alignment): Int =
    alignment match {
      case Alignment.Center => (textAreaWidth / 2).saturating_sub_unsigned(lineWidth / 2)
      case Alignment.Right  => textAreaWidth.saturating_sub_unsigned(lineWidth)
      case Alignment.Left   => 0
    }

  /** Describes how to wrap text across lines.
    *
    * @param trim
    *   Should leading whitespace be trimmed
    */
  case class Wrap(
      trim: Boolean
  )
}
