package tui

package object cassowary {
  implicit class Unwrapper[L, R](private val e: Either[L, R]) extends AnyVal {
    def unwrap(): R =
      e match {
        case Left(e)  => sys.error(s"failure: $e")
        case Right(t) => t
      }
  }
}
