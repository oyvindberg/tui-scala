package jatatui.widgets.canvas;

import java.util.List;

/// A single layer of the canvas.
///
/// This allows the canvas to be drawn in multiple layers. This is useful if you want to draw
/// multiple shapes on the canvas in specific order.
///
/// Mirrors the upstream `Layer` struct.
public record Layer(List<LayerCell> contents) {}
