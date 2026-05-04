package jatatui.core.symbols;

/// Line drawing symbols and reusable [Set] presets.
public final class Line {

  private Line() {}

  public static final String VERTICAL = "│";
  public static final String DOUBLE_VERTICAL = "║";
  public static final String THICK_VERTICAL = "┃";
  public static final String LIGHT_DOUBLE_DASH_VERTICAL = "╎";
  public static final String HEAVY_DOUBLE_DASH_VERTICAL = "╏";
  public static final String LIGHT_TRIPLE_DASH_VERTICAL = "┆";
  public static final String HEAVY_TRIPLE_DASH_VERTICAL = "┇";
  public static final String LIGHT_QUADRUPLE_DASH_VERTICAL = "┊";
  public static final String HEAVY_QUADRUPLE_DASH_VERTICAL = "┋";

  public static final String HORIZONTAL = "─";
  public static final String DOUBLE_HORIZONTAL = "═";
  public static final String THICK_HORIZONTAL = "━";
  public static final String LIGHT_DOUBLE_DASH_HORIZONTAL = "╌";
  public static final String HEAVY_DOUBLE_DASH_HORIZONTAL = "╍";
  public static final String LIGHT_TRIPLE_DASH_HORIZONTAL = "┄";
  public static final String HEAVY_TRIPLE_DASH_HORIZONTAL = "┅";
  public static final String LIGHT_QUADRUPLE_DASH_HORIZONTAL = "┈";
  public static final String HEAVY_QUADRUPLE_DASH_HORIZONTAL = "┉";

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

  /// A set of line symbols making up a complete grid pattern.
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

    /// Returns the default line set ([Line#NORMAL]).
    public static Set defaultSet() {
      return NORMAL;
    }
  }

  public static final Set NORMAL =
      new Set(
          VERTICAL,
          HORIZONTAL,
          TOP_RIGHT,
          TOP_LEFT,
          BOTTOM_RIGHT,
          BOTTOM_LEFT,
          VERTICAL_LEFT,
          VERTICAL_RIGHT,
          HORIZONTAL_DOWN,
          HORIZONTAL_UP,
          CROSS);

  public static final Set ROUNDED =
      new Set(
          VERTICAL,
          HORIZONTAL,
          ROUNDED_TOP_RIGHT,
          ROUNDED_TOP_LEFT,
          ROUNDED_BOTTOM_RIGHT,
          ROUNDED_BOTTOM_LEFT,
          VERTICAL_LEFT,
          VERTICAL_RIGHT,
          HORIZONTAL_DOWN,
          HORIZONTAL_UP,
          CROSS);

  public static final Set DOUBLE =
      new Set(
          DOUBLE_VERTICAL,
          DOUBLE_HORIZONTAL,
          DOUBLE_TOP_RIGHT,
          DOUBLE_TOP_LEFT,
          DOUBLE_BOTTOM_RIGHT,
          DOUBLE_BOTTOM_LEFT,
          DOUBLE_VERTICAL_LEFT,
          DOUBLE_VERTICAL_RIGHT,
          DOUBLE_HORIZONTAL_DOWN,
          DOUBLE_HORIZONTAL_UP,
          DOUBLE_CROSS);

  public static final Set THICK =
      new Set(
          THICK_VERTICAL,
          THICK_HORIZONTAL,
          THICK_TOP_RIGHT,
          THICK_TOP_LEFT,
          THICK_BOTTOM_RIGHT,
          THICK_BOTTOM_LEFT,
          THICK_VERTICAL_LEFT,
          THICK_VERTICAL_RIGHT,
          THICK_HORIZONTAL_DOWN,
          THICK_HORIZONTAL_UP,
          THICK_CROSS);

  public static final Set LIGHT_DOUBLE_DASHED =
      new Set(
          LIGHT_DOUBLE_DASH_VERTICAL,
          LIGHT_DOUBLE_DASH_HORIZONTAL,
          TOP_RIGHT,
          TOP_LEFT,
          BOTTOM_RIGHT,
          BOTTOM_LEFT,
          VERTICAL_LEFT,
          VERTICAL_RIGHT,
          HORIZONTAL_DOWN,
          HORIZONTAL_UP,
          CROSS);

  public static final Set HEAVY_DOUBLE_DASHED =
      new Set(
          HEAVY_DOUBLE_DASH_VERTICAL,
          HEAVY_DOUBLE_DASH_HORIZONTAL,
          THICK_TOP_RIGHT,
          THICK_TOP_LEFT,
          THICK_BOTTOM_RIGHT,
          THICK_BOTTOM_LEFT,
          THICK_VERTICAL_LEFT,
          THICK_VERTICAL_RIGHT,
          THICK_HORIZONTAL_DOWN,
          THICK_HORIZONTAL_UP,
          THICK_CROSS);

  public static final Set LIGHT_TRIPLE_DASHED =
      new Set(
          LIGHT_TRIPLE_DASH_VERTICAL,
          LIGHT_TRIPLE_DASH_HORIZONTAL,
          TOP_RIGHT,
          TOP_LEFT,
          BOTTOM_RIGHT,
          BOTTOM_LEFT,
          VERTICAL_LEFT,
          VERTICAL_RIGHT,
          HORIZONTAL_DOWN,
          HORIZONTAL_UP,
          CROSS);

  public static final Set HEAVY_TRIPLE_DASHED =
      new Set(
          HEAVY_TRIPLE_DASH_VERTICAL,
          HEAVY_TRIPLE_DASH_HORIZONTAL,
          THICK_TOP_RIGHT,
          THICK_TOP_LEFT,
          THICK_BOTTOM_RIGHT,
          THICK_BOTTOM_LEFT,
          THICK_VERTICAL_LEFT,
          THICK_VERTICAL_RIGHT,
          THICK_HORIZONTAL_DOWN,
          THICK_HORIZONTAL_UP,
          THICK_CROSS);

  public static final Set LIGHT_QUADRUPLE_DASHED =
      new Set(
          LIGHT_QUADRUPLE_DASH_VERTICAL,
          LIGHT_QUADRUPLE_DASH_HORIZONTAL,
          TOP_RIGHT,
          TOP_LEFT,
          BOTTOM_RIGHT,
          BOTTOM_LEFT,
          VERTICAL_LEFT,
          VERTICAL_RIGHT,
          HORIZONTAL_DOWN,
          HORIZONTAL_UP,
          CROSS);

  public static final Set HEAVY_QUADRUPLE_DASHED =
      new Set(
          HEAVY_QUADRUPLE_DASH_VERTICAL,
          HEAVY_QUADRUPLE_DASH_HORIZONTAL,
          THICK_TOP_RIGHT,
          THICK_TOP_LEFT,
          THICK_BOTTOM_RIGHT,
          THICK_BOTTOM_LEFT,
          THICK_VERTICAL_LEFT,
          THICK_VERTICAL_RIGHT,
          THICK_HORIZONTAL_DOWN,
          THICK_HORIZONTAL_UP,
          THICK_CROSS);
}
