package jatatui.tests.core.layout;

import static jatatui.core.layout.Constraint.Fill;
import static jatatui.core.layout.Constraint.Length;
import static jatatui.core.layout.Constraint.Max;
import static jatatui.core.layout.Constraint.Min;
import static jatatui.core.layout.Constraint.Percentage;
import static jatatui.core.layout.Constraint.Ratio;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Direction;
import jatatui.core.layout.Flex;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Spacing;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Ports the inline tests in `submodules/ratatui/ratatui-core/src/layout/layout.rs`.
///
/// `letters` upstream renders into a Buffer with single-letter content per chunk and asserts on
/// the rendered string. The Java port doesn't exercise the Buffer path; instead `letters` here
/// builds the equivalent letter-string from the segment widths and asserts against the same
/// expected strings.
public class LayoutTest {

  // --- strength_is_valid --------------------------------------------------

  @Test
  public void strength_is_valid() {
    var s = Layout.Strengths.class;
    assertTrue(Layout.Strengths.SPACER_SIZE_EQ.value() > Layout.Strengths.MAX_SIZE_LE.value());
    assertTrue(Layout.Strengths.MAX_SIZE_LE.value() > Layout.Strengths.MAX_SIZE_EQ.value());
    assertEquals(Layout.Strengths.MIN_SIZE_GE.value(), Layout.Strengths.MAX_SIZE_LE.value());
    assertTrue(Layout.Strengths.MAX_SIZE_LE.value() > Layout.Strengths.LENGTH_SIZE_EQ.value());
    assertTrue(Layout.Strengths.LENGTH_SIZE_EQ.value() > Layout.Strengths.PERCENTAGE_SIZE_EQ.value());
    assertTrue(Layout.Strengths.PERCENTAGE_SIZE_EQ.value() > Layout.Strengths.RATIO_SIZE_EQ.value());
    assertTrue(Layout.Strengths.RATIO_SIZE_EQ.value() > Layout.Strengths.MAX_SIZE_EQ.value());
    assertTrue(Layout.Strengths.MIN_SIZE_GE.value() > Layout.Strengths.FILL_GROW.value());
    assertTrue(Layout.Strengths.FILL_GROW.value() > Layout.Strengths.GROW.value());
    assertTrue(Layout.Strengths.GROW.value() > Layout.Strengths.SPACE_GROW.value());
    assertTrue(Layout.Strengths.SPACE_GROW.value() > Layout.Strengths.ALL_SEGMENT_GROW.value());
  }

  // --- cache_size ---------------------------------------------------------

  @Test
  public void cache_size() {
    // Default size is 500
    assertEquals(Layout.DEFAULT_CACHE_SIZE, Layout.cacheCapacity());
    Layout.initCache(10);
    assertEquals(10, Layout.cacheCapacity());
    // restore default for other tests
    Layout.initCache(Layout.DEFAULT_CACHE_SIZE);
  }

  // --- default ------------------------------------------------------------

  @Test
  public void default_test() {
    Layout l = Layout.empty();
    assertEquals(Direction.Vertical, l.direction());
    assertEquals(new Margin(0, 0), l.margin());
    assertEquals(List.of(), l.constraints());
    assertEquals(Flex.Start, l.flex());
    assertEquals(Spacing.DEFAULT, l.spacing());
  }

  // --- new ----------------------------------------------------------------

  @Test
  public void new_test() {
    Constraint c = new Min(0);
    Layout layout = Layout.of(Direction.Horizontal, c);
    assertEquals(Direction.Horizontal, layout.direction());
    assertEquals(List.of(c), layout.constraints());

    Layout layout2 = Layout.of(Direction.Horizontal, List.of(c));
    assertEquals(Direction.Horizontal, layout2.direction());
    assertEquals(List.of(c), layout2.constraints());
  }

  // --- vertical -----------------------------------------------------------

  @Test
  public void vertical() {
    Layout expected = Layout.empty().withConstraints(new Min(0));
    assertEquals(Direction.Vertical, expected.direction());
    assertEquals(List.of(new Min(0)), expected.constraints());
    assertEquals(new Margin(0, 0), expected.margin());
    assertEquals(Flex.Start, expected.flex());
    assertEquals(Spacing.DEFAULT, expected.spacing());

    Layout actual = Layout.vertical(new Min(0));
    assertEquals(expected, actual);
  }

  // --- horizontal ---------------------------------------------------------

  @Test
  public void horizontal() {
    Layout expected =
        Layout.empty().withConstraints(new Min(0)).withDirection(Direction.Horizontal);
    Layout actual = Layout.horizontal(new Min(0));
    assertEquals(expected, actual);
  }

  // --- constraints --------------------------------------------------------

  @Test
  public void constraints() {
    List<Constraint> cs = List.of(new Min(0), new Max(10));
    Layout layout = Layout.empty().withConstraints(cs);
    assertEquals(cs, layout.constraints());

    Layout layout2 = Layout.empty().withConstraints(new Min(0), new Max(10));
    assertEquals(cs, layout2.constraints());
  }

  // --- direction ----------------------------------------------------------

  @Test
  public void direction() {
    assertEquals(
        Direction.Horizontal, Layout.empty().withDirection(Direction.Horizontal).direction());
    assertEquals(Direction.Vertical, Layout.empty().withDirection(Direction.Vertical).direction());
  }

  // --- margins ------------------------------------------------------------

  @Test
  public void margins() {
    assertEquals(new Margin(10, 10), Layout.empty().withMargin(10).margin());
    assertEquals(new Margin(10, 0), Layout.empty().withHorizontalMargin(10).margin());
    assertEquals(new Margin(0, 10), Layout.empty().withVerticalMargin(10).margin());
    assertEquals(
        new Margin(10, 20),
        Layout.empty().withHorizontalMargin(10).withVerticalMargin(20).margin());
  }

  // --- flex ---------------------------------------------------------------

  @Test
  public void flex() {
    assertEquals(Flex.Start, Layout.empty().flex());
    assertEquals(Flex.Center, Layout.empty().withFlex(Flex.Center).flex());
  }

  // --- spacing ------------------------------------------------------------

  @Test
  public void spacing() {
    assertEquals(new Spacing.Space(10), Layout.empty().withSpacing(10).spacing());
    assertEquals(new Spacing.Space(0), Layout.empty().withSpacing(0).spacing());
    assertEquals(new Spacing.Overlap(10), Layout.empty().withSpacing(-10).spacing());
  }

  // --- split / letters helpers --------------------------------------------

  /// Render letters into a "buffer" represented as a String of width `width`. Each segment is
  /// filled with a different lowercase letter. Mirrors `letters` upstream.
  private static String letters(Flex flex, List<Constraint> constraints, int width) {
    Rect area = Rect.of(0, 0, width, 1);
    Rect[] segments = Layout.horizontal(constraints).withFlex(flex).split(area);
    char[] buf = new char[width];
    java.util.Arrays.fill(buf, ' ');
    char letter = 'a';
    for (Rect r : segments) {
      for (int i = 0; i < r.width(); i++) {
        int idx = r.x() + i;
        if (idx >= 0 && idx < width) buf[idx] = letter;
      }
      letter++;
    }
    return new String(buf);
  }

  static Stream<Arguments> length_cases() {
    return Stream.of(
        cs(Flex.Legacy, 1, "a", new Length(0)),
        cs(Flex.Legacy, 1, "a", new Length(1)),
        cs(Flex.Legacy, 1, "a", new Length(2)),
        cs(Flex.Legacy, 2, "aa", new Length(0)),
        cs(Flex.Legacy, 2, "aa", new Length(1)),
        cs(Flex.Legacy, 2, "aa", new Length(2)),
        cs(Flex.Legacy, 2, "aa", new Length(3)),
        cs(Flex.Legacy, 1, "b", new Length(0), new Length(0)),
        cs(Flex.Legacy, 1, "b", new Length(0), new Length(1)),
        cs(Flex.Legacy, 1, "b", new Length(0), new Length(2)),
        cs(Flex.Legacy, 1, "a", new Length(1), new Length(0)),
        cs(Flex.Legacy, 1, "a", new Length(1), new Length(1)),
        cs(Flex.Legacy, 1, "a", new Length(1), new Length(2)),
        cs(Flex.Legacy, 1, "a", new Length(2), new Length(0)),
        cs(Flex.Legacy, 1, "a", new Length(2), new Length(1)),
        cs(Flex.Legacy, 1, "a", new Length(2), new Length(2)),
        cs(Flex.Legacy, 2, "bb", new Length(0), new Length(0)),
        cs(Flex.Legacy, 2, "bb", new Length(0), new Length(1)),
        cs(Flex.Legacy, 2, "bb", new Length(0), new Length(2)),
        cs(Flex.Legacy, 2, "bb", new Length(0), new Length(3)),
        cs(Flex.Legacy, 2, "ab", new Length(1), new Length(0)),
        cs(Flex.Legacy, 2, "ab", new Length(1), new Length(1)),
        cs(Flex.Legacy, 2, "ab", new Length(1), new Length(2)),
        cs(Flex.Legacy, 2, "ab", new Length(1), new Length(3)),
        cs(Flex.Legacy, 2, "aa", new Length(2), new Length(0)),
        cs(Flex.Legacy, 2, "aa", new Length(2), new Length(1)),
        cs(Flex.Legacy, 2, "aa", new Length(2), new Length(2)),
        cs(Flex.Legacy, 2, "aa", new Length(2), new Length(3)),
        cs(Flex.Legacy, 2, "aa", new Length(3), new Length(0)),
        cs(Flex.Legacy, 2, "aa", new Length(3), new Length(1)),
        cs(Flex.Legacy, 2, "aa", new Length(3), new Length(2)),
        cs(Flex.Legacy, 2, "aa", new Length(3), new Length(3)),
        cs(Flex.Legacy, 3, "aab", new Length(2), new Length(2)));
  }

  @ParameterizedTest
  @MethodSource("length_cases")
  public void length(Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  static Stream<Arguments> max_cases() {
    return Stream.of(
        cs(Flex.Legacy, 1, "a", new Max(0)),
        cs(Flex.Legacy, 1, "a", new Max(1)),
        cs(Flex.Legacy, 1, "a", new Max(2)),
        cs(Flex.Legacy, 2, "aa", new Max(0)),
        cs(Flex.Legacy, 2, "aa", new Max(1)),
        cs(Flex.Legacy, 2, "aa", new Max(2)),
        cs(Flex.Legacy, 2, "aa", new Max(3)),
        cs(Flex.Legacy, 1, "b", new Max(0), new Max(0)),
        cs(Flex.Legacy, 1, "b", new Max(0), new Max(1)),
        cs(Flex.Legacy, 1, "b", new Max(0), new Max(2)),
        cs(Flex.Legacy, 1, "a", new Max(1), new Max(0)),
        cs(Flex.Legacy, 1, "a", new Max(1), new Max(1)),
        cs(Flex.Legacy, 1, "a", new Max(1), new Max(2)),
        cs(Flex.Legacy, 1, "a", new Max(2), new Max(0)),
        cs(Flex.Legacy, 1, "a", new Max(2), new Max(1)),
        cs(Flex.Legacy, 1, "a", new Max(2), new Max(2)),
        cs(Flex.Legacy, 2, "bb", new Max(0), new Max(0)),
        cs(Flex.Legacy, 2, "bb", new Max(0), new Max(1)),
        cs(Flex.Legacy, 2, "bb", new Max(0), new Max(2)),
        cs(Flex.Legacy, 2, "bb", new Max(0), new Max(3)),
        cs(Flex.Legacy, 2, "ab", new Max(1), new Max(0)),
        cs(Flex.Legacy, 2, "ab", new Max(1), new Max(1)),
        cs(Flex.Legacy, 2, "ab", new Max(1), new Max(2)),
        cs(Flex.Legacy, 2, "ab", new Max(1), new Max(3)),
        cs(Flex.Legacy, 2, "aa", new Max(2), new Max(0)),
        cs(Flex.Legacy, 2, "aa", new Max(2), new Max(1)),
        cs(Flex.Legacy, 2, "aa", new Max(2), new Max(2)),
        cs(Flex.Legacy, 2, "aa", new Max(2), new Max(3)),
        cs(Flex.Legacy, 2, "aa", new Max(3), new Max(0)),
        cs(Flex.Legacy, 2, "aa", new Max(3), new Max(1)),
        cs(Flex.Legacy, 2, "aa", new Max(3), new Max(2)),
        cs(Flex.Legacy, 2, "aa", new Max(3), new Max(3)),
        cs(Flex.Legacy, 3, "aab", new Max(2), new Max(2)));
  }

  @ParameterizedTest
  @MethodSource("max_cases")
  public void max(Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  static Stream<Arguments> min_cases() {
    return Stream.of(
        cs(Flex.Legacy, 1, "b", new Min(0), new Min(0)),
        cs(Flex.Legacy, 1, "b", new Min(0), new Min(1)),
        cs(Flex.Legacy, 1, "b", new Min(0), new Min(2)),
        cs(Flex.Legacy, 1, "a", new Min(1), new Min(0)),
        cs(Flex.Legacy, 1, "a", new Min(1), new Min(1)),
        cs(Flex.Legacy, 1, "a", new Min(1), new Min(2)),
        cs(Flex.Legacy, 1, "a", new Min(2), new Min(0)),
        cs(Flex.Legacy, 1, "a", new Min(2), new Min(1)),
        cs(Flex.Legacy, 1, "a", new Min(2), new Min(2)),
        cs(Flex.Legacy, 2, "bb", new Min(0), new Min(0)),
        cs(Flex.Legacy, 2, "bb", new Min(0), new Min(1)),
        cs(Flex.Legacy, 2, "bb", new Min(0), new Min(2)),
        cs(Flex.Legacy, 2, "bb", new Min(0), new Min(3)),
        cs(Flex.Legacy, 2, "ab", new Min(1), new Min(0)),
        cs(Flex.Legacy, 2, "ab", new Min(1), new Min(1)),
        cs(Flex.Legacy, 2, "ab", new Min(1), new Min(2)),
        cs(Flex.Legacy, 2, "ab", new Min(1), new Min(3)),
        cs(Flex.Legacy, 2, "aa", new Min(2), new Min(0)),
        cs(Flex.Legacy, 2, "aa", new Min(2), new Min(1)),
        cs(Flex.Legacy, 2, "aa", new Min(2), new Min(2)),
        cs(Flex.Legacy, 2, "aa", new Min(2), new Min(3)),
        cs(Flex.Legacy, 2, "aa", new Min(3), new Min(0)),
        cs(Flex.Legacy, 2, "aa", new Min(3), new Min(1)),
        cs(Flex.Legacy, 2, "aa", new Min(3), new Min(2)),
        cs(Flex.Legacy, 2, "aa", new Min(3), new Min(3)),
        cs(Flex.Legacy, 3, "aab", new Min(2), new Min(2)));
  }

  @ParameterizedTest
  @MethodSource("min_cases")
  public void min(Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  // --- percentage parameterised tests -------------------------------------

  static Stream<Arguments> percentage_cases() {
    return Stream.of(
        cs(Flex.Legacy, 1, "a", new Percentage(0)),
        cs(Flex.Legacy, 1, "a", new Percentage(25)),
        cs(Flex.Legacy, 1, "a", new Percentage(50)),
        cs(Flex.Legacy, 1, "a", new Percentage(90)),
        cs(Flex.Legacy, 1, "a", new Percentage(100)),
        cs(Flex.Legacy, 1, "a", new Percentage(200)),
        cs(Flex.Legacy, 2, "aa", new Percentage(0)),
        cs(Flex.Legacy, 2, "aa", new Percentage(10)),
        cs(Flex.Legacy, 2, "aa", new Percentage(25)),
        cs(Flex.Legacy, 2, "aa", new Percentage(50)),
        cs(Flex.Legacy, 2, "aa", new Percentage(66)),
        cs(Flex.Legacy, 2, "aa", new Percentage(100)),
        cs(Flex.Legacy, 2, "aa", new Percentage(200)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(0)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(10)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(25)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(50)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(66)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(100)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(200)),
        cs(Flex.Legacy, 1, "b", new Percentage(0), new Percentage(0)),
        cs(Flex.Legacy, 1, "b", new Percentage(0), new Percentage(10)),
        cs(Flex.Legacy, 1, "b", new Percentage(0), new Percentage(50)),
        cs(Flex.Legacy, 1, "b", new Percentage(0), new Percentage(90)),
        cs(Flex.Legacy, 1, "b", new Percentage(0), new Percentage(100)),
        cs(Flex.Legacy, 1, "b", new Percentage(0), new Percentage(200)),
        cs(Flex.Legacy, 1, "b", new Percentage(10), new Percentage(0)),
        cs(Flex.Legacy, 1, "b", new Percentage(10), new Percentage(10)),
        cs(Flex.Legacy, 1, "b", new Percentage(10), new Percentage(50)),
        cs(Flex.Legacy, 1, "b", new Percentage(10), new Percentage(90)),
        cs(Flex.Legacy, 1, "b", new Percentage(10), new Percentage(100)),
        cs(Flex.Legacy, 1, "b", new Percentage(10), new Percentage(200)),
        cs(Flex.Legacy, 1, "a", new Percentage(50), new Percentage(0)),
        cs(Flex.Legacy, 1, "a", new Percentage(50), new Percentage(50)),
        cs(Flex.Legacy, 1, "a", new Percentage(50), new Percentage(100)),
        cs(Flex.Legacy, 1, "a", new Percentage(50), new Percentage(200)),
        cs(Flex.Legacy, 1, "a", new Percentage(90), new Percentage(0)),
        cs(Flex.Legacy, 1, "a", new Percentage(90), new Percentage(50)),
        cs(Flex.Legacy, 1, "a", new Percentage(90), new Percentage(100)),
        cs(Flex.Legacy, 1, "a", new Percentage(90), new Percentage(200)),
        cs(Flex.Legacy, 1, "a", new Percentage(100), new Percentage(0)),
        cs(Flex.Legacy, 1, "a", new Percentage(100), new Percentage(50)),
        cs(Flex.Legacy, 1, "a", new Percentage(100), new Percentage(100)),
        cs(Flex.Legacy, 1, "a", new Percentage(100), new Percentage(200)),
        cs(Flex.Legacy, 2, "bb", new Percentage(0), new Percentage(0)),
        cs(Flex.Legacy, 2, "bb", new Percentage(0), new Percentage(25)),
        cs(Flex.Legacy, 2, "bb", new Percentage(0), new Percentage(50)),
        cs(Flex.Legacy, 2, "bb", new Percentage(0), new Percentage(100)),
        cs(Flex.Legacy, 2, "bb", new Percentage(0), new Percentage(200)),
        cs(Flex.Legacy, 2, "bb", new Percentage(10), new Percentage(0)),
        cs(Flex.Legacy, 2, "bb", new Percentage(10), new Percentage(25)),
        cs(Flex.Legacy, 2, "bb", new Percentage(10), new Percentage(50)),
        cs(Flex.Legacy, 2, "bb", new Percentage(10), new Percentage(100)),
        cs(Flex.Legacy, 2, "bb", new Percentage(10), new Percentage(200)),
        cs(Flex.Legacy, 2, "ab", new Percentage(25), new Percentage(0)),
        cs(Flex.Legacy, 2, "ab", new Percentage(25), new Percentage(25)),
        cs(Flex.Legacy, 2, "ab", new Percentage(25), new Percentage(50)),
        cs(Flex.Legacy, 2, "ab", new Percentage(25), new Percentage(100)),
        cs(Flex.Legacy, 2, "ab", new Percentage(25), new Percentage(200)),
        cs(Flex.Legacy, 2, "ab", new Percentage(33), new Percentage(0)),
        cs(Flex.Legacy, 2, "ab", new Percentage(33), new Percentage(25)),
        cs(Flex.Legacy, 2, "ab", new Percentage(33), new Percentage(50)),
        cs(Flex.Legacy, 2, "ab", new Percentage(33), new Percentage(100)),
        cs(Flex.Legacy, 2, "ab", new Percentage(33), new Percentage(200)),
        cs(Flex.Legacy, 2, "ab", new Percentage(50), new Percentage(0)),
        cs(Flex.Legacy, 2, "ab", new Percentage(50), new Percentage(50)),
        cs(Flex.Legacy, 2, "ab", new Percentage(50), new Percentage(100)),
        cs(Flex.Legacy, 2, "aa", new Percentage(100), new Percentage(0)),
        cs(Flex.Legacy, 2, "aa", new Percentage(100), new Percentage(50)),
        cs(Flex.Legacy, 2, "aa", new Percentage(100), new Percentage(100)),
        cs(Flex.Legacy, 3, "abb", new Percentage(33), new Percentage(33)),
        cs(Flex.Legacy, 3, "abb", new Percentage(33), new Percentage(66)),
        cs(Flex.Legacy, 4, "abbb", new Percentage(33), new Percentage(33)),
        cs(Flex.Legacy, 4, "abbb", new Percentage(33), new Percentage(66)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(0)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(25)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(50)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(100)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(200)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Percentage(10), new Percentage(0)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Percentage(10), new Percentage(25)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Percentage(10), new Percentage(50)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Percentage(10), new Percentage(100)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Percentage(10), new Percentage(200)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(25), new Percentage(0)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(25), new Percentage(25)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(25), new Percentage(50)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(25), new Percentage(100)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(25), new Percentage(200)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(33), new Percentage(0)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(33), new Percentage(25)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(33), new Percentage(50)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(33), new Percentage(100)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Percentage(33), new Percentage(200)),
        cs(Flex.Legacy, 10, "aaaaabbbbb", new Percentage(50), new Percentage(0)),
        cs(Flex.Legacy, 10, "aaaaabbbbb", new Percentage(50), new Percentage(50)),
        cs(Flex.Legacy, 10, "aaaaabbbbb", new Percentage(50), new Percentage(100)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(100), new Percentage(0)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(100), new Percentage(50)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Percentage(100), new Percentage(100)));
  }

  @ParameterizedTest
  @MethodSource("percentage_cases")
  public void percentage(Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  static Stream<Arguments> percentage_start_cases() {
    return Stream.of(
        cs(Flex.Start, 10, "          ", new Percentage(0), new Percentage(0)),
        cs(Flex.Start, 10, "bbb       ", new Percentage(0), new Percentage(25)),
        cs(Flex.Start, 10, "bbbbb     ", new Percentage(0), new Percentage(50)),
        cs(Flex.Start, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(100)),
        cs(Flex.Start, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(200)),
        cs(Flex.Start, 10, "a         ", new Percentage(10), new Percentage(0)),
        cs(Flex.Start, 10, "abbb      ", new Percentage(10), new Percentage(25)),
        cs(Flex.Start, 10, "abbbbb    ", new Percentage(10), new Percentage(50)),
        cs(Flex.Start, 10, "abbbbbbbbb", new Percentage(10), new Percentage(100)),
        cs(Flex.Start, 10, "abbbbbbbbb", new Percentage(10), new Percentage(200)),
        cs(Flex.Start, 10, "aaa       ", new Percentage(25), new Percentage(0)),
        cs(Flex.Start, 10, "aaabb     ", new Percentage(25), new Percentage(25)),
        cs(Flex.Start, 10, "aaabbbbb  ", new Percentage(25), new Percentage(50)),
        cs(Flex.Start, 10, "aaabbbbbbb", new Percentage(25), new Percentage(100)),
        cs(Flex.Start, 10, "aaabbbbbbb", new Percentage(25), new Percentage(200)),
        cs(Flex.Start, 10, "aaa       ", new Percentage(33), new Percentage(0)),
        cs(Flex.Start, 10, "aaabbb    ", new Percentage(33), new Percentage(25)),
        cs(Flex.Start, 10, "aaabbbbb  ", new Percentage(33), new Percentage(50)),
        cs(Flex.Start, 10, "aaabbbbbbb", new Percentage(33), new Percentage(100)),
        cs(Flex.Start, 10, "aaabbbbbbb", new Percentage(33), new Percentage(200)),
        cs(Flex.Start, 10, "aaaaa     ", new Percentage(50), new Percentage(0)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Percentage(50), new Percentage(50)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Percentage(50), new Percentage(100)),
        cs(Flex.Start, 10, "aaaaaaaaaa", new Percentage(100), new Percentage(0)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Percentage(100), new Percentage(50)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Percentage(100), new Percentage(100)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Percentage(100), new Percentage(200)));
  }

  @ParameterizedTest
  @MethodSource("percentage_start_cases")
  public void percentage_start(
      Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  static Stream<Arguments> percentage_spacebetween_cases() {
    return Stream.of(
        cs(Flex.SpaceBetween, 10, "          ", new Percentage(0), new Percentage(0)),
        cs(Flex.SpaceBetween, 10, "        bb", new Percentage(0), new Percentage(25)),
        cs(Flex.SpaceBetween, 10, "     bbbbb", new Percentage(0), new Percentage(50)),
        cs(Flex.SpaceBetween, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(100)),
        cs(Flex.SpaceBetween, 10, "bbbbbbbbbb", new Percentage(0), new Percentage(200)),
        cs(Flex.SpaceBetween, 10, "a         ", new Percentage(10), new Percentage(0)),
        cs(Flex.SpaceBetween, 10, "a       bb", new Percentage(10), new Percentage(25)),
        cs(Flex.SpaceBetween, 10, "a    bbbbb", new Percentage(10), new Percentage(50)),
        cs(Flex.SpaceBetween, 10, "abbbbbbbbb", new Percentage(10), new Percentage(100)),
        cs(Flex.SpaceBetween, 10, "abbbbbbbbb", new Percentage(10), new Percentage(200)),
        cs(Flex.SpaceBetween, 10, "aaa       ", new Percentage(25), new Percentage(0)),
        cs(Flex.SpaceBetween, 10, "aaa     bb", new Percentage(25), new Percentage(25)),
        cs(Flex.SpaceBetween, 10, "aaa  bbbbb", new Percentage(25), new Percentage(50)),
        cs(Flex.SpaceBetween, 10, "aaabbbbbbb", new Percentage(25), new Percentage(100)),
        cs(Flex.SpaceBetween, 10, "aaabbbbbbb", new Percentage(25), new Percentage(200)),
        cs(Flex.SpaceBetween, 10, "aaa       ", new Percentage(33), new Percentage(0)),
        cs(Flex.SpaceBetween, 10, "aaa     bb", new Percentage(33), new Percentage(25)),
        cs(Flex.SpaceBetween, 10, "aaa  bbbbb", new Percentage(33), new Percentage(50)),
        cs(Flex.SpaceBetween, 10, "aaabbbbbbb", new Percentage(33), new Percentage(100)),
        cs(Flex.SpaceBetween, 10, "aaabbbbbbb", new Percentage(33), new Percentage(200)),
        cs(Flex.SpaceBetween, 10, "aaaaa     ", new Percentage(50), new Percentage(0)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Percentage(50), new Percentage(50)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Percentage(50), new Percentage(100)),
        cs(Flex.SpaceBetween, 10, "aaaaaaaaaa", new Percentage(100), new Percentage(0)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Percentage(100), new Percentage(50)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Percentage(100), new Percentage(100)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Percentage(100), new Percentage(200)));
  }

  @ParameterizedTest
  @MethodSource("percentage_spacebetween_cases")
  public void percentage_spacebetween(
      Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  static Stream<Arguments> ratio_cases() {
    return Stream.of(
        cs(Flex.Legacy, 1, "a", new Ratio(0, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 4)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 2)),
        cs(Flex.Legacy, 1, "a", new Ratio(9, 10)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(2, 1)),
        cs(Flex.Legacy, 2, "aa", new Ratio(0, 1)),
        cs(Flex.Legacy, 2, "aa", new Ratio(1, 10)),
        cs(Flex.Legacy, 2, "aa", new Ratio(1, 4)),
        cs(Flex.Legacy, 2, "aa", new Ratio(1, 2)),
        cs(Flex.Legacy, 2, "aa", new Ratio(2, 3)),
        cs(Flex.Legacy, 2, "aa", new Ratio(1, 1)),
        cs(Flex.Legacy, 2, "aa", new Ratio(2, 1)),
        cs(Flex.Legacy, 1, "b", new Ratio(0, 1), new Ratio(0, 1)),
        cs(Flex.Legacy, 1, "b", new Ratio(0, 1), new Ratio(1, 10)),
        cs(Flex.Legacy, 1, "b", new Ratio(0, 1), new Ratio(1, 2)),
        cs(Flex.Legacy, 1, "b", new Ratio(0, 1), new Ratio(9, 10)),
        cs(Flex.Legacy, 1, "b", new Ratio(0, 1), new Ratio(1, 1)),
        cs(Flex.Legacy, 1, "b", new Ratio(0, 1), new Ratio(2, 1)),
        cs(Flex.Legacy, 1, "b", new Ratio(1, 10), new Ratio(0, 1)),
        cs(Flex.Legacy, 1, "b", new Ratio(1, 10), new Ratio(1, 10)),
        cs(Flex.Legacy, 1, "b", new Ratio(1, 10), new Ratio(1, 2)),
        cs(Flex.Legacy, 1, "b", new Ratio(1, 10), new Ratio(9, 10)),
        cs(Flex.Legacy, 1, "b", new Ratio(1, 10), new Ratio(1, 1)),
        cs(Flex.Legacy, 1, "b", new Ratio(1, 10), new Ratio(2, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 2), new Ratio(0, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 2), new Ratio(1, 2)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 2), new Ratio(1, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 2), new Ratio(2, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(9, 10), new Ratio(0, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(9, 10), new Ratio(1, 2)),
        cs(Flex.Legacy, 1, "a", new Ratio(9, 10), new Ratio(1, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(9, 10), new Ratio(2, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 1), new Ratio(0, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 1), new Ratio(1, 2)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 1), new Ratio(1, 1)),
        cs(Flex.Legacy, 1, "a", new Ratio(1, 1), new Ratio(2, 1)),
        cs(Flex.Legacy, 2, "bb", new Ratio(0, 1), new Ratio(0, 1)),
        cs(Flex.Legacy, 2, "bb", new Ratio(0, 1), new Ratio(1, 4)),
        cs(Flex.Legacy, 2, "bb", new Ratio(0, 1), new Ratio(1, 2)),
        cs(Flex.Legacy, 2, "bb", new Ratio(0, 1), new Ratio(1, 1)),
        cs(Flex.Legacy, 2, "bb", new Ratio(0, 1), new Ratio(2, 1)),
        cs(Flex.Legacy, 2, "bb", new Ratio(1, 10), new Ratio(0, 1)),
        cs(Flex.Legacy, 2, "bb", new Ratio(1, 10), new Ratio(1, 4)),
        cs(Flex.Legacy, 2, "bb", new Ratio(1, 10), new Ratio(1, 2)),
        cs(Flex.Legacy, 2, "bb", new Ratio(1, 10), new Ratio(1, 1)),
        cs(Flex.Legacy, 2, "bb", new Ratio(1, 10), new Ratio(2, 1)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 4), new Ratio(0, 1)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 4), new Ratio(1, 4)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 4), new Ratio(1, 2)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 4), new Ratio(1, 1)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 4), new Ratio(2, 1)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 3), new Ratio(0, 1)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 3), new Ratio(1, 4)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 3), new Ratio(1, 2)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 3), new Ratio(1, 1)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 3), new Ratio(2, 1)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 2), new Ratio(0, 1)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 2), new Ratio(1, 2)),
        cs(Flex.Legacy, 2, "ab", new Ratio(1, 2), new Ratio(1, 1)),
        cs(Flex.Legacy, 2, "aa", new Ratio(1, 1), new Ratio(0, 1)),
        cs(Flex.Legacy, 2, "aa", new Ratio(1, 1), new Ratio(1, 2)),
        cs(Flex.Legacy, 2, "aa", new Ratio(1, 1), new Ratio(1, 1)),
        cs(Flex.Legacy, 3, "abb", new Ratio(1, 3), new Ratio(1, 3)),
        cs(Flex.Legacy, 3, "abb", new Ratio(1, 3), new Ratio(2, 3)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(0, 1)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(1, 4)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(1, 2)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(1, 1)),
        cs(Flex.Legacy, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(2, 1)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(0, 1)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(1, 4)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(1, 2)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(1, 1)),
        cs(Flex.Legacy, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(2, 1)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(0, 1)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(1, 4)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(1, 2)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(1, 1)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(2, 1)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(0, 1)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(1, 4)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(1, 2)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(1, 1)),
        cs(Flex.Legacy, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(2, 1)),
        cs(Flex.Legacy, 10, "aaaaabbbbb", new Ratio(1, 2), new Ratio(0, 1)),
        cs(Flex.Legacy, 10, "aaaaabbbbb", new Ratio(1, 2), new Ratio(1, 2)),
        cs(Flex.Legacy, 10, "aaaaabbbbb", new Ratio(1, 2), new Ratio(1, 1)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Ratio(1, 1), new Ratio(0, 1)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Ratio(1, 1), new Ratio(1, 2)),
        cs(Flex.Legacy, 10, "aaaaaaaaaa", new Ratio(1, 1), new Ratio(1, 1)));
  }

  @ParameterizedTest
  @MethodSource("ratio_cases")
  public void ratio(Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  static Stream<Arguments> ratio_start_cases() {
    return Stream.of(
        cs(Flex.Start, 10, "          ", new Ratio(0, 1), new Ratio(0, 1)),
        cs(Flex.Start, 10, "bbb       ", new Ratio(0, 1), new Ratio(1, 4)),
        cs(Flex.Start, 10, "bbbbb     ", new Ratio(0, 1), new Ratio(1, 2)),
        cs(Flex.Start, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(1, 1)),
        cs(Flex.Start, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(2, 1)),
        cs(Flex.Start, 10, "a         ", new Ratio(1, 10), new Ratio(0, 1)),
        cs(Flex.Start, 10, "abbb      ", new Ratio(1, 10), new Ratio(1, 4)),
        cs(Flex.Start, 10, "abbbbb    ", new Ratio(1, 10), new Ratio(1, 2)),
        cs(Flex.Start, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(1, 1)),
        cs(Flex.Start, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(2, 1)),
        cs(Flex.Start, 10, "aaa       ", new Ratio(1, 4), new Ratio(0, 1)),
        cs(Flex.Start, 10, "aaabb     ", new Ratio(1, 4), new Ratio(1, 4)),
        cs(Flex.Start, 10, "aaabbbbb  ", new Ratio(1, 4), new Ratio(1, 2)),
        cs(Flex.Start, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(1, 1)),
        cs(Flex.Start, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(2, 1)),
        cs(Flex.Start, 10, "aaa       ", new Ratio(1, 3), new Ratio(0, 1)),
        cs(Flex.Start, 10, "aaabbb    ", new Ratio(1, 3), new Ratio(1, 4)),
        cs(Flex.Start, 10, "aaabbbbb  ", new Ratio(1, 3), new Ratio(1, 2)),
        cs(Flex.Start, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(1, 1)),
        cs(Flex.Start, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(2, 1)),
        cs(Flex.Start, 10, "aaaaa     ", new Ratio(1, 2), new Ratio(0, 1)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Ratio(1, 2), new Ratio(1, 2)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Ratio(1, 2), new Ratio(1, 1)),
        cs(Flex.Start, 10, "aaaaaaaaaa", new Ratio(1, 1), new Ratio(0, 1)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Ratio(1, 1), new Ratio(1, 2)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Ratio(1, 1), new Ratio(1, 1)),
        cs(Flex.Start, 10, "aaaaabbbbb", new Ratio(1, 1), new Ratio(2, 1)));
  }

  @ParameterizedTest
  @MethodSource("ratio_start_cases")
  public void ratio_start(Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  static Stream<Arguments> ratio_spacebetween_cases() {
    return Stream.of(
        cs(Flex.SpaceBetween, 10, "          ", new Ratio(0, 1), new Ratio(0, 1)),
        cs(Flex.SpaceBetween, 10, "        bb", new Ratio(0, 1), new Ratio(1, 4)),
        cs(Flex.SpaceBetween, 10, "     bbbbb", new Ratio(0, 1), new Ratio(1, 2)),
        cs(Flex.SpaceBetween, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(1, 1)),
        cs(Flex.SpaceBetween, 10, "bbbbbbbbbb", new Ratio(0, 1), new Ratio(2, 1)),
        cs(Flex.SpaceBetween, 10, "a         ", new Ratio(1, 10), new Ratio(0, 1)),
        cs(Flex.SpaceBetween, 10, "a       bb", new Ratio(1, 10), new Ratio(1, 4)),
        cs(Flex.SpaceBetween, 10, "a    bbbbb", new Ratio(1, 10), new Ratio(1, 2)),
        cs(Flex.SpaceBetween, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(1, 1)),
        cs(Flex.SpaceBetween, 10, "abbbbbbbbb", new Ratio(1, 10), new Ratio(2, 1)),
        cs(Flex.SpaceBetween, 10, "aaa       ", new Ratio(1, 4), new Ratio(0, 1)),
        cs(Flex.SpaceBetween, 10, "aaa     bb", new Ratio(1, 4), new Ratio(1, 4)),
        cs(Flex.SpaceBetween, 10, "aaa  bbbbb", new Ratio(1, 4), new Ratio(1, 2)),
        cs(Flex.SpaceBetween, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(1, 1)),
        cs(Flex.SpaceBetween, 10, "aaabbbbbbb", new Ratio(1, 4), new Ratio(2, 1)),
        cs(Flex.SpaceBetween, 10, "aaa       ", new Ratio(1, 3), new Ratio(0, 1)),
        cs(Flex.SpaceBetween, 10, "aaa     bb", new Ratio(1, 3), new Ratio(1, 4)),
        cs(Flex.SpaceBetween, 10, "aaa  bbbbb", new Ratio(1, 3), new Ratio(1, 2)),
        cs(Flex.SpaceBetween, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(1, 1)),
        cs(Flex.SpaceBetween, 10, "aaabbbbbbb", new Ratio(1, 3), new Ratio(2, 1)),
        cs(Flex.SpaceBetween, 10, "aaaaa     ", new Ratio(1, 2), new Ratio(0, 1)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Ratio(1, 2), new Ratio(1, 2)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Ratio(1, 2), new Ratio(1, 1)),
        cs(Flex.SpaceBetween, 10, "aaaaaaaaaa", new Ratio(1, 1), new Ratio(0, 1)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Ratio(1, 1), new Ratio(1, 2)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Ratio(1, 1), new Ratio(1, 1)),
        cs(Flex.SpaceBetween, 10, "aaaaabbbbb", new Ratio(1, 1), new Ratio(2, 1)));
  }

  @ParameterizedTest
  @MethodSource("ratio_spacebetween_cases")
  public void ratio_spacebetween(
      Flex flex, int width, String expected, List<Constraint> constraints) {
    assertEquals(expected, letters(flex, constraints, width));
  }

  // --- vertical_split_by_height -------------------------------------------

  @Test
  public void vertical_split_by_height() {
    Rect target = Rect.of(2, 2, 10, 10);
    Rect[] chunks =
        Layout.empty()
            .withDirection(Direction.Vertical)
            .withConstraints(new Percentage(10), new Max(5), new Min(1))
            .split(target);
    int sumH = 0;
    for (Rect c : chunks) sumH += c.height();
    assertEquals(target.height(), sumH);
    for (int i = 0; i + 1 < chunks.length; i++) {
      assertTrue(chunks[i].y() <= chunks[i + 1].y());
    }
  }

  // --- edge_cases ---------------------------------------------------------

  @Test
  public void edge_cases() {
    // stretches into last
    Rect[] layout1 =
        Layout.empty()
            .withConstraints(new Percentage(50), new Percentage(50), new Min(0))
            .split(Rect.of(0, 0, 1, 1));
    assertArrayEquals(
        new Rect[] {Rect.of(0, 0, 1, 1), Rect.of(0, 1, 1, 0), Rect.of(0, 1, 1, 0)}, layout1);

    Rect[] layout2 =
        Layout.empty()
            .withConstraints(new Max(1), new Percentage(99), new Min(0))
            .split(Rect.of(0, 0, 1, 1));
    assertArrayEquals(
        new Rect[] {Rect.of(0, 0, 1, 0), Rect.of(0, 0, 1, 1), Rect.of(0, 1, 1, 0)}, layout2);

    Rect[] layout3 =
        Layout.empty()
            .withConstraints(new Min(1), new Length(0), new Min(1))
            .withDirection(Direction.Horizontal)
            .split(Rect.of(0, 0, 1, 1));
    assertArrayEquals(
        new Rect[] {Rect.of(0, 0, 1, 1), Rect.of(1, 0, 0, 1), Rect.of(1, 0, 0, 1)}, layout3);

    Rect[] layout4 =
        Layout.empty()
            .withConstraints(new Length(3), new Min(4), new Length(1), new Min(4))
            .withDirection(Direction.Horizontal)
            .split(Rect.of(0, 0, 7, 1));
    assertArrayEquals(
        new Rect[] {
          Rect.of(0, 0, 0, 1), Rect.of(0, 0, 4, 1), Rect.of(4, 0, 0, 1), Rect.of(4, 0, 3, 1)
        },
        layout4);
  }

  // --- range-based parameterised tests ------------------------------------

  static List<int[]> rangesOf(Rect[] rects) {
    List<int[]> out = new ArrayList<>(rects.length);
    for (Rect r : rects) out.add(new int[] {r.left(), r.right()});
    return out;
  }

  static Stream<Arguments> constraint_length_cases() {
    return Stream.of(
        Arguments.of("len_min1", List.of(new Length(25), new Min(100)), ranges(0, 0, 0, 100)),
        Arguments.of("len_min2", List.of(new Length(25), new Min(0)), ranges(0, 25, 25, 100)),
        Arguments.of(
            "len_max1", List.of(new Length(25), new Max(0)), ranges(0, 100, 100, 100)),
        Arguments.of("len_max2", List.of(new Length(25), new Max(100)), ranges(0, 25, 25, 100)),
        Arguments.of(
            "len_perc", List.of(new Length(25), new Percentage(25)), ranges(0, 25, 25, 100)),
        Arguments.of(
            "perc_len", List.of(new Percentage(25), new Length(25)), ranges(0, 75, 75, 100)),
        Arguments.of(
            "len_ratio", List.of(new Length(25), new Ratio(1, 4)), ranges(0, 25, 25, 100)),
        Arguments.of(
            "ratio_len", List.of(new Ratio(1, 4), new Length(25)), ranges(0, 75, 75, 100)),
        Arguments.of("len_len", List.of(new Length(25), new Length(25)), ranges(0, 25, 25, 100)),
        Arguments.of(
            "len1",
            List.of(new Length(25), new Length(25), new Length(25)),
            ranges(0, 25, 25, 50, 50, 100)),
        Arguments.of(
            "len2",
            List.of(new Length(15), new Length(35), new Length(25)),
            ranges(0, 15, 15, 50, 50, 100)),
        Arguments.of(
            "len3",
            List.of(new Length(25), new Length(25), new Length(25)),
            ranges(0, 25, 25, 50, 50, 100)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("constraint_length_cases")
  public void constraint_length(String name, List<Constraint> constraints, int[][] expected) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(Flex.Legacy).split(rect);
    assertRanges(expected, split);
  }

  static Stream<Arguments> table_length_cases() {
    return Stream.of(
        Arguments.of(7, List.of(new Length(4), new Length(4)), ranges(0, 3, 4, 7)),
        Arguments.of(4, List.of(new Length(4), new Length(4)), ranges(0, 2, 3, 4)));
  }

  @ParameterizedTest
  @MethodSource("table_length_cases")
  public void table_length(int width, List<Constraint> constraints, int[][] expected) {
    Rect rect = Rect.of(0, 0, width, 1);
    Rect[] split =
        Layout.horizontal(constraints).withSpacing(1).withFlex(Flex.Start).split(rect);
    assertRanges(expected, split);
  }

  static Stream<Arguments> length_is_higher_priority_cases() {
    return Stream.of(
        Arguments.of(
            "min_len_max",
            List.of(new Min(25), new Length(25), new Max(25)),
            ranges(0, 50, 50, 75, 75, 100)),
        Arguments.of(
            "max_len_min",
            List.of(new Max(25), new Length(25), new Min(25)),
            ranges(0, 25, 25, 50, 50, 100)),
        Arguments.of(
            "len_len_len",
            List.of(new Length(33), new Length(33), new Length(33)),
            ranges(0, 33, 33, 66, 66, 100)),
        Arguments.of(
            "len_len_len_25",
            List.of(new Length(25), new Length(25), new Length(25)),
            ranges(0, 25, 25, 50, 50, 100)),
        Arguments.of(
            "perc_len_ratio",
            List.of(new Percentage(25), new Length(25), new Ratio(1, 4)),
            ranges(0, 25, 25, 50, 50, 100)),
        Arguments.of(
            "len_ratio_perc",
            List.of(new Length(25), new Ratio(1, 4), new Percentage(25)),
            ranges(0, 25, 25, 75, 75, 100)),
        Arguments.of(
            "ratio_len_perc",
            List.of(new Ratio(1, 4), new Length(25), new Percentage(25)),
            ranges(0, 50, 50, 75, 75, 100)),
        Arguments.of(
            "ratio_perc_len",
            List.of(new Ratio(1, 4), new Percentage(25), new Length(25)),
            ranges(0, 50, 50, 75, 75, 100)),
        Arguments.of(
            "len_len_min",
            List.of(new Length(100), new Length(1), new Min(20)),
            ranges(0, 80, 80, 80, 80, 100)),
        Arguments.of(
            "min_len_len",
            List.of(new Min(20), new Length(1), new Length(100)),
            ranges(0, 20, 20, 21, 21, 100)),
        Arguments.of(
            "fill_len_fill",
            List.of(new Fill(1), new Length(10), new Fill(1)),
            ranges(0, 45, 45, 55, 55, 100)),
        Arguments.of(
            "fill_len_fill_2",
            List.of(new Fill(1), new Length(10), new Fill(2)),
            ranges(0, 30, 30, 40, 40, 100)),
        Arguments.of(
            "fill_len_fill_4",
            List.of(new Fill(1), new Length(10), new Fill(4)),
            ranges(0, 18, 18, 28, 28, 100)),
        Arguments.of(
            "fill_len_fill_5",
            List.of(new Fill(1), new Length(10), new Fill(5)),
            ranges(0, 15, 15, 25, 25, 100)),
        Arguments.of(
            "len_len_len_25_2",
            List.of(new Length(25), new Length(25), new Length(25)),
            ranges(0, 25, 25, 50, 50, 100)),
        Arguments.of(
            "unstable_test",
            List.of(new Length(25), new Length(25), new Length(25)),
            ranges(0, 25, 25, 50, 50, 100)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("length_is_higher_priority_cases")
  public void length_is_higher_priority(
      String name, List<Constraint> constraints, int[][] expected) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(Flex.Legacy).split(rect);
    assertRanges(expected, split);
  }

  static Stream<Arguments> length_is_higher_priority_in_flex_cases() {
    return Stream.of(
        Arguments.of(
            "min_len_max",
            List.of(new Min(25), new Length(25), new Max(25)),
            new int[] {50, 25, 25}),
        Arguments.of(
            "max_len_min",
            List.of(new Max(25), new Length(25), new Min(25)),
            new int[] {25, 25, 50}),
        Arguments.of(
            "len_len_len1",
            List.of(new Length(33), new Length(33), new Length(33)),
            new int[] {33, 33, 33}),
        Arguments.of(
            "len_len_len2",
            List.of(new Length(25), new Length(25), new Length(25)),
            new int[] {25, 25, 25}),
        Arguments.of(
            "perc_len_ratio",
            List.of(new Percentage(25), new Length(25), new Ratio(1, 4)),
            new int[] {25, 25, 25}),
        Arguments.of(
            "len_ratio_perc",
            List.of(new Length(25), new Ratio(1, 4), new Percentage(25)),
            new int[] {25, 25, 25}),
        Arguments.of(
            "ratio_len_perc",
            List.of(new Ratio(1, 4), new Length(25), new Percentage(25)),
            new int[] {25, 25, 25}),
        Arguments.of(
            "ratio_perc_len",
            List.of(new Ratio(1, 4), new Percentage(25), new Length(25)),
            new int[] {25, 25, 25}),
        Arguments.of(
            "len_len_min",
            List.of(new Length(100), new Length(1), new Min(20)),
            new int[] {79, 1, 20}),
        Arguments.of(
            "min_len_len",
            List.of(new Min(20), new Length(1), new Length(100)),
            new int[] {20, 1, 79}),
        Arguments.of(
            "fill_len_fill1",
            List.of(new Fill(1), new Length(10), new Fill(1)),
            new int[] {45, 10, 45}),
        Arguments.of(
            "fill_len_fill2",
            List.of(new Fill(1), new Length(10), new Fill(2)),
            new int[] {30, 10, 60}),
        Arguments.of(
            "fill_len_fill4",
            List.of(new Fill(1), new Length(10), new Fill(4)),
            new int[] {18, 10, 72}),
        Arguments.of(
            "fill_len_fill5",
            List.of(new Fill(1), new Length(10), new Fill(5)),
            new int[] {15, 10, 75}),
        Arguments.of(
            "len_len_len3",
            List.of(new Length(25), new Length(25), new Length(25)),
            new int[] {25, 25, 25}));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("length_is_higher_priority_in_flex_cases")
  public void length_is_higher_priority_in_flex(
      String name, List<Constraint> constraints, int[] expected) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Flex[] flexes = {
      Flex.Start, Flex.End, Flex.Center, Flex.SpaceAround, Flex.SpaceEvenly, Flex.SpaceBetween
    };
    for (Flex flex : flexes) {
      Rect[] split = Layout.horizontal(constraints).withFlex(flex).split(rect);
      int[] widths = new int[split.length];
      for (int i = 0; i < split.length; i++) widths[i] = split[i].width();
      assertArrayEquals(expected, widths, "flex=" + flex);
    }
  }

  static Stream<Arguments> fixed_with_50_width_cases() {
    return Stream.of(
        Arguments.of(
            "fill_len_fill",
            List.of(new Fill(1), new Length(10), new Fill(2)),
            ranges(0, 13, 13, 23, 23, 50)),
        Arguments.of(
            "len_fill_fill",
            List.of(new Length(10), new Fill(2), new Fill(1)),
            ranges(0, 10, 10, 37, 37, 50)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fixed_with_50_width_cases")
  public void fixed_with_50_width(String name, List<Constraint> constraints, int[][] expected) {
    Rect rect = Rect.of(0, 0, 50, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(Flex.Legacy).split(rect);
    assertRanges(expected, split);
  }

  static Stream<Arguments> fill_cases() {
    int max = jatatui.core.layout.Position.U16_MAX;
    return Stream.of(
        Arguments.of(
            "same_fill",
            List.of(new Fill(1), new Fill(2), new Fill(1), new Fill(1)),
            ranges(0, 20, 20, 60, 60, 80, 80, 100)),
        Arguments.of(
            "inc_fill",
            List.of(new Fill(1), new Fill(2), new Fill(3), new Fill(4)),
            ranges(0, 10, 10, 30, 30, 60, 60, 100)),
        Arguments.of(
            "dec_fill",
            List.of(new Fill(4), new Fill(3), new Fill(2), new Fill(1)),
            ranges(0, 40, 40, 70, 70, 90, 90, 100)),
        Arguments.of(
            "rand_fill1",
            List.of(new Fill(1), new Fill(3), new Fill(2), new Fill(4)),
            ranges(0, 10, 10, 40, 40, 60, 60, 100)),
        Arguments.of(
            "rand_fill2",
            List.of(new Fill(1), new Fill(3), new Length(50), new Fill(2), new Fill(4)),
            ranges(0, 5, 5, 20, 20, 70, 70, 80, 80, 100)),
        Arguments.of(
            "rand_fill3",
            List.of(new Fill(1), new Fill(3), new Percentage(50), new Fill(2), new Fill(4)),
            ranges(0, 5, 5, 20, 20, 70, 70, 80, 80, 100)),
        Arguments.of(
            "rand_fill4",
            List.of(new Fill(1), new Fill(3), new Min(50), new Fill(2), new Fill(4)),
            ranges(0, 5, 5, 20, 20, 70, 70, 80, 80, 100)),
        Arguments.of(
            "rand_fill5",
            List.of(new Fill(1), new Fill(3), new Max(50), new Fill(2), new Fill(4)),
            ranges(0, 5, 5, 20, 20, 70, 70, 80, 80, 100)),
        Arguments.of(
            "zero_fill1",
            List.of(new Fill(0), new Fill(1), new Fill(0)),
            ranges(0, 0, 0, 100, 100, 100)),
        Arguments.of(
            "zero_fill2",
            List.of(new Fill(0), new Length(1), new Fill(0)),
            ranges(0, 50, 50, 51, 51, 100)),
        Arguments.of(
            "zero_fill3",
            List.of(new Fill(0), new Percentage(1), new Fill(0)),
            ranges(0, 50, 50, 51, 51, 100)),
        Arguments.of(
            "zero_fill4",
            List.of(new Fill(0), new Min(1), new Fill(0)),
            ranges(0, 50, 50, 51, 51, 100)),
        Arguments.of(
            "zero_fill5",
            List.of(new Fill(0), new Max(1), new Fill(0)),
            ranges(0, 50, 50, 51, 51, 100)),
        Arguments.of(
            "zero_fill6",
            List.of(new Fill(0), new Fill(2), new Fill(0), new Fill(1)),
            ranges(0, 0, 0, 67, 67, 67, 67, 100)),
        Arguments.of(
            "space_fill1",
            List.of(new Fill(0), new Fill(2), new Percentage(20)),
            ranges(0, 0, 0, 80, 80, 100)),
        Arguments.of(
            "space_fill2",
            List.of(new Fill(0), new Fill(0), new Percentage(20)),
            ranges(0, 40, 40, 80, 80, 100)),
        Arguments.of(
            "space_fill3", List.of(new Fill(0), new Ratio(1, 5)), ranges(0, 80, 80, 100)),
        Arguments.of(
            "space_fill4", List.of(new Fill(0), new Fill(max)), ranges(0, 0, 0, 100)),
        Arguments.of(
            "space_fill5", List.of(new Fill(max), new Fill(0)), ranges(0, 100, 100, 100)),
        Arguments.of(
            "space_fill6", List.of(new Fill(0), new Percentage(20)), ranges(0, 80, 80, 100)),
        Arguments.of(
            "space_fill7", List.of(new Fill(1), new Percentage(20)), ranges(0, 80, 80, 100)),
        Arguments.of(
            "space_fill8", List.of(new Fill(max), new Percentage(20)), ranges(0, 80, 80, 100)),
        Arguments.of(
            "space_fill9",
            List.of(new Fill(max), new Fill(0), new Percentage(20)),
            ranges(0, 80, 80, 80, 80, 100)),
        Arguments.of("space_fill10", List.of(new Fill(0), new Length(20)), ranges(0, 80, 80, 100)),
        Arguments.of("space_fill11", List.of(new Fill(0), new Min(20)), ranges(0, 80, 80, 100)),
        Arguments.of("space_fill12", List.of(new Fill(0), new Max(20)), ranges(0, 80, 80, 100)),
        Arguments.of(
            "fill_collapse1",
            List.of(new Fill(1), new Fill(1), new Fill(1), new Min(30), new Length(50)),
            ranges(0, 7, 7, 13, 13, 20, 20, 50, 50, 100)),
        Arguments.of(
            "fill_collapse2",
            List.of(new Fill(1), new Fill(1), new Fill(1), new Length(50), new Length(50)),
            ranges(0, 0, 0, 0, 0, 0, 0, 50, 50, 100)),
        Arguments.of(
            "fill_collapse3",
            List.of(new Fill(1), new Fill(1), new Fill(1), new Length(75), new Length(50)),
            ranges(0, 0, 0, 0, 0, 0, 0, 75, 75, 100)),
        Arguments.of(
            "fill_collapse4",
            List.of(new Fill(1), new Fill(1), new Fill(1), new Min(50), new Max(50)),
            ranges(0, 0, 0, 0, 0, 0, 0, 50, 50, 100)),
        Arguments.of(
            "fill_collapse5",
            List.of(new Fill(1), new Fill(1), new Fill(1), new Ratio(1, 1)),
            ranges(0, 0, 0, 0, 0, 0, 0, 100)),
        Arguments.of(
            "fill_collapse6",
            List.of(new Fill(1), new Fill(1), new Fill(1), new Percentage(100)),
            ranges(0, 0, 0, 0, 0, 0, 0, 100)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fill_cases")
  public void fill(String name, List<Constraint> constraints, int[][] expected) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(Flex.Legacy).split(rect);
    assertRanges(expected, split);
  }

  static Stream<Arguments> percentage_parameterized_cases() {
    return Stream.of(
        Arguments.of(
            "min_percentage", List.of(new Min(0), new Percentage(20)), ranges(0, 80, 80, 100)),
        Arguments.of(
            "max_percentage", List.of(new Max(0), new Percentage(20)), ranges(0, 0, 0, 100)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("percentage_parameterized_cases")
  public void percentage_parameterized(
      String name, List<Constraint> constraints, int[][] expected) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(Flex.Legacy).split(rect);
    assertRanges(expected, split);
  }

  static Stream<Arguments> min_max_cases() {
    int max = jatatui.core.layout.Position.U16_MAX;
    return Stream.of(
        Arguments.of("max_min", List.of(new Max(100), new Min(0)), ranges(0, 100, 100, 100)),
        Arguments.of("min_max", List.of(new Min(0), new Max(100)), ranges(0, 0, 0, 100)),
        Arguments.of(
            "length_min", List.of(new Length(max), new Min(10)), ranges(0, 90, 90, 100)),
        Arguments.of(
            "min_length", List.of(new Min(10), new Length(max)), ranges(0, 10, 10, 100)),
        Arguments.of("length_max", List.of(new Length(0), new Max(10)), ranges(0, 90, 90, 100)),
        Arguments.of("max_length", List.of(new Max(10), new Length(0)), ranges(0, 10, 10, 100)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("min_max_cases")
  public void min_max(String name, List<Constraint> constraints, int[][] expected) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(Flex.Legacy).split(rect);
    assertRanges(expected, split);
  }

  static Stream<Arguments> flex_constraint_cases() {
    return Stream.of(
        Arguments.of("length_legacy", List.of(new Length(50)), ranges(0, 100), Flex.Legacy),
        Arguments.of("length_start", List.of(new Length(50)), ranges(0, 50), Flex.Start),
        Arguments.of("length_end", List.of(new Length(50)), ranges(50, 100), Flex.End),
        Arguments.of("length_center", List.of(new Length(50)), ranges(25, 75), Flex.Center),
        Arguments.of("ratio_legacy", List.of(new Ratio(1, 2)), ranges(0, 100), Flex.Legacy),
        Arguments.of("ratio_start", List.of(new Ratio(1, 2)), ranges(0, 50), Flex.Start),
        Arguments.of("ratio_end", List.of(new Ratio(1, 2)), ranges(50, 100), Flex.End),
        Arguments.of("ratio_center", List.of(new Ratio(1, 2)), ranges(25, 75), Flex.Center),
        Arguments.of("percent_legacy", List.of(new Percentage(50)), ranges(0, 100), Flex.Legacy),
        Arguments.of("percent_start", List.of(new Percentage(50)), ranges(0, 50), Flex.Start),
        Arguments.of("percent_end", List.of(new Percentage(50)), ranges(50, 100), Flex.End),
        Arguments.of("percent_center", List.of(new Percentage(50)), ranges(25, 75), Flex.Center),
        Arguments.of("min_legacy", List.of(new Min(50)), ranges(0, 100), Flex.Legacy),
        Arguments.of("min_start", List.of(new Min(50)), ranges(0, 100), Flex.Start),
        Arguments.of("min_end", List.of(new Min(50)), ranges(0, 100), Flex.End),
        Arguments.of("min_center", List.of(new Min(50)), ranges(0, 100), Flex.Center),
        Arguments.of("max_legacy", List.of(new Max(50)), ranges(0, 100), Flex.Legacy),
        Arguments.of("max_start", List.of(new Max(50)), ranges(0, 50), Flex.Start),
        Arguments.of("max_end", List.of(new Max(50)), ranges(50, 100), Flex.End),
        Arguments.of("max_center", List.of(new Max(50)), ranges(25, 75), Flex.Center),
        Arguments.of(
            "spacebetween_becomes_stretch1",
            List.of(new Min(1)),
            ranges(0, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "spacebetween_becomes_stretch2",
            List.of(new Max(20)),
            ranges(0, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "spacebetween_becomes_stretch3",
            List.of(new Length(20)),
            ranges(0, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "length_legacy2",
            List.of(new Length(25), new Length(25)),
            ranges(0, 25, 25, 100),
            Flex.Legacy),
        Arguments.of(
            "length_start2",
            List.of(new Length(25), new Length(25)),
            ranges(0, 25, 25, 50),
            Flex.Start),
        Arguments.of(
            "length_center2",
            List.of(new Length(25), new Length(25)),
            ranges(25, 50, 50, 75),
            Flex.Center),
        Arguments.of(
            "length_end2",
            List.of(new Length(25), new Length(25)),
            ranges(50, 75, 75, 100),
            Flex.End),
        Arguments.of(
            "length_spacebetween",
            List.of(new Length(25), new Length(25)),
            ranges(0, 25, 75, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "length_spaceevenly",
            List.of(new Length(25), new Length(25)),
            ranges(17, 42, 58, 83),
            Flex.SpaceEvenly),
        Arguments.of(
            "length_spacearound",
            List.of(new Length(25), new Length(25)),
            ranges(13, 38, 63, 88),
            Flex.SpaceAround),
        Arguments.of(
            "percentage_legacy",
            List.of(new Percentage(25), new Percentage(25)),
            ranges(0, 25, 25, 100),
            Flex.Legacy),
        Arguments.of(
            "percentage_start",
            List.of(new Percentage(25), new Percentage(25)),
            ranges(0, 25, 25, 50),
            Flex.Start),
        Arguments.of(
            "percentage_center",
            List.of(new Percentage(25), new Percentage(25)),
            ranges(25, 50, 50, 75),
            Flex.Center),
        Arguments.of(
            "percentage_end",
            List.of(new Percentage(25), new Percentage(25)),
            ranges(50, 75, 75, 100),
            Flex.End),
        Arguments.of(
            "percentage_spacebetween",
            List.of(new Percentage(25), new Percentage(25)),
            ranges(0, 25, 75, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "percentage_spaceevenly",
            List.of(new Percentage(25), new Percentage(25)),
            ranges(17, 42, 58, 83),
            Flex.SpaceEvenly),
        Arguments.of(
            "percentage_spacearound",
            List.of(new Percentage(25), new Percentage(25)),
            ranges(13, 38, 63, 88),
            Flex.SpaceAround),
        Arguments.of(
            "min_legacy2",
            List.of(new Min(25), new Min(25)),
            ranges(0, 25, 25, 100),
            Flex.Legacy),
        Arguments.of(
            "min_start2", List.of(new Min(25), new Min(25)), ranges(0, 50, 50, 100), Flex.Start),
        Arguments.of(
            "min_center2",
            List.of(new Min(25), new Min(25)),
            ranges(0, 50, 50, 100),
            Flex.Center),
        Arguments.of(
            "min_end2", List.of(new Min(25), new Min(25)), ranges(0, 50, 50, 100), Flex.End),
        Arguments.of(
            "min_spacebetween",
            List.of(new Min(25), new Min(25)),
            ranges(0, 50, 50, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "min_spaceevenly",
            List.of(new Min(25), new Min(25)),
            ranges(0, 50, 50, 100),
            Flex.SpaceEvenly),
        Arguments.of(
            "min_spacearound",
            List.of(new Min(25), new Min(25)),
            ranges(0, 50, 50, 100),
            Flex.SpaceAround),
        Arguments.of(
            "max_legacy2",
            List.of(new Max(25), new Max(25)),
            ranges(0, 25, 25, 100),
            Flex.Legacy),
        Arguments.of(
            "max_start2", List.of(new Max(25), new Max(25)), ranges(0, 25, 25, 50), Flex.Start),
        Arguments.of(
            "max_center2",
            List.of(new Max(25), new Max(25)),
            ranges(25, 50, 50, 75),
            Flex.Center),
        Arguments.of(
            "max_end2", List.of(new Max(25), new Max(25)), ranges(50, 75, 75, 100), Flex.End),
        Arguments.of(
            "max_spacebetween",
            List.of(new Max(25), new Max(25)),
            ranges(0, 25, 75, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "max_spaceevenly",
            List.of(new Max(25), new Max(25)),
            ranges(17, 42, 58, 83),
            Flex.SpaceEvenly),
        Arguments.of(
            "max_spacearound",
            List.of(new Max(25), new Max(25)),
            ranges(13, 38, 63, 88),
            Flex.SpaceAround),
        Arguments.of(
            "length_spaced_around",
            List.of(new Length(25), new Length(25), new Length(25)),
            ranges(0, 25, 38, 63, 75, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "one_segment_legacy", List.of(new Length(50)), ranges(0, 100), Flex.Legacy),
        Arguments.of("one_segment_start", List.of(new Length(50)), ranges(0, 50), Flex.Start),
        Arguments.of("one_segment_end", List.of(new Length(50)), ranges(50, 100), Flex.End),
        Arguments.of(
            "one_segment_center", List.of(new Length(50)), ranges(25, 75), Flex.Center),
        Arguments.of(
            "one_segment_spacebetween",
            List.of(new Length(50)),
            ranges(0, 100),
            Flex.SpaceBetween),
        Arguments.of(
            "one_segment_spaceevenly",
            List.of(new Length(50)),
            ranges(25, 75),
            Flex.SpaceEvenly),
        Arguments.of(
            "one_segment_spacearound",
            List.of(new Length(50)),
            ranges(25, 75),
            Flex.SpaceAround));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("flex_constraint_cases")
  public void flex_constraint(
      String name, List<Constraint> constraints, int[][] expected, Flex flex) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(flex).split(rect);
    assertRanges(expected, split);
  }

  static Stream<Arguments> flex_overlap_cases() {
    return Stream.of(
        Arguments.of(
            "length_overlap1",
            xw(0, 20, 20, 20, 40, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Start,
            0),
        Arguments.of(
            "length_overlap2",
            xw(0, 20, 19, 20, 38, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Start,
            -1),
        Arguments.of(
            "length_overlap3",
            xw(21, 20, 40, 20, 59, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Center,
            -1),
        Arguments.of(
            "length_overlap4",
            xw(42, 20, 61, 20, 80, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.End,
            -1),
        Arguments.of(
            "length_overlap5",
            xw(0, 20, 19, 20, 38, 62),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Legacy,
            -1),
        Arguments.of(
            "length_overlap6",
            xw(0, 20, 40, 20, 80, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.SpaceBetween,
            -1),
        Arguments.of(
            "length_overlap7",
            xw(10, 20, 40, 20, 70, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.SpaceEvenly,
            -1),
        Arguments.of(
            "length_overlap8",
            xw(7, 20, 40, 20, 73, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.SpaceAround,
            -1));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("flex_overlap_cases")
  public void flex_overlap(
      String name, int[][] expected, List<Constraint> constraints, Flex flex, int spacing) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split =
        Layout.horizontal(constraints).withFlex(flex).withSpacing(spacing).split(rect);
    assertXw(expected, split);
  }

  static Stream<Arguments> flex_spacing_cases() {
    return Stream.of(
        Arguments.of(
            "0",
            xw(0, 20, 20, 20, 40, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Start,
            0),
        Arguments.of(
            "2_start",
            xw(0, 20, 22, 20, 44, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Start,
            2),
        Arguments.of(
            "2_center",
            xw(18, 20, 40, 20, 62, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Center,
            2),
        Arguments.of(
            "2_end",
            xw(36, 20, 58, 20, 80, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.End,
            2),
        Arguments.of(
            "2_legacy",
            xw(0, 20, 22, 20, 44, 56),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Legacy,
            2),
        Arguments.of(
            "2_spacebetween",
            xw(0, 20, 40, 20, 80, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.SpaceBetween,
            2),
        Arguments.of(
            "2_spaceevenly",
            xw(10, 20, 40, 20, 70, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.SpaceEvenly,
            2),
        Arguments.of(
            "2_spacearound",
            xw(7, 20, 40, 20, 73, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.SpaceAround,
            2));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("flex_spacing_cases")
  public void flex_spacing(
      String name, int[][] expected, List<Constraint> constraints, Flex flex, int spacing) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split =
        Layout.horizontal(constraints).withFlex(flex).withSpacing(spacing).split(rect);
    assertXw(expected, split);
  }

  static Stream<Arguments> constraint_specification_tests_for_priority_cases() {
    return Stream.of(
        Arguments.of("a", xw(0, 25, 25, 75), List.of(new Length(25), new Length(25))),
        Arguments.of("b", xw(0, 25, 25, 75), List.of(new Length(25), new Percentage(25))),
        Arguments.of("c", xw(0, 75, 75, 25), List.of(new Percentage(25), new Length(25))),
        Arguments.of("d", xw(0, 75, 75, 25), List.of(new Min(25), new Percentage(25))),
        Arguments.of("e", xw(0, 25, 25, 75), List.of(new Percentage(25), new Min(25))),
        Arguments.of("f", xw(0, 25, 25, 75), List.of(new Min(25), new Percentage(100))),
        Arguments.of("g", xw(0, 75, 75, 25), List.of(new Percentage(100), new Min(25))),
        Arguments.of("h", xw(0, 25, 25, 75), List.of(new Max(75), new Percentage(75))),
        Arguments.of("i", xw(0, 75, 75, 25), List.of(new Percentage(75), new Max(75))),
        Arguments.of("j", xw(0, 25, 25, 75), List.of(new Max(25), new Percentage(25))),
        Arguments.of("k", xw(0, 75, 75, 25), List.of(new Percentage(25), new Max(25))),
        Arguments.of("l", xw(0, 25, 25, 75), List.of(new Length(25), new Ratio(1, 4))),
        Arguments.of("m", xw(0, 75, 75, 25), List.of(new Ratio(1, 4), new Length(25))),
        Arguments.of("n", xw(0, 25, 25, 75), List.of(new Percentage(25), new Ratio(1, 4))),
        Arguments.of("o", xw(0, 75, 75, 25), List.of(new Ratio(1, 4), new Percentage(25))),
        Arguments.of("p", xw(0, 25, 25, 75), List.of(new Ratio(1, 4), new Fill(25))),
        Arguments.of("q", xw(0, 75, 75, 25), List.of(new Fill(25), new Ratio(1, 4))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("constraint_specification_tests_for_priority_cases")
  public void constraint_specification_tests_for_priority(
      String name, int[][] expected, List<Constraint> constraints) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(Flex.Legacy).split(rect);
    assertXw(expected, split);
  }

  static Stream<Arguments> constraint_specification_tests_for_priority_with_spacing_cases() {
    return Stream.of(
        Arguments.of(
            "a",
            xw(0, 20, 20, 20, 40, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Start,
            0),
        Arguments.of(
            "b",
            xw(18, 20, 40, 20, 62, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Center,
            2),
        Arguments.of(
            "c",
            xw(36, 20, 58, 20, 80, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.End,
            2),
        Arguments.of(
            "d",
            xw(0, 20, 22, 20, 44, 56),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Legacy,
            2),
        Arguments.of(
            "e",
            xw(0, 20, 22, 20, 44, 56),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.Legacy,
            2),
        Arguments.of(
            "f",
            xw(10, 20, 40, 20, 70, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.SpaceEvenly,
            2),
        Arguments.of(
            "g",
            xw(7, 20, 40, 20, 73, 20),
            List.of(new Length(20), new Length(20), new Length(20)),
            Flex.SpaceAround,
            2));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("constraint_specification_tests_for_priority_with_spacing_cases")
  public void constraint_specification_tests_for_priority_with_spacing(
      String name, int[][] expected, List<Constraint> constraints, Flex flex, int spacing) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split =
        Layout.horizontal(constraints).withSpacing(spacing).withFlex(flex).split(rect);
    assertXw(expected, split);
  }

  static Stream<Arguments> fill_vs_flex_cases() {
    return Stream.of(
        Arguments.of(
            "prop1",
            xw(0, 10, 10, 80, 90, 10),
            List.of(new Length(10), new Fill(1), new Length(10)),
            Flex.Legacy),
        Arguments.of(
            "flex1", xw(0, 10, 90, 10), List.of(new Length(10), new Length(10)), Flex.SpaceBetween),
        Arguments.of(
            "prop2",
            xw(0, 27, 27, 10, 37, 26, 63, 10, 73, 27),
            List.of(new Fill(1), new Length(10), new Fill(1), new Length(10), new Fill(1)),
            Flex.Legacy),
        Arguments.of(
            "flex2",
            xw(27, 10, 63, 10),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceEvenly),
        Arguments.of(
            "prop3",
            xw(0, 10, 10, 10, 20, 80),
            List.of(new Length(10), new Length(10), new Fill(1)),
            Flex.Legacy),
        Arguments.of(
            "flex3", xw(0, 10, 10, 10), List.of(new Length(10), new Length(10)), Flex.Start),
        Arguments.of(
            "prop4",
            xw(0, 80, 80, 10, 90, 10),
            List.of(new Fill(1), new Length(10), new Length(10)),
            Flex.Legacy),
        Arguments.of(
            "flex4", xw(80, 10, 90, 10), List.of(new Length(10), new Length(10)), Flex.End),
        Arguments.of(
            "prop5",
            xw(0, 40, 40, 10, 50, 10, 60, 40),
            List.of(new Fill(1), new Length(10), new Length(10), new Fill(1)),
            Flex.Legacy),
        Arguments.of(
            "flex5", xw(40, 10, 50, 10), List.of(new Length(10), new Length(10)), Flex.Center),
        Arguments.of(
            "flex6",
            xw(20, 10, 70, 10),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceAround));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fill_vs_flex_cases")
  public void fill_vs_flex(
      String name, int[][] expected, List<Constraint> constraints, Flex flex) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(flex).split(rect);
    assertXw(expected, split);
  }

  static Stream<Arguments> fill_spacing_cases() {
    return Stream.of(
        Arguments.of(
            "flex0_legacy", xw(0, 50, 50, 50), List.of(new Fill(1), new Fill(1)), Flex.Legacy, 0),
        Arguments.of(
            "flex0_spaceevenly",
            xw(0, 50, 50, 50),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceEvenly,
            0),
        Arguments.of(
            "flex0_spacebetween",
            xw(0, 50, 50, 50),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceBetween,
            0),
        Arguments.of(
            "flex0_spacearound",
            xw(0, 50, 50, 50),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceAround,
            0),
        Arguments.of(
            "flex0_start", xw(0, 50, 50, 50), List.of(new Fill(1), new Fill(1)), Flex.Start, 0),
        Arguments.of(
            "flex0_center", xw(0, 50, 50, 50), List.of(new Fill(1), new Fill(1)), Flex.Center, 0),
        Arguments.of(
            "flex0_end", xw(0, 50, 50, 50), List.of(new Fill(1), new Fill(1)), Flex.End, 0),
        Arguments.of(
            "flex10_legacy",
            xw(0, 45, 55, 45),
            List.of(new Fill(1), new Fill(1)),
            Flex.Legacy,
            10),
        Arguments.of(
            "flex10_start", xw(0, 45, 55, 45), List.of(new Fill(1), new Fill(1)), Flex.Start, 10),
        Arguments.of(
            "flex10_center",
            xw(0, 45, 55, 45),
            List.of(new Fill(1), new Fill(1)),
            Flex.Center,
            10),
        Arguments.of(
            "flex10_end", xw(0, 45, 55, 45), List.of(new Fill(1), new Fill(1)), Flex.End, 10),
        Arguments.of(
            "flex10_spaceevenly",
            xw(10, 35, 55, 35),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceEvenly,
            10),
        Arguments.of(
            "flex10_spacebetween",
            xw(0, 45, 55, 45),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceBetween,
            10),
        Arguments.of(
            "flex10_spacearound",
            xw(10, 30, 60, 30),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceAround,
            10),
        Arguments.of(
            "flex_length0_legacy",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Legacy,
            0),
        Arguments.of(
            "flex_length0_spaceevenly",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceEvenly,
            0),
        Arguments.of(
            "flex_length0_spacebetween",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceBetween,
            0),
        Arguments.of(
            "flex_length0_spacearound",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceAround,
            0),
        Arguments.of(
            "flex_length0_start",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Start,
            0),
        Arguments.of(
            "flex_length0_center",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Center,
            0),
        Arguments.of(
            "flex_length0_end",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.End,
            0),
        Arguments.of(
            "flex_length10_legacy",
            xw(0, 35, 45, 10, 65, 35),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Legacy,
            10),
        Arguments.of(
            "flex_length10_start",
            xw(0, 35, 45, 10, 65, 35),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Start,
            10),
        Arguments.of(
            "flex_length10_center",
            xw(0, 35, 45, 10, 65, 35),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Center,
            10),
        Arguments.of(
            "flex_length10_end",
            xw(0, 35, 45, 10, 65, 35),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.End,
            10),
        Arguments.of(
            "flex_length10_spaceevenly",
            xw(10, 25, 45, 10, 65, 25),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceEvenly,
            10),
        Arguments.of(
            "flex_length10_spacebetween",
            xw(0, 35, 45, 10, 65, 35),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceBetween,
            10),
        Arguments.of(
            "flex_length10_spacearound",
            xw(10, 15, 45, 10, 75, 15),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceAround,
            10));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fill_spacing_cases")
  public void fill_spacing(
      String name, int[][] expected, List<Constraint> constraints, Flex flex, int spacing) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split =
        Layout.horizontal(constraints).withFlex(flex).withSpacing(spacing).split(rect);
    assertXw(expected, split);
  }

  static Stream<Arguments> fill_overlap_cases() {
    return Stream.of(
        Arguments.of(
            "flex0_1", xw(0, 55, 45, 55), List.of(new Fill(1), new Fill(1)), Flex.Legacy, -10),
        Arguments.of(
            "flex0_2",
            xw(0, 50, 50, 50),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceAround,
            -10),
        Arguments.of(
            "flex0_3",
            xw(0, 55, 45, 55),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceBetween,
            -10),
        Arguments.of(
            "flex0_4", xw(0, 55, 45, 55), List.of(new Fill(1), new Fill(1)), Flex.Start, -10),
        Arguments.of(
            "flex0_5", xw(0, 55, 45, 55), List.of(new Fill(1), new Fill(1)), Flex.Center, -10),
        Arguments.of(
            "flex0_6", xw(0, 55, 45, 55), List.of(new Fill(1), new Fill(1)), Flex.End, -10),
        Arguments.of(
            "flex0_7",
            xw(0, 50, 50, 50),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceEvenly,
            -10),
        Arguments.of(
            "flex10_1", xw(0, 51, 50, 50), List.of(new Fill(1), new Fill(1)), Flex.Legacy, -1),
        Arguments.of(
            "flex10_2", xw(0, 51, 50, 50), List.of(new Fill(1), new Fill(1)), Flex.Start, -1),
        Arguments.of(
            "flex10_3", xw(0, 51, 50, 50), List.of(new Fill(1), new Fill(1)), Flex.Center, -1),
        Arguments.of(
            "flex10_4", xw(0, 51, 50, 50), List.of(new Fill(1), new Fill(1)), Flex.End, -1),
        Arguments.of(
            "flex10_5",
            xw(0, 50, 50, 50),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceAround,
            -1),
        Arguments.of(
            "flex10_6",
            xw(0, 51, 50, 50),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceBetween,
            -1),
        Arguments.of(
            "flex10_7",
            xw(0, 50, 50, 50),
            List.of(new Fill(1), new Fill(1)),
            Flex.SpaceEvenly,
            -1),
        Arguments.of(
            "flex_length0_1",
            xw(0, 55, 45, 10, 45, 55),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Legacy,
            -10),
        Arguments.of(
            "flex_length0_2",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceAround,
            -10),
        Arguments.of(
            "flex_length0_3",
            xw(0, 55, 45, 10, 45, 55),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceBetween,
            -10),
        Arguments.of(
            "flex_length0_4",
            xw(0, 55, 45, 10, 45, 55),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Start,
            -10),
        Arguments.of(
            "flex_length0_5",
            xw(0, 55, 45, 10, 45, 55),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Center,
            -10),
        Arguments.of(
            "flex_length0_6",
            xw(0, 55, 45, 10, 45, 55),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.End,
            -10),
        Arguments.of(
            "flex_length0_7",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceEvenly,
            -10),
        Arguments.of(
            "flex_length10_1",
            xw(0, 46, 45, 10, 54, 46),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Legacy,
            -1),
        Arguments.of(
            "flex_length10_2",
            xw(0, 46, 45, 10, 54, 46),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Start,
            -1),
        Arguments.of(
            "flex_length10_3",
            xw(0, 46, 45, 10, 54, 46),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.Center,
            -1),
        Arguments.of(
            "flex_length10_4",
            xw(0, 46, 45, 10, 54, 46),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.End,
            -1),
        Arguments.of(
            "flex_length10_5",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceAround,
            -1),
        Arguments.of(
            "flex_length10_6",
            xw(0, 46, 45, 10, 54, 46),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceBetween,
            -1),
        Arguments.of(
            "flex_length10_7",
            xw(0, 45, 45, 10, 55, 45),
            List.of(new Fill(1), new Length(10), new Fill(1)),
            Flex.SpaceEvenly,
            -1));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fill_overlap_cases")
  public void fill_overlap(
      String name, int[][] expected, List<Constraint> constraints, Flex flex, int spacing) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split =
        Layout.horizontal(constraints).withFlex(flex).withSpacing(spacing).split(rect);
    assertXw(expected, split);
  }

  @Test
  public void flex_spacing_lower_priority_than_user_spacing() {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split =
        Layout.horizontal(new Length(10), new Length(10))
            .withFlex(Flex.Center)
            .withSpacing(80)
            .split(rect);
    assertXw(xw(0, 10, 90, 10), split);
  }

  // --- spacers tests ------------------------------------------------------

  static Stream<Arguments> split_with_spacers_no_spacing_cases() {
    return Stream.of(
        Arguments.of(
            "legacy", xw(0, 0, 10, 0, 100, 0), List.of(new Length(10), new Length(10)), Flex.Legacy),
        Arguments.of(
            "spacebetween",
            xw(0, 0, 10, 80, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceBetween),
        Arguments.of(
            "spaceevenly",
            xw(0, 27, 37, 26, 73, 27),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceEvenly),
        Arguments.of(
            "spacearound",
            xw(0, 20, 30, 40, 80, 20),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceAround),
        Arguments.of(
            "start", xw(0, 0, 10, 0, 20, 80), List.of(new Length(10), new Length(10)), Flex.Start),
        Arguments.of(
            "center",
            xw(0, 40, 50, 0, 60, 40),
            List.of(new Length(10), new Length(10)),
            Flex.Center),
        Arguments.of(
            "end", xw(0, 80, 90, 0, 100, 0), List.of(new Length(10), new Length(10)), Flex.End));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("split_with_spacers_no_spacing_cases")
  public void split_with_spacers_no_spacing(
      String name, int[][] expected, List<Constraint> constraints, Flex flex) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] spacers = Layout.horizontal(constraints).withFlex(flex).splitWithSpacers(rect).spacers();
    assertEquals(constraints.size() + 1, spacers.length);
    assertXw(expected, spacers);
  }

  static Stream<Arguments> split_with_spacers_and_spacing_cases() {
    return Stream.of(
        Arguments.of(
            "legacy",
            xw(0, 0, 10, 5, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.Legacy,
            5),
        Arguments.of(
            "spacebetween",
            xw(0, 0, 10, 80, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceBetween,
            5),
        Arguments.of(
            "spaceevenly",
            xw(0, 27, 37, 26, 73, 27),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceEvenly,
            5),
        Arguments.of(
            "spacearound",
            xw(0, 20, 30, 40, 80, 20),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceAround,
            5),
        Arguments.of(
            "start",
            xw(0, 0, 10, 5, 25, 75),
            List.of(new Length(10), new Length(10)),
            Flex.Start,
            5),
        Arguments.of(
            "center",
            xw(0, 38, 48, 5, 63, 37),
            List.of(new Length(10), new Length(10)),
            Flex.Center,
            5),
        Arguments.of(
            "end",
            xw(0, 75, 85, 5, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.End,
            5));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("split_with_spacers_and_spacing_cases")
  public void split_with_spacers_and_spacing(
      String name, int[][] expected, List<Constraint> constraints, Flex flex, int spacing) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] spacers =
        Layout.horizontal(constraints)
            .withFlex(flex)
            .withSpacing(spacing)
            .splitWithSpacers(rect)
            .spacers();
    assertEquals(constraints.size() + 1, spacers.length);
    assertXw(expected, spacers);
  }

  static Stream<Arguments> split_with_spacers_and_overlap_cases() {
    return Stream.of(
        Arguments.of(
            "legacy",
            xw(0, 0, 10, 0, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.Legacy,
            -1),
        Arguments.of(
            "spacebetween",
            xw(0, 0, 10, 80, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceBetween,
            -1),
        Arguments.of(
            "spaceevenly",
            xw(0, 27, 37, 26, 73, 27),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceEvenly,
            -1),
        Arguments.of(
            "spacearound",
            xw(0, 20, 30, 40, 80, 20),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceAround,
            -1),
        Arguments.of(
            "start",
            xw(0, 0, 10, 0, 19, 81),
            List.of(new Length(10), new Length(10)),
            Flex.Start,
            -1),
        Arguments.of(
            "center",
            xw(0, 41, 51, 0, 60, 40),
            List.of(new Length(10), new Length(10)),
            Flex.Center,
            -1),
        Arguments.of(
            "end",
            xw(0, 81, 91, 0, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.End,
            -1));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("split_with_spacers_and_overlap_cases")
  public void split_with_spacers_and_overlap(
      String name, int[][] expected, List<Constraint> constraints, Flex flex, int spacing) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] spacers =
        Layout.horizontal(constraints)
            .withFlex(flex)
            .withSpacing(spacing)
            .splitWithSpacers(rect)
            .spacers();
    assertEquals(constraints.size() + 1, spacers.length);
    assertXw(expected, spacers);
  }

  static Stream<Arguments> split_with_spacers_and_too_much_spacing_cases() {
    return Stream.of(
        Arguments.of(
            "legacy",
            xw(0, 0, 0, 100, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.Legacy,
            200),
        Arguments.of(
            "spacebetween",
            xw(0, 0, 0, 100, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceBetween,
            200),
        Arguments.of(
            "spaceevenly",
            xw(0, 33, 33, 34, 67, 33),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceEvenly,
            200),
        Arguments.of(
            "spacearound",
            xw(0, 25, 25, 50, 75, 25),
            List.of(new Length(10), new Length(10)),
            Flex.SpaceAround,
            200),
        Arguments.of(
            "start",
            xw(0, 0, 0, 100, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.Start,
            200),
        Arguments.of(
            "center",
            xw(0, 0, 0, 100, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.Center,
            200),
        Arguments.of(
            "end",
            xw(0, 0, 0, 100, 100, 0),
            List.of(new Length(10), new Length(10)),
            Flex.End,
            200));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("split_with_spacers_and_too_much_spacing_cases")
  public void split_with_spacers_and_too_much_spacing(
      String name, int[][] expected, List<Constraint> constraints, Flex flex, int spacing) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] spacers =
        Layout.horizontal(constraints)
            .withFlex(flex)
            .withSpacing(spacing)
            .splitWithSpacers(rect)
            .spacers();
    assertEquals(constraints.size() + 1, spacers.length);
    assertXw(expected, spacers);
  }

  static Stream<Arguments> legacy_vs_default_cases() {
    return Stream.of(
        Arguments.of(
            "compare1", xw(0, 90, 90, 10), List.of(new Min(10), new Length(10)), Flex.Legacy),
        Arguments.of(
            "compare2", xw(0, 90, 90, 10), List.of(new Min(10), new Length(10)), Flex.Start),
        Arguments.of(
            "compare3", xw(0, 10, 10, 90), List.of(new Min(10), new Percentage(100)), Flex.Legacy),
        Arguments.of(
            "compare4", xw(0, 10, 10, 90), List.of(new Min(10), new Percentage(100)), Flex.Start),
        Arguments.of(
            "compare5",
            xw(0, 50, 50, 50),
            List.of(new Percentage(50), new Percentage(50)),
            Flex.Legacy),
        Arguments.of(
            "compare6",
            xw(0, 50, 50, 50),
            List.of(new Percentage(50), new Percentage(50)),
            Flex.Start));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("legacy_vs_default_cases")
  public void legacy_vs_default(
      String name, int[][] expected, List<Constraint> constraints, Flex flex) {
    Rect rect = Rect.of(0, 0, 100, 1);
    Rect[] split = Layout.horizontal(constraints).withFlex(flex).split(rect);
    assertXw(expected, split);
  }

  // --- helpers ------------------------------------------------------------

  /// Build a parameterised test case for the `letters`-style tests.
  private static Arguments cs(Flex flex, int width, String expected, Constraint... cs) {
    return Arguments.of(flex, width, expected, Arrays.asList(cs));
  }

  /// Build an `int[][]` of `[start, end]` pairs from a flat sequence of integers.
  private static int[][] ranges(int... xs) {
    int n = xs.length / 2;
    int[][] out = new int[n][2];
    for (int i = 0; i < n; i++) {
      out[i][0] = xs[2 * i];
      out[i][1] = xs[2 * i + 1];
    }
    return out;
  }

  /// Build an `int[][]` of `[x, width]` pairs from a flat sequence of integers.
  private static int[][] xw(int... xs) {
    return ranges(xs);
  }

  private static void assertRanges(int[][] expected, Rect[] actual) {
    assertEquals(expected.length, actual.length, "different number of ranges");
    for (int i = 0; i < expected.length; i++) {
      int[] e = expected[i];
      assertEquals(e[0], actual[i].left(), "rect " + i + " start");
      assertEquals(e[1], actual[i].right(), "rect " + i + " end");
    }
  }

  private static void assertXw(int[][] expected, Rect[] actual) {
    assertEquals(expected.length, actual.length, "different number of segments");
    for (int i = 0; i < expected.length; i++) {
      int[] e = expected[i];
      assertEquals(e[0], actual[i].x(), "rect " + i + " x");
      assertEquals(e[1], actual[i].width(), "rect " + i + " width");
    }
  }
}
