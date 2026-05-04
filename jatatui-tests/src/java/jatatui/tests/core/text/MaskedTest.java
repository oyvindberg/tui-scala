package jatatui.tests.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.text.Line;
import jatatui.core.text.Masked;
import jatatui.core.text.Text;
import java.util.List;
import org.junit.jupiter.api.Test;

/// Ports of the inline tests from `ratatui-core/src/text/masked.rs`.
///
/// **Skipped**: the Rust `debug` and `display` truncation tests (`{:.3}`, `{:.3?}`) — Java has no
/// equivalent format specifiers and there is no point asserting the same. The `Cow` round-trip
/// `into_cow` test is N/A: Java has no `Cow`, and the equivalent contract is `value()` returning
/// a `String`, which is already covered by [#value()].
public class MaskedTest {

  @Test
  public void new_() {
    Masked masked = Masked.of("12345", 'x');
    assertEquals("12345", masked.inner());
    assertEquals('x', masked.maskChar());
  }

  @Test
  public void value() {
    Masked masked = Masked.of("12345", 'x');
    assertEquals("xxxxx", masked.value());
  }

  @Test
  public void mask_char() {
    Masked masked = Masked.of("12345", 'x');
    assertEquals('x', masked.maskChar());
  }

  @Test
  public void debug() {
    // Rust's Debug for Masked prints the inner string. Java's toString shows the masked value
    // (matches Display in upstream); we assert that here as the documented Java behavior.
    Masked masked = Masked.of("12345", 'x');
    assertEquals("xxxxx", masked.toString());
  }

  @Test
  public void display() {
    Masked masked = Masked.of("12345", 'x');
    assertEquals("xxxxx", masked.toString());
  }

  @Test
  public void into_text() {
    Masked masked = Masked.of("12345", 'x');

    Text text = Text.from(masked);
    assertEquals(List.of(Line.from("xxxxx")), text.lines);
  }
}
