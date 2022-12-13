package tui

/// A string composed of clusters of graphemes, each with their own style.
case class Spans(spans: Array[Span]) {
  /// Returns the width of the underlying string.
  def width: Int =
    spans.map(_.width).sum

  override def toString: String = spans.mkString("")
}

object Spans {
  def nostyle(s: String): Spans = from(Span.nostyle(s))
  def from(spans: Span*): Spans = Spans(spans.toArray)
}
