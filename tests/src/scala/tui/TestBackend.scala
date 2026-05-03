package tui

/// A backend used for the integration tests.
case class TestBackend(
    var width: Int,
    var height: Int,
    var cursor: Boolean,
    var pos: Position
) extends Backend {
  val buffer: Buffer = Buffer.empty(new Rect(0, 0, width, height))

  def resize(width: Int, height: Int): Unit = {
    buffer.resize(new Rect(0, 0, width, height))
    this.width = width
    this.height = height
  }

  override def draw(content: Array[BufferUpdate]): Unit =
    content.foreach(u => buffer.set(u.x(), u.y(), u.cell()))

  override def hideCursor(): Unit =
    this.cursor = false

  override def showCursor(): Unit =
    this.cursor = true

  override def getCursor(): Position =
    pos

  override def setCursor(x: Int, y: Int): Unit =
    pos = new Position(x, y)

  override def clear(): Unit =
    buffer.reset()

  override def size(): Rect =
    new Rect(0, 0, width, height)

  override def flush(): Unit =
    ()
}

object TestBackend {
  def apply(width: Int, height: Int): TestBackend =
    new TestBackend(width, height, cursor = false, pos = new Position(0, 0))
}
