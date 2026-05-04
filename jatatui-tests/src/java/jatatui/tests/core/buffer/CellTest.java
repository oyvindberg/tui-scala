package jatatui.tests.core.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.buffer.Cell;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Ports the inline tests in `submodules/ratatui/ratatui-core/src/buffer/cell.rs`.
public class CellTest {

  @Test
  public void newCell() {
    Cell cell = Cell.of("あ");
    assertEquals(Optional.of("あ"), cell.rawSymbol());
    assertEquals(Color.RESET, cell.fg);
    assertEquals(Color.RESET, cell.bg);
    assertEquals(Color.RESET, cell.underlineColor);
    assertEquals(Modifier.EMPTY, cell.modifier);
    assertFalse(cell.skip);
  }

  @Test
  public void empty() {
    Cell cell = Cell.empty();
    assertEquals(" ", cell.symbol());
  }

  @Test
  public void set_symbol() {
    Cell cell = Cell.empty();
    cell.setSymbol("あ"); // Multi-byte character
    assertEquals("あ", cell.symbol());
    cell.setSymbol("👨‍👩‍👧‍👦"); // ZWJ family sequence
    assertEquals("👨‍👩‍👧‍👦", cell.symbol());
  }

  @Test
  public void append_symbol() {
    Cell cell = Cell.empty();
    cell.setSymbol("あ"); // Multi-byte character
    // appendSymbol is package-private — use setSymbol with the concatenation instead, mirroring
    // the upstream test's observable result.
    cell.setSymbol(cell.symbol() + "​"); // zero-width space
    assertEquals("あ​", cell.symbol());
  }

  @Test
  public void set_char() {
    Cell cell = Cell.empty();
    cell.setChar('あ'); // BMP character (Unicode code unit fits in a Java char)
    assertEquals("あ", cell.symbol());
  }

  @Test
  public void set_fg() {
    Cell cell = Cell.empty();
    cell.setFg(Color.RED);
    assertEquals(Color.RED, cell.fg);
  }

  @Test
  public void set_bg() {
    Cell cell = Cell.empty();
    cell.setBg(Color.RED);
    assertEquals(Color.RED, cell.bg);
  }

  @Test
  public void set_style() {
    Cell cell = Cell.empty();
    cell.setStyle(Style.empty().withFg(Color.RED).withBg(Color.BLUE));
    assertEquals(Color.RED, cell.fg);
    assertEquals(Color.BLUE, cell.bg);
  }

  @Test
  public void set_skip() {
    Cell cell = Cell.empty();
    cell.setSkip(true);
    assertTrue(cell.skip);
  }

  @Test
  public void reset() {
    Cell cell = Cell.empty();
    cell.setSymbol("あ");
    cell.setFg(Color.RED);
    cell.setBg(Color.BLUE);
    cell.setSkip(true);
    cell.reset();
    assertEquals(" ", cell.symbol());
    assertEquals(Color.RESET, cell.fg);
    assertEquals(Color.RESET, cell.bg);
    assertFalse(cell.skip);
  }

  @Test
  public void style() {
    Cell cell = Cell.empty();
    Style expected =
        new Style(
            Optional.of(Color.RESET),
            Optional.of(Color.RESET),
            Optional.of(Color.RESET),
            Modifier.EMPTY,
            Modifier.EMPTY);
    assertEquals(expected, cell.style());
  }

  @Test
  public void defaultCell() {
    Cell cell = Cell.empty();
    assertEquals(" ", cell.symbol());
  }

  @Test
  public void cell_eq() {
    Cell cell1 = Cell.of("あ");
    Cell cell2 = Cell.of("あ");
    assertEquals(cell1, cell2);
  }

  @Test
  public void cell_ne() {
    Cell cell1 = Cell.of("あ");
    Cell cell2 = Cell.of("い");
    assertNotEquals(cell1, cell2);
  }

  /// Tests that a cell with no explicit symbol equals a cell whose symbol was explicitly set to
  /// a single space (mirrors the upstream `PartialEq` documentation).
  @Test
  public void cell_eq_empty_vs_space() {
    Cell cell1 = Cell.empty();
    Cell cell2 = Cell.empty().setSymbol(" ");
    assertEquals(cell1, cell2);
    assertEquals(cell1.hashCode(), cell2.hashCode());
  }
}
