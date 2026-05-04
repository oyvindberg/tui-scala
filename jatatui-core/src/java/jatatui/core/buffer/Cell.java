package jatatui.core.buffer;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Merge;
import java.util.Objects;
import java.util.Optional;

/// A buffer cell.
///
/// Mirrors the upstream Rust `ratatui_core::buffer::Cell` (v0.30). The cell is **mutable**
/// because the buffer mutates cells in place during render.
///
/// ## Symbol storage
///
/// Upstream stores the symbol as `Option<CompactString>` where `None` is treated as a single
/// space when read via [#symbol()]. We mirror that exactly: the field is an `Optional<String>`
/// and [#symbol()] returns `" "` for the empty state. This keeps equality semantics aligned with
/// upstream (cells with `None` and cells with `Some(" ")` compare equal).
///
/// ## Colors
///
/// `fg`, `bg`, and `underlineColor` are required fields defaulting to [Color#RESET]. This matches
/// upstream where the same fields are typed `Color` (not `Option<Color>`) — `Color::Reset` is the
/// sentinel that means "no override".
public final class Cell {

  /// The string to be drawn in the cell.
  ///
  /// Accepts unicode grapheme clusters which might take up more than one cell. `Optional.empty()`
  /// is the default and equivalent to a single space when read.
  private Optional<String> symbol;

  /// The foreground color of the cell.
  public Color fg;

  /// The background color of the cell.
  public Color bg;

  /// The underline color of the cell.
  public Color underlineColor;

  /// The modifier of the cell.
  public Modifier modifier;

  /// Whether the cell should be skipped when copying (diffing) the buffer to the screen.
  public boolean skip;

  private Cell(
      Optional<String> symbol,
      Color fg,
      Color bg,
      Color underlineColor,
      Modifier modifier,
      boolean skip) {
    this.symbol = symbol;
    this.fg = fg;
    this.bg = bg;
    this.underlineColor = underlineColor;
    this.modifier = modifier;
    this.skip = skip;
  }

  /// Returns a new empty cell.
  ///
  /// This is the equivalent of upstream's `Cell::EMPTY` constant. Java has no `const` so each
  /// call returns a fresh mutable instance — callers that mutate must not hold a shared one.
  public static Cell empty() {
    return new Cell(Optional.empty(), Color.RESET, Color.RESET, Color.RESET, Modifier.EMPTY, false);
  }

  /// Creates a new `Cell` with the given symbol.
  ///
  /// Mirrors upstream `Cell::new(symbol)`.
  public static Cell of(String symbol) {
    Cell c = empty();
    c.symbol = Optional.of(symbol);
    return c;
  }

  /// Creates a `Cell` from a single character (mirrors `From<char> for Cell`).
  public static Cell ofChar(char ch) {
    Cell c = empty();
    c.setChar(ch);
    return c;
  }

  /// Creates a `Cell` from a single code point.
  public static Cell ofCodePoint(int codePoint) {
    Cell c = empty();
    c.symbol = Optional.of(new String(Character.toChars(codePoint)));
    return c;
  }

  /// Returns a copy of this cell. The buffer's `filled`/`merge` operations clone cells so they
  /// can be mutated independently.
  public Cell copy() {
    return new Cell(symbol, fg, bg, underlineColor, modifier, skip);
  }

  /// Gets the symbol of the cell. If the cell has no symbol, returns a single space.
  public String symbol() {
    return symbol.orElse(" ");
  }

  /// Returns the raw `Optional<String>` symbol — exposed so the buffer's debug formatter can tell
  /// "no symbol set" apart from "explicit space". Most callers should use [#symbol()].
  public Optional<String> rawSymbol() {
    return symbol;
  }

  /// Merges the symbol of the cell with the one already on the cell, using the provided strategy.
  ///
  /// If the cell has no symbol set, sets the symbol to the provided one rather than merging.
  public Cell mergeSymbol(String symbol, Merge.MergeStrategy strategy) {
    String merged = this.symbol.map(s -> strategy.merge(s, symbol)).orElse(symbol);
    this.symbol = Optional.of(merged);
    return this;
  }

  /// Sets the symbol of the cell.
  public Cell setSymbol(String symbol) {
    this.symbol = Optional.of(symbol);
    return this;
  }

  /// Appends a symbol to the cell.
  ///
  /// Particularly useful for adding zero-width characters to the cell. Mirrors the
  /// `pub(crate) append_symbol` upstream — kept package-private here so other packages don't
  /// touch it accidentally.
  Cell appendSymbol(String symbol) {
    String existing = this.symbol.orElse("");
    this.symbol = Optional.of(existing + symbol);
    return this;
  }

  /// Sets the symbol of the cell to a single character.
  public Cell setChar(char ch) {
    this.symbol = Optional.of(String.valueOf(ch));
    return this;
  }

  /// Sets the foreground color of the cell.
  public Cell setFg(Color color) {
    this.fg = color;
    return this;
  }

  /// Sets the background color of the cell.
  public Cell setBg(Color color) {
    this.bg = color;
    return this;
  }

  /// Sets the underline color of the cell.
  public Cell setUnderlineColor(Color color) {
    this.underlineColor = color;
    return this;
  }

  /// Sets the style of the cell.
  ///
  /// Applies fg/bg/underlineColor when the style has them set, and merges modifiers (add then
  /// remove). Mirrors upstream `Cell::set_style`.
  public Cell setStyle(Style style) {
    style.fg().ifPresent(c -> this.fg = c);
    style.bg().ifPresent(c -> this.bg = c);
    style.underlineColor().ifPresent(c -> this.underlineColor = c);
    modifier = modifier.insert(style.addModifier()).remove(style.subModifier());
    return this;
  }

  /// Returns the style of the cell.
  public Style style() {
    return new Style(
        Optional.of(fg),
        Optional.of(bg),
        Optional.of(underlineColor),
        modifier,
        Modifier.EMPTY);
  }

  /// Sets the cell to be skipped when copying (diffing) the buffer to the screen.
  ///
  /// Helpful to prevent the buffer from overwriting a cell covered by an image from a terminal
  /// graphics protocol (Sixel / iTerm / Kitty / ...).
  public Cell setSkip(boolean skip) {
    this.skip = skip;
    return this;
  }

  /// Resets the cell to the empty state.
  public void reset() {
    this.symbol = Optional.empty();
    this.fg = Color.RESET;
    this.bg = Color.RESET;
    this.underlineColor = Color.RESET;
    this.modifier = Modifier.EMPTY;
    this.skip = false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Cell other)) return false;
    // Treat None and Some(" ") as equal — see upstream Cell::eq doc.
    return symbol().equals(other.symbol())
        && fg.equals(other.fg)
        && bg.equals(other.bg)
        && underlineColor.equals(other.underlineColor)
        && modifier.equals(other.modifier)
        && skip == other.skip;
  }

  @Override
  public int hashCode() {
    // Hash via symbol() not the optional so None and Some(" ") collide, matching upstream.
    return Objects.hash(symbol(), fg, bg, underlineColor, modifier, skip);
  }

  @Override
  public String toString() {
    return "Cell { symbol: "
        + symbol().replace("\\", "\\\\").replace("\"", "\\\"")
        + ", fg: "
        + fg
        + ", bg: "
        + bg
        + ", underline_color: "
        + underlineColor
        + ", modifier: "
        + modifier
        + ", skip: "
        + skip
        + " }";
  }
}
