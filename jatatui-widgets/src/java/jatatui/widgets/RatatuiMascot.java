package jatatui.widgets;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.widgets.Widget;
import java.util.Optional;

/// A widget that renders the Ratatui mascot.
///
/// Mirrors `ratatui_widgets::mascot::RatatuiMascot` (v0.30).
///
/// The mascot takes 32x16 cells and is rendered using half block characters.
public final class RatatuiMascot implements Widget {

  /// State for the mascot's eye.
  public enum MascotEyeColor {
    /// The default eye color.
    Default,
    /// The red eye color.
    Red
  }

  /// Raw mascot art. Upstream uses Rust's `indoc!` macro which strips a common 4-space leading
  /// indent from every line. We bake that in here so the strings are already at their final indent.
  private static final String RATATUI_MASCOT =
      "               hhh\n"
          + "             hhhhhh\n"
          + "            hhhhhhh\n"
          + "           hhhhhhhh\n"
          + "          hhhhhhhhh\n"
          + "         hhhhhhhhhh\n"
          + "        hhhhhhhhhhhh\n"
          + "        hhhhhhhhhhhhh\n"
          + "        hhhhhhhhhhhhh     ██████\n"
          + "         hhhhhhhhhhh    ████████\n"
          + "              hhhhh ███████████\n"
          + "               hhh ██ee████████\n"
          + "                h █████████████\n"
          + "            ████ █████████████\n"
          + "           █████████████████\n"
          + "           ████████████████\n"
          + "           ████████████████\n"
          + "            ███ ██████████\n"
          + "          ▒▒    █████████\n"
          + "         ▒░░▒   █████████\n"
          + "        ▒░░░░▒ ██████████\n"
          + "       ▒░░▓░░░▒ █████████\n"
          + "      ▒░░▓▓░░░░▒ ████████\n"
          + "     ▒░░░░░░░░░░▒ ██████████\n"
          + "    ▒░░░░░░░░░░░░▒ ██████████\n"
          + "   ▒░░░░░░░▓▓░░░░░▒ █████████\n"
          + "  ▒░░░░░░░░░▓▓░░░░░▒ ████  ███\n"
          + " ▒░░░░░░░░░░░░░░░░░░▒ ██   ███\n"
          + "▒░░░░░░░░░░░░░░░░░░░░▒ █   ███\n"
          + "▒░░░░░░░░░░░░░░░░░░░░░▒   ███\n"
          + " ▒░░░░░░░░░░░░░░░░░░░░░▒ ███\n"
          + "  ▒░░░░░░░░░░░░░░░░░░░░░▒ █";

  private static final int EMPTY = ' ';
  private static final int RAT = '█';
  private static final int HAT = 'h';
  private static final int EYE = 'e';
  private static final int TERM = '░';
  private static final int TERM_BORDER = '▒';
  private static final int TERM_CURSOR = '▓';

  /// The current eye state.
  public final MascotEyeColor eyeState;

  /// The color of the rat.
  public final Color ratColor;

  /// The color of the rat's eye.
  public final Color ratEyeColor;

  /// The color of the rat's eye when blinking.
  public final Color ratEyeBlink;

  /// The color of the rat's hat.
  public final Color hatColor;

  /// The color of the terminal.
  public final Color termColor;

  /// The color of the terminal border.
  public final Color termBorderColor;

  /// The color of the terminal cursor.
  public final Color termCursorColor;

  private RatatuiMascot(
      MascotEyeColor eyeState,
      Color ratColor,
      Color ratEyeColor,
      Color ratEyeBlink,
      Color hatColor,
      Color termColor,
      Color termBorderColor,
      Color termCursorColor) {
    this.eyeState = eyeState;
    this.ratColor = ratColor;
    this.ratEyeColor = ratEyeColor;
    this.ratEyeBlink = ratEyeBlink;
    this.hatColor = hatColor;
    this.termColor = termColor;
    this.termBorderColor = termBorderColor;
    this.termCursorColor = termCursorColor;
  }

  /// Create a new Ratatui mascot widget with the default colors and eye state.
  public static RatatuiMascot newMascot() {
    return new RatatuiMascot(
        MascotEyeColor.Default,
        new Color.Indexed(252), // light_gray
        new Color.Indexed(236), // dark_charcoal
        new Color.Indexed(196), // red
        new Color.Indexed(231), // white
        new Color.Indexed(232), // vampire_black
        new Color.Indexed(237), // gray
        new Color.Indexed(248)); // dark_gray
  }

  /// Returns a copy with the eye state replaced.
  public RatatuiMascot withEye(MascotEyeColor eyeState) {
    return new RatatuiMascot(
        eyeState,
        ratColor,
        ratEyeColor,
        ratEyeBlink,
        hatColor,
        termColor,
        termBorderColor,
        termCursorColor);
  }

  private Optional<Color> colorFor(int c) {
    if (c == RAT) return Optional.of(ratColor);
    if (c == HAT) return Optional.of(hatColor);
    if (c == EYE) {
      return Optional.of(eyeState == MascotEyeColor.Red ? ratEyeBlink : ratEyeColor);
    }
    if (c == TERM) return Optional.of(termColor);
    if (c == TERM_CURSOR) return Optional.of(termCursorColor);
    if (c == TERM_BORDER) return Optional.of(termBorderColor);
    return Optional.empty();
  }

  @Override
  public void render(Rect area, Buffer buf) {
    Rect clipped = area.intersection(buf.area());
    if (clipped.isEmpty()) {
      return;
    }
    String[] lines = RATATUI_MASCOT.split("\n", -1);
    // Pair lines (line[2k], line[2k+1]) — render to row k. If the last line is unpaired (odd
    // number of lines), it's dropped to mirror upstream `tuples()`.
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
        Optional<Color> bg;
        Optional<String> symbol;

        if (ch1 == EMPTY && ch2 == EMPTY) {
          fg = Optional.empty();
          bg = Optional.empty();
          symbol = Optional.empty();
        } else if (ch1 == EMPTY) {
          fg = colorFor(ch2);
          bg = Optional.empty();
        } else if (ch2 == EMPTY) {
          fg = colorFor(ch1);
          bg = Optional.empty();
        } else if (ch1 == TERM && ch2 == TERM_BORDER) {
          fg = colorFor(TERM_BORDER);
          bg = colorFor(TERM);
        } else if (ch1 == TERM) {
          fg = colorFor(ch2);
          bg = colorFor(TERM);
        } else if (ch2 == TERM) {
          fg = colorFor(ch1);
          bg = colorFor(TERM);
        } else {
          fg = colorFor(ch1);
          bg = colorFor(ch2);
        }

        // Determine symbol.
        if (ch1 == EMPTY && ch2 == EMPTY) {
          symbol = Optional.empty();
        } else if (ch1 == TERM && ch2 == TERM) {
          symbol = Optional.of(" ");
        } else if (ch2 == EMPTY || ch2 == TERM) {
          symbol = Optional.of("▀");
        } else if (ch1 == EMPTY || ch1 == TERM) {
          symbol = Optional.of("▄");
        } else if (ch1 == ch2) {
          symbol = Optional.of("█");
        } else {
          symbol = Optional.of("▀");
        }

        Cell cell = buf.cellAt(absX, absY);
        fg.ifPresent(cell::setFg);
        bg.ifPresent(cell::setBg);
        symbol.ifPresent(cell::setSymbol);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RatatuiMascot other)) return false;
    return eyeState == other.eyeState
        && ratColor.equals(other.ratColor)
        && ratEyeColor.equals(other.ratEyeColor)
        && ratEyeBlink.equals(other.ratEyeBlink)
        && hatColor.equals(other.hatColor)
        && termColor.equals(other.termColor)
        && termBorderColor.equals(other.termBorderColor)
        && termCursorColor.equals(other.termCursorColor);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(
        eyeState,
        ratColor,
        ratEyeColor,
        ratEyeBlink,
        hatColor,
        termColor,
        termBorderColor,
        termCursorColor);
  }
}
