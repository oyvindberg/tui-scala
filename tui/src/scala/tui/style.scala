package tui

//! `style` contains the primitives used to control how your user interface will look.

//#[derive(Debug, Clone, Copy, PartialEq, Eq)]
//#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
sealed trait Color

object Color {
  case object Reset extends Color

  case object Black extends Color

  case object Red extends Color

  case object Green extends Color

  case object Yellow extends Color

  case object Blue extends Color

  case object Magenta extends Color

  case object Cyan extends Color

  case object Gray extends Color

  case object DarkGray extends Color

  case object LightRed extends Color

  case object LightGreen extends Color

  case object LightYellow extends Color

  case object LightBlue extends Color

  case object LightMagenta extends Color

  case object LightCyan extends Color

  case object White extends Color

  case class Rgb(r: Byte, g: Byte, b: Byte) extends Color

  case class Indexed(byte: Byte) extends Color
}

/*  Modifier changes the way a piece of text is displayed.
 *
 *  They are bitflags so they can easily be composed.
 *
 *  ## Examples
 *
 *  ```rust
 *  # use tui::style::Modifier;
 *
 *  let m = Modifier::BOLD | Modifier::ITALIC;
 *  ```
 */
//#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
//#[derive(Copy, PartialEq, Eq, Clone, PartialOrd, Ord, Hash)]
case class Modifier(bits: Int) {
  def fmt(sb: StringBuilder): Unit = {
    var first = true;
    if (Modifier.BOLD.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("BOLD");
    }
    if (Modifier.DIM.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("DIM")
    }
    if (Modifier.ITALIC.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("ITALIC")
    }
    if (Modifier.UNDERLINED.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("UNDERLINED")
    }
    if (Modifier.SLOW_BLINK.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("SLOW_BLINK")
    }
    if (Modifier.RAPID_BLINK.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("RAPID_BLINK")
    }
    if (Modifier.REVERSED.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("REVERSED")
    }
    if (Modifier.HIDDEN.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("HIDDEN")
    }
    if (Modifier.CROSSED_OUT.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("CROSSED_OUT")
    }
    if (first) {
      sb.append("(empty)")
    }
    ()
  }

  override def toString: String = {
    val sb = new StringBuilder()
    fmt(sb)
    sb.toString()
  }

  /// Returns `true` if all of the flags in `other` are contained within `self`.
  def contains(other: Modifier): Boolean =
    other != Modifier.EMPTY && (bits & other.bits) == other.bits

  /// Inserts the specified flags in-place.
  def insert(other: Modifier): Modifier =
    copy(bits = bits | other.bits)

  /// Removes the specified flags in-place.
  def remove(other: Modifier): Modifier =
    copy(bits = bits & ~other.bits)

  def |(mod: Modifier): Modifier =
    insert(mod)

  def -(mod: Modifier): Modifier =
    remove(mod)
}

object Modifier {
  val BOLD: Modifier = Modifier(bits = 1 << 0)
  val DIM: Modifier = Modifier(bits = 1 << 1)
  val ITALIC: Modifier = Modifier(bits = 1 << 2)
  val UNDERLINED: Modifier = Modifier(bits = 1 << 3)
  val SLOW_BLINK: Modifier = Modifier(bits = 1 << 4)
  val RAPID_BLINK: Modifier = Modifier(bits = 1 << 5)
  val REVERSED: Modifier = Modifier(bits = 1 << 6)
  val HIDDEN: Modifier = Modifier(bits = 1 << 7)
  val CROSSED_OUT: Modifier = Modifier(bits = 1 << 8)
  /// Returns an empty set of flags.
  val EMPTY = Modifier(bits = 0)
  /// Returns the set containing all flags.
  val ALL = Modifier(bits = Integer.parseInt("000111111111", 2))
}

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
  val DEFAULT = Style()

  /// Returns a `Style` resetting all properties.
  val RESET = Style(
    fg = Some(Color.Reset),
    bg = Some(Color.Reset),
    add_modifier = Modifier.EMPTY,
    sub_modifier = Modifier.ALL
  )
}
