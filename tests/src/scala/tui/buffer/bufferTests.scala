package tui
package buffer

import tui.layout.Rect

import scala.util.control.NonFatal

class bufferTests extends TuiTest {
  def cell(s: String): Cell = {
    val cell = Cell.default
    cell.set_symbol(s)
    cell
  }

  test("it_translates_to_and_from_coordinates") {

    val rect = Rect(x = 200, y = 100, width = 50, height = 80)
    val buf = Buffer.empty(rect)

    // First cell is at the upper left corner.
    assert_eq(buf.pos_of(0), (200, 100))
    assert_eq(buf.index_of(200, 100), 0)

    // Last cell is in the lower right.
    assert_eq(buf.pos_of(buf.content.length - 1), (249, 179))
    assert_eq(buf.index_of(249, 179), buf.content.length - 1)
  }

  test("pos_of_panics_on_out_of_bounds") {
    val rect = Rect(x = 0, y = 0, width = 10, height = 10)
    val buf = Buffer.empty(rect)

    // There are a total of 100 cells; zero-indexed means that 100 would be the 101st cell.
    try buf.pos_of(100)
    catch {
      case NonFatal(th) if th.getMessage.contains("assertion failed: Trying to get the coords of a cell outside the buffer") => true
    }
  }

  test("index_of_panics_on_out_of_bounds") {
    val rect = Rect(x = 0, y = 0, width = 10, height = 10)
    val buf = Buffer.empty(rect)

    // width is 10; zero-indexed means that 10 would be the 11th cell.
    try buf.index_of(10, 0)
    catch {
      case NonFatal(th) if th.getMessage.contains("Trying to access position outside the buffer:") => true
    }
  }

  test("buffer_set_string") {
    val area = Rect(x = 0, y = 0, width = 5, height = 1)
    val buffer = Buffer.empty(area)

    // Zero-width
    buffer.set_stringn(0, 0, "aaa", 0, Style())
    assert_eq(buffer, Buffer.with_lines(Array("     ")))

    buffer.set_string(0, 0, "aaa", Style())
    assert_eq(buffer, Buffer.with_lines(Array("aaa  ")))

    // Width limit:
    buffer.set_stringn(0, 0, "bbbbbbbbbbbbbb", 4, Style())
    assert_eq(buffer, Buffer.with_lines(Array("bbbb ")))

    buffer.set_string(0, 0, "12345", Style())
    assert_eq(buffer, Buffer.with_lines(Array("12345")))

    // Width truncation:
    buffer.set_string(0, 0, "123456", Style())
    assert_eq(buffer, Buffer.with_lines(Array("12345")))
  }

  test("buffer_set_string_zero_width") {
    val area = Rect(x = 0, y = 0, width = 1, height = 1)
    val buffer = Buffer.empty(area)

    // Leading grapheme with zero width
    {
      val s = "\u0001a"
      buffer.set_stringn(0, 0, s, 1, Style())
      assert_eq(buffer, Buffer.with_lines(Array("a")))
    }
    {
      // Trailing grapheme with zero with
      val s = "a\u0001"
      buffer.set_stringn(0, 0, s, 1, Style())
      assert_eq(buffer, Buffer.with_lines(Array("a")))
    }
  }

  test("buffer_set_string_double_width") {
    val area = Rect(x = 0, y = 0, width = 5, height = 1)
    val buffer = Buffer.empty(area)
    buffer.set_string(0, 0, "コン", Style())
    assert_eq(buffer, Buffer.with_lines(Array("コン ")))

    // Only 1 space left.
    buffer.set_string(0, 0, "コンピ", Style())
    assert_eq(buffer, Buffer.with_lines(Array("コン ")))
  }

  test("buffer_with_lines") {
    val buffer = Buffer.with_lines(Array("┌────────┐", "│コンピュ│", "│ーa 上で│", "└────────┘"))
    assert_eq(buffer.area.x, 0)
    assert_eq(buffer.area.y, 0)
    assert_eq(buffer.area.width, 10)
    assert_eq(buffer.area.height, 4)
  }

  test("buffer_diffing_empty_empty") {
    val area = Rect(x = 0, y = 0, width = 40, height = 40)
    val prev = Buffer.empty(area)
    val next = Buffer.empty(area)
    val diff = prev.diff(next)
    assert_eq(diff, Array.empty[(Int, Int, Cell)])
  }

  test("buffer_diffing_empty_filled") {
    val area = Rect(x = 0, y = 0, width = 40, height = 40)
    val prev = Buffer.empty(area)
    val next = Buffer.filled(area, Cell.default.set_symbol("a"))
    val diff = prev.diff(next)
    assert_eq(diff.length, 40 * 40)
  }

  test("buffer_diffing_filled_filled") {
    val area = Rect(x = 0, y = 0, width = 40, height = 40)
    val prev = Buffer.filled(area, Cell.default.set_symbol("a"))
    val next = Buffer.filled(area, Cell.default.set_symbol("a"))
    val diff = prev.diff(next)
    assert_eq(diff, Array.empty[(Int, Int, Cell)])
  }

  test("buffer_diffing_single_width") {
    val prev = Buffer.with_lines(
      Array(
        "          ",
        "┌Title─┐  ",
        "│      │  ",
        "│      │  ",
        "└──────┘  "
      )
    )
    val next = Buffer.with_lines(
      Array(
        "          ",
        "┌TITLE─┐  ",
        "│      │  ",
        "│      │  ",
        "└──────┘  "
      )
    )
    val diff = prev.diff(next)
    assert_eq(
      diff,
      Array(
        (2, 1, cell("I")),
        (3, 1, cell("T")),
        (4, 1, cell("L")),
        (5, 1, cell("E"))
      )
    )
  }

  test("buffer_diffing_multi_width") {
    val prev = Buffer.with_lines(
      Array(
        "┌Title─┐  ",
        "└──────┘  "
      )
    )
    val next = Buffer.with_lines(
      Array(
        "┌称号──┐  ",
        "└──────┘  "
      )
    )
    val diff = prev.diff(next)
    assert_eq(diff, Array((1, 0, cell("称")), /* Skipped "i" */ (3, 0, cell("号")), /* Skipped "l" */ (5, 0, cell("─"))))
  }

  test("buffer_diffing_multi_width_offset") {
    val prev = Buffer.with_lines(Array("┌称号──┐"))
    val next = Buffer.with_lines(Array("┌─称号─┐"))

    val diff = prev.diff(next)
    assert_eq(diff, Array((1, 0, cell("─")), (2, 0, cell("称")), (4, 0, cell("号"))))
  }

  test("buffer_merge") {
    val one = Buffer.filled(Rect(x = 0, y = 0, width = 2, height = 2), Cell.default.set_symbol("1"))
    val two = Buffer.filled(Rect(x = 0, y = 2, width = 2, height = 2), Cell.default.set_symbol("2"))
    one.merge(two)
    assert_eq(one, Buffer.with_lines(Array("11", "11", "22", "22")))
  }

  test("buffer_merge2") {
    val one = Buffer.filled(Rect(x = 2, y = 2, width = 2, height = 2), Cell.default.set_symbol("1"))
    val two = Buffer.filled(Rect(x = 0, y = 0, width = 2, height = 2), Cell.default.set_symbol("2"))
    one.merge(two)
    val expected = Buffer.with_lines(Array("22  ", "22  ", "  11", "  11"))
    assert_eq(one, expected)
  }

  test("buffer_merge3") {
    val one = Buffer.filled(Rect(x = 3, y = 3, width = 2, height = 2), Cell.default.set_symbol("1"))
    val two = Buffer.filled(Rect(x = 1, y = 1, width = 3, height = 4), Cell.default.set_symbol("2"))
    one.merge(two)
    val merged = Buffer.with_lines(Array("222 ", "222 ", "2221", "2221"))
    merged.area = Rect(x = 1, y = 1, width = 4, height = 4)
    assert_eq(one, merged)
  }
}
