package tui
package internal

object breakableForeach {
  sealed trait Res
  case object Continue extends Res
  case object Break extends Res

  implicit class BreakableForeachIterator[T](private val it: Iterator[T]) extends AnyVal {
    @inline
    def breakableForeach(f: T => Res): Unit = {
      var continue = true
      while (it.hasNext && continue)
        f(it.next()) match {
          case Continue =>
            ()
          case Break =>
            continue = false
        }
    }
  }
  implicit class BreakableForeachArray[T](private val ts: Array[T]) extends AnyVal {
    @inline
    def breakableForeach(f: (T, Int) => Res): Unit = {
      var continue = true
      var i = 0
      while (i < ts.length && continue) {
        f(ts(i), i) match {
          case Continue =>
            ()
          case Break =>
            continue = false
        }
        i += 1
      }
    }
  }
}
