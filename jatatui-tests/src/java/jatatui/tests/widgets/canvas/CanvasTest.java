package jatatui.tests.widgets.canvas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.symbols.Marker;
import jatatui.tests._support.BufferAssertions;
import jatatui.widgets.canvas.Canvas;
import jatatui.widgets.canvas.Line;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Mirrors upstream tests in `submodules/ratatui/ratatui-widgets/src/canvas.rs`.
///
/// Note: literal symbols use `\\uXXXX` escapes (and explicit surrogate pairs for supplementary
/// plane characters) so the source file remains pure ASCII and survives editor round-trips.
public class CanvasTest {

  // ---- Common per-marker glyphs (single-cell strings, used to build expected rows) ----

  /// "█" full-block U+2588.
  private static final String BLOCK = "█";
  /// "▄" lower half-block U+2584.
  private static final String LOWER_HALF = "▄";
  /// "▌" left half U+258C.
  private static final String LEFT_HALF = "▌";
  /// "▙" U+2599.
  private static final String QUADRANT_BL_FULL = "▙";
  /// "▚" U+259A.
  private static final String QUADRANT_DIAG_BACK = "▚";
  /// "▞" U+259E.
  private static final String QUADRANT_DIAG_FWD = "▞";
  /// "•" bullet U+2022.
  private static final String DOT = "•";

  /// Braille "⡇" U+2847.
  private static final String BRAILLE_VLINE = "⡇";
  /// Braille "⣇" U+28C7.
  private static final String BRAILLE_LCORNER = "⣇";
  /// Braille "⣀" U+28C0.
  private static final String BRAILLE_HLINE = "⣀";
  /// Braille "⢣" U+28A3.
  private static final String BRAILLE_DIAG_BACK = "⢣";
  /// Braille "⡜" U+285C.
  private static final String BRAILLE_DIAG_FWD = "⡜";
  /// Braille "⣿" U+28FF.
  private static final String BRAILLE_FULL = "⣿";

  /// Sextant U+1FB32 — bottom-left full quadrant equivalent for sextant grid.
  private static final String SEXTANT_BL = "🬲";
  /// Sextant U+1FB2D — sextant horizontal-line piece.
  private static final String SEXTANT_HLINE = "🬭";
  /// Sextant U+1FB27 — diagonal back top.
  private static final String SEXTANT_DIAG_BACK_TOP = "🬧";
  /// Sextant U+1FB14 — diagonal forward top.
  private static final String SEXTANT_DIAG_FWD_TOP = "🬔";
  /// Sextant U+1FB18 — diagonal back bottom.
  private static final String SEXTANT_DIAG_BACK_BOT = "🬘";
  /// Sextant U+1FB23 — diagonal forward bottom.
  private static final String SEXTANT_DIAG_FWD_BOT = "🬣";

  /// Octant U+1CDC0.
  private static final String OCTANT_BL = "𜷀";
  /// "▂" U+2582.
  private static final String LOWER_QUARTER = "▂";

  // Row helpers — each is a 5-character row used by the canvas tests.
  private static String row5(String first) {
    return first + "xxxx";
  }

  // Mirrors upstream `test_horizontal_with_vertical` (rstest cases).
  static Stream<Arguments> horizontalWithVerticalCases() {
    return Stream.of(
        Arguments.of(
            Marker.Block,
            new String[] {row5(BLOCK), row5(BLOCK), row5(BLOCK), row5(BLOCK), BLOCK.repeat(5)}),
        Arguments.of(
            Marker.HalfBlock,
            new String[] {
              row5(BLOCK), row5(BLOCK), row5(BLOCK), row5(BLOCK), BLOCK + LOWER_HALF.repeat(4)
            }),
        Arguments.of(
            Marker.Bar,
            new String[] {
              row5(LOWER_HALF),
              row5(LOWER_HALF),
              row5(LOWER_HALF),
              row5(LOWER_HALF),
              LOWER_HALF.repeat(5)
            }),
        Arguments.of(
            Marker.Braille,
            new String[] {
              row5(BRAILLE_VLINE),
              row5(BRAILLE_VLINE),
              row5(BRAILLE_VLINE),
              row5(BRAILLE_VLINE),
              BRAILLE_LCORNER + BRAILLE_HLINE.repeat(4)
            }),
        Arguments.of(
            Marker.Quadrant,
            new String[] {
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              QUADRANT_BL_FULL + LOWER_HALF.repeat(4)
            }),
        Arguments.of(
            Marker.Sextant,
            new String[] {
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              SEXTANT_BL + SEXTANT_HLINE.repeat(4)
            }),
        Arguments.of(
            Marker.Octant,
            new String[] {
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              row5(LEFT_HALF),
              OCTANT_BL + LOWER_QUARTER.repeat(4)
            }),
        Arguments.of(
            Marker.Dot, new String[] {row5(DOT), row5(DOT), row5(DOT), row5(DOT), DOT.repeat(5)}));
  }

  @ParameterizedTest
  @MethodSource("horizontalWithVerticalCases")
  void test_horizontal_with_vertical(Marker marker, String[] expected) {
    Rect area = new Rect(0, 0, 5, 5);
    Buffer buf = Buffer.filled(area, Cell.of("x"));
    Line horizontalLine = new Line(0.0, 0.0, 10.0, 0.0, Color.RESET);
    Line verticalLine = new Line(0.0, 0.0, 0.0, 10.0, Color.RESET);
    Canvas canvas =
        Canvas.empty()
            .withMarker(marker)
            .withPaintFn(
                ctx -> {
                  ctx.draw(verticalLine);
                  ctx.draw(horizontalLine);
                })
            .withXBounds(new double[] {0.0, 10.0})
            .withYBounds(new double[] {0.0, 10.0});
    canvas.render(area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines(expected));
  }

  // Mirrors upstream `test_diagonal_lines` (rstest cases).
  static Stream<Arguments> diagonalLinesCases() {
    return Stream.of(
        Arguments.of(
            Marker.Block,
            new String[] {
              BLOCK + "xxx" + BLOCK,
              "x" + BLOCK + "x" + BLOCK + "x",
              "xx" + BLOCK + "xx",
              "x" + BLOCK + "x" + BLOCK + "x",
              BLOCK + "xxx" + BLOCK
            }),
        Arguments.of(
            Marker.HalfBlock,
            new String[] {
              BLOCK + "xxx" + BLOCK,
              "x" + BLOCK + "x" + BLOCK + "x",
              "xx" + BLOCK + "xx",
              "x" + BLOCK + "x" + BLOCK + "x",
              BLOCK + "xxx" + BLOCK
            }),
        Arguments.of(
            Marker.Bar,
            new String[] {
              LOWER_HALF + "xxx" + LOWER_HALF,
              "x" + LOWER_HALF + "x" + LOWER_HALF + "x",
              "xx" + LOWER_HALF + "xx",
              "x" + LOWER_HALF + "x" + LOWER_HALF + "x",
              LOWER_HALF + "xxx" + LOWER_HALF
            }),
        Arguments.of(
            Marker.Braille,
            new String[] {
              BRAILLE_DIAG_BACK + "xxx" + BRAILLE_DIAG_FWD,
              "x" + BRAILLE_DIAG_BACK + "x" + BRAILLE_DIAG_FWD + "x",
              "xx" + BRAILLE_FULL + "xx",
              "x" + BRAILLE_DIAG_FWD + "x" + BRAILLE_DIAG_BACK + "x",
              BRAILLE_DIAG_FWD + "xxx" + BRAILLE_DIAG_BACK
            }),
        Arguments.of(
            Marker.Quadrant,
            new String[] {
              QUADRANT_DIAG_BACK + "xxx" + QUADRANT_DIAG_FWD,
              "x" + QUADRANT_DIAG_BACK + "x" + QUADRANT_DIAG_FWD + "x",
              "xx" + BLOCK + "xx",
              "x" + QUADRANT_DIAG_FWD + "x" + QUADRANT_DIAG_BACK + "x",
              QUADRANT_DIAG_FWD + "xxx" + QUADRANT_DIAG_BACK
            }),
        Arguments.of(
            Marker.Octant,
            new String[] {
              QUADRANT_DIAG_BACK + "xxx" + QUADRANT_DIAG_FWD,
              "x" + QUADRANT_DIAG_BACK + "x" + QUADRANT_DIAG_FWD + "x",
              "xx" + BLOCK + "xx",
              "x" + QUADRANT_DIAG_FWD + "x" + QUADRANT_DIAG_BACK + "x",
              QUADRANT_DIAG_FWD + "xxx" + QUADRANT_DIAG_BACK
            }),
        Arguments.of(
            Marker.Dot,
            new String[] {
              DOT + "xxx" + DOT,
              "x" + DOT + "x" + DOT + "x",
              "xx" + DOT + "xx",
              "x" + DOT + "x" + DOT + "x",
              DOT + "xxx" + DOT
            }));
  }

  @ParameterizedTest
  @MethodSource("diagonalLinesCases")
  void test_diagonal_lines(Marker marker, String[] expected) {
    Rect area = new Rect(0, 0, 5, 5);
    Buffer buf = Buffer.filled(area, Cell.of("x"));
    Line diagonalUp = new Line(0.0, 0.0, 10.0, 10.0, Color.RESET);
    Line diagonalDown = new Line(0.0, 10.0, 10.0, 0.0, Color.RESET);
    Canvas canvas =
        Canvas.empty()
            .withMarker(marker)
            .withPaintFn(
                ctx -> {
                  ctx.draw(diagonalDown);
                  ctx.draw(diagonalUp);
                })
            .withXBounds(new double[] {0.0, 10.0})
            .withYBounds(new double[] {0.0, 10.0});
    canvas.render(area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines(expected));
  }

  /// Sextant case for diagonal lines uses supplementary-plane code points; kept as a separate
  /// test since the wide-grapheme symbols are distinct.
  @Test
  void test_diagonal_lines_sextant() {
    Rect area = new Rect(0, 0, 5, 5);
    Buffer buf = Buffer.filled(area, Cell.of("x"));
    Line diagonalUp = new Line(0.0, 0.0, 10.0, 10.0, Color.RESET);
    Line diagonalDown = new Line(0.0, 10.0, 10.0, 0.0, Color.RESET);
    Canvas canvas =
        Canvas.empty()
            .withMarker(Marker.Sextant)
            .withPaintFn(
                ctx -> {
                  ctx.draw(diagonalDown);
                  ctx.draw(diagonalUp);
                })
            .withXBounds(new double[] {0.0, 10.0})
            .withYBounds(new double[] {0.0, 10.0});
    canvas.render(area, buf);
    Buffer expected =
        Buffer.withLines(
            SEXTANT_DIAG_BACK_TOP + "xxx" + SEXTANT_DIAG_FWD_TOP,
            "x" + SEXTANT_DIAG_BACK_TOP + "x" + SEXTANT_DIAG_FWD_TOP + "x",
            "xx" + BLOCK + "xx",
            "x" + SEXTANT_DIAG_BACK_BOT + "x" + SEXTANT_DIAG_FWD_BOT + "x",
            SEXTANT_DIAG_BACK_BOT + "xxx" + SEXTANT_DIAG_FWD_BOT);
    BufferAssertions.assertBufferEq(buf, expected);
  }

  // Mirrors upstream `check_canvas_paint_max` and `check_canvas_paint_overflow` — both sanity
  // checks that the underlying grids do not panic when painted at extreme coordinates.
  @Test
  void check_canvas_paint_max() {
    int max = 0xFFFF;
    assertDoesNotThrow(
        () -> {
          jatatui.widgets.canvas.CanvasGridProbe.paintPatternMax(0xFFFF, 2, 2, 4, 0, 0);
          jatatui.widgets.canvas.CanvasGridProbe.paintPatternMax(0xFFFF, 2, 2, 4, 0, max);
          jatatui.widgets.canvas.CanvasGridProbe.paintPatternMax(0xFFFF, 2, 2, 4, max, 0);
          jatatui.widgets.canvas.CanvasGridProbe.paintPatternMax(0xFFFF, 2, 2, 4, max, max);
          jatatui.widgets.canvas.CanvasGridProbe.paintCharMax(0xFFFF, 2, 0, 0);
          jatatui.widgets.canvas.CanvasGridProbe.paintCharMax(0xFFFF, 2, 0, max);
          jatatui.widgets.canvas.CanvasGridProbe.paintCharMax(0xFFFF, 2, max, 0);
          jatatui.widgets.canvas.CanvasGridProbe.paintCharMax(0xFFFF, 2, max, max);
        });
  }

  @Test
  void check_canvas_paint_overflow() {
    int max = 0xFFFF + 10;
    int huge = Integer.MAX_VALUE;
    assertDoesNotThrow(
        () -> {
          jatatui.widgets.canvas.CanvasGridProbe.paintPatternMax(0xFFFF, 3, 2, 4, max, max);
          jatatui.widgets.canvas.CanvasGridProbe.paintCharMax(0xFFFF, 3, max, max);
          jatatui.widgets.canvas.CanvasGridProbe.paintPatternMax(0xFFFF, 3, 2, 4, huge, huge);
          jatatui.widgets.canvas.CanvasGridProbe.paintCharMax(0xFFFF, 3, huge, huge);
        });
  }

  // Mirrors upstream `render_in_minimal_buffer` — a 1x1 buffer must not panic.
  @Test
  void render_in_minimal_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 1, 1));
    Canvas canvas =
        Canvas.empty()
            .withXBounds(new double[] {0.0, 10.0})
            .withYBounds(new double[] {0.0, 10.0})
            .withPaintFn(ctx -> {});
    canvas.render(buffer.area(), buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines(" "));
  }

  // Mirrors upstream `render_in_zero_size_buffer` — a Rect.ZERO area must not panic.
  @Test
  void render_in_zero_size_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 0));
    Canvas canvas =
        Canvas.empty()
            .withXBounds(new double[] {0.0, 10.0})
            .withYBounds(new double[] {0.0, 10.0})
            .withPaintFn(ctx -> {});
    assertDoesNotThrow(() -> canvas.render(buffer.area(), buffer));
  }
}
