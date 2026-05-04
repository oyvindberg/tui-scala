package jatatui.core.symbols;

/// Marker to use when plotting data points.
public enum Marker {
  /// One point per cell in the shape of a dot (`•`).
  Dot,
  /// One point per cell in the shape of a block (`█`).
  Block,
  /// One point per cell in the shape of a bar (`▄`).
  Bar,
  /// Use the Unicode Braille Patterns block to represent data points.
  ///
  /// This is a 2x4 grid of dots, where each dot can be either on or off.
  Braille,
  /// Use the unicode block and half-block characters (`█`, `▄`, and `▀`) to represent points in
  /// a grid that is double the resolution of the terminal.
  HalfBlock,
  /// Use quadrant characters to represent data points.
  ///
  /// Quadrant characters display densely packed and regularly spaced pseudo-pixels with a 2x2
  /// resolution per character.
  Quadrant,
  /// Use sextant characters from the Unicode Symbols for Legacy Computing Supplement to represent
  /// data points (2x3 resolution per character).
  Sextant,
  /// Use octant characters from the Unicode Symbols for Legacy Computing Supplement to represent
  /// data points.
  ///
  /// Octant characters have the same 2x4 resolution as Braille characters but display densely
  /// packed and regularly spaced pseudo-pixels.
  Octant;

  /// The dot symbol (`•`).
  public static final String DOT = "•";

  /// Returns the default marker ([Marker#Dot]).
  public static Marker defaultMarker() {
    return Dot;
  }
}
