package jatatui.widgets.block;

/// Defines the padding for a [Block].
///
/// See [Block#withPadding(Padding)] to configure a block's padding.
///
/// This concept is similar to CSS padding.
///
/// Note: terminal cells are usually taller than they are wide, so to make horizontal and vertical
/// padding seem equal, doubling the horizontal padding is usually a good fit.
public record Padding(int left, int right, int top, int bottom) {

  /// Padding with all fields set to `0`.
  public static final Padding ZERO = new Padding(0, 0, 0, 0);

  /// Returns a `Padding` with all fields set to `0` (alias for [#ZERO]).
  public static Padding zero() {
    return ZERO;
  }

  /// Creates a `Padding` with the same value for `left` and `right`.
  public static Padding horizontal(int value) {
    return new Padding(value, value, 0, 0);
  }

  /// Creates a `Padding` with the same value for `top` and `bottom`.
  public static Padding vertical(int value) {
    return new Padding(0, 0, value, value);
  }

  /// Creates a `Padding` with the same value for all four fields.
  public static Padding uniform(int value) {
    return new Padding(value, value, value, value);
  }

  /// Creates a `Padding` that is visually proportional to the terminal.
  ///
  /// Sets `left`/`right` to `2 * value` and `top`/`bottom` to `value`.
  public static Padding proportional(int value) {
    return new Padding(2 * value, 2 * value, value, value);
  }

  /// Creates a symmetric `Padding`: `x` for left/right, `y` for top/bottom.
  public static Padding symmetric(int x, int y) {
    return new Padding(x, x, y, y);
  }

  /// Creates a `Padding` that only sets the `left` padding.
  public static Padding left(int value) {
    return new Padding(value, 0, 0, 0);
  }

  /// Creates a `Padding` that only sets the `right` padding.
  public static Padding right(int value) {
    return new Padding(0, value, 0, 0);
  }

  /// Creates a `Padding` that only sets the `top` padding.
  public static Padding top(int value) {
    return new Padding(0, 0, value, 0);
  }

  /// Creates a `Padding` that only sets the `bottom` padding.
  public static Padding bottom(int value) {
    return new Padding(0, 0, 0, value);
  }
}
