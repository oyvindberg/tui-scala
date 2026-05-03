package tui;

/// Bitflags that can be composed to set the visible borders essentially on the block widget.
public record Borders(int bits) {
  public void fmt(StringBuilder sb) {
    boolean first = true;
    if (Borders.NONE.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("NONE");
    }
    if (Borders.TOP.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("TOP");
    }
    if (Borders.RIGHT.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("RIGHT");
    }
    if (Borders.BOTTOM.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("BOTTOM");
    }
    if (Borders.LEFT.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("LEFT");
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
  public boolean contains(Borders other) {
    return !other.equals(Borders.EMPTY) && (bits & other.bits) == other.bits;
  }

  public boolean intersects(Borders other) {
    return Borders.EMPTY.bits != (bits & other.bits);
  }

  /// Inserts the specified flags in-place.
  public Borders insert(Borders other) {
    return new Borders(bits | other.bits);
  }

  /// Removes the specified flags in-place.
  public Borders remove(Borders other) {
    return new Borders(bits & Integer.reverse(other.bits));
  }

  public Borders or(Borders other) {
    return new Borders(bits | other.bits);
  }

  public Borders minus(Borders other) {
    return remove(other);
  }

  /// Show no border (default)
  public static final Borders NONE = new Borders(1 << 0);

  /// Show the top border
  public static final Borders TOP = new Borders(1 << 1);

  /// Show the right border
  public static final Borders RIGHT = new Borders(1 << 2);

  /// Show the bottom border
  public static final Borders BOTTOM = new Borders(1 << 3);

  /// Show the left border
  public static final Borders LEFT = new Borders(1 << 4);

  /// Returns an empty set of flags.
  public static final Borders EMPTY = new Borders(0);

  /// Show all borders
  public static final Borders ALL = TOP.or(RIGHT).or(BOTTOM).or(LEFT);
}
