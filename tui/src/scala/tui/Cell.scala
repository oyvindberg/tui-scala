package tui

/** A buffer cell
  */
case class Cell(
    var symbol: tui.Grapheme,
    var fg: Color,
    var bg: Color,
    var modifier: Modifier
) {
  override def clone(): Cell =
    new Cell(symbol, fg, bg, modifier)

  def setSymbol(symbol: String): this.type =
    setSymbol(Grapheme(symbol))

  def setSymbol(symbol: tui.Grapheme): this.type = {
    this.symbol = symbol
    this
  }

  def setChar(ch: Char): this.type = {
    this.symbol = Grapheme(ch.toString)
    this
  }

  def setFg(color: Color): this.type = {
    this.fg = color
    this
  }

  def setBg(color: Color): this.type = {
    this.bg = color
    this
  }

  def setStyle(style: Style): this.type = {
    style.fg.foreach(fg = _)
    style.bg.foreach(bg = _)
    modifier = modifier.insert(style.addModifier).remove(style.subModifier)
    this
  }

  def style: Style =
    Style(fg = Some(fg), bg = Some(bg), addModifier = modifier)

  def reset(): Unit = {
    this.symbol = Grapheme.Empty
    this.fg = Color.Reset
    this.bg = Color.Reset
    this.modifier = Modifier.EMPTY
  }
}

object Cell {
  def default: Cell = Cell(Grapheme.Empty, fg = Color.Reset, bg = Color.Reset, modifier = Modifier.EMPTY)
}
