package tui
package widgets

class BlockTests extends TuiTest {
  test("inner_takes_into_account_the_borders") {
    // No borders
    assertEq(
      BlockWidget.noChildren().inner(Rect.default),
      Rect.default,
      "no borders, width=0, height=0"
    )
    assertEq(
      BlockWidget.noChildren().inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 0, y = 0, width = 1, height = 1),
      "no borders, width=1, height=1"
    )

    // Left border
    assertEq(
      BlockWidget.noChildren(borders = Borders.LEFT).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 0, width = 0, height = 1),
      "left, width=0"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.LEFT).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 1, y = 0, width = 0, height = 1),
      "left, width=1"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.LEFT).inner(Rect(x = 0, y = 0, width = 2, height = 1)),
      Rect(x = 1, y = 0, width = 1, height = 1),
      "left, width=2"
    )

    // Top border
    assertEq(
      BlockWidget.noChildren(borders = Borders.TOP).inner(Rect(x = 0, y = 0, width = 1, height = 0)),
      Rect(x = 0, y = 0, width = 1, height = 0),
      "top, height=0"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.TOP).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 0, y = 1, width = 1, height = 0),
      "top, height=1"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.TOP).inner(Rect(x = 0, y = 0, width = 1, height = 2)),
      Rect(x = 0, y = 1, width = 1, height = 1),
      "top, height=2"
    )

    // Right border
    assertEq(
      BlockWidget.noChildren(borders = Borders.RIGHT).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 0, width = 0, height = 1),
      "right, width=0"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.RIGHT).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 0, y = 0, width = 0, height = 1),
      "right, width=1"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.RIGHT).inner(Rect(x = 0, y = 0, width = 2, height = 1)),
      Rect(x = 0, y = 0, width = 1, height = 1),
      "right, width=2"
    )

    // Bottom border
    assertEq(
      BlockWidget.noChildren(borders = Borders.BOTTOM).inner(Rect(x = 0, y = 0, width = 1, height = 0)),
      Rect(x = 0, y = 0, width = 1, height = 0),
      "bottom, height=0"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.BOTTOM).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 0, y = 0, width = 1, height = 0),
      "bottom, height=1"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.BOTTOM).inner(Rect(x = 0, y = 0, width = 1, height = 2)),
      Rect(x = 0, y = 0, width = 1, height = 1),
      "bottom, height=2"
    )

    // All borders
    assertEq(
      BlockWidget.noChildren(borders = Borders.ALL).inner(Rect.default),
      Rect.default,
      "all borders, width=0, height=0"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.ALL).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 1, y = 1, width = 0, height = 0),
      "all borders, width=1, height=1"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.ALL).inner(Rect(x = 0, y = 0, width = 2, height = 2)),
      Rect(x = 1, y = 1, width = 0, height = 0),
      "all borders, width=2, height=2"
    )
    assertEq(
      BlockWidget.noChildren(borders = Borders.ALL).inner(Rect(x = 0, y = 0, width = 3, height = 3)),
      Rect(x = 1, y = 1, width = 1, height = 1),
      "all borders, width=3, height=3"
    )
  }

  test("inner_takes_into_account_the_title") {
    assertEq(
      BlockWidget.noChildren(title = Some(Spans.nostyle("Test"))).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 1, width = 0, height = 0)
    )
    assertEq(
      BlockWidget.noChildren(title = Some(Spans.nostyle("Test")), titleAlignment = Alignment.Center).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 1, width = 0, height = 0)
    )
    assertEq(
      BlockWidget.noChildren(title = Some(Spans.nostyle("Test")), titleAlignment = Alignment.Right).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 1, width = 0, height = 0)
    )
  }
}
