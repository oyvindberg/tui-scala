package jatatui.widgets.chart;

import jatatui.core.layout.solver.Either;

/// Determines which style of graphing to use for a [Dataset].
public enum GraphType {
  /// Draw each point. This is the default.
  Scatter,

  /// Draw a line between each following point.
  ///
  /// The order of the lines will be the same as the order of the points in the dataset, which
  /// allows this widget to draw lines both left-to-right and right-to-left.
  Line,

  /// Draw a bar chart. Draws a bar from the x axis to each point in the dataset.
  Bar;

  /// Returns the default graph type, [#Scatter].
  public static GraphType defaultType() {
    return Scatter;
  }

  /// Mirrors upstream `Display`. Returns the variant name as-is.
  @Override
  public String toString() {
    return name();
  }

  /// Mirrors upstream `FromStr`. Returns `Either.Left` with a descriptive message on unknown
  /// inputs (mirroring `strum::ParseError::VariantNotFound`).
  public static Either<String, GraphType> fromString(String s) {
    return switch (s) {
      case "Scatter" -> Either.right(Scatter);
      case "Line" -> Either.right(Line);
      case "Bar" -> Either.right(Bar);
      default -> Either.left("VariantNotFound");
    };
  }
}
