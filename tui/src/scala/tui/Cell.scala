package tui

/// A buffer cell
case class Cell(symbol: Grapheme, fg: Color, bg: Color, modifier: Modifier) {
  def set_symbol(symbol: String): Cell =
    set_symbol(Grapheme(symbol))

  def set_symbol(symbol: tui.Grapheme): Cell =
    copy(symbol = symbol)

  def set_char(ch: Char): Cell =
    copy(symbol = Grapheme(ch.toString))

  def set_fg(color: Color): Cell =
    copy(fg = color)

  def set_bg(color: Color): Cell =
    copy(bg = color)

  def set_style(style: Style): Cell =
    Cell(
      symbol = symbol,
      fg = style.fg.getOrElse(fg),
      bg = style.bg.getOrElse(bg),
      modifier = modifier.insert(style.add_modifier).remove(style.sub_modifier)
    )

  def style: Style =
    Style(fg = Some(fg), bg = Some(bg), add_modifier = modifier)
}

object Cell {
  val Empty: Cell = Cell(Grapheme.Empty, Color.Reset, Color.Reset, Modifier.EMPTY)

  def apply(str: String, style: Style): Cell =
    apply(Grapheme(str), style)

  def apply(grapheme: Grapheme, style: Style) =
    new Cell(
      grapheme,
      fg = style.fg.getOrElse(Color.Reset),
      bg = style.bg.getOrElse(Color.Reset),
      modifier = style.add_modifier.remove(style.sub_modifier)
    )
  def default: Cell = Cell(Grapheme.Empty, fg = Color.Reset, bg = Color.Reset, modifier = Modifier.EMPTY)
}
