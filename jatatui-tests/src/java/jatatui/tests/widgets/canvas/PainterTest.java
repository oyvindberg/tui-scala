package jatatui.tests.widgets.canvas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.symbols.Marker;
import jatatui.widgets.canvas.Context;
import jatatui.widgets.canvas.Painter;
import org.junit.jupiter.api.Test;

/// Painter-level tests ported from upstream doctests on `Painter::get_point` and
/// `Painter::bounds`. They are ordinary `@Test`s here since Java has no doctest runner.
public class PainterTest {

  @Test
  void get_point_examples() {
    Context ctx =
        new Context(2, 2, new double[] {1.0, 2.0}, new double[] {0.0, 2.0}, Marker.Braille);
    Painter painter = new Painter(ctx);

    assertEquals(0, painter.getPoint(1.0, 0.0).get().x());
    assertEquals(7, painter.getPoint(1.0, 0.0).get().y());

    assertEquals(2, painter.getPoint(1.5, 1.0).get().x());
    assertEquals(4, painter.getPoint(1.5, 1.0).get().y());

    assertTrue(painter.getPoint(0.0, 0.0).isEmpty());

    assertEquals(3, painter.getPoint(2.0, 2.0).get().x());
    assertEquals(0, painter.getPoint(2.0, 2.0).get().y());

    assertEquals(0, painter.getPoint(1.0, 2.0).get().x());
    assertEquals(0, painter.getPoint(1.0, 2.0).get().y());
  }

  @Test
  void bounds_returns_axis_pair() {
    Context ctx =
        new Context(1, 1, new double[] {0.0, 2.0}, new double[] {0.0, 2.0}, Marker.Braille);
    Painter painter = new Painter(ctx);
    assertArrayEquals(new double[] {0.0, 2.0}, painter.bounds().x());
    assertArrayEquals(new double[] {0.0, 2.0}, painter.bounds().y());
  }
}
