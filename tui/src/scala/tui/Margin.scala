package tui

case class Margin(vertical: Int, horizontal: Int)
object Margin {
  val None: Margin = Margin(0, 0)
  def apply(value: Int): Margin = Margin(value, value)
}
