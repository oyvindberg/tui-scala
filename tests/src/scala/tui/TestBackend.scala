package tui

/// A backend used for the integration tests.
case class TestBackend(
    var width: Int,
    var height: Int,
    var cursor: Boolean = false,
    var pos: (Int, Int) = (0, 0)
) extends Backend {
  val buffer: Buffer = Buffer.empty(Rect(0, 0, width, height))

  def resize(width: Int, height: Int): Unit = {
    buffer.resize(Rect(0, 0, width, height))
    this.width = width
    this.height = height
  }

  def draw(content: Array[(Int, Int, Cell)]): Unit =
    content.foreach { case (x, y, c) => buffer.set(x, y, c) }

  def hide_cursor(): Unit =
    this.cursor = false

  def show_cursor(): Unit =
    this.cursor = true

  def get_cursor(): (Int, Int) =
    pos

  def set_cursor(x: Int, y: Int): Unit =
    pos = (x, y)

  def clear(): Unit =
    buffer.reset()

  def size(): Rect =
    Rect(0, 0, width, height)

  def flush(): Unit =
    ()
}
