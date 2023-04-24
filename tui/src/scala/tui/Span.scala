package tui

import tui.internal.UnicodeSegmentation

/** string where all graphemes have the same style.
  */
case class Span(content: String, style: Style) {

  /** Returns the width of the content held by this span.
    */
  def width: Int = content.length

  /** Returns an iterator over the graphemes held by this span.
    *
    * `base_style` is the `Style` that will be patched with each grapheme `Style` to get the resulting `Style`.
    */
  def styled_graphemes(base_style: Style): Array[StyledGrapheme] =
    UnicodeSegmentation
      .graphemes(content, is_extended = true)
      .map(g =>
        StyledGrapheme(
          symbol = g,
          style = base_style.patch(style)
        )
      )
      .filter(s => s.symbol.str != "\n")
}

object Span {

  /** Create a span with no style.
    */
  def nostyle(content: String): Span =
    Span(content = content, style = Style())

  /** Create a span with a style.
    */
  def styled(content: String, style: Style): Span =
    Span(content = content, style)
}
