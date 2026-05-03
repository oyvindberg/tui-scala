package tui

class StylesTests extends TuiTest {
  def styles(): Array[Style] =
    Array(
      Style.empty(),
      Style.empty().withFg(Color.Yellow),
      Style.empty().withBg(Color.Yellow),
      Style.empty().withAddModifier(Modifier.BOLD),
      Style.empty().withRemoveModifier(Modifier.BOLD),
      Style.empty().withAddModifier(Modifier.ITALIC),
      Style.empty().withRemoveModifier(Modifier.ITALIC),
      Style.empty().withAddModifier(Modifier.ITALIC.or(Modifier.BOLD)),
      Style.empty().withRemoveModifier(Modifier.ITALIC.or(Modifier.BOLD))
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
      assertEq(Style.empty().patch(a).patch(b).patch(c).patch(d), Style.empty().patch(combined))
    }
  }
  test("flaff") {
    val both = Modifier.ITALIC.or(Modifier.BOLD)
    assert(both.contains(Modifier.ITALIC))
    assert(both.contains(Modifier.BOLD))
    assert(!both.contains(Modifier.DIM))

    val onlyBold = both.remove(Modifier.ITALIC)
    assert(!onlyBold.contains(Modifier.ITALIC))
    assert(onlyBold.contains(Modifier.BOLD))
    assert(!onlyBold.contains(Modifier.DIM))
  }
}
