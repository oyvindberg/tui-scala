package jatatui.tests.core.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.backend.TestBackend;
import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.widgets.StatefulWidget;
import jatatui.core.widgets.Widget;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/// Tests for [Frame]. Upstream `ratatui-core/src/terminal/frame.rs` has no inline `#[cfg(test)]`
/// block; these tests cover the Java port's surface — area accessors, widget/stateful-widget
/// rendering, cursor positioning and frame counting — using a [TestBackend].
public class FrameTest {

  @Test
  public void frame_area_returns_viewport_area() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(10, 10));
    Frame frame = terminal.getFrame();
    assertEquals(new Rect(0, 0, 10, 10), frame.area());
  }

  @Test
  public void frame_count_is_zero_before_any_draw() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(10, 10));
    Frame frame = terminal.getFrame();
    assertEquals(0, frame.count());
  }

  @Test
  public void frame_render_widget_writes_to_buffer() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(10, 1));
    Frame frame = terminal.getFrame();
    frame.renderWidget(
        (area, buf) -> Widget.renderString("Hello", area, buf), new Rect(0, 0, 10, 1));
    Buffer buffer = frame.bufferMut();
    assertEquals("H", buffer.cellAt(0, 0).symbol());
    assertEquals("e", buffer.cellAt(1, 0).symbol());
    assertEquals("l", buffer.cellAt(2, 0).symbol());
    assertEquals("l", buffer.cellAt(3, 0).symbol());
    assertEquals("o", buffer.cellAt(4, 0).symbol());
  }

  @Test
  public void frame_render_stateful_widget_passes_state() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(10, 1));
    Frame frame = terminal.getFrame();
    StatefulWidget<StringBuilder> widget =
        (area, buf, state) -> buf.setString(area.x(), area.y(), state.toString(), Style.empty());
    StringBuilder state = new StringBuilder("Hi!");
    frame.renderStatefulWidget(widget, new Rect(0, 0, 10, 1), state);
    assertEquals("H", frame.bufferMut().cellAt(0, 0).symbol());
    assertEquals("i", frame.bufferMut().cellAt(1, 0).symbol());
    assertEquals("!", frame.bufferMut().cellAt(2, 0).symbol());
  }

  @Test
  public void frame_set_cursor_position_with_position_record() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(10, 5));
    terminal.draw(f -> f.setCursorPosition(new Position(3, 2)));
    assertEquals(new Position(3, 2), terminal.backend().pos());
    assertEquals(true, terminal.backend().cursorShown());
  }

  @Test
  public void frame_set_cursor_position_with_xy_overload() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(10, 5));
    terminal.draw(f -> f.setCursorPosition(7, 4));
    assertEquals(new Position(7, 4), terminal.backend().pos());
  }

  @Test
  public void frame_buffer_mut_returns_writable_reference() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(5, 1));
    Frame frame = terminal.getFrame();
    Buffer buffer = frame.bufferMut();
    buffer.setString(0, 0, "Hello", Style.empty());
    assertEquals("H", frame.bufferMut().cellAt(0, 0).symbol());
  }
}
