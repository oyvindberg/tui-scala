package tui.widgets.canvas

sealed trait MapResolution
object MapResolution {
  case object Low extends MapResolution
  case object High extends MapResolution
}
