package tui

/// A buffer cell
case class Cell(symbol: Grapheme, fg: Color, bg: Color, modifier: Modifier) {
  def withSymbol(symbol: String): Cell =
    withSymbol(Grapheme(symbol))

  def withSymbol(symbol: Grapheme): Cell =
    copy(symbol = symbol)

  def withSymbol(ch: Char): Cell =
    copy(symbol = Grapheme(ch.toString))

  def withFg(color: Color): Cell =
    copy(fg = color)

  def withBg(color: Color): Cell =
    copy(bg = color)

  def withStyle(style: Style): Cell =
    Cell(
      symbol = symbol,
      fg = style.fg.getOrElse(fg),
      bg = style.bg.getOrElse(bg),
      modifier = modifier.insert(style.add_modifier).remove(style.sub_modifier)
    )
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
}
