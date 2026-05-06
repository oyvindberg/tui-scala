package jatatui.widgets;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.widgets.Widget;
import java.util.Optional;

/// A widget that renders the jatatui mascot — a steaming coffee mug with a `J` on the front.
///
/// Stands in for the upstream `ratatui_widgets::mascot::RatatuiMascot` (a chef-hatted rat) on the
/// Java side. Same dimensions: 32 columns wide, 16 rows tall, rendered using half-block
/// characters from a 32x32 source grid.
public final class JatatuiMascot implements Widget {

  /// Source art. Each pair of source rows (2k, 2k+1) becomes display row k via the half-block
  /// pairing in [#render]. Use full-block paired rows for solid cells; mismatched rows produce
  /// upper/lower half-blocks.
  private static final String JATATUI_MASCOT =
      "                                \n"
          + "                                \n"
          + "              h    h            \n"
          + "              h    h            \n"
          + "               h  h             \n"
          + "               h  h             \n"
          + "                hh              \n"
          + "                hh              \n"
          + "                                \n"
          + "                                \n"
          + "    ████████████████            \n"
          + "    ████████████████            \n"
          + "    █              █            \n"
          + "    █              █            \n"
          + "    █  jjjjjjjj    █            \n"
          + "    █  jjjjjjjj    █            \n"
          + "    █        j     █            \n"
          + "    █        j     █            \n"
          + "    █        j     █            \n"
          + "    █        j     █            \n"
          + "    █  j     j     █            \n"
          + "    █  j     j     █            \n"
          + "    █   jjjjj      █            \n"
          + "    █   jjjjj      █            \n"
          + "    █              █            \n"
          + "    █              █            \n"
          + "     █            █             \n"
          + "     █            █             \n"
          + "       ██████████               \n"
          + "       ██████████               \n"
          + "                                \n"
          + "                                ";

  private static final int EMPTY = ' ';
  private static final int CUP = '█';
  private static final int STEAM = 'h';
  private static final int LOGO = 'j';

  /// Color of the cup body.
  public final Color cupColor;

  /// Color of the rising steam wisps.
  public final Color steamColor;

  /// Color of the `J` logo on the cup.
  public final Color logoColor;

  private JatatuiMascot(Color cupColor, Color steamColor, Color logoColor) {
    this.cupColor = cupColor;
    this.steamColor = steamColor;
    this.logoColor = logoColor;
  }

  /// Default mascot — light gray cup, white steam, red `J`.
  public static JatatuiMascot newMascot() {
    return new JatatuiMascot(
        new Color.Indexed(252), // light_gray
        new Color.Indexed(231), // white
        new Color.Indexed(196)); // red
  }

  /// Returns a copy with `cupColor` replaced.
  public JatatuiMascot withCupColor(Color cupColor) {
    return new JatatuiMascot(cupColor, steamColor, logoColor);
  }

  /// Returns a copy with `steamColor` replaced.
  public JatatuiMascot withSteamColor(Color steamColor) {
    return new JatatuiMascot(cupColor, steamColor, logoColor);
  }

  /// Returns a copy with `logoColor` replaced.
  public JatatuiMascot withLogoColor(Color logoColor) {
    return new JatatuiMascot(cupColor, steamColor, logoColor);
  }

  private Optional<Color> colorFor(int c) {
    if (c == CUP) return Optional.of(cupColor);
    if (c == STEAM) return Optional.of(steamColor);
    if (c == LOGO) return Optional.of(logoColor);
    return Optional.empty();
  }

  @Override
  public void render(Rect area, Buffer buf) {
    Rect clipped = area.intersection(buf.area());
    if (clipped.isEmpty()) {
      return;
    }
    String[] lines = JATATUI_MASCOT.split("\n", -1);
    int pairs = lines.length / 2;
    for (int y = 0; y < pairs; y++) {
      String line1 = lines[2 * y];
      String line2 = lines[2 * y + 1];
      int n = Math.min(line1.length(), line2.length());
      for (int x = 0; x < n; x++) {
        int absX = clipped.left() + x;
        int absY = clipped.top() + y;
        if (absX >= clipped.right() || absY >= clipped.bottom()) {
          continue;
        }
        char ch1 = line1.charAt(x);
        char ch2 = line2.charAt(x);

        Optional<Color> fg;
        Optional<String> symbol;

        if (ch1 == EMPTY && ch2 == EMPTY) {
          fg = Optional.empty();
          symbol = Optional.empty();
        } else if (ch1 == EMPTY) {
          fg = colorFor(ch2);
          symbol = Optional.of("▄");
        } else if (ch2 == EMPTY) {
          fg = colorFor(ch1);
          symbol = Optional.of("▀");
        } else if (ch1 == ch2) {
          fg = colorFor(ch1);
          symbol = Optional.of("█");
        } else {
          fg = colorFor(ch1);
          symbol = Optional.of("▀");
        }

        Cell cell = buf.cellAt(absX, absY);
        fg.ifPresent(cell::setFg);
        symbol.ifPresent(cell::setSymbol);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JatatuiMascot other)) return false;
    return cupColor.equals(other.cupColor)
        && steamColor.equals(other.steamColor)
        && logoColor.equals(other.logoColor);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(cupColor, steamColor, logoColor);
  }
}
