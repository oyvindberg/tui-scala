package tui

class StylesTests extends TuiTest {
  def styles(): Array[Style] =
    Array(
      Style(),
      Style().fg(Color.Yellow),
      Style().bg(Color.Yellow),
      Style().addModifier(Modifier.BOLD),
      Style().removeModifier(Modifier.BOLD),
      Style().addModifier(Modifier.ITALIC),
      Style().removeModifier(Modifier.ITALIC),
      Style().addModifier(Modifier.ITALIC | Modifier.BOLD),
      Style().removeModifier(Modifier.ITALIC | Modifier.BOLD)
    )

  test("combined_patch_gives_same_result_as_individual_patch") {
    val styles2 = styles()
    for {
      a <- styles2
      b <- styles2
      c <- styles2
      d <- styles2
    } {
      val combined = a.patch(b.patch(c.patch(d)))
      assertEq(Style().patch(a).patch(b).patch(c).patch(d), Style().patch(combined))
    }
  }
  test("flaff") {
    val both = Modifier.ITALIC | Modifier.BOLD
    assert(both.contains(Modifier.ITALIC))
    assert(both.contains(Modifier.BOLD))
    assert(!both.contains(Modifier.DIM))

    val onlyBold = both.remove(Modifier.ITALIC)
    assert(!onlyBold.contains(Modifier.ITALIC))
    assert(onlyBold.contains(Modifier.BOLD))
    assert(!onlyBold.contains(Modifier.DIM))
  }
}
