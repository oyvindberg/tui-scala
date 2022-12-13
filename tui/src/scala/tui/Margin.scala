package tui

case class Margin(vertical: Int, horizontal: Int)
object Margin {
  def apply(value: Int): Margin = Margin(value, value)
}
