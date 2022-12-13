package tui

//! `style` contains the primitives used to control how your user interface will look.

/// Style let you control the main characteristics of the displayed elements.
///
/// ```rust
/// # use tui::style::{Color, Modifier, Style};
/// Style::DEFAULT
///     .fg(Color::Black)
///     .bg(Color::Green)
///     .add_modifier(Modifier::ITALIC | Modifier::BOLD);
/// ```
///
/// It represents an incremental change. If you apply the styles S1, S2, S3 to a cell of the
/// terminal buffer, the style of this cell will be the result of the merge of S1, S2 and S3, not
/// just S3.
///
/// ```rust
/// # use tui::style::{Color, Modifier, Style};
/// # use tui::buffer::Buffer;
/// # use tui::layout::Rect;
/// let styles = [
///     Style::DEFAULT.fg(Color::Blue).add_modifier(Modifier::BOLD | Modifier::ITALIC),
///     Style::DEFAULT.bg(Color::Red),
///     Style::DEFAULT.fg(Color::Yellow).remove_modifier(Modifier::ITALIC),
/// ];
/// let mut buffer = Buffer::empty(Rect::new(0, 0, 1, 1));
/// for style in &styles {
///   buffer.get_mut(0, 0).set_style(*style);
/// }
/// assert_eq!(
///     Style {
///         fg: Some(Color::Yellow),
///         bg: Some(Color::Red),
///         add_modifier: Modifier::BOLD,
///         sub_modifier: Modifier::empty(),
///     },
///     buffer.get(0, 0).style(),
/// );
/// ```
///
/// The default implementation returns a `Style` that does not modify anything. If you wish to
/// reset all properties until that point use [`Style::reset`].
///
/// ```
/// # use tui::style::{Color, Modifier, Style};
/// # use tui::buffer::Buffer;
/// # use tui::layout::Rect;
/// let styles = [
///     Style::DEFAULT.fg(Color::Blue).add_modifier(Modifier::BOLD | Modifier::ITALIC),
///     Style::reset().fg(Color::Yellow),
/// ];
/// let mut buffer = Buffer::empty(Rect::new(0, 0, 1, 1));
/// for style in &styles {
///   buffer.get_mut(0, 0).set_style(*style);
/// }
/// assert_eq!(
///     Style {
///         fg: Some(Color::Yellow),
///         bg: Some(Color::Reset),
///         add_modifier: Modifier::empty(),
///         sub_modifier: Modifier::empty(),
///     },
///     buffer.get(0, 0).style(),
/// );
/// ```
//#[derive(Debug, Clone, Copy, PartialEq, Eq)]
//#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
case class Style(
    fg: Option[Color] = None,
    bg: Option[Color] = None,
    add_modifier: Modifier = Modifier.EMPTY,
    sub_modifier: Modifier = Modifier.EMPTY
) {
  /// Changes the foreground color.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// # use tui::style::{Color, Style};
  /// let style = Style::DEFAULT.fg(Color::Blue);
  /// let diff = Style::DEFAULT.fg(Color::Red);
  /// assert_eq!(style.patch(diff), Style::DEFAULT.fg(Color::Red));
  /// ```
  def fg(color: Color): Style =
    copy(fg = Some(color))

  /// Changes the background color.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// # use tui::style::{Color, Style};
  /// let style = Style::DEFAULT.bg(Color::Blue);
  /// let diff = Style::DEFAULT.bg(Color::Red);
  /// assert_eq!(style.patch(diff), Style::DEFAULT.bg(Color::Red));
  /// ```
  def bg(color: Color): Style =
    copy(bg = Some(color))

  /// Changes the text emphasis.
  ///
  /// When applied, it adds the given modifier to the `Style` modifiers.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// # use tui::style::{Color, Modifier, Style};
  /// let style = Style::DEFAULT.add_modifier(Modifier::BOLD);
  /// let diff = Style::DEFAULT.add_modifier(Modifier::ITALIC);
  /// let patched = style.patch(diff);
  /// assert_eq!(patched.add_modifier, Modifier::BOLD | Modifier::ITALIC);
  /// assert_eq!(patched.sub_modifier, Modifier::empty());
  /// ```
  def add_modifier(modifier: Modifier): Style =
    copy(
      sub_modifier = sub_modifier.remove(modifier),
      add_modifier = add_modifier.insert(modifier)
    )

  /// Changes the text emphasis.
  ///
  /// When applied, it removes the given modifier from the `Style` modifiers.
  ///
  /// ## Examples
  ///
  /// ```rust
  /// # use tui::style::{Color, Modifier, Style};
  /// let style = Style::DEFAULT.add_modifier(Modifier::BOLD | Modifier::ITALIC);
  /// let diff = Style::DEFAULT.remove_modifier(Modifier::ITALIC);
  /// let patched = style.patch(diff);
  /// assert_eq!(patched.add_modifier, Modifier::BOLD);
  /// assert_eq!(patched.sub_modifier, Modifier::ITALIC);
  /// ```
  def remove_modifier(modifier: Modifier): Style =
    copy(
      add_modifier = add_modifier.remove(modifier),
      sub_modifier = sub_modifier.insert(modifier)
    )

  /// Results in a combined style that is equivalent to applying the two individual styles to
  /// a style one after the other.
  ///
  /// ## Examples
  /// ```
  /// # use tui::style::{Color, Modifier, Style};
  /// let style_1 = Style::DEFAULT.fg(Color::Yellow);
  /// let style_2 = Style::DEFAULT.bg(Color::Red);
  /// let combined = style_1.patch(style_2);
  /// assert_eq!(
  ///     Style::DEFAULT.patch(style_1).patch(style_2),
  ///     Style::DEFAULT.patch(combined));
  /// ```
  def patch(other: Style): Style =
    Style(
      fg = other.fg.orElse(this.fg),
      bg = other.bg.orElse(this.bg),
      add_modifier = add_modifier.remove(other.sub_modifier).insert(other.add_modifier),
      sub_modifier = sub_modifier.remove(other.add_modifier).insert(other.sub_modifier)
    )
}

object Style {
  val DEFAULT: Style = Style()

  /// Returns a `Style` resetting all properties.
  val RESET: Style = Style(
    fg = Some(Color.Reset),
    bg = Some(Color.Reset),
    add_modifier = Modifier.EMPTY,
    sub_modifier = Modifier.ALL
  )
}
