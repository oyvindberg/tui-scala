package jatatui.tests.widgets.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.widgets.table.HighlightSpacing;
import org.junit.jupiter.api.Test;

/// Inline tests from `ratatui_widgets::table::highlight_spacing`.
///
/// Mapped tests:
/// - `to_string` - port of upstream `to_string` (uses Java enum `name()` instead of `Display`).
/// - `from_str` - N/A: Rust uses `strum::EnumString` to derive `from_str`. Java enums use
///   `valueOf` for the same purpose; we test that path here.
public class HighlightSpacingTest {

  @Test
  public void to_string() {
    assertEquals("Always", HighlightSpacing.Always.name());
    assertEquals("WhenSelected", HighlightSpacing.WhenSelected.name());
    assertEquals("Never", HighlightSpacing.Never.name());
  }

  @Test
  public void from_str() {
    assertEquals(HighlightSpacing.Always, HighlightSpacing.valueOf("Always"));
    assertEquals(HighlightSpacing.WhenSelected, HighlightSpacing.valueOf("WhenSelected"));
    assertEquals(HighlightSpacing.Never, HighlightSpacing.valueOf("Never"));
  }

  @Test
  public void should_add() {
    assertTrue(HighlightSpacing.Always.shouldAdd(false));
    assertTrue(HighlightSpacing.Always.shouldAdd(true));
    assertFalse(HighlightSpacing.WhenSelected.shouldAdd(false));
    assertTrue(HighlightSpacing.WhenSelected.shouldAdd(true));
    assertFalse(HighlightSpacing.Never.shouldAdd(false));
    assertFalse(HighlightSpacing.Never.shouldAdd(true));
  }

  @Test
  public void default_value() {
    assertEquals(HighlightSpacing.WhenSelected, HighlightSpacing.defaultValue());
  }
}
