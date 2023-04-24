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

  def set_symbol(symbol: String): this.type =
    set_symbol(Grapheme(symbol))

  def set_symbol(symbol: tui.Grapheme): this.type = {
    this.symbol = symbol
    this
  }

  def set_char(ch: Char): this.type = {
    this.symbol = Grapheme(ch.toString)
    this
  }

  def set_fg(color: Color): this.type = {
    this.fg = color
    this
  }

  def set_bg(color: Color): this.type = {
    this.bg = color
    this
  }

  def set_style(style: Style): this.type = {
    style.fg.foreach(fg = _)
    style.bg.foreach(bg = _)
    modifier = modifier.insert(style.add_modifier).remove(style.sub_modifier)
    this
  }

  def style: Style =
    Style(fg = Some(fg), bg = Some(bg), add_modifier = modifier)

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
