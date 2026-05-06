package jatatui.tests.crossterm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.crossterm.ContentStyle;
import jatatui.crossterm.CrosstermColorConv;
import jatatui.crossterm.CrosstermModifierConv;
import jatatui.crossterm.CrosstermStyleConv;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tui.crossterm.Attribute;
import tui.crossterm.Attributes;

/// Port of the inline `#[cfg(test)] mod tests` from upstream `ratatui-crossterm/src/lib.rs`.
///
/// Upstream's tests are pure conversion tests (Color, Modifier, Style). The `draw` /
/// `set_cursor_position` integration tests in upstream go through a captured `Vec<u8>` writer
/// to assert exact ANSI escape sequences; the local JNI binding we drive does not expose any
/// way to intercept the bytes written to the terminal, so the conversion tests are the
/// portable subset.
public class CrosstermBackendTest {

  // ---- from_crossterm_color ----

  static Stream<Arguments> from_crossterm_color_cases() {
    return Stream.of(
        Arguments.of(new tui.crossterm.Color.Reset(), Color.RESET),
        Arguments.of(new tui.crossterm.Color.Black(), Color.BLACK),
        Arguments.of(new tui.crossterm.Color.DarkGrey(), Color.DARK_GRAY),
        Arguments.of(new tui.crossterm.Color.Red(), Color.LIGHT_RED),
        Arguments.of(new tui.crossterm.Color.DarkRed(), Color.RED),
        Arguments.of(new tui.crossterm.Color.Green(), Color.LIGHT_GREEN),
        Arguments.of(new tui.crossterm.Color.DarkGreen(), Color.GREEN),
        Arguments.of(new tui.crossterm.Color.Yellow(), Color.LIGHT_YELLOW),
        Arguments.of(new tui.crossterm.Color.DarkYellow(), Color.YELLOW),
        Arguments.of(new tui.crossterm.Color.Blue(), Color.LIGHT_BLUE),
        Arguments.of(new tui.crossterm.Color.DarkBlue(), Color.BLUE),
        Arguments.of(new tui.crossterm.Color.Magenta(), Color.LIGHT_MAGENTA),
        Arguments.of(new tui.crossterm.Color.DarkMagenta(), Color.MAGENTA),
        Arguments.of(new tui.crossterm.Color.Cyan(), Color.LIGHT_CYAN),
        Arguments.of(new tui.crossterm.Color.DarkCyan(), Color.CYAN),
        Arguments.of(new tui.crossterm.Color.White(), Color.WHITE),
        Arguments.of(new tui.crossterm.Color.Grey(), Color.GRAY),
        Arguments.of(new tui.crossterm.Color.Rgb(0, 0, 0), new Color.Rgb(0, 0, 0)),
        Arguments.of(new tui.crossterm.Color.Rgb(10, 20, 30), new Color.Rgb(10, 20, 30)),
        Arguments.of(new tui.crossterm.Color.AnsiValue(32), new Color.Indexed(32)),
        Arguments.of(new tui.crossterm.Color.AnsiValue(37), new Color.Indexed(37)));
  }

  @ParameterizedTest
  @MethodSource("from_crossterm_color_cases")
  public void from_crossterm_color(tui.crossterm.Color crosstermColor, Color color) {
    assertEquals(color, CrosstermColorConv.fromCrossterm(crosstermColor));
  }

  // ---- modifier::from_crossterm_attribute ----

  static Stream<Arguments> from_crossterm_attribute_cases() {
    return Stream.of(
        Arguments.of(Attribute.Reset, Modifier.EMPTY),
        Arguments.of(Attribute.Bold, Modifier.BOLD),
        Arguments.of(Attribute.NoBold, Modifier.EMPTY),
        Arguments.of(Attribute.Italic, Modifier.ITALIC),
        Arguments.of(Attribute.NoItalic, Modifier.EMPTY),
        Arguments.of(Attribute.Underlined, Modifier.UNDERLINED),
        Arguments.of(Attribute.NoUnderline, Modifier.EMPTY),
        Arguments.of(Attribute.OverLined, Modifier.EMPTY),
        Arguments.of(Attribute.NotOverLined, Modifier.EMPTY),
        Arguments.of(Attribute.DoubleUnderlined, Modifier.UNDERLINED),
        Arguments.of(Attribute.Undercurled, Modifier.UNDERLINED),
        Arguments.of(Attribute.Underdotted, Modifier.UNDERLINED),
        Arguments.of(Attribute.Underdashed, Modifier.UNDERLINED),
        Arguments.of(Attribute.Dim, Modifier.DIM),
        Arguments.of(Attribute.NormalIntensity, Modifier.EMPTY),
        Arguments.of(Attribute.CrossedOut, Modifier.CROSSED_OUT),
        Arguments.of(Attribute.NotCrossedOut, Modifier.EMPTY),
        Arguments.of(Attribute.SlowBlink, Modifier.SLOW_BLINK),
        Arguments.of(Attribute.RapidBlink, Modifier.RAPID_BLINK),
        Arguments.of(Attribute.Hidden, Modifier.HIDDEN),
        Arguments.of(Attribute.NoHidden, Modifier.EMPTY),
        Arguments.of(Attribute.Reverse, Modifier.REVERSED),
        Arguments.of(Attribute.NoReverse, Modifier.EMPTY));
  }

  @ParameterizedTest
  @MethodSource("from_crossterm_attribute_cases")
  public void from_crossterm_attribute(Attribute crosstermAttribute, Modifier jatatuiModifier) {
    assertEquals(jatatuiModifier, CrosstermModifierConv.fromCrossterm(crosstermAttribute));
  }

  // ---- modifier::from_crossterm_attributes ----

  static Stream<Arguments> from_crossterm_attributes_cases() {
    return Stream.of(
        Arguments.of(List.of(Attribute.Bold), Modifier.BOLD),
        Arguments.of(
            List.of(Attribute.Bold, Attribute.Italic), Modifier.BOLD.insert(Modifier.ITALIC)),
        Arguments.of(List.of(Attribute.Bold, Attribute.NotCrossedOut), Modifier.BOLD),
        Arguments.of(
            List.of(Attribute.Dim, Attribute.Underdotted),
            Modifier.DIM.insert(Modifier.UNDERLINED)),
        Arguments.of(
            List.of(Attribute.Dim, Attribute.SlowBlink, Attribute.Italic),
            Modifier.DIM.insert(Modifier.SLOW_BLINK).insert(Modifier.ITALIC)),
        Arguments.of(
            List.of(Attribute.Hidden, Attribute.NoUnderline, Attribute.NotCrossedOut),
            Modifier.HIDDEN),
        Arguments.of(List.of(Attribute.Reverse), Modifier.REVERSED),
        Arguments.of(List.of(Attribute.Reset), Modifier.EMPTY),
        Arguments.of(
            List.of(Attribute.RapidBlink, Attribute.CrossedOut),
            Modifier.RAPID_BLINK.insert(Modifier.CROSSED_OUT)));
  }

  @ParameterizedTest
  @MethodSource("from_crossterm_attributes_cases")
  public void from_crossterm_attributes(
      List<Attribute> crosstermAttributes, Modifier jatatuiModifier) {
    assertEquals(
        jatatuiModifier, CrosstermModifierConv.fromCrossterm(new Attributes(crosstermAttributes)));
  }

  // ---- from_crossterm_content_style ----

  private static ContentStyle styleWithAttrs(Attribute... attrs) {
    return ContentStyle.empty().withAttributes(new Attributes(List.of(attrs)));
  }

  static Stream<Arguments> from_crossterm_content_style_cases() {
    return Stream.of(
        Arguments.of(ContentStyle.empty(), Style.DEFAULT),
        Arguments.of(
            ContentStyle.empty().withForegroundColor(new tui.crossterm.Color.DarkYellow()),
            Style.DEFAULT.withFg(Color.YELLOW)),
        Arguments.of(
            ContentStyle.empty().withBackgroundColor(new tui.crossterm.Color.DarkYellow()),
            Style.DEFAULT.withBg(Color.YELLOW)),
        Arguments.of(styleWithAttrs(Attribute.Bold), Style.DEFAULT.withAddModifier(Modifier.BOLD)),
        Arguments.of(
            styleWithAttrs(Attribute.NoBold), Style.DEFAULT.withRemoveModifier(Modifier.BOLD)),
        Arguments.of(
            styleWithAttrs(Attribute.Italic), Style.DEFAULT.withAddModifier(Modifier.ITALIC)),
        Arguments.of(
            styleWithAttrs(Attribute.NoItalic), Style.DEFAULT.withRemoveModifier(Modifier.ITALIC)),
        Arguments.of(
            styleWithAttrs(Attribute.Bold, Attribute.Italic),
            Style.DEFAULT.withAddModifier(Modifier.BOLD).withAddModifier(Modifier.ITALIC)),
        Arguments.of(
            styleWithAttrs(Attribute.NoBold, Attribute.NoItalic),
            Style.DEFAULT.withRemoveModifier(Modifier.BOLD).withRemoveModifier(Modifier.ITALIC)));
  }

  @ParameterizedTest
  @MethodSource("from_crossterm_content_style_cases")
  public void from_crossterm_content_style(ContentStyle contentStyle, Style style) {
    assertEquals(style, CrosstermStyleConv.fromCrossterm(contentStyle));
  }

  // ---- from_crossterm_content_style_underline ----

  @org.junit.jupiter.api.Test
  public void from_crossterm_content_style_underline() {
    ContentStyle contentStyle =
        new ContentStyle(
            Optional.empty(),
            Optional.empty(),
            Optional.of(new tui.crossterm.Color.DarkRed()),
            new Attributes(List.of()));
    assertEquals(
        Style.DEFAULT.withUnderlineColor(Color.RED),
        CrosstermStyleConv.fromCrossterm(contentStyle));
  }
}
