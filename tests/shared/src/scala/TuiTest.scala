package tui

import org.scalactic.{source, CanEqual, Prettifier, TypeCheckedTripleEquals}
import org.scalatest.funsuite.AnyFunSuite

trait TuiTest extends AnyFunSuite with TypeCheckedTripleEquals {
  def assert_eq[L, R](actual: L, expected: R, msg: String = "")(implicit constraint: L CanEqual R, prettifier: Prettifier, pos: source.Position) =
    assert(actual === expected)
}
