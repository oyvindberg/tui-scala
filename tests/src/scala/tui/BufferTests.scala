package tui

import scala.util.control.NonFatal

class BufferTests extends TuiTest {
  def cell(s: String): Cell = {
    val cell = Cell.empty()
    cell.setSymbol(s)
    cell
  }

  test("it_translates_to_and_from_coordinates") {

    val rect = new Rect(200, 100, 50, 80)
    val buf = Buffer.empty(rect)

    // First cell is at the upper left corner.
    assertEq(buf.posOf(0), new Position(200, 100))
    assertEq(buf.indexOf(200, 100), 0)

    // Last cell is in the lower right.
    assertEq(buf.posOf(buf.content.length - 1), new Position(249, 179))
    assertEq(buf.indexOf(249, 179), buf.content.length - 1)
  }

  test("pos_of_panics_on_out_of_bounds") {
    val rect = new Rect(0, 0, 10, 10)
    val buf = Buffer.empty(rect)

    // There are a total of 100 cells; zero-indexed means that 100 would be the 101st cell.
    try buf.posOf(100)
    catch {
      case NonFatal(th) if th.getMessage.contains("assertion failed: Trying to get the coords of a cell outside the buffer") => true
    }
  }

  test("index_of_panics_on_out_of_bounds") {
    val rect = new Rect(0, 0, 10, 10)
    val buf = Buffer.empty(rect)

    // width is 10; zero-indexed means that 10 would be the 11th cell.
    try buf.indexOf(10, 0)
    catch {
      case NonFatal(th) if th.getMessage.contains("Trying to access position outside the buffer:") => true
    }
  }

  test("buffer_set_string") {
    val area = new Rect(0, 0, 5, 1)
    val buffer = Buffer.empty(area)

    // Zero-width
    buffer.setStringn(0, 0, "aaa", 0, Style.empty())
    assertEq(buffer, Buffer.withLines("     "))

    buffer.setString(0, 0, "aaa", Style.empty())
    assertEq(buffer, Buffer.withLines("aaa  "))

    // Width limit:
    buffer.setStringn(0, 0, "bbbbbbbbbbbbbb", 4, Style.empty())
    assertEq(buffer, Buffer.withLines("bbbb "))

    buffer.setString(0, 0, "12345", Style.empty())
    assertEq(buffer, Buffer.withLines("12345"))

    // Width truncation:
    buffer.setString(0, 0, "123456", Style.empty())
    assertEq(buffer, Buffer.withLines("12345"))
  }

  test("buffer_set_string_zero_width") {
    val area = new Rect(0, 0, 1, 1)
    val buffer = Buffer.empty(area)

    // Leading grapheme with zero width
    {
      val s = "a"
      buffer.setStringn(0, 0, s, 1, Style.empty())
      assertEq(buffer, Buffer.withLines("a"))
    }
    {
      // Trailing grapheme with zero with
      val s = "a"
      buffer.setStringn(0, 0, s, 1, Style.empty())
      assertEq(buffer, Buffer.withLines("a"))
    }
  }

  test("buffer_set_string_double_width") {
    val area = new Rect(0, 0, 5, 1)
    val buffer = Buffer.empty(area)
    buffer.setString(0, 0, "コン", Style.empty())
    assertEq(buffer, Buffer.withLines("コン "))

    // Only 1 space left.
    buffer.setString(0, 0, "コンピ", Style.empty())
    assertEq(buffer, Buffer.withLines("コン "))
  }

  test("buffer_with_lines") {
    val buffer = Buffer.withLines("┌────────┐", "│コンピュ│", "│ーa 上で│", "└────────┘")
    assertEq(buffer.area.x, 0)
    assertEq(buffer.area.y, 0)
    assertEq(buffer.area.width, 10)
    assertEq(buffer.area.height, 4)
  }

  test("buffer_diffing_empty_empty") {
    val area = new Rect(0, 0, 40, 40)
    val prev = Buffer.empty(area)
    val next = Buffer.empty(area)
    val diff = prev.diff(next)
    assertEq(diff, Array.empty[BufferUpdate])
  }

  test("buffer_diffing_empty_filled") {
    val area = new Rect(0, 0, 40, 40)
    val prev = Buffer.empty(area)
    val next = Buffer.filled(area, Cell.empty().setSymbol("a"))
    val diff = prev.diff(next)
    assertEq(diff.length, 40 * 40)
  }

  test("buffer_diffing_filled_filled") {
    val area = new Rect(0, 0, 40, 40)
    val prev = Buffer.filled(area, Cell.empty().setSymbol("a"))
    val next = Buffer.filled(area, Cell.empty().setSymbol("a"))
    val diff = prev.diff(next)
    assertEq(diff, Array.empty[BufferUpdate])
  }

  test("buffer_diffing_single_width") {
    val prev = Buffer.withLines(
      "          ",
      "┌Title─┐  ",
      "│      │  ",
      "│      │  ",
      "└──────┘  "
    )
    val next = Buffer.withLines(
      "          ",
      "┌TITLE─┐  ",
      "│      │  ",
      "│      │  ",
      "└──────┘  "
    )
    val diff = prev.diff(next)
    assertEq(
      diff,
      Array(
        new BufferUpdate(2, 1, cell("I")),
        new BufferUpdate(3, 1, cell("T")),
        new BufferUpdate(4, 1, cell("L")),
        new BufferUpdate(5, 1, cell("E"))
      )
    )
  }

  test("buffer_diffing_multi_width") {
    val prev = Buffer.withLines(
      "┌Title─┐  ",
      "└──────┘  "
    )
    val next = Buffer.withLines(
      "┌称号──┐  ",
      "└──────┘  "
    )
    val diff = prev.diff(next)
    assertEq(
      diff,
      Array(
        new BufferUpdate(1, 0, cell("称")),
        /* Skipped "i" */ new BufferUpdate(3, 0, cell("号")),
        /* Skipped "l" */ new BufferUpdate(5, 0, cell("─"))
      )
    )
  }

  test("buffer_diffing_multi_width_offset") {
    val prev = Buffer.withLines("┌称号──┐")
    val next = Buffer.withLines("┌─称号─┐")

    val diff = prev.diff(next)
    assertEq(
      diff,
      Array(
        new BufferUpdate(1, 0, cell("─")),
        new BufferUpdate(2, 0, cell("称")),
        new BufferUpdate(4, 0, cell("号"))
      )
    )
  }

  test("buffer_merge") {
    val one = Buffer.filled(new Rect(0, 0, 2, 2), Cell.empty().setSymbol("1"))
    val two = Buffer.filled(new Rect(0, 2, 2, 2), Cell.empty().setSymbol("2"))
    one.merge(two)
    assertEq(one, Buffer.withLines("11", "11", "22", "22"))
  }

  test("buffer_merge2") {
    val one = Buffer.filled(new Rect(2, 2, 2, 2), Cell.empty().setSymbol("1"))
    val two = Buffer.filled(new Rect(0, 0, 2, 2), Cell.empty().setSymbol("2"))
    one.merge(two)
    val expected = Buffer.withLines("22  ", "22  ", "  11", "  11")
    assertEq(one, expected)
  }

  test("buffer_merge3") {
    val one = Buffer.filled(new Rect(3, 3, 2, 2), Cell.empty().setSymbol("1"))
    val two = Buffer.filled(new Rect(1, 1, 3, 4), Cell.empty().setSymbol("2"))
    one.merge(two)
    val merged = Buffer.withLines("222 ", "222 ", "2221", "2221")
    merged.area = new Rect(1, 1, 4, 4)
    assertEq(one, merged)
  }

  // Ported from ratatui v0.23.0 #215.
  test("buffer_diffing_skip") {
    val prev = Buffer.withLines("123")
    val next = Buffer.withLines("456")
    next.content(1).setSkip(true)
    next.content(2).setSkip(true)
    val diff = prev.diff(next)
    assertEq(diff.length, 1)
    assertEq(diff(0).x, 0)
    assertEq(diff(0).y, 0)
    assertEq(diff(0).cell, cell("4"))
  }
}
