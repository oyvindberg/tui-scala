package tui.widgets.canvas

import tui.Spans

/// Label to draw some text on the canvas
case class Label(
    x: Double,
    y: Double,
    spans: Spans
)
