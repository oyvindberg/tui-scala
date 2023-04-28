package tui

case class Viewport(
    var area: Rect,
    resizeBehavior: ResizeBehavior
)

object Viewport {
  // UNSTABLE
  def fixed(area: Rect): Viewport =
    Viewport(
      area,
      resizeBehavior = ResizeBehavior.Fixed
    )
}
