package jatatui.tests.widgets.sparkline;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Shade;
import jatatui.widgets.sparkline.RenderDirection;
import jatatui.widgets.sparkline.Sparkline;
import jatatui.widgets.sparkline.SparklineBar;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class SparklineTest {

  private static Buffer renderXFilled(Sparkline widget, int width) {
    Rect area = new Rect(0, 0, width, 1);
    Buffer buf = Buffer.filled(area, Cell.of("x"));
    widget.render(area, buf);
    return buf;
  }

  @Test
  public void it_can_be_created_from_list_of_long() {
    List<SparklineBar> data = Sparkline.empty().withDataLongs(1, 2, 3).data();
    List<SparklineBar> expected =
        List.of(SparklineBar.of(1), SparklineBar.of(2), SparklineBar.of(3));
    assertEquals(expected, data);
  }

  @Test
  public void it_can_be_created_from_list_of_optional_long() {
    List<Optional<Long>> input = new ArrayList<>();
    input.add(Optional.of(1L));
    input.add(Optional.empty());
    input.add(Optional.of(3L));
    List<SparklineBar> data = Sparkline.empty().withDataOptionals(input).data();
    List<SparklineBar> expected =
        List.of(SparklineBar.of(1L), SparklineBar.absent(), SparklineBar.of(3L));
    assertEquals(expected, data);
  }

  @Test
  public void it_does_not_panic_if_max_is_zero() {
    Sparkline widget = Sparkline.empty().withDataLongs(0, 0, 0);
    Buffer buf = renderXFilled(widget, 6);
    assertBufferEq(buf, Buffer.withLines("   xxx"));
  }

  @Test
  public void it_does_not_panic_if_max_is_set_to_zero() {
    Sparkline widget = Sparkline.empty().withDataLongs(0, 1, 2).withMax(0);
    Buffer buf = renderXFilled(widget, 6);
    assertBufferEq(buf, Buffer.withLines("   xxx"));
  }

  @Test
  public void it_draws() {
    Sparkline widget = Sparkline.empty().withDataLongs(0, 1, 2, 3, 4, 5, 6, 7, 8);
    Buffer buf = renderXFilled(widget, 12);
    assertBufferEq(buf, Buffer.withLines(" ▁▂▃▄▅▆▇█xxx"));
  }

  @Test
  public void it_draws_double_height() {
    Sparkline widget = Sparkline.empty().withDataLongs(0, 1, 2, 3, 4, 5, 6, 7, 8);
    Rect area = new Rect(0, 0, 12, 2);
    Buffer buf = Buffer.filled(area, Cell.of("x"));
    widget.render(area, buf);
    assertBufferEq(buf, Buffer.withLines("     ▂▄▆█xxx", " ▂▄▆█████xxx"));
  }

  @Test
  public void it_renders_left_to_right() {
    Sparkline widget =
        Sparkline.empty()
            .withDataLongs(0, 1, 2, 3, 4, 5, 6, 7, 8)
            .withDirection(RenderDirection.LeftToRight);
    Buffer buf = renderXFilled(widget, 12);
    assertBufferEq(buf, Buffer.withLines(" ▁▂▃▄▅▆▇█xxx"));
  }

  @Test
  public void it_renders_right_to_left() {
    Sparkline widget =
        Sparkline.empty()
            .withDataLongs(0, 1, 2, 3, 4, 5, 6, 7, 8)
            .withDirection(RenderDirection.RightToLeft);
    Buffer buf = renderXFilled(widget, 12);
    assertBufferEq(buf, Buffer.withLines("xxx█▇▆▅▄▃▂▁ "));
  }

  @Test
  public void it_renders_with_absent_value_style() {
    List<Optional<Long>> data = new ArrayList<>();
    data.add(Optional.empty());
    for (long i = 1; i <= 8; i++) data.add(Optional.of(i));
    Sparkline widget =
        Sparkline.empty()
            .withAbsentValueStyle(Style.empty().withFg(Color.RED))
            .withAbsentValueSymbol(Shade.FULL)
            .withDataOptionals(data);
    Buffer buf = renderXFilled(widget, 12);
    Buffer expected = Buffer.withLines("█▁▂▃▄▅▆▇█xxx");
    expected.setStyle(new Rect(0, 0, 1, 1), Style.empty().withFg(Color.RED));
    assertBufferEq(buf, expected);
  }

  @Test
  public void it_renders_with_custom_absent_value_symbol() {
    List<Optional<Long>> data = new ArrayList<>();
    data.add(Optional.empty());
    for (long i = 1; i <= 8; i++) data.add(Optional.of(i));
    Sparkline widget = Sparkline.empty().withAbsentValueSymbol("*").withDataOptionals(data);
    Buffer buf = renderXFilled(widget, 12);
    assertBufferEq(buf, Buffer.withLines("*▁▂▃▄▅▆▇█xxx"));
  }

  @Test
  public void it_renders_with_custom_bar_styles() {
    List<SparklineBar> bars = new ArrayList<>();
    bars.add(SparklineBar.of(0).withStyle(Style.empty().withFg(Color.RED)));
    bars.add(SparklineBar.of(1).withStyle(Style.empty().withFg(Color.RED)));
    bars.add(SparklineBar.of(2).withStyle(Style.empty().withFg(Color.RED)));
    bars.add(SparklineBar.of(3).withStyle(Style.empty().withFg(Color.GREEN)));
    bars.add(SparklineBar.of(4).withStyle(Style.empty().withFg(Color.GREEN)));
    bars.add(SparklineBar.of(5).withStyle(Style.empty().withFg(Color.GREEN)));
    bars.add(SparklineBar.of(6).withStyle(Style.empty().withFg(Color.BLUE)));
    bars.add(SparklineBar.of(7).withStyle(Style.empty().withFg(Color.BLUE)));
    bars.add(SparklineBar.of(8).withStyle(Style.empty().withFg(Color.BLUE)));
    Sparkline widget = Sparkline.empty().withData(bars);
    Buffer buf = renderXFilled(widget, 12);
    Buffer expected = Buffer.withLines(" ▁▂▃▄▅▆▇█xxx");
    expected.setStyle(new Rect(0, 0, 3, 1), Style.empty().withFg(Color.RED));
    expected.setStyle(new Rect(3, 0, 3, 1), Style.empty().withFg(Color.GREEN));
    expected.setStyle(new Rect(6, 0, 3, 1), Style.empty().withFg(Color.BLUE));
    assertBufferEq(buf, expected);
  }

  @Test
  public void can_be_stylized() {
    Sparkline styled = Sparkline.empty().black().onWhite().bold().notDim();
    Style expected =
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM);
    assertEquals(expected, styled.style());
  }

  @Test
  public void render_in_minimal_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    Sparkline sparkline =
        Sparkline.empty().withDataLongs(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).withMax(10);
    // Should not throw.
    sparkline.render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines(" "));
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    Sparkline sparkline =
        Sparkline.empty().withDataLongs(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).withMax(10);
    // Should not throw.
    sparkline.render(buf.area(), buf);
  }
}
