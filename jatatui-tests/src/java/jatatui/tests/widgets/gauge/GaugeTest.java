package jatatui.tests.widgets.gauge;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.widgets.gauge.Gauge;
import org.junit.jupiter.api.Test;

public class GaugeTest {

  @Test
  public void gauge_invalid_percentage() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> Gauge.empty().withPercent(110));
    assertEquals("Percentage should be between 0 and 100 inclusively.", ex.getMessage());
  }

  @Test
  public void gauge_invalid_ratio_upper_bound() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> Gauge.empty().withRatio(1.1));
    assertEquals("Ratio should be between 0 and 1 inclusively.", ex.getMessage());
  }

  @Test
  public void gauge_invalid_ratio_lower_bound() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> Gauge.empty().withRatio(-0.5));
    assertEquals("Ratio should be between 0 and 1 inclusively.", ex.getMessage());
  }

  @Test
  public void gauge_can_be_stylized() {
    Gauge styled = Gauge.empty().black().onWhite().bold().notDim();
    Style expected =
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM);
    assertEquals(expected, styled.style());
  }

  @Test
  public void render_in_minimal_buffer_gauge() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    Gauge gauge = Gauge.empty().withPercent(50);
    // Should not throw.
    gauge.render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines("5"));
  }

  @Test
  public void render_in_zero_size_buffer_gauge() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    Gauge gauge = Gauge.empty().withPercent(50);
    // Should not throw.
    gauge.render(buf.area(), buf);
  }
}
