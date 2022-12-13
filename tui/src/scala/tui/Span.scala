package tui

import tui.internal.UnicodeSegmentation

/// A string where all graphemes have the same style.
//#[derive(Debug, Clone, PartialEq, Eq)]
case class Span(
    content: String,
    style: Style
) {
  /// Returns the width of the content held by this span.
  def width: Int = content.length

  /// Returns an iterator over the graphemes held by this span.
  ///
  /// `base_style` is the [`Style`] that will be patched with each grapheme [`Style`] to get
  /// the resulting [`Style`].
  ///
  /// ## Examples
  ///
  /// ```rust
  /// # use tui::text::{Span, StyledGrapheme};
  /// # use tui::style::{Color, Modifier, Style};
  /// # use std::iter::Iterator;
  /// let style = Style::DEFAULT.fg(Color::Yellow);
  /// let span = Span::styled("Text", style);
  /// let style = Style::DEFAULT.fg(Color::Green).bg(Color::Black);
  /// let styled_graphemes = span.styled_graphemes(style);
  /// assert_eq!(
  ///     vec![
  ///         StyledGrapheme {
  ///             symbol: "T",
  ///             style: Style {
  ///                 fg: Some(Color::Yellow),
  ///                 bg: Some(Color::Black),
  ///                 add_modifier: Modifier::empty(),
  ///                 sub_modifier: Modifier::empty(),
  ///             },
  ///         },
  ///         StyledGrapheme {
  ///             symbol: "e",
  ///             style: Style {
  ///                 fg: Some(Color::Yellow),
  ///                 bg: Some(Color::Black),
  ///                 add_modifier: Modifier::empty(),
  ///                 sub_modifier: Modifier::empty(),
  ///             },
  ///         },
  ///         StyledGrapheme {
  ///             symbol: "x",
  ///             style: Style {
  ///                 fg: Some(Color::Yellow),
  ///                 bg: Some(Color::Black),
  ///                 add_modifier: Modifier::empty(),
  ///                 sub_modifier: Modifier::empty(),
  ///             },
  ///         },
  ///         StyledGrapheme {
  ///             symbol: "t",
  ///             style: Style {
  ///                 fg: Some(Color::Yellow),
  ///                 bg: Some(Color::Black),
  ///                 add_modifier: Modifier::empty(),
  ///                 sub_modifier: Modifier::empty(),
  ///             },
  ///         },
  ///     ],
  ///     styled_graphemes.collect::<Vec<StyledGrapheme>>()
  /// );
  /// ```
  def styled_graphemes(base_style: Style): Array[StyledGrapheme] =
    UnicodeSegmentation
      .graphemes(content, true)
      .map(g =>
        StyledGrapheme(
          symbol = g,
          style = base_style.patch(style)
        )
      )
      .filter(s => s.symbol.str != "\n")
}

object Span {
  /// Create a span with no style.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// # use tui::text::Span;
  /// Span::raw("My text");
  /// Span::raw(String::from("My text"));
  /// ```
  def raw[T](content: T)(implicit ev: T => String): Span =
    Span(
      content = ev(content),
      style = Style()
    )

  /// Create a span with a style.
  ///
  /// # Examples
  ///
  /// ```rust
  /// # use tui::text::Span;
  /// # use tui::style::{Color, Modifier, Style};
  /// let style = Style::DEFAULT.fg(Color::Yellow).add_modifier(Modifier::ITALIC);
  /// Span::styled("My text", style);
  /// Span::styled(String::from("My text"), style);
  /// ```
  def styled[T](content: T, style: Style)(implicit ev: T => String): Span =
    Span(
      content = ev(content),
      style
    )

  def from(str: String): Span = raw(str)
}
