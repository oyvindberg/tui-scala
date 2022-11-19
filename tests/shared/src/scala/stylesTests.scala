package tui

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.funsuite.AnyFunSuite

class stylesTests extends AnyFunSuite with TypeCheckedTripleEquals {
  def styles(): Array[Style] = {
    Array(
      Style(),
      Style().fg(Color.Yellow),
      Style().bg(Color.Yellow),
      Style().add_modifier(Modifier.BOLD),
      Style().remove_modifier(Modifier.BOLD),
      Style().add_modifier(Modifier.ITALIC),
      Style().remove_modifier(Modifier.ITALIC),
      Style().add_modifier(Modifier.ITALIC | Modifier.BOLD),
      Style().remove_modifier(Modifier.ITALIC | Modifier.BOLD),
    )
  }

  test("combined_patch_gives_same_result_as_individual_patch") {
    val styles2 = styles();
    for {
      a <- styles2
      b <- styles2
      c <- styles2
      d <- styles2
    } {
      val combined = a.patch(b.patch(c.patch(d)))
      assert(
        Style().patch(a).patch(b).patch(c).patch(d)
          ===
          Style().patch(combined)
      )
    }
  }
}
