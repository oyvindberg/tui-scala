package tui

/// A string composed of clusters of graphemes, each with their own style.
case class Spans(spans: Array[Span]) {
  /// Returns the width of the underlying string.
  def width: Int =
    spans.map(_.width).sum

  override def toString: String = spans.mkString("")

  /// Apply a new style to existing text.
  def patchedStyle(style: Style, overwrite: Boolean): Spans =
    Spans(spans.map(_.patchedStyle(style, overwrite)))

  def /(style: Style): Spans =
    patchedStyle(style, overwrite = true)

  def /(optionalStyle: Option[Style]): Spans =
    optionalStyle match {
      case Some(style) => this / style
      case None        => this
    }
}

object Spans {
  def nostyle(s: String): Spans = from(Span.nostyle(s))
  def styled(s: String, style: Style): Spans = from(Span.styled(s, style))
  def from(spans: Span*): Spans = Spans(spans.toArray)
}
