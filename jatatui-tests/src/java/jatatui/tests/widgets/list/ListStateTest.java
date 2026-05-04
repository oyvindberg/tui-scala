package jatatui.tests.widgets.list;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.widgets.list.ListState;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Tests for [ListState], ported from `ratatui-widgets/src/list/state.rs` (upstream tests).
public class ListStateTest {

  @Test
  public void selected() {
    ListState state = ListState.empty();
    assertEquals(Optional.empty(), state.selected());

    state.select(Optional.of(1));
    assertEquals(Optional.of(1), state.selected());

    state.select(Optional.empty());
    assertEquals(Optional.empty(), state.selected());
  }

  @Test
  public void select() {
    ListState state = ListState.empty();
    assertEquals(Optional.empty(), state.selected());
    assertEquals(0, state.offset());

    state.select(Optional.of(2));
    assertEquals(Optional.of(2), state.selected());
    assertEquals(0, state.offset());

    state.select(Optional.empty());
    assertEquals(Optional.empty(), state.selected());
    assertEquals(0, state.offset());
  }

  @Test
  public void state_navigation() {
    ListState state = ListState.empty();
    state.selectFirst();
    assertEquals(Optional.of(0), state.selected());

    state.selectPrevious(); // should not go below 0
    assertEquals(Optional.of(0), state.selected());

    state.selectNext();
    assertEquals(Optional.of(1), state.selected());

    state.selectPrevious();
    assertEquals(Optional.of(0), state.selected());

    state.selectLast();
    assertEquals(Optional.of(Integer.MAX_VALUE), state.selected());

    state.selectNext(); // should not go above Integer.MAX_VALUE
    assertEquals(Optional.of(Integer.MAX_VALUE), state.selected());

    state.selectPrevious();
    assertEquals(Optional.of(Integer.MAX_VALUE - 1), state.selected());

    state.selectNext();
    assertEquals(Optional.of(Integer.MAX_VALUE), state.selected());

    state = ListState.empty();
    state.selectNext();
    assertEquals(Optional.of(0), state.selected());

    state = ListState.empty();
    state.selectPrevious();
    assertEquals(Optional.of(Integer.MAX_VALUE), state.selected());

    state = ListState.empty();
    state.select(Optional.of(2));
    state.scrollDownBy(4);
    assertEquals(Optional.of(6), state.selected());

    state = ListState.empty();
    state.scrollUpBy(3);
    assertEquals(Optional.of(0), state.selected());

    state.select(Optional.of(6));
    state.scrollUpBy(4);
    assertEquals(Optional.of(2), state.selected());

    state.scrollUpBy(4);
    assertEquals(Optional.of(0), state.selected());
  }
}
