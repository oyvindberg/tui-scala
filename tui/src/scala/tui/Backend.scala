package tui

trait Backend {
  def draw(content: Array[(Int, Int, Cell)]): Unit
  def hideCursor(): Unit
  def showCursor(): Unit
  def getCursor(): (Int, Int)
  def setCursor(x: Int, y: Int): Unit
  def clear(): Unit
  def size(): Rect
  def flush(): Unit
}
