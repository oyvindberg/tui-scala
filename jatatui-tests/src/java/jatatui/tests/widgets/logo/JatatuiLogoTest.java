package jatatui.tests.widgets.logo;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.widgets.JatatuiLogo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class JatatuiLogoTest {

  @ParameterizedTest
  @EnumSource(JatatuiLogo.Size.class)
  public void new_size(JatatuiLogo.Size size) {
    JatatuiLogo logo = JatatuiLogo.of(size);
    assertEquals(size, logo.size);
  }

  @Test
  public void default_logo_is_tiny() {
    // Java doesn't have Default; mirror by using the canonical tiny() factory.
    JatatuiLogo logo = JatatuiLogo.tiny();
    assertEquals(JatatuiLogo.Size.Tiny, logo.size);
  }

  @Test
  public void set_logo_size_to_small() {
    JatatuiLogo logo = JatatuiLogo.tiny().withSize(JatatuiLogo.Size.Small);
    assertEquals(JatatuiLogo.Size.Small, logo.size);
  }

  @Test
  public void tiny_logo_constant() {
    JatatuiLogo logo = JatatuiLogo.tiny();
    assertEquals(JatatuiLogo.Size.Tiny, logo.size);
  }

  @Test
  public void small_logo_constant() {
    JatatuiLogo logo = JatatuiLogo.small();
    assertEquals(JatatuiLogo.Size.Small, logo.size);
  }

  @Test
  public void render_tiny() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 2));
    JatatuiLogo.tiny().render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines("▀█▗▀▖▜▘▞▚▝▛▐ ▌▌", "▄▛▐▀▌▐ ▛▜ ▌▝▄▘▌"));
  }

  @Test
  public void render_small() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 27, 2));
    JatatuiLogo.small().render(buf.area(), buf);
    assertBufferEq(
        buf, Buffer.withLines("▀▀▀█ ▄▀▀▄▝▜▛▘▄▀▀▄▝▜▛▘█  █ █", "▄▄▄▀ █▀▀█ ▐▌ █▀▀█ ▐▌ ▀▄▄▀ █"));
  }

  @Test
  public void render_in_minimal_buffer_tiny() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    JatatuiLogo.tiny().render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines("▀"));
  }

  @Test
  public void render_in_minimal_buffer_small() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    JatatuiLogo.small().render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines("▀"));
  }

  @ParameterizedTest
  @EnumSource(JatatuiLogo.Size.class)
  public void render_in_zero_size_buffer(JatatuiLogo.Size size) {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    JatatuiLogo logo = JatatuiLogo.of(size);
    // Should not throw.
    logo.render(buf.area(), buf);
  }
}
