package jatatui.core.layout;

/// Defines how excess space is distributed between layout segments.
public enum Flex {
  /// Default — fills available space, putting excess into the last constraint of the lowest
  // priority.
  Legacy,
  /// Push all segments to the start; excess at the end.
  Start,
  /// Push all segments to the end; excess at the start.
  End,
  /// Center all segments; excess split between start and end.
  Center,
  /// Equal space between segments; no space at start/end.
  SpaceBetween,
  /// Equal space between, before, and after each segment.
  SpaceEvenly,
  /// Equal space around each segment (half-width gap at start/end).
  SpaceAround
}
