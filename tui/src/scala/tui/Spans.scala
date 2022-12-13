package tui

/// A string composed of clusters of graphemes, each with their own style.
case class Spans(spans: Array[Span]) {
  /// Returns the width of the underlying string.
  def width: Int =
    spans.map(_.width).sum

  override def toString = spans.mkString("")
}

object Spans {
  def from(s: String): Spans = from(Span.from(s))
  def from(span: Span): Spans = from(Array(span))
  def from(spans: Array[Span]): Spans = Spans(spans)
}
