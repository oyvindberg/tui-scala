package tui.widgets.canvas

import tui.{symbols, Point, Spans}

import scala.collection.mutable

/// Holds the state of the Canvas when painting to it.
case class Context(
    x_bounds: Point,
    y_bounds: Point,
    grid: Grid,
    var dirty: Boolean,
    layers: mutable.ArrayBuffer[Layer],
    labels: mutable.ArrayBuffer[Label]
) {
  /// Draw any object that may implement the Shape trait
  def draw(shape: Shape): Unit = {
    this.dirty = true
    shape.draw(Painter(this))
  }

  /// Go one layer above in the canvas.
  def layer(): Unit = {
    this.layers.addOne(this.grid.save())
    this.grid.reset()
    this.dirty = false
  }

  /// Print a string on the canvas at the given position
  def print(x: Double, y: Double, spans: Spans): Unit =
    this.labels.addOne(Label(x, y, spans))

  /// Push the last layer if necessary
  def finish(): Unit =
    if (this.dirty) {
      this.layer()
    }
}

object Context {
  def apply(
      width: Int,
      height: Int,
      x_bounds: Point,
      y_bounds: Point,
      marker: symbols.Marker
  ): Context = {
    val grid: Grid = marker match {
      case symbols.Marker.Dot     => CharGrid(width, height, '•')
      case symbols.Marker.Block   => CharGrid(width, height, '▄')
      case symbols.Marker.Braille => BrailleGrid(width, height)
    }
    Context(
      x_bounds,
      y_bounds,
      grid,
      dirty = false,
      layers = mutable.ArrayBuffer.empty,
      labels = mutable.ArrayBuffer.empty
    )
  }
}
