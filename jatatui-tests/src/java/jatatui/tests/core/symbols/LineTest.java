package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Line;
import org.junit.jupiter.api.Test;

public class LineTest {

  @Test
  public void default_set_is_normal() {
    assertEquals(Line.NORMAL, Line.Set.defaultSet());
  }

  /// Renders a 4x4 grid using each line set, mirroring the upstream `render` helper.
  private static String render(Line.Set set) {
    StringBuilder b = new StringBuilder();
    b.append(set.topLeft()).append(set.horizontal()).append(set.horizontalDown()).append(set.topRight()).append('\n');
    b.append(set.vertical()).append(' ').append(set.vertical()).append(set.vertical()).append('\n');
    b.append(set.verticalRight()).append(set.horizontal()).append(set.cross()).append(set.verticalLeft()).append('\n');
    b.append(set.bottomLeft()).append(set.horizontal()).append(set.horizontalUp()).append(set.bottomRight());
    return b.toString();
  }

  @Test
  public void normal() {
    assertEquals(
        """
        ┌─┬┐
        │ ││
        ├─┼┤
        └─┴┘""",
        render(Line.NORMAL));
  }

  @Test
  public void rounded() {
    assertEquals(
        """
        ╭─┬╮
        │ ││
        ├─┼┤
        ╰─┴╯""",
        render(Line.ROUNDED));
  }

  @Test
  public void doubled() {
    assertEquals(
        """
        ╔═╦╗
        ║ ║║
        ╠═╬╣
        ╚═╩╝""",
        render(Line.DOUBLE));
  }

  @Test
  public void thick() {
    assertEquals(
        """
        ┏━┳┓
        ┃ ┃┃
        ┣━╋┫
        ┗━┻┛""",
        render(Line.THICK));
  }
}
