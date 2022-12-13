package tui
package widgets
package canvas

sealed trait MapResolution
object MapResolution {
  case object Low extends MapResolution
  case object High extends MapResolution
}

/// Shape to draw a world map with the given resolution and color
case class WorldMap(
    resolution: MapResolution = MapResolution.Low,
    color: Color = Color.Reset
) extends Shape {
  override def draw(painter: Painter): Unit = {
    val data = resolution match {
      case MapResolution.Low  => world.WORLD_LOW_RESOLUTION
      case MapResolution.High => world.WORLD_HIGH_RESOLUTION
    }
    data.foreach { case Point(x, y) =>
      painter.get_point(x, y) match {
        case Some((x, y)) => painter.paint(x, y, color)
        case None         => ()
      }
    }
  }
}
