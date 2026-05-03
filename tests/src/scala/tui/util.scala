package tui

import java.util.Optional

/// Test helpers that approximate the old Scala API by exposing small shorthand
/// factory methods. We avoid default parameters and instead provide explicit overloads.
object util {
  // ----- Optional helpers -----
  implicit class OptionToOptional[A](private val o: Option[A]) extends AnyVal {
    def toOptional: Optional[A] = o match {
      case Some(v) => Optional.of(v)
      case None    => Optional.empty[A]()
    }
  }

  // ----- Style helpers -----
  def styleFg(c: Color): Style = Style.empty().withFg(c)
  def styleBg(c: Color): Style = Style.empty().withBg(c)
  def styleAdd(m: Modifier): Style = Style.empty().withAddModifier(m)
  def styleSub(m: Modifier): Style = Style.empty().withRemoveModifier(m)
  def styleFgBg(fg: Color, bg: Color): Style =
    Style.empty().withFg(fg).withBg(bg)
  def styleFgAdd(fg: Color, add: Modifier): Style =
    Style.empty().withFg(fg).withAddModifier(add)
  def styleFgBgAdd(fg: Color, bg: Color, add: Modifier): Style =
    Style.empty().withFg(fg).withBg(bg).withAddModifier(add)
}
