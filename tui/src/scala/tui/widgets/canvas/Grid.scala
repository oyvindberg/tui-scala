package tui.widgets.canvas

import tui.{Color, Point}

trait Grid {
  def width: Int

  def height: Int

  def resolution: Point

  def paint(x: Int, y: Int, color: Color): Unit

  def save(): Layer

  def reset(): Unit
}
