package tui

//#[derive(Debug, Clone, PartialEq)]
/// UNSTABLE
sealed trait ResizeBehavior
object ResizeBehavior {
  case object Fixed extends ResizeBehavior
  case object Auto extends ResizeBehavior
}
