package jatatui.tests._support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import org.junit.jupiter.api.Test;

/// Smoke tests for [BufferAssertions], mirroring the inline tests in upstream
/// `ratatui_core::buffer::assert`.
public class BufferAssertionsTest {

  @Test
  public void assert_buffer_eq_does_not_panic_on_equal_buffers() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 1));
    Buffer otherBuffer = Buffer.empty(new Rect(0, 0, 5, 1));
    assertDoesNotThrow(() -> BufferAssertions.assertBufferEq(buffer, otherBuffer));
  }

  @Test
  public void assert_buffer_eq_panics_on_unequal_area() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 1));
    Buffer otherBuffer = Buffer.empty(new Rect(0, 0, 6, 1));
    AssertionError err =
        assertThrows(AssertionError.class, () -> BufferAssertions.assertBufferEq(buffer, otherBuffer));
    assertTrue(err.getMessage().contains("buffer areas not equal"), err.getMessage());
  }

  @Test
  public void assert_buffer_eq_panics_on_unequal_style() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 5, 1));
    Buffer otherBuffer = Buffer.empty(new Rect(0, 0, 5, 1));
    otherBuffer.setString(0, 0, " ", Style.empty().withFg(Color.RED));
    AssertionError err =
        assertThrows(AssertionError.class, () -> BufferAssertions.assertBufferEq(buffer, otherBuffer));
    assertTrue(err.getMessage().contains("buffer contents not equal"), err.getMessage());
  }
}
