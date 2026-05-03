package tui;

/// Modifier changes the way a piece of text is displayed.
///
/// They are bitflags so they can easily be composed.
public record Modifier(int bits) {
  public void fmt(StringBuilder sb) {
    boolean first = true;
    if (Modifier.BOLD.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("BOLD");
    }
    if (Modifier.DIM.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("DIM");
    }
    if (Modifier.ITALIC.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("ITALIC");
    }
    if (Modifier.UNDERLINED.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("UNDERLINED");
    }
    if (Modifier.SLOW_BLINK.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("SLOW_BLINK");
    }
    if (Modifier.RAPID_BLINK.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("RAPID_BLINK");
    }
    if (Modifier.REVERSED.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("REVERSED");
    }
    if (Modifier.HIDDEN.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("HIDDEN");
    }
    if (Modifier.CROSSED_OUT.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("CROSSED_OUT");
    }
    if (first) {
      sb.append("(empty)");
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    fmt(sb);
    return sb.toString();
  }

  /// Returns `true` if all of the flags in `other` are contained within `self`.
  public boolean contains(Modifier other) {
    return !other.equals(Modifier.EMPTY) && (bits & other.bits) == other.bits;
  }

  /// Inserts the specified flags in-place.
  public Modifier insert(Modifier other) {
    return new Modifier(bits | other.bits);
  }

  /// Removes the specified flags in-place.
  public Modifier remove(Modifier other) {
    return new Modifier(bits & ~other.bits);
  }

  public Modifier or(Modifier mod) {
    return insert(mod);
  }

  public Modifier minus(Modifier mod) {
    return remove(mod);
  }

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
  public static final Modifier ALL = new Modifier(Integer.parseInt("000111111111", 2));
}
