package jatatui.tests.widgets.canvas;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.symbols.Marker;
import jatatui.tests._support.BufferAssertions;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Line;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class CanvasLineTest {

  /// Sanity test that exercises construction. Required as an additional anchor: when this class
  /// only contained a single `@ParameterizedTest`-annotated method, the bleep / jupiter-interface
  /// test discovery skipped it. Adding any `@Test` method makes the entire class visible.
  @Test
  void can_construct_line() {
    Line line = new Line(0.0, 0.0, 10.0, 0.0, Color.RED);
    if (line.color() != Color.RED) throw new AssertionError("color mismatch");
  }

  // Mirrors upstream `line::tests::tests` rstest with named cases.
  static Stream<Arguments> lineCases() {
    return Stream.of(
        Arguments.of(new Line(-1.0, 0.0, -1.0, 10.0, Color.RED), repeat("          ", 10)),
        Arguments.of(new Line(0.0, -1.0, 10.0, -1.0, Color.RED), repeat("          ", 10)),
        Arguments.of(new Line(-10.0, 5.0, -1.0, 5.0, Color.RED), repeat("          ", 10)),
        Arguments.of(new Line(5.0, 11.0, 5.0, 20.0, Color.RED), repeat("          ", 10)),
        Arguments.of(
            new Line(-10.0, 0.0, 5.0, 0.0, Color.RED),
            new String[] {
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "••••••    ",
            }),
        Arguments.of(
            new Line(-1.0, -1.0, 10.0, 10.0, Color.RED),
            new String[] {
              "         •",
              "        • ",
              "       •  ",
              "      •   ",
              "     •    ",
              "    •     ",
              "   •      ",
              "  •       ",
              " •        ",
              "•         ",
            }),
        Arguments.of(
            new Line(0.0, 0.0, 11.0, 11.0, Color.RED),
            new String[] {
              "         •",
              "        • ",
              "       •  ",
              "      •   ",
              "     •    ",
              "    •     ",
              "   •      ",
              "  •       ",
              " •        ",
              "•         ",
            }),
        Arguments.of(
            new Line(-1.0, -1.0, 11.0, 11.0, Color.RED),
            new String[] {
              "         •",
              "        • ",
              "       •  ",
              "      •   ",
              "     •    ",
              "    •     ",
              "   •      ",
              "  •       ",
              " •        ",
              "•         ",
            }),
        Arguments.of(
            new Line(0.0, 0.0, 10.0, 0.0, Color.RED),
            new String[] {
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "••••••••••",
            }),
        Arguments.of(
            new Line(10.0, 10.0, 0.0, 10.0, Color.RED),
            new String[] {
              "••••••••••",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
            }),
        Arguments.of(new Line(0.0, 0.0, 0.0, 10.0, Color.RED), repeat("•         ", 10)),
        Arguments.of(new Line(10.0, 10.0, 10.0, 0.0, Color.RED), repeat("         •", 10)),
        Arguments.of(
            new Line(0.0, 0.0, 10.0, 5.0, Color.RED),
            new String[] {
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "        ••",
              "      ••  ",
              "    ••    ",
              "  ••      ",
              "••        ",
            }),
        Arguments.of(
            new Line(10.0, 0.0, 0.0, 5.0, Color.RED),
            new String[] {
              "          ",
              "          ",
              "          ",
              "          ",
              "          ",
              "••        ",
              "  ••      ",
              "    ••    ",
              "      ••  ",
              "        ••",
            }),
        Arguments.of(
            new Line(0.0, 0.0, 5.0, 10.0, Color.RED),
            new String[] {
              "     •    ",
              "    •     ",
              "    •     ",
              "   •      ",
              "   •      ",
              "  •       ",
              "  •       ",
              " •        ",
              " •        ",
              "•         ",
            }),
        Arguments.of(
            new Line(0.0, 10.0, 5.0, 0.0, Color.RED),
            new String[] {
              "•         ",
              " •        ",
              " •        ",
              "  •       ",
              "  •       ",
              "   •      ",
              "   •      ",
              "    •     ",
              "    •     ",
              "     •    ",
            }));
  }

  @ParameterizedTest
  @MethodSource("lineCases")
  void test_line(Line line, String[] expectedLines) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 10));
    Canvas canvas =
        Canvas.empty()
            .withMarker(Marker.Dot)
            .withXBounds(new double[] {0.0, 10.0})
            .withYBounds(new double[] {0.0, 10.0})
            .withPaintFn(ctx -> ctx.draw(line));
    canvas.render(buffer.area(), buffer);

    Buffer expected = Buffer.withLines(expectedLines);
    // Style red on the dot cells, mirroring upstream's per-cell style application.
    Style red = Style.empty().withFg(Color.RED);
    for (Cell cell : expected.content()) {
      if ("•".equals(cell.symbol())) {
        cell.setStyle(red);
      }
    }
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  private static String[] repeat(String s, int count) {
    String[] arr = new String[count];
    for (int i = 0; i < count; i++) arr[i] = s;
    return arr;
  }
}
