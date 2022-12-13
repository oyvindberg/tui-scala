package tui

/// A string composed of clusters of graphemes, each with their own style.
//#[derive(Debug, Clone, PartialEq, Default, Eq)]
case class Spans(spans: Array[Span]) {
  /// Returns the width of the underlying string.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// # use tui::text::{Span, Spans};
  /// # use tui::style::{Color, Style};
  /// let spans = Spans::from(vec![
  ///     Span::styled("My", Style::DEFAULT.fg(Color::Yellow)),
  ///     Span::raw(" text"),
  /// ]);
  /// assert_eq!(7, spans.width());
  /// ```
  def width: Int =
    spans.map(_.width).sum

  override def toString = spans.mkString("")
}

object Spans {
  def from(s: String): Spans = from(Span.from(s))
  def from(span: Span): Spans = from(Array(span))
  def from(spans: Array[Span]): Spans = Spans(spans)
}
