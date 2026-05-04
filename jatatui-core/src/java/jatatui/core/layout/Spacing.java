package jatatui.core.layout;

/// Represents the spacing between segments in a layout.
///
/// `Spacing` defines the spacing between segments in a layout. It can be either positive spacing
/// (`Space`) — space between segments — or negative spacing (`Overlap`) — overlap between
/// segments.
///
/// The default value is `Space(0)`, which means no spacing or no overlap between segments.
public sealed interface Spacing permits Spacing.Space, Spacing.Overlap {

  /// Positive spacing between segments. The value is the number of cells.
  record Space(int cells) implements Spacing {}

  /// Negative spacing — segments overlap by this many cells.
  record Overlap(int cells) implements Spacing {}

  /// The default — no spacing.
  Spacing DEFAULT = new Space(0);

  /// Build a `Spacing` from a non-negative `int` (number of cells of space).
  static Spacing space(int cells) {
    return new Space(cells);
  }

  /// Build a `Spacing` from a non-negative `int` (number of cells of overlap).
  static Spacing overlap(int cells) {
    return new Overlap(cells);
  }

  /// Build a `Spacing` from a signed value (`i16` semantics): negative means overlap, non-negative
  /// means space. The value is clamped to `[i16::MIN, i16::MAX]` first (matching the upstream
  /// `From<i32>` behavior).
  static Spacing fromSigned(int v) {
    int clamped = Math.max(Math.min(v, Short.MAX_VALUE), Short.MIN_VALUE);
    if (clamped < 0) {
      // unsigned absolute value of a 16-bit signed integer
      return new Overlap(-clamped);
    }
    return new Space(clamped);
  }

  /// Number of cells (positive for `Space`, positive for `Overlap`).
  default int cells() {
    return switch (this) {
      case Space s -> s.cells();
      case Overlap o -> o.cells();
    };
  }

  /// Returns the signed `i16`-equivalent value: positive for `Space`, negative for `Overlap`.
  default int toSigned() {
    return switch (this) {
      case Space s -> s.cells();
      case Overlap o -> -o.cells();
    };
  }
}
