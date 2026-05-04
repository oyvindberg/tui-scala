package jatatui.tests.core.widgets;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.widgets.StatefulWidget;
import jatatui.core.widgets.Widget;
import jatatui.tests._support.BufferAssertions;
import org.junit.jupiter.api.Test;

/// Ports the inline tests in `submodules/ratatui/ratatui-core/src/widgets/stateful_widget.rs`.
public class StatefulWidgetTest {

  /// Test fixture: an empty 20x1 buffer (mirrors upstream's `#[fixture] fn buf()`).
  private static Buffer buf() {
    return Buffer.empty(new Rect(0, 0, 20, 1));
  }

  /// Test fixture: the initial mutable state "world".
  ///
  /// Upstream uses `String` directly because Rust strings are mutable through `&mut String`. Java
  /// `String` is immutable, so we use a [StringBuilder] for the personal-greeting test and a
  /// `byte[]` for the unsized-state test.
  private static StringBuilder state() {
    return new StringBuilder("world");
  }

  /// A stateful widget that renders "Hello {state}" where state is a [StringBuilder].
  private static final class PersonalGreeting implements StatefulWidget<StringBuilder> {
    @Override
    public void render(Rect area, Buffer buf, StringBuilder state) {
      Widget.renderString("Hello " + state, area, buf);
    }
  }

  @Test
  public void render() {
    Buffer buf = buf();
    StringBuilder state = state();
    StatefulWidget<StringBuilder> widget = new PersonalGreeting();
    widget.render(buf.area, buf, state);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("Hello world         "));
  }

  /// A stateful widget with an "unsized" state type — upstream uses `type State = [u8]`. In Java
  /// the closest equivalent is `byte[]`.
  private static final class Bytes implements StatefulWidget<byte[]> {
    @Override
    public void render(Rect area, Buffer buf, byte[] state) {
      String slice = new String(state, java.nio.charset.StandardCharsets.UTF_8);
      Widget.renderString("Bytes: " + slice, area, buf);
    }
  }

  @Test
  public void render_unsized_state_type() {
    Buffer buf = buf();
    Bytes widget = new Bytes();
    byte[] state = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    widget.render(buf.area, buf, state);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("Bytes: hello        "));
  }
}
