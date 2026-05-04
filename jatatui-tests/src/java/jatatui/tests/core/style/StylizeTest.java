package jatatui.tests.core.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import org.junit.jupiter.api.Test;

/// Tests for the [Stylize] interface and its standalone [Stylize.StylizedStyle] helper.
///
/// Many upstream tests in `style/stylize.rs` exercise `&str` / `String` / `Span` which require
/// the `text` module that has not been ported yet. Those tests will land alongside the `text`
/// port. Here we test the `Stylize` vocabulary itself via the [Stylize#of(Style)] helper, which
/// is the generic entry point for arbitrary `Style` building.
public class StylizeTest {

  @Test
  public void fg() {
    Style cyanFg = Style.empty().withFg(Color.CYAN);
    assertEquals(cyanFg, Stylize.of(Style.empty()).cyan().build());
  }

  @Test
  public void bg() {
    Style cyanBg = Style.empty().withBg(Color.CYAN);
    assertEquals(cyanBg, Stylize.of(Style.empty()).onCyan().build());
  }

  @Test
  public void color_modifier() {
    Style cyanBold = Style.empty().withFg(Color.CYAN).withAddModifier(Modifier.BOLD);
    assertEquals(cyanBold, Stylize.of(Style.empty()).cyan().bold().build());
  }

  @Test
  public void fg_bg() {
    Style cyanFgBg = Style.empty().withBg(Color.CYAN).withFg(Color.CYAN);
    assertEquals(cyanFgBg, Stylize.of(Style.empty()).cyan().onCyan().build());
  }

  @Test
  public void repeated_attributes() {
    Style bg = Style.empty().withBg(Color.CYAN);
    Style fg = Style.empty().withFg(Color.CYAN);

    // Behavior: the last one set is the definitive one
    assertEquals(bg, Stylize.of(Style.empty()).onRed().onCyan().build());
    assertEquals(fg, Stylize.of(Style.empty()).red().cyan().build());
  }

  @Test
  public void all_chained() {
    Style allModifierBlack =
        Style.empty()
            .withBg(Color.BLACK)
            .withFg(Color.BLACK)
            .withAddModifier(
                Modifier.UNDERLINED
                    .or(Modifier.BOLD)
                    .or(Modifier.DIM)
                    .or(Modifier.SLOW_BLINK)
                    .or(Modifier.REVERSED)
                    .or(Modifier.CROSSED_OUT));
    assertEquals(
        allModifierBlack,
        Stylize.of(Style.empty())
            .onBlack()
            .black()
            .bold()
            .underlined()
            .dim()
            .slowBlink()
            .crossedOut()
            .reversed()
            .build());
  }

  @Test
  public void reset() {
    // Stylize.reset() replaces the entire style with Style.RESET.
    Style after =
        Stylize.of(Style.empty()).onCyan().lightRed().bold().underlined().reset().build();
    assertEquals(Style.RESET, after);
  }

  // Foreground/background/modifier shorthands all delegate to the same `setStyle`+`.style().with*`
  // chain — covered exhaustively by StyleTest, but we exercise a few here against a styled wrapper
  // to make sure the interface methods are wired up.
  @Test
  public void all_color_shorthands() {
    assertEquals(Style.empty().withFg(Color.BLACK), Stylize.of(Style.empty()).black().build());
    assertEquals(Style.empty().withFg(Color.RED), Stylize.of(Style.empty()).red().build());
    assertEquals(Style.empty().withFg(Color.GREEN), Stylize.of(Style.empty()).green().build());
    assertEquals(Style.empty().withFg(Color.YELLOW), Stylize.of(Style.empty()).yellow().build());
    assertEquals(Style.empty().withFg(Color.BLUE), Stylize.of(Style.empty()).blue().build());
    assertEquals(Style.empty().withFg(Color.MAGENTA), Stylize.of(Style.empty()).magenta().build());
    assertEquals(Style.empty().withFg(Color.CYAN), Stylize.of(Style.empty()).cyan().build());
    assertEquals(Style.empty().withFg(Color.GRAY), Stylize.of(Style.empty()).gray().build());
    assertEquals(
        Style.empty().withFg(Color.DARK_GRAY), Stylize.of(Style.empty()).darkGray().build());
    assertEquals(
        Style.empty().withFg(Color.LIGHT_RED), Stylize.of(Style.empty()).lightRed().build());
    assertEquals(
        Style.empty().withFg(Color.LIGHT_GREEN), Stylize.of(Style.empty()).lightGreen().build());
    assertEquals(
        Style.empty().withFg(Color.LIGHT_YELLOW), Stylize.of(Style.empty()).lightYellow().build());
    assertEquals(
        Style.empty().withFg(Color.LIGHT_BLUE), Stylize.of(Style.empty()).lightBlue().build());
    assertEquals(
        Style.empty().withFg(Color.LIGHT_MAGENTA),
        Stylize.of(Style.empty()).lightMagenta().build());
    assertEquals(
        Style.empty().withFg(Color.LIGHT_CYAN), Stylize.of(Style.empty()).lightCyan().build());
    assertEquals(Style.empty().withFg(Color.WHITE), Stylize.of(Style.empty()).white().build());
  }

  @Test
  public void all_modifier_shorthands() {
    assertEquals(Style.empty().withAddModifier(Modifier.BOLD), Stylize.of(Style.empty()).bold().build());
    assertEquals(Style.empty().withAddModifier(Modifier.DIM), Stylize.of(Style.empty()).dim().build());
    assertEquals(
        Style.empty().withAddModifier(Modifier.ITALIC), Stylize.of(Style.empty()).italic().build());
    assertEquals(
        Style.empty().withAddModifier(Modifier.UNDERLINED),
        Stylize.of(Style.empty()).underlined().build());
    assertEquals(
        Style.empty().withAddModifier(Modifier.SLOW_BLINK),
        Stylize.of(Style.empty()).slowBlink().build());
    assertEquals(
        Style.empty().withAddModifier(Modifier.RAPID_BLINK),
        Stylize.of(Style.empty()).rapidBlink().build());
    assertEquals(
        Style.empty().withAddModifier(Modifier.REVERSED),
        Stylize.of(Style.empty()).reversed().build());
    assertEquals(
        Style.empty().withAddModifier(Modifier.HIDDEN), Stylize.of(Style.empty()).hidden().build());
    assertEquals(
        Style.empty().withAddModifier(Modifier.CROSSED_OUT),
        Stylize.of(Style.empty()).crossedOut().build());

    assertEquals(
        Style.empty().withRemoveModifier(Modifier.BOLD), Stylize.of(Style.empty()).notBold().build());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.DIM), Stylize.of(Style.empty()).notDim().build());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.ITALIC),
        Stylize.of(Style.empty()).notItalic().build());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.UNDERLINED),
        Stylize.of(Style.empty()).notUnderlined().build());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.SLOW_BLINK),
        Stylize.of(Style.empty()).notSlowBlink().build());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.RAPID_BLINK),
        Stylize.of(Style.empty()).notRapidBlink().build());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.REVERSED),
        Stylize.of(Style.empty()).notReversed().build());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.HIDDEN),
        Stylize.of(Style.empty()).notHidden().build());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.CROSSED_OUT),
        Stylize.of(Style.empty()).notCrossedOut().build());
  }

  @Test
  public void underline_color_shorthand() {
    assertEquals(
        Style.empty().withUnderlineColor(Color.RED),
        Stylize.of(Style.empty()).underlineColor(Color.RED).build());
  }
}
