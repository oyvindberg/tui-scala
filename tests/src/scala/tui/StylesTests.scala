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

  test("color_fromString_rgb") {
    val color = Color.fromString("#FF0000")
    assert(color.isPresent)
    assertEq(color.get, new Color.Rgb(255, 0, 0))
  }

  test("color_fromString_indexed") {
    val color = Color.fromString("10")
    assert(color.isPresent)
    assertEq(color.get, new Color.Indexed(10))
  }

  test("color_fromString_named") {
    assertEq[Color, Color](Color.fromString("lightblue").get, Color.LightBlue)
    assertEq[Color, Color](Color.fromString("light blue").get, Color.LightBlue)
    assertEq[Color, Color](Color.fromString("Reset").get, Color.Reset)
  }

  test("color_fromString_invalid") {
    val bad = Array(
      "invalid_color", // not a color string
      "abcdef0",       // 7 chars but no '#'
      " bcdefa",       // not hex
      "#abcdef00"      // too many chars
    )
    bad.foreach(s => assert(Color.fromString(s).isEmpty, s"bad color: '$s'"))
  }

  test("color_fromString_extended_formats") {
    // Spaces, dashes, underscores all stripped (added in v0.22.0)
    assertEq[Color, Color](Color.fromString("light-red").get, Color.LightRed)
    assertEq[Color, Color](Color.fromString("light_red").get, Color.LightRed)
    assertEq[Color, Color](Color.fromString("lightRed").get, Color.LightRed)
    // "bright" treated as "light"
    assertEq[Color, Color](Color.fromString("bright red").get, Color.LightRed)
    assertEq[Color, Color](Color.fromString("bright-red").get, Color.LightRed)
    // synonyms
    assertEq[Color, Color](Color.fromString("silver").get, Color.Gray)
    assertEq[Color, Color](Color.fromString("dark-grey").get, Color.DarkGray)
    assertEq[Color, Color](Color.fromString("dark gray").get, Color.DarkGray)
    assertEq[Color, Color](Color.fromString("light-black").get, Color.DarkGray)
    assertEq[Color, Color](Color.fromString("bright white").get, Color.White)
  }
}
