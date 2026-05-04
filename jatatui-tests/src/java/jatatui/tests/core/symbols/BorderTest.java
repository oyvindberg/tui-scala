package jatatui.tests.core.symbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.symbols.Border;
import jatatui.core.symbols.Line;
import org.junit.jupiter.api.Test;

public class BorderTest {

  @Test
  public void default_set_is_plain() {
    assertEquals(Border.PLAIN, Border.Set.defaultSet());
  }

  /// Renders a 4x4 area framed by the border set, surrounded by `░` placeholders.
  private static String render(Border.Set set) {
    StringBuilder b = new StringBuilder();
    b.append("░░░░░░\n");
    b.append('░')
        .append(set.topLeft())
        .append(set.horizontalTop())
        .append(set.horizontalTop())
        .append(set.topRight())
        .append("░\n");
    b.append('░').append(set.verticalLeft()).append("░░").append(set.verticalRight()).append("░\n");
    b.append('░').append(set.verticalLeft()).append("░░").append(set.verticalRight()).append("░\n");
    b.append('░')
        .append(set.bottomLeft())
        .append(set.horizontalBottom())
        .append(set.horizontalBottom())
        .append(set.bottomRight())
        .append("░\n");
    b.append("░░░░░░");
    return b.toString();
  }

  @Test
  public void border_set_from_line_set() {
    Line.Set custom = new Line.Set("e", "f", "b", "a", "d", "c", "g", "h", "i", "j", "k");
    Border.Set bs = Border.fromLineSet(custom);
    assertEquals(new Border.Set("a", "b", "c", "d", "e", "e", "f", "f"), bs);
  }

  @Test
  public void plain() {
    assertEquals(
        """
        ░░░░░░
        ░┌──┐░
        ░│░░│░
        ░│░░│░
        ░└──┘░
        ░░░░░░\
        """,
        render(Border.PLAIN));
  }

  @Test
  public void rounded() {
    assertEquals(
        """
        ░░░░░░
        ░╭──╮░
        ░│░░│░
        ░│░░│░
        ░╰──╯░
        ░░░░░░\
        """,
        render(Border.ROUNDED));
  }

  @Test
  public void doubled() {
    assertEquals(
        """
        ░░░░░░
        ░╔══╗░
        ░║░░║░
        ░║░░║░
        ░╚══╝░
        ░░░░░░\
        """,
        render(Border.DOUBLE));
  }

  @Test
  public void thick() {
    assertEquals(
        """
        ░░░░░░
        ░┏━━┓░
        ░┃░░┃░
        ░┃░░┃░
        ░┗━━┛░
        ░░░░░░\
        """,
        render(Border.THICK));
  }

  @Test
  public void light_double_dashed() {
    assertEquals(
        """
        ░░░░░░
        ░┌╌╌┐░
        ░╎░░╎░
        ░╎░░╎░
        ░└╌╌┘░
        ░░░░░░\
        """,
        render(Border.LIGHT_DOUBLE_DASHED));
  }

  @Test
  public void heavy_double_dashed() {
    assertEquals(
        """
        ░░░░░░
        ░┏╍╍┓░
        ░╏░░╏░
        ░╏░░╏░
        ░┗╍╍┛░
        ░░░░░░\
        """,
        render(Border.HEAVY_DOUBLE_DASHED));
  }

  @Test
  public void light_triple_dashed() {
    assertEquals(
        """
        ░░░░░░
        ░┌┄┄┐░
        ░┆░░┆░
        ░┆░░┆░
        ░└┄┄┘░
        ░░░░░░\
        """,
        render(Border.LIGHT_TRIPLE_DASHED));
  }

  @Test
  public void heavy_triple_dashed() {
    assertEquals(
        """
        ░░░░░░
        ░┏┅┅┓░
        ░┇░░┇░
        ░┇░░┇░
        ░┗┅┅┛░
        ░░░░░░\
        """,
        render(Border.HEAVY_TRIPLE_DASHED));
  }

  @Test
  public void light_quadruple_dashed() {
    assertEquals(
        """
        ░░░░░░
        ░┌┈┈┐░
        ░┊░░┊░
        ░┊░░┊░
        ░└┈┈┘░
        ░░░░░░\
        """,
        render(Border.LIGHT_QUADRUPLE_DASHED));
  }

  @Test
  public void heavy_quadruple_dashed() {
    assertEquals(
        """
        ░░░░░░
        ░┏┉┉┓░
        ░┋░░┋░
        ░┋░░┋░
        ░┗┉┉┛░
        ░░░░░░\
        """,
        render(Border.HEAVY_QUADRUPLE_DASHED));
  }

  @Test
  public void quadrant_outside() {
    assertEquals(
        """
        ░░░░░░
        ░▛▀▀▜░
        ░▌░░▐░
        ░▌░░▐░
        ░▙▄▄▟░
        ░░░░░░\
        """,
        render(Border.QUADRANT_OUTSIDE));
  }

  @Test
  public void quadrant_inside() {
    assertEquals(
        """
        ░░░░░░
        ░▗▄▄▖░
        ░▐░░▌░
        ░▐░░▌░
        ░▝▀▀▘░
        ░░░░░░\
        """,
        render(Border.QUADRANT_INSIDE));
  }

  @Test
  public void one_eighth_wide() {
    assertEquals(
        """
        ░░░░░░
        ░▁▁▁▁░
        ░▏░░▕░
        ░▏░░▕░
        ░▔▔▔▔░
        ░░░░░░\
        """,
        render(Border.ONE_EIGHTH_WIDE));
  }

  @Test
  public void one_eighth_tall() {
    assertEquals(
        """
        ░░░░░░
        ░▕▔▔▏░
        ░▕░░▏░
        ░▕░░▏░
        ░▕▁▁▏░
        ░░░░░░\
        """,
        render(Border.ONE_EIGHTH_TALL));
  }

  @Test
  public void proportional_wide() {
    assertEquals(
        """
        ░░░░░░
        ░▄▄▄▄░
        ░█░░█░
        ░█░░█░
        ░▀▀▀▀░
        ░░░░░░\
        """,
        render(Border.PROPORTIONAL_WIDE));
  }

  @Test
  public void proportional_tall() {
    assertEquals(
        """
        ░░░░░░
        ░█▀▀█░
        ░█░░█░
        ░█░░█░
        ░█▄▄█░
        ░░░░░░\
        """,
        render(Border.PROPORTIONAL_TALL));
  }

  @Test
  public void full() {
    assertEquals(
        """
        ░░░░░░
        ░████░
        ░█░░█░
        ░█░░█░
        ░████░
        ░░░░░░\
        """,
        render(Border.FULL));
  }

  @Test
  public void empty() {
    assertEquals(
        """
        ░░░░░░
        ░    ░
        ░ ░░ ░
        ░ ░░ ░
        ░    ░
        ░░░░░░\
        """,
        render(Border.EMPTY));
  }
}
