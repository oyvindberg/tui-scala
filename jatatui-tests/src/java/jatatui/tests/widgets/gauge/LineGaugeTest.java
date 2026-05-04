package jatatui.tests.widgets.gauge;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Line;
import jatatui.widgets.gauge.LineGauge;
import org.junit.jupiter.api.Test;

public class LineGaugeTest {

  @Test
  public void line_gauge_can_be_stylized() {
    LineGauge styled = LineGauge.empty().black().onWhite().bold().notDim();
    Style expected =
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM);
    assertEquals(expected, styled.style());
  }

  @Test
  public void line_gauge_set_filled_symbol() {
    assertEquals("▰", LineGauge.empty().withFilledSymbol("▰").filledSymbol());
  }

  @Test
  public void line_gauge_set_unfilled_symbol() {
    assertEquals("▱", LineGauge.empty().withUnfilledSymbol("▱").unfilledSymbol());
  }

  @Test
  public void line_gauge_default() {
    LineGauge expected =
        LineGauge.empty()
            .withRatio(0.0)
            .withFilledSymbol(Line.HORIZONTAL)
            .withUnfilledSymbol(Line.HORIZONTAL);
    LineGauge actual = LineGauge.empty();
    assertEquals(expected.filledSymbol(), actual.filledSymbol());
    assertEquals(expected.unfilledSymbol(), actual.unfilledSymbol());
    assertEquals(expected.style(), actual.style());
    assertEquals(expected.filledStyle(), actual.filledStyle());
    assertEquals(expected.unfilledStyle(), actual.unfilledStyle());
  }

  @Test
  public void render_in_minimal_buffer_line_gauge() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    LineGauge lineGauge = LineGauge.empty().withRatio(0.5);
    // Should not throw.
    lineGauge.render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines(" "));
  }

  @Test
  public void render_in_zero_size_buffer_line_gauge() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    LineGauge lineGauge = LineGauge.empty().withRatio(0.5);
    // Should not throw.
    lineGauge.render(buf.area(), buf);
  }
}
