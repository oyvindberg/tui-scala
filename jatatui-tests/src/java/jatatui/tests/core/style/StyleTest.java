package jatatui.tests.core.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import org.junit.jupiter.api.Test;

public class StyleTest {

  // Upstream `combined_patch_gives_same_result_as_individual_patch` — applying patches in any
  // grouping order should yield the same final style.
  @Test
  public void combined_patch_gives_same_result_as_individual_patch() {
    Style[] styles = {
      Style.empty(),
      Style.empty().withFg(Color.YELLOW),
      Style.empty().withBg(Color.YELLOW),
      Style.empty().withAddModifier(Modifier.BOLD),
      Style.empty().withRemoveModifier(Modifier.BOLD),
      Style.empty().withAddModifier(Modifier.ITALIC),
      Style.empty().withRemoveModifier(Modifier.ITALIC),
      Style.empty().withAddModifier(Modifier.ITALIC.or(Modifier.BOLD)),
      Style.empty().withRemoveModifier(Modifier.ITALIC.or(Modifier.BOLD))
    };
    for (Style a : styles) {
      for (Style b : styles) {
        for (Style c : styles) {
          for (Style d : styles) {
            Style left = Style.empty().patch(a).patch(b).patch(c).patch(d);
            Style right = Style.empty().patch(a.patch(b.patch(c.patch(d))));
            assertEquals(left, right);
          }
        }
      }
    }
  }

  @Test
  public void has_modifier_checks() {
    // basic presence
    Style style = Style.empty().withAddModifier(Modifier.BOLD.or(Modifier.ITALIC));
    assertTrue(style.hasModifier(Modifier.BOLD));
    assertTrue(style.hasModifier(Modifier.ITALIC));
    assertFalse(style.hasModifier(Modifier.UNDERLINED));

    // removal prevents the modifier from being reported as present
    Style style2 =
        Style.empty()
            .withAddModifier(Modifier.BOLD.or(Modifier.ITALIC))
            .withRemoveModifier(Modifier.ITALIC);
    assertTrue(style2.hasModifier(Modifier.BOLD));
    assertFalse(style2.hasModifier(Modifier.ITALIC));

    // patching with a style that removes a modifier clears it
    Style style3 = Style.empty().withAddModifier(Modifier.BOLD.or(Modifier.ITALIC));
    Style patched = style3.patch(Style.empty().withRemoveModifier(Modifier.ITALIC));
    assertTrue(patched.hasModifier(Modifier.BOLD));
    assertFalse(patched.hasModifier(Modifier.ITALIC));
  }

  // Foreground shorthand stylization — mirrors upstream `fg_can_be_stylized`.
  @Test
  public void fg_can_be_stylized() {
    assertEquals(Style.empty().withFg(Color.BLACK), Style.empty().black());
    assertEquals(Style.empty().withFg(Color.RED), Style.empty().red());
    assertEquals(Style.empty().withFg(Color.GREEN), Style.empty().green());
    assertEquals(Style.empty().withFg(Color.YELLOW), Style.empty().yellow());
    assertEquals(Style.empty().withFg(Color.BLUE), Style.empty().blue());
    assertEquals(Style.empty().withFg(Color.MAGENTA), Style.empty().magenta());
    assertEquals(Style.empty().withFg(Color.CYAN), Style.empty().cyan());
    assertEquals(Style.empty().withFg(Color.WHITE), Style.empty().white());
    assertEquals(Style.empty().withFg(Color.GRAY), Style.empty().gray());
    assertEquals(Style.empty().withFg(Color.DARK_GRAY), Style.empty().darkGray());
    assertEquals(Style.empty().withFg(Color.LIGHT_RED), Style.empty().lightRed());
    assertEquals(Style.empty().withFg(Color.LIGHT_GREEN), Style.empty().lightGreen());
    assertEquals(Style.empty().withFg(Color.LIGHT_YELLOW), Style.empty().lightYellow());
    assertEquals(Style.empty().withFg(Color.LIGHT_BLUE), Style.empty().lightBlue());
    assertEquals(Style.empty().withFg(Color.LIGHT_MAGENTA), Style.empty().lightMagenta());
    assertEquals(Style.empty().withFg(Color.LIGHT_CYAN), Style.empty().lightCyan());
  }

  @Test
  public void bg_can_be_stylized() {
    assertEquals(Style.empty().withBg(Color.BLACK), Style.empty().onBlack());
    assertEquals(Style.empty().withBg(Color.RED), Style.empty().onRed());
    assertEquals(Style.empty().withBg(Color.GREEN), Style.empty().onGreen());
    assertEquals(Style.empty().withBg(Color.YELLOW), Style.empty().onYellow());
    assertEquals(Style.empty().withBg(Color.BLUE), Style.empty().onBlue());
    assertEquals(Style.empty().withBg(Color.MAGENTA), Style.empty().onMagenta());
    assertEquals(Style.empty().withBg(Color.CYAN), Style.empty().onCyan());
    assertEquals(Style.empty().withBg(Color.WHITE), Style.empty().onWhite());
    assertEquals(Style.empty().withBg(Color.GRAY), Style.empty().onGray());
    assertEquals(Style.empty().withBg(Color.DARK_GRAY), Style.empty().onDarkGray());
    assertEquals(Style.empty().withBg(Color.LIGHT_RED), Style.empty().onLightRed());
    assertEquals(Style.empty().withBg(Color.LIGHT_GREEN), Style.empty().onLightGreen());
    assertEquals(Style.empty().withBg(Color.LIGHT_YELLOW), Style.empty().onLightYellow());
    assertEquals(Style.empty().withBg(Color.LIGHT_BLUE), Style.empty().onLightBlue());
    assertEquals(Style.empty().withBg(Color.LIGHT_MAGENTA), Style.empty().onLightMagenta());
    assertEquals(Style.empty().withBg(Color.LIGHT_CYAN), Style.empty().onLightCyan());
  }

  @Test
  public void add_modifier_can_be_stylized() {
    assertEquals(Style.empty().withAddModifier(Modifier.BOLD), Style.empty().bold());
    assertEquals(Style.empty().withAddModifier(Modifier.DIM), Style.empty().dim());
    assertEquals(Style.empty().withAddModifier(Modifier.ITALIC), Style.empty().italic());
    assertEquals(Style.empty().withAddModifier(Modifier.UNDERLINED), Style.empty().underlined());
    assertEquals(Style.empty().withAddModifier(Modifier.SLOW_BLINK), Style.empty().slowBlink());
    assertEquals(Style.empty().withAddModifier(Modifier.RAPID_BLINK), Style.empty().rapidBlink());
    assertEquals(Style.empty().withAddModifier(Modifier.REVERSED), Style.empty().reversed());
    assertEquals(Style.empty().withAddModifier(Modifier.HIDDEN), Style.empty().hidden());
    assertEquals(Style.empty().withAddModifier(Modifier.CROSSED_OUT), Style.empty().crossedOut());
  }

  @Test
  public void remove_modifier_can_be_stylized() {
    assertEquals(Style.empty().withRemoveModifier(Modifier.BOLD), Style.empty().notBold());
    assertEquals(Style.empty().withRemoveModifier(Modifier.DIM), Style.empty().notDim());
    assertEquals(Style.empty().withRemoveModifier(Modifier.ITALIC), Style.empty().notItalic());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.UNDERLINED), Style.empty().notUnderlined());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.SLOW_BLINK), Style.empty().notSlowBlink());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.RAPID_BLINK), Style.empty().notRapidBlink());
    assertEquals(Style.empty().withRemoveModifier(Modifier.REVERSED), Style.empty().notReversed());
    assertEquals(Style.empty().withRemoveModifier(Modifier.HIDDEN), Style.empty().notHidden());
    assertEquals(
        Style.empty().withRemoveModifier(Modifier.CROSSED_OUT), Style.empty().notCrossedOut());
  }

  @Test
  public void from_color() {
    assertEquals(Style.empty().withFg(Color.RED), Style.fromFg(Color.RED));
  }

  @Test
  public void from_color_color() {
    assertEquals(
        Style.empty().withFg(Color.RED).withBg(Color.BLUE),
        Style.fromFgBg(Color.RED, Color.BLUE));
  }

  @Test
  public void from_modifier() {
    assertEquals(
        Style.empty().withAddModifier(Modifier.BOLD).withAddModifier(Modifier.ITALIC),
        Style.fromModifier(Modifier.BOLD.or(Modifier.ITALIC)));
  }

  @Test
  public void from_modifier_modifier() {
    assertEquals(
        Style.empty()
            .withAddModifier(Modifier.BOLD)
            .withAddModifier(Modifier.ITALIC)
            .withRemoveModifier(Modifier.DIM),
        Style.fromModifierModifier(Modifier.BOLD.or(Modifier.ITALIC), Modifier.DIM));
  }

  @Test
  public void from_color_modifier() {
    assertEquals(
        Style.empty()
            .withFg(Color.RED)
            .withAddModifier(Modifier.BOLD)
            .withAddModifier(Modifier.ITALIC),
        Style.fromFgModifier(Color.RED, Modifier.BOLD.or(Modifier.ITALIC)));
  }

  @Test
  public void from_color_color_modifier() {
    assertEquals(
        Style.empty()
            .withFg(Color.RED)
            .withBg(Color.BLUE)
            .withAddModifier(Modifier.BOLD)
            .withAddModifier(Modifier.ITALIC),
        Style.fromFgBgModifier(Color.RED, Color.BLUE, Modifier.BOLD.or(Modifier.ITALIC)));
  }

  @Test
  public void from_color_color_modifier_modifier() {
    assertEquals(
        Style.empty()
            .withFg(Color.RED)
            .withBg(Color.BLUE)
            .withAddModifier(Modifier.BOLD)
            .withAddModifier(Modifier.ITALIC)
            .withRemoveModifier(Modifier.DIM),
        Style.fromFgBgModifierModifier(
            Color.RED, Color.BLUE, Modifier.BOLD.or(Modifier.ITALIC), Modifier.DIM));
  }

  // ---- Java-specific consistency checks for record/constants ----

  @Test
  public void default_and_reset_constants() {
    assertEquals(Style.empty(), Style.DEFAULT);
    assertEquals(Style.reset(), Style.RESET);
    // RESET sets fg/bg/underlineColor to Color.RESET and sub_modifier = ALL.
    assertEquals(java.util.Optional.of(Color.RESET), Style.RESET.fg());
    assertEquals(java.util.Optional.of(Color.RESET), Style.RESET.bg());
    assertEquals(java.util.Optional.of(Color.RESET), Style.RESET.underlineColor());
    assertEquals(Modifier.EMPTY, Style.RESET.addModifier());
    assertEquals(Modifier.ALL, Style.RESET.subModifier());
  }
}
