package tui
package widgets

class BlockTests extends TuiTest {
  test("inner_takes_into_account_the_borders") {
    // No borders
    assertEq(
      BlockWidget.empty().inner(Rect.DEFAULT),
      Rect.DEFAULT,
      "no borders, width=0, height=0"
    )
    assertEq(
      BlockWidget.empty().inner(new Rect(0, 0, 1, 1)),
      new Rect(0, 0, 1, 1),
      "no borders, width=1, height=1"
    )

    // Left border
    assertEq(
      BlockWidget.empty().withBorders(Borders.LEFT).inner(new Rect(0, 0, 0, 1)),
      new Rect(0, 0, 0, 1),
      "left, width=0"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.LEFT).inner(new Rect(0, 0, 1, 1)),
      new Rect(1, 0, 0, 1),
      "left, width=1"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.LEFT).inner(new Rect(0, 0, 2, 1)),
      new Rect(1, 0, 1, 1),
      "left, width=2"
    )

    // Top border
    assertEq(
      BlockWidget.empty().withBorders(Borders.TOP).inner(new Rect(0, 0, 1, 0)),
      new Rect(0, 0, 1, 0),
      "top, height=0"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.TOP).inner(new Rect(0, 0, 1, 1)),
      new Rect(0, 1, 1, 0),
      "top, height=1"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.TOP).inner(new Rect(0, 0, 1, 2)),
      new Rect(0, 1, 1, 1),
      "top, height=2"
    )

    // Right border
    assertEq(
      BlockWidget.empty().withBorders(Borders.RIGHT).inner(new Rect(0, 0, 0, 1)),
      new Rect(0, 0, 0, 1),
      "right, width=0"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.RIGHT).inner(new Rect(0, 0, 1, 1)),
      new Rect(0, 0, 0, 1),
      "right, width=1"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.RIGHT).inner(new Rect(0, 0, 2, 1)),
      new Rect(0, 0, 1, 1),
      "right, width=2"
    )

    // Bottom border
    assertEq(
      BlockWidget.empty().withBorders(Borders.BOTTOM).inner(new Rect(0, 0, 1, 0)),
      new Rect(0, 0, 1, 0),
      "bottom, height=0"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.BOTTOM).inner(new Rect(0, 0, 1, 1)),
      new Rect(0, 0, 1, 0),
      "bottom, height=1"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.BOTTOM).inner(new Rect(0, 0, 1, 2)),
      new Rect(0, 0, 1, 1),
      "bottom, height=2"
    )

    // All borders
    assertEq(
      BlockWidget.empty().withBorders(Borders.ALL).inner(Rect.DEFAULT),
      Rect.DEFAULT,
      "all borders, width=0, height=0"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.ALL).inner(new Rect(0, 0, 1, 1)),
      new Rect(1, 1, 0, 0),
      "all borders, width=1, height=1"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.ALL).inner(new Rect(0, 0, 2, 2)),
      new Rect(1, 1, 0, 0),
      "all borders, width=2, height=2"
    )
    assertEq(
      BlockWidget.empty().withBorders(Borders.ALL).inner(new Rect(0, 0, 3, 3)),
      new Rect(1, 1, 1, 1),
      "all borders, width=3, height=3"
    )
  }

  test("inner_takes_into_account_the_title") {
    assertEq(
      BlockWidget.empty().withTitle(Spans.nostyle("Test")).inner(new Rect(0, 0, 0, 1)),
      new Rect(0, 1, 0, 0)
    )
    assertEq(
      BlockWidget.empty().withTitle(Spans.nostyle("Test")).withTitleAlignment(Alignment.Center).inner(new Rect(0, 0, 0, 1)),
      new Rect(0, 1, 0, 0)
    )
    assertEq(
      BlockWidget.empty().withTitle(Spans.nostyle("Test")).withTitleAlignment(Alignment.Right).inner(new Rect(0, 0, 0, 1)),
      new Rect(0, 1, 0, 0)
    )
  }
}
