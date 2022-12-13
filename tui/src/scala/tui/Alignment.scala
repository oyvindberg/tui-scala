package tui

//#[derive(Debug, Clone, Copy, PartialEq, Eq)]
sealed trait Alignment
object Alignment {
  case object Left extends Alignment
  case object Center extends Alignment
  case object Right extends Alignment
}
