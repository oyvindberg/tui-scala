package jatatui.core.symbols;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/// Strategies and helpers for merging two box-drawing symbols into one.
///
/// See [MergeStrategy] for the supported strategies. The actual lookup tables are encoded in
/// [BorderSymbol].
public final class Merge {

  private Merge() {}

  /// A strategy for merging two symbols into one.
  public enum MergeStrategy {
    /// Replaces the previous symbol with the next one.
    Replace,
    /// Merges symbols only if an exact composite unicode character exists, otherwise replaces.
    Exact,
    /// Merges symbols using the closest match, even if no exact composite exists.
    Fuzzy;

    /// Returns the default merge strategy ([MergeStrategy#Replace]).
    public static MergeStrategy defaultStrategy() {
      return Replace;
    }

    /// Merges two symbols using this strategy.
    ///
    /// If either of the symbols are not in the [Box Drawing Unicode block][1], the behaviour is:
    ///
    /// - [Replace]: always returns `next`.
    /// - other strategies: if `prev` is a non-border symbol it takes precedence and is returned;
    ///   if `next` is a non-border symbol it is returned.
    ///
    /// [1]: https://en.wikipedia.org/wiki/Box_Drawing
    public String merge(String prev, String next) {
      if (this == Replace) {
        return next;
      }
      Optional<BorderSymbol> p = BorderSymbol.fromString(prev);
      Optional<BorderSymbol> n = BorderSymbol.fromString(next);
      if (p.isPresent() && n.isPresent()) {
        BorderSymbol merged = p.get().merge(n.get(), this);
        return merged.toSymbol().orElse(next);
      }
      // Non-border symbols take precedence in strategies other than Replace.
      if (p.isEmpty() && n.isPresent()) {
        return prev;
      }
      // (n.isEmpty()) — return next regardless of prev.
      return next;
    }
  }

  /// A visual style defining the appearance of a single line making up a block border.
  enum LineStyle {
    Nothing,
    Plain,
    Rounded,
    Double,
    Thick,
    DoubleDash,
    DoubleDashThick,
    TripleDash,
    TripleDashThick,
    QuadrupleDash,
    QuadrupleDashThick;

    /// Merges two line styles. `Nothing` on the right is ignored; otherwise the right wins.
    LineStyle merge(LineStyle other) {
      return other == Nothing ? this : other;
    }
  }

  /// Represents a composite border symbol using individual line components for each direction.
  record BorderSymbol(LineStyle right, LineStyle up, LineStyle left, LineStyle down) {

    /// Parse a single-character symbol into its component line styles. Returns empty if the symbol
    /// is not a known box-drawing character.
    static Optional<BorderSymbol> fromString(String s) {
      return Optional.ofNullable(SYMBOL_TO_BORDER.get(s));
    }

    /// Returns the unicode symbol matching this combination of line styles, if one exists.
    Optional<String> toSymbol() {
      return Optional.ofNullable(BORDER_TO_SYMBOL.get(this));
    }

    /// Returns true if all sides match `style`.
    boolean contains(LineStyle style) {
      return up == style || right == style || down == style || left == style;
    }

    /// Returns true only if the symbol is a straight line (horizontal or vertical), and both
    /// halves have the same line style.
    boolean isStraight() {
      return (up == down && left == right)
          && (up == LineStyle.Nothing || left == LineStyle.Nothing);
    }

    /// Returns true only if the symbol is a corner and both arms have the same line style.
    boolean isCorner() {
      LineStyle u = up, r = right, d = down, l = left;
      LineStyle N = LineStyle.Nothing;
      if (d == N && l == N) {
        return u == r;
      }
      if (u == N && l == N) {
        return r == d;
      }
      if (u == N && r == N) {
        return d == l;
      }
      if (r == N && d == N) {
        return u == l;
      }
      return false;
    }

    /// Returns a new [BorderSymbol] with all sides equal to `from` replaced by `to`.
    BorderSymbol replace(LineStyle from, LineStyle to) {
      return new BorderSymbol(
          right == from ? to : right,
          up == from ? to : up,
          left == from ? to : left,
          down == from ? to : down);
    }

    /// Find the closest representable [BorderSymbol] using the rules of [MergeStrategy#Fuzzy].
    BorderSymbol fuzzy(BorderSymbol other) {
      BorderSymbol self = this;

      // Dashes only include vertical and horizontal lines.
      if (!self.isStraight()) {
        self =
            self.replace(LineStyle.DoubleDash, LineStyle.Plain)
                .replace(LineStyle.TripleDash, LineStyle.Plain)
                .replace(LineStyle.QuadrupleDash, LineStyle.Plain)
                .replace(LineStyle.DoubleDashThick, LineStyle.Thick)
                .replace(LineStyle.TripleDashThick, LineStyle.Thick)
                .replace(LineStyle.QuadrupleDashThick, LineStyle.Thick);
      }

      // Rounded has only corner variants.
      if (!self.isCorner()) {
        self = self.replace(LineStyle.Rounded, LineStyle.Plain);
      }

      // There are no Double + Thick variants.
      if (self.contains(LineStyle.Double) && self.contains(LineStyle.Thick)) {
        if (other.contains(LineStyle.Double)) {
          self = self.replace(LineStyle.Thick, LineStyle.Double);
        } else {
          self = self.replace(LineStyle.Double, LineStyle.Thick);
        }
      }

      // Some Plain + Double variants don't exist.
      if (self.toSymbol().isEmpty()) {
        if (other.contains(LineStyle.Double)) {
          self = self.replace(LineStyle.Plain, LineStyle.Double);
        } else {
          self = self.replace(LineStyle.Double, LineStyle.Plain);
        }
      }
      return self;
    }

    /// Merges two border symbols into one using the given strategy.
    BorderSymbol merge(BorderSymbol other, MergeStrategy strategy) {
      BorderSymbol exact =
          new BorderSymbol(
              right.merge(other.right),
              up.merge(other.up),
              left.merge(other.left),
              down.merge(other.down));
      return switch (strategy) {
        case Replace -> other;
        case Fuzzy -> exact.fuzzy(other);
        case Exact -> exact;
      };
    }
  }

  // ---- Symbol tables ----------------------------------------------------------------------------

  private static final Map<String, BorderSymbol> SYMBOL_TO_BORDER = new HashMap<>();
  private static final Map<BorderSymbol, String> BORDER_TO_SYMBOL = new HashMap<>();

  private static void define(
      String symbol, LineStyle right, LineStyle up, LineStyle left, LineStyle down) {
    BorderSymbol bs = new BorderSymbol(right, up, left, down);
    SYMBOL_TO_BORDER.put(symbol, bs);
    BORDER_TO_SYMBOL.put(bs, symbol);
  }

  static {
    LineStyle Nothing = LineStyle.Nothing;
    LineStyle Plain = LineStyle.Plain;
    LineStyle Rounded = LineStyle.Rounded;
    LineStyle Double_ = LineStyle.Double;
    LineStyle Thick = LineStyle.Thick;
    LineStyle DoubleDash = LineStyle.DoubleDash;
    LineStyle DoubleDashThick = LineStyle.DoubleDashThick;
    LineStyle TripleDash = LineStyle.TripleDash;
    LineStyle TripleDashThick = LineStyle.TripleDashThick;
    LineStyle QuadrupleDash = LineStyle.QuadrupleDash;
    LineStyle QuadrupleDashThick = LineStyle.QuadrupleDashThick;

    define("─", Plain, Nothing, Plain, Nothing);
    define("━", Thick, Nothing, Thick, Nothing);
    define("│", Nothing, Plain, Nothing, Plain);
    define("┃", Nothing, Thick, Nothing, Thick);
    define("┄", TripleDash, Nothing, TripleDash, Nothing);
    define("┅", TripleDashThick, Nothing, TripleDashThick, Nothing);
    define("┆", Nothing, TripleDash, Nothing, TripleDash);
    define("┇", Nothing, TripleDashThick, Nothing, TripleDashThick);
    define("┈", QuadrupleDash, Nothing, QuadrupleDash, Nothing);
    define("┉", QuadrupleDashThick, Nothing, QuadrupleDashThick, Nothing);
    define("┊", Nothing, QuadrupleDash, Nothing, QuadrupleDash);
    define("┋", Nothing, QuadrupleDashThick, Nothing, QuadrupleDashThick);
    define("┌", Plain, Nothing, Nothing, Plain);
    define("┍", Thick, Nothing, Nothing, Plain);
    define("┎", Plain, Nothing, Nothing, Thick);
    define("┏", Thick, Nothing, Nothing, Thick);
    define("┐", Nothing, Nothing, Plain, Plain);
    define("┑", Nothing, Nothing, Thick, Plain);
    define("┒", Nothing, Nothing, Plain, Thick);
    define("┓", Nothing, Nothing, Thick, Thick);
    define("└", Plain, Plain, Nothing, Nothing);
    define("┕", Thick, Plain, Nothing, Nothing);
    define("┖", Plain, Thick, Nothing, Nothing);
    define("┗", Thick, Thick, Nothing, Nothing);
    define("┘", Nothing, Plain, Plain, Nothing);
    define("┙", Nothing, Plain, Thick, Nothing);
    define("┚", Nothing, Thick, Plain, Nothing);
    define("┛", Nothing, Thick, Thick, Nothing);
    define("├", Plain, Plain, Nothing, Plain);
    define("┝", Thick, Plain, Nothing, Plain);
    define("┞", Plain, Thick, Nothing, Plain);
    define("┟", Plain, Plain, Nothing, Thick);
    define("┠", Plain, Thick, Nothing, Thick);
    define("┡", Thick, Thick, Nothing, Plain);
    define("┢", Thick, Plain, Nothing, Thick);
    define("┣", Thick, Thick, Nothing, Thick);
    define("┤", Nothing, Plain, Plain, Plain);
    define("┥", Nothing, Plain, Thick, Plain);
    define("┦", Nothing, Thick, Plain, Plain);
    define("┧", Nothing, Plain, Plain, Thick);
    define("┨", Nothing, Thick, Plain, Thick);
    define("┩", Nothing, Thick, Thick, Plain);
    define("┪", Nothing, Plain, Thick, Thick);
    define("┫", Nothing, Thick, Thick, Thick);
    define("┬", Plain, Nothing, Plain, Plain);
    define("┭", Plain, Nothing, Thick, Plain);
    define("┮", Thick, Nothing, Plain, Plain);
    define("┯", Thick, Nothing, Thick, Plain);
    define("┰", Plain, Nothing, Plain, Thick);
    define("┱", Plain, Nothing, Thick, Thick);
    define("┲", Thick, Nothing, Plain, Thick);
    define("┳", Thick, Nothing, Thick, Thick);
    define("┴", Plain, Plain, Plain, Nothing);
    define("┵", Plain, Plain, Thick, Nothing);
    define("┶", Thick, Plain, Plain, Nothing);
    define("┷", Thick, Plain, Thick, Nothing);
    define("┸", Plain, Thick, Plain, Nothing);
    define("┹", Plain, Thick, Thick, Nothing);
    define("┺", Thick, Thick, Plain, Nothing);
    define("┻", Thick, Thick, Thick, Nothing);
    define("┼", Plain, Plain, Plain, Plain);
    define("┽", Plain, Plain, Thick, Plain);
    define("┾", Thick, Plain, Plain, Plain);
    define("┿", Thick, Plain, Thick, Plain);
    define("╀", Plain, Thick, Plain, Plain);
    define("╁", Plain, Plain, Plain, Thick);
    define("╂", Plain, Thick, Plain, Thick);
    define("╃", Plain, Thick, Thick, Plain);
    define("╄", Thick, Thick, Plain, Plain);
    define("╅", Plain, Plain, Thick, Thick);
    define("╆", Thick, Plain, Plain, Thick);
    define("╇", Thick, Thick, Thick, Plain);
    define("╈", Thick, Plain, Thick, Thick);
    define("╉", Plain, Thick, Thick, Thick);
    define("╊", Thick, Thick, Plain, Thick);
    define("╋", Thick, Thick, Thick, Thick);
    define("╌", DoubleDash, Nothing, DoubleDash, Nothing);
    define("╍", DoubleDashThick, Nothing, DoubleDashThick, Nothing);
    define("╎", Nothing, DoubleDash, Nothing, DoubleDash);
    define("╏", Nothing, DoubleDashThick, Nothing, DoubleDashThick);
    define("═", Double_, Nothing, Double_, Nothing);
    define("║", Nothing, Double_, Nothing, Double_);
    define("╒", Double_, Nothing, Nothing, Plain);
    define("╓", Plain, Nothing, Nothing, Double_);
    define("╔", Double_, Nothing, Nothing, Double_);
    define("╕", Nothing, Nothing, Double_, Plain);
    define("╖", Nothing, Nothing, Plain, Double_);
    define("╗", Nothing, Nothing, Double_, Double_);
    define("╘", Double_, Plain, Nothing, Nothing);
    define("╙", Plain, Double_, Nothing, Nothing);
    define("╚", Double_, Double_, Nothing, Nothing);
    define("╛", Nothing, Plain, Double_, Nothing);
    define("╜", Nothing, Double_, Plain, Nothing);
    define("╝", Nothing, Double_, Double_, Nothing);
    define("╞", Double_, Plain, Nothing, Plain);
    define("╟", Plain, Double_, Nothing, Double_);
    define("╠", Double_, Double_, Nothing, Double_);
    define("╡", Nothing, Plain, Double_, Plain);
    define("╢", Nothing, Double_, Plain, Double_);
    define("╣", Nothing, Double_, Double_, Double_);
    define("╤", Double_, Nothing, Double_, Plain);
    define("╥", Plain, Nothing, Plain, Double_);
    define("╦", Double_, Nothing, Double_, Double_);
    define("╧", Double_, Plain, Double_, Nothing);
    define("╨", Plain, Double_, Plain, Nothing);
    define("╩", Double_, Double_, Double_, Nothing);
    define("╪", Double_, Plain, Double_, Plain);
    define("╫", Plain, Double_, Plain, Double_);
    define("╬", Double_, Double_, Double_, Double_);
    define("╭", Rounded, Nothing, Nothing, Rounded);
    define("╮", Nothing, Nothing, Rounded, Rounded);
    define("╯", Nothing, Rounded, Rounded, Nothing);
    define("╰", Rounded, Rounded, Nothing, Nothing);
    define("╴", Nothing, Nothing, Plain, Nothing);
    define("╵", Nothing, Plain, Nothing, Nothing);
    define("╶", Plain, Nothing, Nothing, Nothing);
    define("╷", Nothing, Nothing, Nothing, Plain);
    define("╸", Nothing, Nothing, Thick, Nothing);
    define("╹", Nothing, Thick, Nothing, Nothing);
    define("╺", Thick, Nothing, Nothing, Nothing);
    define("╻", Nothing, Nothing, Nothing, Thick);
    define("╼", Thick, Nothing, Plain, Nothing);
    define("╽", Nothing, Plain, Nothing, Thick);
    define("╾", Plain, Nothing, Thick, Nothing);
    define("╿", Nothing, Thick, Nothing, Plain);
  }
}
