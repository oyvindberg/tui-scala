package jatatui.tests._support;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.BufferUpdate;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Position;
import java.util.List;

/// Assertion helpers for [Buffer], ported from upstream `ratatui_core::buffer::assert`.
///
/// Upstream provides an `assert_buffer_eq!` macro (deprecated in favour of `assert_eq!`). We
/// keep the same diff-on-mismatch behaviour: on failure, we list every cell that differs with
/// "expected vs actual" details, plus the full debug rendering of both buffers.
public final class BufferAssertions {

  private BufferAssertions() {}

  /// Asserts that two buffers are equal.
  ///
  /// On mismatch, throws an `AssertionError` whose message lists every differing cell
  /// (using `expected.diff(actual)`) plus the debug renderings of both buffers.
  public static void assertBufferEq(Buffer actual, Buffer expected) {
    if (!actual.area().equals(expected.area())) {
      throw new AssertionError(
          "buffer areas not equal\nexpected: " + expected + "\nactual:   " + actual);
    }
    List<BufferUpdate> diff = expected.diff(actual);
    if (!diff.isEmpty()) {
      StringBuilder niceDiff = new StringBuilder();
      for (int i = 0; i < diff.size(); i++) {
        BufferUpdate u = diff.get(i);
        Cell expectedCell = expected.cellAt(new Position(u.x(), u.y()));
        if (i > 0) niceDiff.append("\n");
        niceDiff
            .append(i)
            .append(": at (")
            .append(u.x())
            .append(", ")
            .append(u.y())
            .append(")\n  expected: ")
            .append(expectedCell)
            .append("\n  actual:   ")
            .append(u.cell());
      }
      throw new AssertionError(
          "buffer contents not equal\nexpected: "
              + expected
              + "\nactual:   "
              + actual
              + "\ndiff:\n"
              + niceDiff);
    }
    // Guard against future changes to equality that don't affect area or content.
    if (!actual.equals(expected)) {
      throw new AssertionError(
          "buffers are not equal in an unexpected way. Please open an issue about this.");
    }
  }
}
