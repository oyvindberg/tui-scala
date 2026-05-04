package jatatui.tests.core.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.backend.ClearType;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Ports the inline tests from `submodules/ratatui/ratatui-core/src/backend.rs`.
public class BackendTest {

  @Test
  public void clear_type_tostring() {
    assertEquals("All", ClearType.All.toString());
    assertEquals("AfterCursor", ClearType.AfterCursor.toString());
    assertEquals("BeforeCursor", ClearType.BeforeCursor.toString());
    assertEquals("CurrentLine", ClearType.CurrentLine.toString());
    assertEquals("UntilNewLine", ClearType.UntilNewLine.toString());
  }

  @Test
  public void clear_type_from_str() {
    assertEquals(Optional.of(ClearType.All), ClearType.fromString("All"));
    assertEquals(Optional.of(ClearType.AfterCursor), ClearType.fromString("AfterCursor"));
    assertEquals(Optional.of(ClearType.BeforeCursor), ClearType.fromString("BeforeCursor"));
    assertEquals(Optional.of(ClearType.CurrentLine), ClearType.fromString("CurrentLine"));
    assertEquals(Optional.of(ClearType.UntilNewLine), ClearType.fromString("UntilNewLine"));
    // Upstream returns `Err(ParseError::VariantNotFound)`; we return Optional.empty().
    assertTrue(ClearType.fromString("").isEmpty());
  }
}
