package jatatui.core.layout;

/// Defines the direction of a layout.
///
/// - `Horizontal`: Layout segments are arranged side by side (left to right)
/// - `Vertical`: Layout segments are arranged top to bottom (default)
public enum Direction {
  Horizontal,
  Vertical;

  /// The perpendicular direction to this direction.
  public Direction perpendicular() {
    return switch (this) {
      case Horizontal -> Vertical;
      case Vertical -> Horizontal;
    };
  }
}
