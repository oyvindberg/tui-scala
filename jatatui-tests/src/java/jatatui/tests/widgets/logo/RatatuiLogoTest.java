package jatatui.tests.widgets.logo;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.widgets.RatatuiLogo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RatatuiLogoTest {

  @ParameterizedTest
  @EnumSource(RatatuiLogo.Size.class)
  public void new_size(RatatuiLogo.Size size) {
    RatatuiLogo logo = RatatuiLogo.of(size);
    assertEquals(size, logo.size);
  }

  @Test
  public void default_logo_is_tiny() {
    // Java doesn't have Default; mirror by using the canonical tiny() factory.
    RatatuiLogo logo = RatatuiLogo.tiny();
    assertEquals(RatatuiLogo.Size.Tiny, logo.size);
  }

  @Test
  public void set_logo_size_to_small() {
    RatatuiLogo logo = RatatuiLogo.tiny().withSize(RatatuiLogo.Size.Small);
    assertEquals(RatatuiLogo.Size.Small, logo.size);
  }

  @Test
  public void tiny_logo_constant() {
    RatatuiLogo logo = RatatuiLogo.tiny();
    assertEquals(RatatuiLogo.Size.Tiny, logo.size);
  }

  @Test
  public void small_logo_constant() {
    RatatuiLogo logo = RatatuiLogo.small();
    assertEquals(RatatuiLogo.Size.Small, logo.size);
  }

  @Test
  public void render_tiny() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 15, 2));
    RatatuiLogo.tiny().render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines("▛▚▗▀▖▜▘▞▚▝▛▐ ▌▌", "▛▚▐▀▌▐ ▛▜ ▌▝▄▘▌"));
  }

  @Test
  public void render_small() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 27, 2));
    RatatuiLogo.small().render(buf.area(), buf);
    assertBufferEq(
        buf,
        Buffer.withLines(
            "█▀▀▄ ▄▀▀▄▝▜▛▘▄▀▀▄▝▜▛▘█  █ █", "█▀▀▄ █▀▀█ ▐▌ █▀▀█ ▐▌ ▀▄▄▀ █"));
  }

  @Test
  public void render_in_minimal_buffer_tiny() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    RatatuiLogo.tiny().render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines("▛"));
  }

  @Test
  public void render_in_minimal_buffer_small() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    RatatuiLogo.small().render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines("█"));
  }

  @ParameterizedTest
  @EnumSource(RatatuiLogo.Size.class)
  public void render_in_zero_size_buffer(RatatuiLogo.Size size) {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    RatatuiLogo logo = RatatuiLogo.of(size);
    // Should not throw.
    logo.render(buf.area(), buf);
  }
}
