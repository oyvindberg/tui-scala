package jatatui.widgets.canvas;

import jatatui.core.style.Color;

/// A grid of cells that can be painted on.
///
/// The grid represents a particular screen region measured in rows and columns. The underlying
/// resolution of the grid might exceed the number of rows and columns. For example, a grid of
/// Braille patterns will have a resolution of 2x4 dots per cell. This means that a grid of 10x10
/// cells will have a resolution of 20x40 dots.
///
/// Mirrors the upstream `Grid` trait. Implementations are package-private.
interface Grid {

  /// Get the resolution of the grid in number of dots.
  ///
  /// This doesn't have to be the same as the number of rows and columns of the grid. For example,
  /// a grid of Braille patterns will have a resolution of 2x4 dots per cell. This means that a
  /// grid of 10x10 cells will have a resolution of 20x40 dots.
  Resolution resolution();

  /// Paint a point of the grid.
  ///
  /// The point is expressed in number of dots starting at the origin of the grid in the top left
  /// corner. Note that this is not the same as the `(x, y)` coordinates of the canvas.
  void paint(int x, int y, Color color);

  /// Save the current state of the [Grid] as a layer to be rendered.
  Layer save();

  /// Reset the grid to its initial state.
  void reset();

  /// Resolution of a grid, in dots.
  record Resolution(double x, double y) {}
}
