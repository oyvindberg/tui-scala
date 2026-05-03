package tui

class LayoutTests extends TuiTest {
  // Ported from ratatui v0.22.0 src/layout.rs `test_constraint_apply`.
  test("test_constraint_apply") {
    assertEq(new Constraint.Percentage(0).apply(100), 0)
    assertEq(new Constraint.Percentage(50).apply(100), 50)
    assertEq(new Constraint.Percentage(100).apply(100), 100)
    assertEq(new Constraint.Percentage(200).apply(100), 100)

    // 0/0 intentionally avoids a panic by returning 0.
    assertEq(new Constraint.Ratio(0, 0).apply(100), 0)
    // 1/0 intentionally avoids a panic by returning 100% of the length.
    assertEq(new Constraint.Ratio(1, 0).apply(100), 100)
    assertEq(new Constraint.Ratio(0, 1).apply(100), 0)
    assertEq(new Constraint.Ratio(1, 2).apply(100), 50)
    assertEq(new Constraint.Ratio(2, 2).apply(100), 100)
    assertEq(new Constraint.Ratio(3, 2).apply(100), 100)

    assertEq(new Constraint.Length(0).apply(100), 0)
    assertEq(new Constraint.Length(50).apply(100), 50)
    assertEq(new Constraint.Length(100).apply(100), 100)
    assertEq(new Constraint.Length(200).apply(100), 100)

    assertEq(new Constraint.Max(0).apply(100), 0)
    assertEq(new Constraint.Max(50).apply(100), 50)
    assertEq(new Constraint.Max(100).apply(100), 100)
    assertEq(new Constraint.Max(200).apply(100), 100)

    assertEq(new Constraint.Min(0).apply(100), 100)
    assertEq(new Constraint.Min(50).apply(100), 100)
    assertEq(new Constraint.Min(100).apply(100), 100)
    assertEq(new Constraint.Min(200).apply(100), 200)
  }

  // Ported from ratatui v0.25.0 #678.
  test("rect_intersection_underflow") {
    val a = new Rect(1, 1, 2, 2)
    val b = new Rect(4, 4, 2, 2)
    assertEq(a.intersection(b), new Rect(4, 4, 0, 0))
  }
}
