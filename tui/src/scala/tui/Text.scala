package tui

import scala.collection.Factory
import scala.jdk.StreamConverters.StreamHasToScala

/** Primitives for styled text.
  *
  * A terminal UI is at its root a lot of strings. In order to make it accessible and stylish, those strings may be associated to a set of styles. `tui` has
  * three ways to represent them:
  *   - A single line string where all graphemes have the same style is represented by a `Span`.
  *   - A single line string where each grapheme may have its own style is represented by `Spans`.
  *   - A multiple line string where each grapheme may have its own style is represented by a `Text`.
  *
  * These types form a hierarchy: `Spans` is a collection of `Span` and each line of `Text` is a `Spans`.
  *
  * Keep it mind that a lot of widgets will use those types to advertise what kind of string is supported for their properties. Moreover, `tui` provides
  * convenient `From` implementations so that you can start by using simple `String` or `&str` and then promote them to the previous primitives when you need
  * additional styling capabilities.
  *
  * For example, for the `Block` widget, all the following calls are valid to set its `title` property (which is a `Spans` under the hood):
  *
  * A string split over multiple lines where each line is composed of several clusters, each with their own style.
  *
  * A `Text`, like a `Span`, can be constructed using one of the many `From` implementations or via the `Text.unstyled` and `Text.styled` methods. Helpfully,
  * `Text` also implements `Extend` which enables the concatenation of several `Text` blocks.
  */
case class Text(lines: Array[Spans]) {

  /** Returns the max width of all the lines.
    */
  def width: Int =
    lines
      .map(_.width)
      .maxOption
      .getOrElse(0)

  /** Returns the height.
    */
  def height: Int =
    lines.length

  /** Apply a new style to existing text.
    */
  def overwrittenStyle(style: Style): Text =
    Text(lines.map { case Spans(spans) =>
      Spans(spans.map { span =>
        span.copy(style = span.style.patch(style))
      })
    })
}

object Text {

  /** Create some text (potentially multiple lines) with no style.
    */
  def nostyle(content: String): Text =
    Text(content.lines().map(Spans.nostyle).toScala(Factory.arrayFactory[Spans]))
  def from(span: Span): Text =
    from(Spans(Array(span)))
  def from(spans: Span*): Text =
    from(Spans(spans.toArray))
  def from(spans: Spans): Text =
    Text(lines = Array(spans))
  def fromSpans(spans: Spans*): Text =
    Text(lines = spans.toArray)
}
