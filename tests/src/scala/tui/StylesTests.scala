package tui

class StylesTests extends TuiTest {
  def styles(): Array[Style] =
    Array(
      Style(),
      Style().fg(Color.Yellow),
      Style().bg(Color.Yellow),
      Style().add_modifier(Modifier.BOLD),
      Style().remove_modifier(Modifier.BOLD),
      Style().add_modifier(Modifier.ITALIC),
      Style().remove_modifier(Modifier.ITALIC),
      Style().add_modifier(Modifier.ITALIC | Modifier.BOLD),
      Style().remove_modifier(Modifier.ITALIC | Modifier.BOLD)
    )

  test("combined_patch_gives_same_result_as_individual_patch") {
    val styles2 = styles();
    for {
      a <- styles2
      b <- styles2
      c <- styles2
      d <- styles2
    } {
      val combined = a.patch(b.patch(c.patch(d)))
      assert_eq(Style().patch(a).patch(b).patch(c).patch(d), Style().patch(combined))
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
