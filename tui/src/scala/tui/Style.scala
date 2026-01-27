package tui

/** `style` contains the primitives used to control how your user interface will look.
  *
  * Style let you control the main characteristics of the displayed elements.
  *
  * It represents an incremental change. If you apply the styles S1, S2, S3 to a cell of the terminal buffer, the style of this cell will be the result of the
  * merge of S1, S2 and S3, not just S3.
  */
case class Style(
    fg: Option[Color] = None,
    bg: Option[Color] = None,
    addModifier: Modifier = Modifier.EMPTY,
    subModifier: Modifier = Modifier.EMPTY
) {

  /** Changes the foreground color.
    */
  def fg(color: Color): Style =
    copy(fg = Some(color))

  /** Changes the background color.
    */
  def bg(color: Color): Style =
    copy(bg = Some(color))

  /** Changes the text emphasis.
    *
    * When applied, it adds the given modifier to the `Style` modifiers.
    */
  def addModifier(modifier: Modifier): Style =
    copy(
      subModifier = subModifier.remove(modifier),
      addModifier = addModifier.insert(modifier)
    )

  /** Changes the text emphasis.
    *
    * When applied, it removes the given modifier from the `Style` modifiers.
    */
  def removeModifier(modifier: Modifier): Style =
    copy(
      addModifier = addModifier.remove(modifier),
      subModifier = subModifier.insert(modifier)
    )

  /** Results in a combined style that is equivalent to applying the two individual styles to a style one after the other.
    */
  def patch(other: Style): Style =
    Style(
      fg = other.fg.orElse(this.fg),
      bg = other.bg.orElse(this.bg),
      addModifier = addModifier.remove(other.subModifier).insert(other.addModifier),
      subModifier = subModifier.remove(other.addModifier).insert(other.subModifier)
    )
}

object Style {
  val DEFAULT: Style = Style()

  /** Returns a `Style` resetting all properties.
    */
  val RESET: Style = Style(
    fg = Some(Color.Reset),
    bg = Some(Color.Reset),
    addModifier = Modifier.EMPTY,
    subModifier = Modifier.ALL
  )
}
