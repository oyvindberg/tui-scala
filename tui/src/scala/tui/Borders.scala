package tui

/** Bitflags that can be composed to set the visible borders essentially on the block widget.
  */
case class Borders(bits: Int) {
  def fmt(sb: StringBuilder): Unit = {
    var first = true
    if (Borders.NONE.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false
      sb.append("NONE")
    }
    if (Borders.TOP.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false
      sb.append("TOP")
    }
    if (Borders.RIGHT.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false
      sb.append("RIGHT")
    }
    if (Borders.BOTTOM.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false
      sb.append("BOTTOM")
    }
    if (Borders.LEFT.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false
      sb.append("LEFT")
    }
    if (first) {
      sb.append("(empty)")
    }
    ()
  }

  override def toString: String = {
    val sb = new StringBuilder()
    fmt(sb)
    sb.toString()
  }

  /** Returns `true` if all of the flags in `other` are contained within `self`.
    */
  def contains(other: Borders): Boolean =
    other != Borders.EMPTY && (bits & other.bits) == other.bits

  def intersects(other: Borders): Boolean =
    Borders.EMPTY.bits != (bits & other.bits)

  /** Inserts the specified flags in-place.
    */
  def insert(other: Borders): Borders =
    copy(bits = bits | other.bits)

  /** Removes the specified flags in-place.
    */
  def remove(other: Borders): Borders =
    copy(bits = bits & Integer.reverse(other.bits))

  def |(other: Borders): Borders =
    copy(bits = bits | other.bits)

  def -(other: Borders): Borders =
    remove(other)
}

object Borders {

  /** Show no border (default)
    */
  val NONE: Borders = Borders(1 << 0)

  /** Show the top border
    */
  val TOP: Borders = Borders(1 << 1)

  /** Show the right border
    */
  val RIGHT: Borders = Borders(1 << 2)

  /** Show the bottom border
    */
  val BOTTOM: Borders = Borders(1 << 3)

  /** Show the left border
    */
  val LEFT: Borders = Borders(1 << 4)

  /** Returns an empty set of flags.
    */
  val EMPTY: Borders = Borders(bits = 0)

  /** Show all borders
    */
  val ALL: Borders = List(TOP, RIGHT, BOTTOM, LEFT).reduce(_ | _)
}
