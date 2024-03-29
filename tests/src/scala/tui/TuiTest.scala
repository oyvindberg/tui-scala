package tui

import org.scalactic.{source, CanEqual, Prettifier, TypeCheckedTripleEquals}
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite

trait TuiTest extends AnyFunSuite with TypeCheckedTripleEquals {
  def assertEq[L, R](actual: L, expected: R, msg: String = "")(implicit constraint: L CanEqual R, prettifier: Prettifier, pos: source.Position): Assertion =
    assert(actual === expected)

  def assertBuffer(actual: TestBackend, expected: Buffer): Unit = {
    assertEq(expected.area, actual.buffer.area)
    val diff = expected.diff(actual.buffer)
    if (diff.isEmpty) {
      return
    }

    val debug_info = new StringBuilder("Buffers are not equal")
    debug_info.append('\n')
    debug_info.append("Expected:")
    debug_info.append('\n')
    val expected_view = bufferView(expected)
    debug_info.append(expected_view)
    debug_info.append('\n')
    debug_info.append("Got:")
    debug_info.append('\n')
    val view = bufferView(actual.buffer)
    debug_info.append(view)
    debug_info.append('\n')

    debug_info.append("Diff:")
    debug_info.append('\n')
    val nice_diff = diff.zipWithIndex
      .map { case ((x, y, cell), i) => s"$i: at ($x, $y) expected ${expected.get(x, y)} got $cell" }
      .mkString("\n")
    debug_info.append(nice_diff)
    sys.error(debug_info.toString())
  }
}
