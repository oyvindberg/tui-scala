package tui

import tui.internal.saturating._

import scala.collection.mutable

object test {
  /// Returns a string representation of the given buffer for debugging purpose.
  def buffer_view(buffer: Buffer): String = {
    val view = new StringBuilder(buffer.content.length + buffer.area.height * 3);
    val value: Iterator[mutable.ArraySeq[Cell]] = buffer.content.grouped(buffer.area.width)
    value.foreach { cells =>
      val overwritten = mutable.ArrayBuffer.empty[(Int, String)]
      var skip: Int = 0;
      view.append('"');
      cells.zipWithIndex.foreach { case (c, x) =>
        if (skip == 0) {
          view.append(c.symbol.str);
        } else {
          overwritten += ((x, c.symbol.str))
        }
        skip = math.max(skip, c.symbol.width).saturating_sub_unsigned(1);
      }
      view.append('"');
      if (overwritten.nonEmpty) {
        view.append(s" Hidden by multi-width symbols: $overwritten")
      }
      view.append('\n');
    }
    view.toString()
  }

  /// A backend used for the integration tests.
  case class TestBackend(
      var width: Int,
      var height: Int,
      var cursor: Boolean = false,
      var pos: (Int, Int) = (0, 0)
  ) extends Backend {
    val buffer: Buffer = Buffer.empty(Rect(0, 0, width, height))

    def resize(width: Int, height: Int): Unit = {
      buffer.resize(Rect(0, 0, width, height));
      this.width = width;
      this.height = height;
    }

    def draw(content: Array[(Int, Int, Cell)]): Unit =
      content.foreach { case (x, y, c) => buffer.set(x, y, c) }

    def hide_cursor(): Unit =
      this.cursor = false;

    def show_cursor(): Unit =
      this.cursor = true;

    def get_cursor(): (Int, Int) =
      pos

    def set_cursor(x: Int, y: Int): Unit =
      pos = (x, y);

    def clear(): Unit =
      buffer.reset();

    def size(): Rect =
      Rect(0, 0, width, height)

    def flush(): Unit =
      ()
  }
}
