package tui
package internal

object ranges {
  @inline def revRange(fromInclusive: Int, toExclusive: Int)(f: Int => Unit): Unit = {
    var idx = toExclusive - 1
    while (idx >= fromInclusive) {
      f(idx)
      idx -= 1
    }
  }

  @inline def range(fromInclusive: Int, toExclusive: Int)(f: Int => Unit): Unit = {
    var idx = fromInclusive
    while (idx < toExclusive) {
      f(idx)
      idx += 1
    }
  }
}
