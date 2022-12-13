package tui

case class Viewport(
    var area: Rect,
    resize_behavior: ResizeBehavior
)

object Viewport {
  /// UNSTABLE
  def fixed(area: Rect): Viewport =
    Viewport(
      area,
      resize_behavior = ResizeBehavior.Fixed
    )
}
