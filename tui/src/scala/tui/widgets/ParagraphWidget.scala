package tui
package widgets

import tui.internal.UnicodeSegmentation
import tui.internal.saturating._
import tui.internal.reflow.{LineComposer, LineTruncator, WordWrapper}

/// A widget to display some text.
case class ParagraphWidget(
    /// The text to display
    text: Text,
    /// A block to wrap the widget in
    block: Option[BlockWidget] = None,
    /// Widget style
    style: Style = Style.DEFAULT,
    /// How to wrap the text
    wrap: Option[ParagraphWidget.Wrap] = None,
    /// Scroll
    scroll: (Int, Int) = (0, 0),
    /// Alignment of the text
    alignment: Alignment = Alignment.Left
) extends Widget {

  def render(area: Rect, buf: Buffer): Unit = {
    val text_area = block match {
      case Some(b) =>
        val inner_area = b.inner(area)
        b.patchedStyle(style).render(area, buf)
        inner_area
      case None => area
    }

    if (text_area.height < 1) {
      return
    }

    val styled: Array[StyledGrapheme] = {
      val NewLine = StyledGrapheme(Grapheme("\n"), this.style)

      text.lines.flatMap { case Spans(spans: Array[Span]) =>
        spans.flatMap(span => ParagraphWidget.styled_graphemes(span, this.style)) :+ NewLine
      }
    }

    val line_composer: LineComposer =
      wrap match {
        case Some(ParagraphWidget.Wrap(trim)) => WordWrapper(styled.iterator, text_area.width, trim)
        case None =>
          val line_composer = LineTruncator(styled.iterator, text_area.width)
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
            var x = ParagraphWidget.get_line_offset(current_line_width, text_area.width, this.alignment)
            current_line.foreach { case StyledGrapheme(symbol, style) =>
              // If the symbol is empty, the last char which rendered last time will
              // leave on the line. It's a quick fix.
              val newSymbol = if (symbol.str.isEmpty) " " else symbol.str

              buf.set(text_area.left + x, text_area.top + y - this.scroll._1, Cell(newSymbol, this.style / style))

              x += symbol.width
            }
          }
          y += 1
          if (y >= text_area.height + this.scroll._1) {
            continue = false
          }
      }
  }
}
object ParagraphWidget {
  /// Returns an iterator over the graphemes held by this span.
  ///
  /// `base_style` is the [`Style`] that will be patched with each grapheme [`Style`] to get
  /// the resulting [`Style`].
  ///
  def styled_graphemes(span: Span, base_style: Style): Array[StyledGrapheme] =
    UnicodeSegmentation
      .graphemes(span.content, is_extended = true)
      .map(g =>
        StyledGrapheme(
          symbol = g,
          style = base_style.patched_with(span.style, overwrite = true)
        )
      )
      .filter(s => s.symbol.str != "\n")

  def get_line_offset(line_width: Int, text_area_width: Int, alignment: Alignment): Int =
    alignment match {
      case Alignment.Center => (text_area_width / 2).saturating_sub_unsigned(line_width / 2)
      case Alignment.Right  => text_area_width.saturating_sub_unsigned(line_width)
      case Alignment.Left   => 0
    }

  /// Describes how to wrap text across lines.
  case class Wrap(
      /// Should leading whitespace be trimmed
      trim: Boolean
  )
}
