package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.Offset;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Ports the inline tests in `submodules/ratatui/ratatui-core/src/layout/rect/ops.rs`.
///
/// `add_offset` and `add_assign_offset` collapse to the same Java semantics — `Rect.plus(Offset)`
/// is non-mutating, but the asserted output values are identical.
public class RectOpsTest {

  static Stream<Arguments> add_offset_cases() {
    int max = Position.U16_MAX;
    return Stream.of(
        Arguments.of("zero", Rect.of(3, 4, 5, 6), Offset.ZERO, Rect.of(3, 4, 5, 6)),
        Arguments.of("positive", Rect.of(3, 4, 5, 6), Offset.of(1, 2), Rect.of(4, 6, 5, 6)),
        Arguments.of("negative", Rect.of(3, 4, 5, 6), Offset.of(-1, -2), Rect.of(2, 2, 5, 6)),
        Arguments.of(
            "saturate_negative", Rect.of(3, 4, 5, 6), Offset.MIN, Rect.of(0, 0, 5, 6)),
        Arguments.of(
            "saturate_positive",
            Rect.of(3, 4, 5, 6),
            Offset.MAX,
            Rect.of(max - 5, max - 6, 5, 6)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("add_offset_cases")
  public void add_offset(String name, Rect rect, Offset offset, Rect expected) {
    assertEquals(expected, rect.plus(offset));
  }

  static Stream<Arguments> sub_offset_cases() {
    int max = Position.U16_MAX;
    return Stream.of(
        Arguments.of("zero", Rect.of(3, 4, 5, 6), Offset.ZERO, Rect.of(3, 4, 5, 6)),
        Arguments.of("positive", Rect.of(3, 4, 5, 6), Offset.of(1, 2), Rect.of(2, 2, 5, 6)),
        Arguments.of("negative", Rect.of(3, 4, 5, 6), Offset.of(-1, -2), Rect.of(4, 6, 5, 6)),
        Arguments.of(
            "saturate_negative", Rect.of(3, 4, 5, 6), Offset.MAX, Rect.of(0, 0, 5, 6)),
        Arguments.of(
            "saturate_positive",
            Rect.of(3, 4, 5, 6),
            Offset.MAX.negate(),
            Rect.of(max - 5, max - 6, 5, 6)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sub_offset_cases")
  public void sub_offset(String name, Rect rect, Offset offset, Rect expected) {
    assertEquals(expected, rect.minus(offset));
  }

  // N/A: `add_assign_offset` and `sub_assign_offset` — Java records are immutable. The same
  // assertions are covered by `add_offset` / `sub_offset` above (Java has no `+=` operator
  // overload to test separately).

  @Test
  public void offset_negate() {
    // Mirrors the impl Neg for Offset on `Offset::negate` (already tested in OffsetTest, but
    // we re-assert here to mirror upstream's `-Offset` usage in the ops tests).
    assertEquals(Offset.of(-3, 7), Offset.of(3, -7).negate());
  }
}
