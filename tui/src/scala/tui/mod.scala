package tui

trait Backend {
  def draw(content: Array[(Int, Int, Cell)]): Unit
  def hide_cursor(): Unit
  def show_cursor(): Unit
  def get_cursor(): (Int, Int)
  def set_cursor(x: Int, y: Int): Unit
  def clear(): Unit
  def size(): Rect
  def flush(): Unit
}
