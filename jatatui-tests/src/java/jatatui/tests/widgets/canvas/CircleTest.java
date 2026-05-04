package jatatui.tests.widgets.canvas;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.symbols.Marker;
import jatatui.tests._support.BufferAssertions;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Circle;
import org.junit.jupiter.api.Test;

public class CircleTest {

  // Mirrors upstream `circle::tests::test_it_draws_a_circle`.
  @Test
  void test_it_draws_a_circle() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 5));
    Canvas canvas =
        Canvas.empty()
            .withPaintFn(ctx -> ctx.draw(new Circle(5.0, 2.0, 5.0, Color.RESET)))
            .withMarker(Marker.Braille)
            .withXBounds(new double[] {-10.0, 10.0})
            .withYBounds(new double[] {-10.0, 10.0});
    canvas.render(buffer.area(), buffer);
    Buffer expected =
        Buffer.withLines("      ⣀⣀⣀ ", "     ⡞⠁ ⠈⢣", "     ⢇⡀ ⢀⡼", "      ⠉⠉⠉ ", "          ");
    BufferAssertions.assertBufferEq(buffer, expected);
  }
}
