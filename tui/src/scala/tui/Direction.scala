package tui

sealed trait Direction

object Direction {
  case object Horizontal extends Direction
  case object Vertical extends Direction
}
