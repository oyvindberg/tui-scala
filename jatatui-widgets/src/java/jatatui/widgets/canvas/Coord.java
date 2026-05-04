package jatatui.widgets.canvas;

/// A 2D coordinate pair on a canvas. Replaces upstream's `(f64, f64)` tuple per the project's
/// "tuples get dedicated record types" rule.
public record Coord(double x, double y) {}
