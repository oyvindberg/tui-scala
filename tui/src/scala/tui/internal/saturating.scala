package tui
package internal

object saturating {
  implicit class IntOps(private val i1: Int) extends AnyVal {
    def saturating_add(i2: Int): Int = {
      val res = i1 + i2
      if (res < i2) Int.MaxValue else res
    }

    def saturating_sub_signed(i2: Int): Int = {
      val res = i1 - i2
      if (res > i2) Int.MinValue else res
    }

    def saturating_sub_unsigned(i2: Int): Int = {
      val res = i1 - i2
      math.max(0, res)
    }
  }
}
