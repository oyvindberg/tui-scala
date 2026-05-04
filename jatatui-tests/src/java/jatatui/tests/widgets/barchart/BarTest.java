package jatatui.tests.widgets.barchart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.widgets.barchart.Bar;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BarTest {

  @Test
  void test_bar_new() {
    Bar bar = Bar.of(42).withLabel(Line.from("Label"));
    assertEquals(Optional.of(Line.from("Label")), bar.label);
    assertEquals(42L, bar.value);
  }

  @Test
  void test_bar_with_label() {
    Bar bar = Bar.withLabel("Label", 42);
    assertEquals(Optional.of(Line.from("Label")), bar.label);
    assertEquals(42L, bar.value);
  }

  @Test
  void test_bar_stylized() {
    Bar bar = Bar.empty().red().bold();
    assertEquals(Style.empty().withFg(Color.RED).withAddModifier(Modifier.BOLD), bar.style);
  }
}
