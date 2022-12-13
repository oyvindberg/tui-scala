package tui

/// A string where all graphemes have the same style.
case class Span(content: String, style: Style) {
  /// Returns the width of the content held by this span.
  def width: Int = content.length

  /// Apply a new style to existing text.
  def patchedStyle(s: Style, overwrite: Boolean): Span =
    copy(style = if (overwrite) style.patch(s) else s.patch(s))
}

object Span {
  /// Create a span with no style.
  ///
  def nostyle(content: String): Span =
    Span(content = content, style = Style())

  /// Create a span with a style.
  def styled(content: String, style: Style): Span =
    Span(content = content, style)
}
