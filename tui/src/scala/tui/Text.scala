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
//! ```rust
//! # use tui::widgets::Block;
//! # use tui::text::{Span, Spans};
//! # use tui::style::{Color, Style};
//! // A simple string with no styling.
//! // Converted to Spans(vec![
//! //   Span { content: Cow::Borrowed("My title"), style: Style { .. } }
//! // ])
//! let block = Block::default().title("My title");
//!
//! // A simple string with a unique style.
//! // Converted to Spans(vec![
//! //   Span { content: Cow::Borrowed("My title"), style: Style { fg: Some(Color::Yellow), .. }
//! // ])
//! let block = Block::default().title(
//!     Span::styled("My title", Style::DEFAULT.fg(Color::Yellow))
//! );
//!
//! // A string with multiple styles.
//! // Converted to Spans(vec![
//! //   Span { content: Cow::Borrowed("My"), style: Style { fg: Some(Color::Yellow), .. } },
//! //   Span { content: Cow::Borrowed(" title"), .. }
//! // ])
//! let block = Block::default().title(vec![
//!     Span::styled("My", Style::DEFAULT.fg(Color::Yellow)),
//!     Span::raw(" title"),
//! ]);
//! ```

/// A string split over multiple lines where each line is composed of several clusters, each with
/// their own style.
///
/// A [`Text`], like a [`Span`], can be constructed using one of the many `From` implementations
/// or via the [`Text::raw`] and [`Text::styled`] methods. Helpfully, [`Text`] also implements
/// [`core::iter::Extend`] which enables the concatenation of several [`Text`] blocks.
///
/// ```rust
/// # use tui::text::Text;
/// # use tui::style::{Color, Modifier, Style};
/// let style = Style::DEFAULT.fg(Color::Yellow).add_modifier(Modifier::ITALIC);
///
/// // An initial two lines of `Text` built from a `&str`
/// let mut text = Text::from("The first line\nThe second line");
/// assert_eq!(2, text.height());
///
/// // Adding two more unstyled lines
/// text.extend(Text::raw("These are two\nmore lines!"));
/// assert_eq!(4, text.height());
///
/// // Adding a final two styled lines
/// text.extend(Text::styled("Some more lines\nnow with more style!", style));
/// assert_eq!(6, text.height());
/// ```
//#[derive(Debug, Clone, PartialEq, Default, Eq)]
case class Text(lines: Array[Spans]) {

  /// Returns the max width of all the lines.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// use tui::text::Text;
  /// let text = Text::from("The first line\nThe second line");
  /// assert_eq!(15, text.width());
  /// ```
  def width: Int =
    lines
      .map(_.width)
      .maxOption
      .getOrElse(0)

  /// Returns the height.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// use tui::text::Text;
  /// let text = Text::from("The first line\nThe second line");
  /// assert_eq!(2, text.height());
  /// ```
  def height: Int =
    lines.length

  /// Apply a new style to existing text.
  ///
  /// # Examples
  ///
  /// ```rust
  /// # use tui::text::Text;
  /// # use tui::style::{Color, Modifier, Style};
  /// let style = Style::DEFAULT.fg(Color::Yellow).add_modifier(Modifier::ITALIC);
  /// let mut raw_text = Text::raw("The first line\nThe second line");
  /// let styled_text = Text::styled(String::from("The first line\nThe second line"), style);
  /// assert_ne!(raw_text, styled_text);
  ///
  /// raw_text.patch_style(style);
  /// assert_eq!(raw_text, styled_text);
  /// ```
  def patch_style(style: Style): Text = {
    val newLines = lines.map { case Spans(spans) => Spans(spans.map(span => span.copy(style = span.style.patch(style)))) }
    Text(newLines)
  }
}

object Text {
  /// Create some text (potentially multiple lines) with no style.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// # use tui::text::Text;
  /// Text::raw("The first line\nThe second line");
  /// Text::raw(String::from("The first line\nThe second line"));
  /// ```
  def raw[T](content: T)(implicit ev: T => String): Text = {
    val spans = ev(content).lines().map(Spans.from).toScala(Factory.arrayFactory[Spans])
    Text(spans)
  }

  /// Create some text (potentially multiple lines) with a style.
  ///
  /// # Examples
  ///
  /// ```rust
  /// # use tui::text::Text;
  /// # use tui::style::{Color, Modifier, Style};
  /// let style = Style::DEFAULT.fg(Color::Yellow).add_modifier(Modifier::ITALIC);
  /// Text::styled("The first line\nThe second line", style);
  /// Text::styled(String::from("The first line\nThe second line"), style);
  /// ```
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
