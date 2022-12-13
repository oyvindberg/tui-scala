package tui

//#[derive(Debug, Hash, Clone, PartialEq, Eq)]
sealed trait Direction

object Direction {
  case object Horizontal extends Direction
  case object Vertical extends Direction
}
