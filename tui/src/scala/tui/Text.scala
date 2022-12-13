package tui

import scala.collection.Factory
import scala.jdk.StreamConverters.StreamHasToScala

//! Primitives for styled text.
//!
//! A terminal UI is at its root a lot of strings. In order to make it accessible and stylish,
//! those strings may be associated to a set of styles. `tui` has three ways to represent them:
//! - A single line string where all graphemes have the same style is represented by a [`Span`].
//! - A single line string where each grapheme may have its own style is represented by [`Spans`].
//! - A multiple line string where each grapheme may have its own style is represented by a
//! [`Text`].
//!
//! These types form a hierarchy: [`Spans`] is a collection of [`Span`] and each line of [`Text`]
//! is a [`Spans`].
//!
//! Keep it mind that a lot of widgets will use those types to advertise what kind of string is
//! supported for their properties. Moreover, `tui` provides convenient `From` implementations so
//! that you can start by using simple `String` or `&str` and then promote them to the previous
//! primitives when you need additional styling capabilities.
//!
//! For example, for the [`crate::widgets::Block`] widget, all the following calls are valid to set
//! its `title` property (which is a [`Spans`] under the hood):
//!

/// A string split over multiple lines where each line is composed of several clusters, each with
/// their own style.
///
/// A [`Text`], like a [`Span`], can be constructed using one of the many `From` implementations
/// or via the [`Text::raw`] and [`Text::styled`] methods. Helpfully, [`Text`] also implements
/// [`core::iter::Extend`] which enables the concatenation of several [`Text`] blocks.
case class Text(lines: Array[Spans]) {

  /// Returns the max width of all the lines.
  def width: Int =
    lines
      .map(_.width)
      .maxOption
      .getOrElse(0)

  /// Returns the height.
  def height: Int =
    lines.length

  /// Apply a new style to existing text.
  def patch_style(style: Style): Text = {
    val newLines = lines.map { case Spans(spans) => Spans(spans.map(span => span.copy(style = span.style.patch(style)))) }
    Text(newLines)
  }
}

object Text {
  /// Create some text (potentially multiple lines) with no style.
  def raw[T](content: T)(implicit ev: T => String): Text = {
    val spans = ev(content).lines().map(Spans.from).toScala(Factory.arrayFactory[Spans])
    Text(spans)
  }

  /// Create some text (potentially multiple lines) with a style.
  def styled[T](content: T, style: Style)(implicit ev: T => String): Text = {
    val text = Text.raw(content)
    text.patch_style(style);
    text
  }

  def from(str: String): Text =
    from(Span.from(str))
  def from(span: Span): Text =
    from(Spans.from(span))
  def from(spans: Array[Span]): Text =
    from(Spans.from(spans))
  def from(spans: Spans): Text =
    Text(lines = Array(spans))
}
