package tui

import org.scalactic.{source, CanEqual, Prettifier, TypeCheckedTripleEquals}
import org.scalatest.funsuite.AnyFunSuite
import test.{buffer_view, TestBackend}

trait TuiTest extends AnyFunSuite with TypeCheckedTripleEquals {
  def assert_eq[L, R](actual: L, expected: R, msg: String = "")(implicit constraint: L CanEqual R, prettifier: Prettifier, pos: source.Position) =
    assert(actual === expected)

  def assert_buffer(actual: TestBackend, expected: Buffer): Unit = {
    assert_eq(expected.area, actual.buffer.area)
    val diff = expected.diff(actual.buffer)
    if (diff.isEmpty) {
      return
    }

    val debug_info = new StringBuilder("Buffers are not equal")
    debug_info.append('\n')
    debug_info.append("Expected:")
    debug_info.append('\n')
    val expected_view = buffer_view(expected)
    debug_info.append(expected_view)
    debug_info.append('\n')
    debug_info.append("Got:")
    debug_info.append('\n')
    val view = buffer_view(actual.buffer)
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
