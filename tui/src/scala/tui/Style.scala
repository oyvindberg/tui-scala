package tui

//! `style` contains the primitives used to control how your user interface will look.

/// Style let you control the main characteristics of the displayed elements.
///
/// It represents an incremental change. If you apply the styles S1, S2, S3 to a cell of the
/// terminal buffer, the style of this cell will be the result of the merge of S1, S2 and S3, not
/// just S3.
///
case class Style(
    fg: Option[Color] = None,
    bg: Option[Color] = None,
    add_modifier: Modifier = Modifier.EMPTY,
    sub_modifier: Modifier = Modifier.EMPTY
) {
  /// Changes the foreground color.
  def fg(color: Color): Style =
    copy(fg = Some(color))

  /// Changes the background color.
  def bg(color: Color): Style =
    copy(bg = Some(color))

  /// Changes the text emphasis.
  ///
  /// When applied, it adds the given modifier to the `Style` modifiers.
  def add_modifier(modifier: Modifier): Style =
    copy(
      sub_modifier = sub_modifier.remove(modifier),
      add_modifier = add_modifier.insert(modifier)
    )

  /// Changes the text emphasis.
  ///
  /// When applied, it removes the given modifier from the `Style` modifiers.
  def remove_modifier(modifier: Modifier): Style =
    copy(
      add_modifier = add_modifier.remove(modifier),
      sub_modifier = sub_modifier.insert(modifier)
    )

  def /(other: Style): Style = patched_with(other, overwrite = true)
  def /(other: Option[Style]): Style = other match {
    case Some(other) => this / other
    case None        => this
  }
  def /(spans: Spans): Spans = spans.patchedStyle(this, overwrite = false)
  def /(span: Span): Span = span.patchedStyle(this, overwrite = false)
  def /(text: Text): Text = text.patchedStyle(this, overwrite = false)

  /// Results in a combined style that is equivalent to applying the two individual styles to
  /// a style one after the other.
  private[tui] def patched_with(other: Style, overwrite: Boolean): Style =
    if (overwrite)
      Style(
        fg = other.fg.orElse(this.fg),
        bg = other.bg.orElse(this.bg),
        add_modifier = this.add_modifier.remove(other.sub_modifier).insert(other.add_modifier),
        sub_modifier = this.sub_modifier.remove(other.add_modifier).insert(other.sub_modifier)
      )
    else
      Style(
        fg = this.fg.orElse(other.fg),
        bg = this.bg.orElse(other.bg),
        add_modifier = other.add_modifier.remove(this.sub_modifier).insert(this.add_modifier),
        sub_modifier = other.sub_modifier.remove(this.add_modifier).insert(this.sub_modifier)
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
