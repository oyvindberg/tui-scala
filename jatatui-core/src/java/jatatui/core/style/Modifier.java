package jatatui.core.style;

/// Modifier changes the way a piece of text is displayed.
///
/// They are bitflags so they can easily be composed. Mirrors the Rust `bitflags!` macro.
public record Modifier(int bits) {

  // ---- Bit constants ----

  public static final Modifier BOLD = new Modifier(1 << 0);
  public static final Modifier DIM = new Modifier(1 << 1);
  public static final Modifier ITALIC = new Modifier(1 << 2);
  public static final Modifier UNDERLINED = new Modifier(1 << 3);
  public static final Modifier SLOW_BLINK = new Modifier(1 << 4);
  public static final Modifier RAPID_BLINK = new Modifier(1 << 5);
  public static final Modifier REVERSED = new Modifier(1 << 6);
  public static final Modifier HIDDEN = new Modifier(1 << 7);
  public static final Modifier CROSSED_OUT = new Modifier(1 << 8);

  /// Returns an empty set of flags.
  public static final Modifier EMPTY = new Modifier(0);

  /// Returns the set containing all flags.
  public static final Modifier ALL =
      new Modifier(
          BOLD.bits
              | DIM.bits
              | ITALIC.bits
              | UNDERLINED.bits
              | SLOW_BLINK.bits
              | RAPID_BLINK.bits
              | REVERSED.bits
              | HIDDEN.bits
              | CROSSED_OUT.bits);

  /// Returns true if no flags are set.
  public boolean isEmpty() {
    return bits == 0;
  }

  /// Returns true if all of the flags in `other` are contained within this set.
  ///
  /// Mirrors `bitflags::contains`. Empty `other` returns true (the empty set is contained in any
  /// set), matching the Rust behavior.
  public boolean contains(Modifier other) {
    return (bits & other.bits) == other.bits;
  }

  /// Returns true if there are flags common to both this set and `other`.
  public boolean intersects(Modifier other) {
    return (bits & other.bits) != 0;
  }

  /// Returns a new set with the specified flags inserted.
  public Modifier insert(Modifier other) {
    return new Modifier(bits | other.bits);
  }

  /// Returns a new set with the specified flags removed.
  public Modifier remove(Modifier other) {
    return new Modifier(bits & ~other.bits);
  }

  /// Alias for [#insert(Modifier)] (bitwise OR).
  public Modifier or(Modifier other) {
    return insert(other);
  }

  /// Alias for [#remove(Modifier)] (set difference).
  public Modifier minus(Modifier other) {
    return remove(other);
  }

  /// Returns the set difference: flags in this set that are not in `other`.
  ///
  /// Mirrors `bitflags::difference`.
  public Modifier difference(Modifier other) {
    return new Modifier(bits & ~other.bits);
  }

  /// Returns the union of both sets (bitwise OR).
  public Modifier union(Modifier other) {
    return new Modifier(bits | other.bits);
  }

  /// Returns the intersection of both sets (bitwise AND).
  public Modifier intersection(Modifier other) {
    return new Modifier(bits & other.bits);
  }

  /// Returns the modifier as a `|`-separated list of named flags, or `NONE` if empty.
  ///
  /// Mirrors the Rust `Debug` impl exactly so that test expectations transfer over.
  @Override
  public String toString() {
    if (isEmpty()) {
      return "NONE";
    }
    StringBuilder sb = new StringBuilder();
    appendIfSet(sb, BOLD, "BOLD");
    appendIfSet(sb, DIM, "DIM");
    appendIfSet(sb, ITALIC, "ITALIC");
    appendIfSet(sb, UNDERLINED, "UNDERLINED");
    appendIfSet(sb, SLOW_BLINK, "SLOW_BLINK");
    appendIfSet(sb, RAPID_BLINK, "RAPID_BLINK");
    appendIfSet(sb, REVERSED, "REVERSED");
    appendIfSet(sb, HIDDEN, "HIDDEN");
    appendIfSet(sb, CROSSED_OUT, "CROSSED_OUT");
    return sb.toString();
  }

  private void appendIfSet(StringBuilder sb, Modifier flag, String name) {
    if (contains(flag)) {
      if (sb.length() > 0) {
        sb.append(" | ");
      }
      sb.append(name);
    }
  }
}
