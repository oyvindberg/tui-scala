package jatatui.tests.widgets.canvas;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.symbols.Marker;
import jatatui.tests._support.BufferAssertions;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Rectangle;
import org.junit.jupiter.api.Test;

public class RectangleTest {

  // Mirrors upstream `rectangle::tests::draw_block_lines`.
  @Test
  void draw_block_lines() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 10));
    Canvas canvas =
        Canvas.empty()
            .withMarker(Marker.Block)
            .withXBounds(new double[] {0.0, 10.0})
            .withYBounds(new double[] {0.0, 10.0})
            .withPaintFn(ctx -> ctx.draw(new Rectangle(0.0, 0.0, 10.0, 10.0, Color.RED)));
    canvas.render(buffer.area(), buffer);
    Buffer expected =
        Buffer.withLines(
            "██████████",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "██████████");
    expected.setStyle(buffer.area(), Style.empty().withFg(Color.RED).withBg(Color.RED));
    expected.setStyle(buffer.area().inner(new Margin(1, 1)), Style.reset());
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  // Mirrors upstream `rectangle::tests::draw_half_block_lines`.
  @Test
  void draw_half_block_lines() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 10));
    Canvas canvas =
        Canvas.empty()
            .withMarker(Marker.HalfBlock)
            .withXBounds(new double[] {0.0, 10.0})
            .withYBounds(new double[] {0.0, 10.0})
            .withPaintFn(ctx -> ctx.draw(new Rectangle(0.0, 0.0, 10.0, 10.0, Color.RED)));
    canvas.render(buffer.area(), buffer);
    Buffer expected =
        Buffer.withLines(
            "█▀▀▀▀▀▀▀▀█",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█        █",
            "█▄▄▄▄▄▄▄▄█");
    expected.setStyle(buffer.area(), Style.empty().withFg(Color.RED).withBg(Color.RED));
    expected.setStyle(buffer.area().inner(new Margin(1, 0)), Style.reset().withFg(Color.RED));
    expected.setStyle(buffer.area().inner(new Margin(1, 1)), Style.reset());
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  // Mirrors upstream `rectangle::tests::draw_braille_lines`.
  @Test
  void draw_braille_lines() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 10));
    Canvas canvas =
        Canvas.empty()
            .withMarker(Marker.Braille)
            .withXBounds(new double[] {0.0, 20.0})
            .withYBounds(new double[] {0.0, 20.0})
            .withPaintFn(
                ctx -> {
                  ctx.draw(new Rectangle(0.0, 0.0, 20.0, 20.0, Color.RED));
                  ctx.draw(new Rectangle(4.0, 4.0, 12.0, 12.0, Color.GREEN));
                });
    canvas.render(buffer.area(), buffer);
    Buffer expected =
        Buffer.withLines(
            "⡏⠉⠉⠉⠉⠉⠉⠉⠉⢹",
            "⡇        ⢸",
            "⡇ ⡏⠉⠉⠉⠉⢹ ⢸",
            "⡇ ⡇    ⢸ ⢸",
            "⡇ ⡇    ⢸ ⢸",
            "⡇ ⡇    ⢸ ⢸",
            "⡇ ⡇    ⢸ ⢸",
            "⡇ ⣇⣀⣀⣀⣀⣸ ⢸",
            "⡇        ⢸",
            "⣇⣀⣀⣀⣀⣀⣀⣀⣀⣸");
    expected.setStyle(buffer.area(), Style.empty().withFg(Color.RED));
    expected.setStyle(buffer.area().inner(new Margin(1, 1)), Style.reset());
    expected.setStyle(buffer.area().inner(new Margin(2, 2)), Style.empty().withFg(Color.GREEN));
    expected.setStyle(buffer.area().inner(new Margin(3, 3)), Style.reset());
    BufferAssertions.assertBufferEq(buffer, expected);
  }
}
