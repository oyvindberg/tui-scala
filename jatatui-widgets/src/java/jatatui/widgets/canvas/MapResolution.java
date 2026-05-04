package jatatui.widgets.canvas;

/// Defines how many points are going to be used to draw a [Map].
///
/// You generally want a [#High] resolution map.
///
/// Mirrors `ratatui_widgets::canvas::MapResolution` (v0.30).
public enum MapResolution {

  /// A lesser resolution for the [Map] [Shape]. Contains about 1000 points.
  Low,
  /// A higher resolution for the [Map] [Shape]. Contains about 5000 points; you likely want to
  /// use `Marker.Braille` with this.
  High;

  /// Returns the default [MapResolution] ([#Low]).
  public static MapResolution defaultResolution() {
    return Low;
  }

  /// Underlying point data for this resolution.
  public Coord[] data() {
    return switch (this) {
      case Low -> WorldMap.low();
      case High -> WorldMap.high();
    };
  }
}
