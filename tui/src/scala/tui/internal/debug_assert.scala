package tui
package internal

object debug_assert {
  def apply(pred: Boolean, msg: String, details: Any*): Unit =
    if (pred) ()
    else {
      // todo: template in at correct place
      val formattedDetails = if (details.isEmpty) "" else details.mkString(" (", ", ", ")")
      sys.error(s"assertion failed: $msg$formattedDetails")
    }
}
