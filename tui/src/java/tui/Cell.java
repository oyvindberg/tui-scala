package tui;

/// A buffer cell
public final class Cell {
  public Grapheme symbol;
  public Color fg;
  public Color bg;
  public Modifier modifier;
  /// When true, the cell is left untouched when diffing the buffer to the screen.
  /// Useful so an image rendered via a terminal graphics protocol (Sixel / iTerm / Kitty)
  /// is not overwritten by subsequent draws that happen to land on its area.
  public boolean skip;

  public Cell(Grapheme symbol, Color fg, Color bg, Modifier modifier) {
    this.symbol = symbol;
    this.fg = fg;
    this.bg = bg;
    this.modifier = modifier;
    this.skip = false;
  }

  @Override
  public Cell clone() {
    Cell c = new Cell(symbol, fg, bg, modifier);
    c.skip = skip;
    return c;
  }

  public Cell setSymbol(String symbol) {
    return setSymbol(new Grapheme(symbol));
  }

  public Cell setSymbol(Grapheme symbol) {
    this.symbol = symbol;
    return this;
  }

  public Cell setChar(char ch) {
    this.symbol = new Grapheme(String.valueOf(ch));
    return this;
  }

  public Cell setFg(Color color) {
    this.fg = color;
    return this;
  }

  public Cell setBg(Color color) {
    this.bg = color;
    return this;
  }

  public Cell setStyle(Style style) {
    style.fg().ifPresent(c -> this.fg = c);
    style.bg().ifPresent(c -> this.bg = c);
    modifier = modifier.insert(style.addModifier()).remove(style.subModifier());
    return this;
  }

  public Style style() {
    return new Style(
        java.util.Optional.of(fg), java.util.Optional.of(bg), modifier, Modifier.EMPTY);
  }

  public Cell setSkip(boolean skip) {
    this.skip = skip;
    return this;
  }

  public void reset() {
    this.symbol = Grapheme.Empty;
    this.fg = Color.Reset;
    this.bg = Color.Reset;
    this.modifier = Modifier.EMPTY;
    this.skip = false;
  }

  public static Cell empty() {
    return new Cell(Grapheme.Empty, Color.Reset, Color.Reset, Modifier.EMPTY);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Cell other)) return false;
    return symbol.equals(other.symbol)
        && fg.equals(other.fg)
        && bg.equals(other.bg)
        && modifier.equals(other.modifier);
  }

  @Override
  public int hashCode() {
    int h = symbol.hashCode();
    h = 31 * h + fg.hashCode();
    h = 31 * h + bg.hashCode();
    h = 31 * h + modifier.hashCode();
    return h;
  }
}
