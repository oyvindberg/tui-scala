package jatatui.widgets.canvas;

import jatatui.core.text.Line;

/// Label to draw some text on the canvas.
///
/// Mirrors `ratatui_widgets::canvas::Label` (v0.30).
public record Label(double x, double y, Line line) {}
