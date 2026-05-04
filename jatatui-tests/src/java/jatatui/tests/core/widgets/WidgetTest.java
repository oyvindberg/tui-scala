package jatatui.tests.core.widgets;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.widgets.Widget;
import jatatui.tests._support.BufferAssertions;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Ports the inline tests in `submodules/ratatui/ratatui-core/src/widgets/widget.rs`.
///
/// The upstream tests use `Line::from(...).render(area, buf)` to populate buffers; in Java this
/// is the same as `Widget.renderString(...)` because [Widget#renderString(String,Rect,Buffer)]
/// delegates to `buf.setStringn(area.x, area.y, s, area.width, Style::new())` — the exact
/// behaviour of `impl Widget for &str` upstream.
public class WidgetTest {

  /// Test fixture: an empty 20x1 buffer (mirrors upstream's `#[fixture] fn buf()`).
  private static Buffer buf() {
    return Buffer.empty(new Rect(0, 0, 20, 1));
  }

  /// Test fixture widget: renders the literal string "Hello".
  private static final class Greeting implements Widget {
    @Override
    public void render(Rect area, Buffer buf) {
      Widget.renderString("Hello", area, buf);
    }
  }

  @Test
  public void render() {
    Buffer buf = buf();
    Widget widget = new Greeting();
    widget.render(buf.area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("Hello               "));
  }

  @Test
  public void render_str() {
    Buffer buf = buf();
    Widget.renderString("hello world", buf.area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("hello world         "));
  }

  @Test
  public void render_str_truncate() {
    Buffer buf = buf();
    Rect area = new Rect(buf.area.x(), buf.area.y(), 11, buf.area.height());
    Widget.renderString("hello world, just hello", area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("hello world         "));
  }

  @Test
  public void render_option_str() {
    Buffer buf = buf();
    Widget.renderOptional(Optional.of(stringWidget("hello world")), buf.area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("hello world         "));
  }

  @Test
  public void render_option_str_empty() {
    Buffer buf = buf();
    Widget.renderOptional(Optional.empty(), buf.area, buf);
    // Empty optional renders nothing; buffer remains untouched.
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("                    "));
  }

  @Test
  public void render_string() {
    Buffer buf = buf();
    String s = "hello world";
    Widget.renderString(s, buf.area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("hello world         "));
  }

  @Test
  public void render_string_truncate() {
    Buffer buf = buf();
    Rect area = new Rect(buf.area.x(), buf.area.y(), 11, buf.area.height());
    String s = "hello world, just hello";
    Widget.renderString(s, area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("hello world         "));
  }

  @Test
  public void render_option_string() {
    Buffer buf = buf();
    Optional<Widget> w = Optional.of(stringWidget("hello world"));
    Widget.renderOptional(w, buf.area, buf);
    BufferAssertions.assertBufferEq(buf, Buffer.withLines("hello world         "));
  }

  /// Wraps a string as a [Widget] (the Java equivalent of upstream's `impl Widget for String`).
  private static Widget stringWidget(String s) {
    return (area, buf) -> Widget.renderString(s, area, buf);
  }
}
