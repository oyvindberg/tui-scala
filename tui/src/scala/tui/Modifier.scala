package tui

/*  Modifier changes the way a piece of text is displayed.
 *
 *  They are bitflags so they can easily be composed.
 *
 *  ## Examples
 *
 *  ```rust
 *  # use tui::style::Modifier;
 *
 *  let m = Modifier::BOLD | Modifier::ITALIC;
 *  ```
 */
//#[cfg_attr(feature = "serde", derive(serde::Serialize, serde::Deserialize))]
//#[derive(Copy, PartialEq, Eq, Clone, PartialOrd, Ord, Hash)]
case class Modifier(bits: Int) {
  def fmt(sb: StringBuilder): Unit = {
    var first = true;
    if (Modifier.BOLD.contains(this)) {
      if (!first) {
        sb.append(" | ");
      }
      first = false;
      sb.append("BOLD");
    }
    if (Modifier.DIM.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("DIM")
    }
    if (Modifier.ITALIC.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("ITALIC")
    }
    if (Modifier.UNDERLINED.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("UNDERLINED")
    }
    if (Modifier.SLOW_BLINK.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("SLOW_BLINK")
    }
    if (Modifier.RAPID_BLINK.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("RAPID_BLINK")
    }
    if (Modifier.REVERSED.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("REVERSED")
    }
    if (Modifier.HIDDEN.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("HIDDEN")
    }
    if (Modifier.CROSSED_OUT.contains(this)) {
      if (!first) {
        sb.append(" | ")
      }
      first = false;
      sb.append("CROSSED_OUT")
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

  /// Returns `true` if all of the flags in `other` are contained within `self`.
  def contains(other: Modifier): Boolean =
    other != Modifier.EMPTY && (bits & other.bits) == other.bits

  /// Inserts the specified flags in-place.
  def insert(other: Modifier): Modifier =
    copy(bits = bits | other.bits)

  /// Removes the specified flags in-place.
  def remove(other: Modifier): Modifier =
    copy(bits = bits & ~other.bits)

  def |(mod: Modifier): Modifier =
    insert(mod)

  def -(mod: Modifier): Modifier =
    remove(mod)
}

object Modifier {
  val BOLD: Modifier = Modifier(bits = 1 << 0)
  val DIM: Modifier = Modifier(bits = 1 << 1)
  val ITALIC: Modifier = Modifier(bits = 1 << 2)
  val UNDERLINED: Modifier = Modifier(bits = 1 << 3)
  val SLOW_BLINK: Modifier = Modifier(bits = 1 << 4)
  val RAPID_BLINK: Modifier = Modifier(bits = 1 << 5)
  val REVERSED: Modifier = Modifier(bits = 1 << 6)
  val HIDDEN: Modifier = Modifier(bits = 1 << 7)
  val CROSSED_OUT: Modifier = Modifier(bits = 1 << 8)
  /// Returns an empty set of flags.
  val EMPTY = Modifier(bits = 0)
  /// Returns the set containing all flags.
  val ALL = Modifier(bits = Integer.parseInt("000111111111", 2))
}
