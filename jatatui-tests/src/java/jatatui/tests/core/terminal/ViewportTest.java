package jatatui.tests.core.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.Rect;
import jatatui.core.terminal.Viewport;
import org.junit.jupiter.api.Test;

/// Ports the inline tests in `submodules/ratatui/ratatui-core/src/terminal/viewport.rs`.
public class ViewportTest {

  @Test
  public void viewport_to_string() {
    assertEquals("Fullscreen", Viewport.fullscreen().toString());
    assertEquals("Inline(5)", Viewport.inline(5).toString());
    assertEquals("Fixed(5x5+0+0)", Viewport.fixed(new Rect(0, 0, 5, 5)).toString());
  }
}
