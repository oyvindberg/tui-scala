package tui
package widgets

class BlockTests extends TuiTest {
  test("inner_takes_into_account_the_borders") {
    // No borders
    assert_eq(
      BlockWidget().inner(Rect.default),
      Rect.default,
      "no borders, width=0, height=0"
    )
    assert_eq(
      BlockWidget().inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 0, y = 0, width = 1, height = 1),
      "no borders, width=1, height=1"
    )

    // Left border
    assert_eq(
      BlockWidget(borders = Borders.LEFT).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 0, width = 0, height = 1),
      "left, width=0"
    )
    assert_eq(
      BlockWidget(borders = Borders.LEFT).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 1, y = 0, width = 0, height = 1),
      "left, width=1"
    )
    assert_eq(
      BlockWidget(borders = Borders.LEFT).inner(Rect(x = 0, y = 0, width = 2, height = 1)),
      Rect(x = 1, y = 0, width = 1, height = 1),
      "left, width=2"
    )

    // Top border
    assert_eq(
      BlockWidget(borders = Borders.TOP).inner(Rect(x = 0, y = 0, width = 1, height = 0)),
      Rect(x = 0, y = 0, width = 1, height = 0),
      "top, height=0"
    )
    assert_eq(
      BlockWidget(borders = Borders.TOP).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 0, y = 1, width = 1, height = 0),
      "top, height=1"
    )
    assert_eq(
      BlockWidget(borders = Borders.TOP).inner(Rect(x = 0, y = 0, width = 1, height = 2)),
      Rect(x = 0, y = 1, width = 1, height = 1),
      "top, height=2"
    )

    // Right border
    assert_eq(
      BlockWidget(borders = Borders.RIGHT).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 0, width = 0, height = 1),
      "right, width=0"
    )
    assert_eq(
      BlockWidget(borders = Borders.RIGHT).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 0, y = 0, width = 0, height = 1),
      "right, width=1"
    )
    assert_eq(
      BlockWidget(borders = Borders.RIGHT).inner(Rect(x = 0, y = 0, width = 2, height = 1)),
      Rect(x = 0, y = 0, width = 1, height = 1),
      "right, width=2"
    )

    // Bottom border
    assert_eq(
      BlockWidget(borders = Borders.BOTTOM).inner(Rect(x = 0, y = 0, width = 1, height = 0)),
      Rect(x = 0, y = 0, width = 1, height = 0),
      "bottom, height=0"
    )
    assert_eq(
      BlockWidget(borders = Borders.BOTTOM).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 0, y = 0, width = 1, height = 0),
      "bottom, height=1"
    )
    assert_eq(
      BlockWidget(borders = Borders.BOTTOM).inner(Rect(x = 0, y = 0, width = 1, height = 2)),
      Rect(x = 0, y = 0, width = 1, height = 1),
      "bottom, height=2"
    )

    // All borders
    assert_eq(
      BlockWidget(borders = Borders.ALL).inner(Rect.default),
      Rect.default,
      "all borders, width=0, height=0"
    )
    assert_eq(
      BlockWidget(borders = Borders.ALL).inner(Rect(x = 0, y = 0, width = 1, height = 1)),
      Rect(x = 1, y = 1, width = 0, height = 0),
      "all borders, width=1, height=1"
    )
    assert_eq(
      BlockWidget(borders = Borders.ALL).inner(Rect(x = 0, y = 0, width = 2, height = 2)),
      Rect(x = 1, y = 1, width = 0, height = 0),
      "all borders, width=2, height=2"
    )
    assert_eq(
      BlockWidget(borders = Borders.ALL).inner(Rect(x = 0, y = 0, width = 3, height = 3)),
      Rect(x = 1, y = 1, width = 1, height = 1),
      "all borders, width=3, height=3"
    )
  }

  test("inner_takes_into_account_the_title") {
    assert_eq(
      BlockWidget(title = Some(Spans.nostyle("Test"))).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 1, width = 0, height = 0)
    )
    assert_eq(
      BlockWidget(title = Some(Spans.nostyle("Test")), title_alignment = Alignment.Center).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 1, width = 0, height = 0)
    )
    assert_eq(
      BlockWidget(title = Some(Spans.nostyle("Test")), title_alignment = Alignment.Right).inner(Rect(x = 0, y = 0, width = 0, height = 1)),
      Rect(x = 0, y = 1, width = 0, height = 0)
    )
  }
}
