package tui

import tui.internal.Saturating

import scala.collection.mutable

/// Returns a string representation of the given buffer for debugging purpose.
object bufferView {
  def apply(buffer: Buffer): String = {
    val view = new StringBuilder(buffer.content.length + buffer.area.height * 3)
    val value: Iterator[Array[Cell]] = buffer.content.grouped(buffer.area.width)
    value.foreach { cells =>
      val overwritten = mutable.ArrayBuffer.empty[(Int, String)]
      var skip: Int = 0
      view.append('"')
      cells.zipWithIndex.foreach { case (c, x) =>
        if (skip == 0) {
          view.append(c.symbol.str)
        } else {
          overwritten += ((x, c.symbol.str))
        }
        skip = Saturating.saturatingSubUnsigned(math.max(skip, c.symbol.width), 1)
      }
      view.append('"')
      if (overwritten.nonEmpty) {
        view.append(s" Hidden by multi-width symbols: $overwritten")
      }
      view.append('\n');
    }
    view.toString()
  }
}
