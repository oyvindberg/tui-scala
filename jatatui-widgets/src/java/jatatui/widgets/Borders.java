package jatatui.widgets;

/// Bitflags that can be composed to set the visible borders on the
// [Block](jatatui.widgets.block.Block) widget.
///
/// Mirrors the Rust `bitflags!`-generated `Borders` struct in `ratatui_widgets::borders`. The
/// underlying representation is an `int` carrying the OR of the individual flag bits.
///
/// Hard rule: there is no implicit default — use [#NONE] (or [#ALL] for "all sides").
public record Borders(int bits) {

  // ---- Bit constants ----

  /// Show no border (default).
  public static final Borders NONE = new Borders(0);

  /// Show the top border.
  public static final Borders TOP = new Borders(0b0001);

  /// Show the right border.
  public static final Borders RIGHT = new Borders(0b0010);

  /// Show the bottom border.
  public static final Borders BOTTOM = new Borders(0b0100);

  /// Show the left border.
  public static final Borders LEFT = new Borders(0b1000);

  /// Show all borders (top, right, bottom and left).
  public static final Borders ALL = new Borders(TOP.bits | RIGHT.bits | BOTTOM.bits | LEFT.bits);

  /// Returns `true` if no flags are set.
  public boolean isEmpty() {
    return bits == 0;
  }

  /// Returns `true` if all flags are set.
  public boolean isAll() {
    return bits == ALL.bits;
  }

  /// Returns `true` if all flags in `other` are contained within this set.
  ///
  /// Mirrors `bitflags::contains`. The empty set is contained in every set, matching upstream.
  public boolean contains(Borders other) {
    return (bits & other.bits) == other.bits;
  }

  /// Returns `true` if there are any flags common to this set and `other`.
  public boolean intersects(Borders other) {
    return (bits & other.bits) != 0;
  }

  /// Returns a new set with the specified flags inserted.
  public Borders insert(Borders other) {
    return new Borders(bits | other.bits);
  }

  /// Returns a new set with the specified flags removed.
  public Borders remove(Borders other) {
    return new Borders(bits & ~other.bits);
  }

  /// Alias for [#insert(Borders)] (bitwise OR).
  public Borders or(Borders other) {
    return insert(other);
  }

  /// Alias for [#remove(Borders)] (set difference).
  public Borders minus(Borders other) {
    return remove(other);
  }

  /// Returns the union of both sets (bitwise OR).
  public Borders union(Borders other) {
    return new Borders(bits | other.bits);
  }

  /// Returns the intersection of both sets (bitwise AND).
  public Borders intersection(Borders other) {
    return new Borders(bits & other.bits);
  }

  /// Returns the set difference: flags in this set that are not in `other`.
  public Borders difference(Borders other) {
    return new Borders(bits & ~other.bits);
  }

  /// Returns the borders as a `|`-separated list of named flags (e.g. `"TOP | BOTTOM"`).
  ///
  /// Mirrors the Rust `Debug` impl exactly so test expectations transfer over:
  ///
  /// - `NONE` (empty) -> `"NONE"`
  /// - `ALL` -> `"ALL"`
  /// - otherwise: pipe-separated flag names in `TOP | RIGHT | BOTTOM | LEFT` order.
  @Override
  public String toString() {
    if (isEmpty()) {
      return "NONE";
    }
    if (isAll()) {
      return "ALL";
    }
    StringBuilder sb = new StringBuilder();
    appendIfSet(sb, TOP, "TOP");
    appendIfSet(sb, RIGHT, "RIGHT");
    appendIfSet(sb, BOTTOM, "BOTTOM");
    appendIfSet(sb, LEFT, "LEFT");
    return sb.toString();
  }

  private void appendIfSet(StringBuilder sb, Borders flag, String name) {
    if ((bits & flag.bits) == flag.bits) {
      if (sb.length() > 0) sb.append(" | ");
      sb.append(name);
    }
  }
}
