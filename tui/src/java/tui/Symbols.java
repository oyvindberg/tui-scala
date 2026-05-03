package tui;

public final class Symbols {
  private Symbols() {}

  public static final class block {
    private block() {}
    public static final String FULL = "█";
    public static final String SEVEN_EIGHTHS = "▉";
    public static final String THREE_QUARTERS = "▊";
    public static final String FIVE_EIGHTHS = "▋";
    public static final String HALF = "▌";
    public static final String THREE_EIGHTHS = "▍";
    public static final String ONE_QUARTER = "▎";
    public static final String ONE_EIGHTH = "▏";

    public record Set(
        String full,
        String sevenEighths,
        String threeQuarters,
        String fiveEighths,
        String half,
        String threeEights,
        String oneQuarter,
        String oneEighth,
        String empty) {}

    public static final Set THREE_LEVELS =
        new Set(FULL, FULL, HALF, HALF, HALF, HALF, HALF, " ", " ");

    public static final Set NINE_LEVELS =
        new Set(
            FULL, SEVEN_EIGHTHS, THREE_QUARTERS, FIVE_EIGHTHS, HALF, THREE_EIGHTHS, ONE_QUARTER,
            ONE_EIGHTH, " ");
  }

  public static final class bar {
    private bar() {}
    public static final String FULL = "█";
    public static final String SEVEN_EIGHTHS = "▇";
    public static final String THREE_QUARTERS = "▆";
    public static final String FIVE_EIGHTHS = "▅";
    public static final String HALF = "▄";
    public static final String THREE_EIGHTHS = "▃";
    public static final String ONE_QUARTER = "▂";
    public static final String ONE_EIGHTH = "▁";

    public record Set(
        String full,
        String sevenEighths,
        String threeQuarters,
        String fiveEighths,
        String half,
        String threeEighths,
        String oneQuarter,
        String oneEighth,
        String empty) {}

    public static final Set THREE_LEVELS =
        new Set(FULL, FULL, HALF, HALF, HALF, HALF, HALF, " ", " ");

    public static final Set NINE_LEVELS =
        new Set(
            FULL, SEVEN_EIGHTHS, THREE_QUARTERS, FIVE_EIGHTHS, HALF, THREE_EIGHTHS, ONE_QUARTER,
            ONE_EIGHTH, " ");
  }

  public static final class line {
    private line() {}
    public static final String VERTICAL = "│";
    public static final String DOUBLE_VERTICAL = "║";
    public static final String THICK_VERTICAL = "┃";

    public static final String HORIZONTAL = "─";
    public static final String DOUBLE_HORIZONTAL = "═";
    public static final String THICK_HORIZONTAL = "━";

    public static final String TOP_RIGHT = "┐";
    public static final String ROUNDED_TOP_RIGHT = "╮";
    public static final String DOUBLE_TOP_RIGHT = "╗";
    public static final String THICK_TOP_RIGHT = "┓";

    public static final String TOP_LEFT = "┌";
    public static final String ROUNDED_TOP_LEFT = "╭";
    public static final String DOUBLE_TOP_LEFT = "╔";
    public static final String THICK_TOP_LEFT = "┏";

    public static final String BOTTOM_RIGHT = "┘";
    public static final String ROUNDED_BOTTOM_RIGHT = "╯";
    public static final String DOUBLE_BOTTOM_RIGHT = "╝";
    public static final String THICK_BOTTOM_RIGHT = "┛";

    public static final String BOTTOM_LEFT = "└";
    public static final String ROUNDED_BOTTOM_LEFT = "╰";
    public static final String DOUBLE_BOTTOM_LEFT = "╚";
    public static final String THICK_BOTTOM_LEFT = "┗";

    public static final String VERTICAL_LEFT = "┤";
    public static final String DOUBLE_VERTICAL_LEFT = "╣";
    public static final String THICK_VERTICAL_LEFT = "┫";

    public static final String VERTICAL_RIGHT = "├";
    public static final String DOUBLE_VERTICAL_RIGHT = "╠";
    public static final String THICK_VERTICAL_RIGHT = "┣";

    public static final String HORIZONTAL_DOWN = "┬";
    public static final String DOUBLE_HORIZONTAL_DOWN = "╦";
    public static final String THICK_HORIZONTAL_DOWN = "┳";

    public static final String HORIZONTAL_UP = "┴";
    public static final String DOUBLE_HORIZONTAL_UP = "╩";
    public static final String THICK_HORIZONTAL_UP = "┻";

    public static final String CROSS = "┼";
    public static final String DOUBLE_CROSS = "╬";
    public static final String THICK_CROSS = "╋";

    public record Set(
        String vertical,
        String horizontal,
        String topRight,
        String topLeft,
        String bottomRight,
        String bottomLeft,
        String verticalLeft,
        String verticalRight,
        String horizontalDown,
        String horizontalUp,
        String cross) {

      public Set withCorners(String tr, String tl, String br, String bl) {
        return new Set(
            vertical, horizontal, tr, tl, br, bl, verticalLeft, verticalRight, horizontalDown,
            horizontalUp, cross);
      }
    }

    public static final Set NORMAL =
        new Set(
            VERTICAL, HORIZONTAL, TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT, VERTICAL_LEFT,
            VERTICAL_RIGHT, HORIZONTAL_DOWN, HORIZONTAL_UP, CROSS);

    public static final Set ROUNDED =
        NORMAL.withCorners(
            ROUNDED_TOP_RIGHT, ROUNDED_TOP_LEFT, ROUNDED_BOTTOM_RIGHT, ROUNDED_BOTTOM_LEFT);

    public static final Set DOUBLE =
        new Set(
            DOUBLE_VERTICAL, DOUBLE_HORIZONTAL, DOUBLE_TOP_RIGHT, DOUBLE_TOP_LEFT,
            DOUBLE_BOTTOM_RIGHT, DOUBLE_BOTTOM_LEFT, DOUBLE_VERTICAL_LEFT, DOUBLE_VERTICAL_RIGHT,
            DOUBLE_HORIZONTAL_DOWN, DOUBLE_HORIZONTAL_UP, DOUBLE_CROSS);

    public static final Set THICK =
        new Set(
            THICK_VERTICAL, THICK_HORIZONTAL, THICK_TOP_RIGHT, THICK_TOP_LEFT, THICK_BOTTOM_RIGHT,
            THICK_BOTTOM_LEFT, THICK_VERTICAL_LEFT, THICK_VERTICAL_RIGHT, THICK_HORIZONTAL_DOWN,
            THICK_HORIZONTAL_UP, THICK_CROSS);
  }

  public static final class DOT {
    private DOT() {}
    public static final String value = "•";
  }

  public static final class braille {
    private braille() {}
    public static final int BLANK = 0x2800;
    public static final int[][] DOTS =
        new int[][] {{0x0001, 0x0008}, {0x0002, 0x0010}, {0x0004, 0x0020}, {0x0040, 0x0080}};
  }

  /// Marker to use when plotting data points
  public enum Marker {
    /// One point per cell in shape of dot
    Dot,
    /// One point per cell in shape of a block
    Block,
    /// One point per cell in the shape of a bar
    Bar,
    /// Up to 8 points per cell
    Braille
  }
}
