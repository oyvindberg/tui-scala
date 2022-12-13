package tui

//#[derive(Debug, Hash, Clone, Copy, PartialEq, Eq)]
sealed trait Corner
object Corner {
  case object TopLeft extends Corner
  case object TopRight extends Corner
  case object BottomRight extends Corner
  case object BottomLeft extends Corner
}
