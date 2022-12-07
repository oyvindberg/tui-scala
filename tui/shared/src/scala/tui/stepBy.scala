package tui

import scala.reflect.ClassTag

object stepBy {
  implicit class StepBySyntax[T](private val ts: Array[T]) extends AnyVal {
    def stepBy(n: Int)(implicit CT: ClassTag[T]): Array[T] = {
      require(n > 0)
      val b = Array.newBuilder[T]
      var i = 0
      while (i < ts.length) {
        b += ts(i)
        i += n
      }
      b.result()
    }
  }
}
