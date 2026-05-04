package jatatui.tests.widgets.barchart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.text.Line;
import jatatui.widgets.barchart.Bar;
import jatatui.widgets.barchart.BarGroup;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BarGroupTest {

  @Test
  void test_bargroup_new() {
    BarGroup group =
        BarGroup.of(List.of(Bar.withLabel("Label1", 1), Bar.withLabel("Label2", 2)))
            .withLabel(Line.from("Group1"));
    assertEquals(Optional.of(Line.from("Group1")), group.label);
    assertEquals(2, group.bars.size());
  }
}
