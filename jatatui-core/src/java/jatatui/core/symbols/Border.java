package jatatui.core.symbols;

/// Border symbol sets used by widgets to draw box borders.
public final class Border {

  private Border() {}

  /// A border set defining the eight characters that make up a rectangular border.
  public record Set(
      String topLeft,
      String topRight,
      String bottomLeft,
      String bottomRight,
      String verticalLeft,
      String verticalRight,
      String horizontalTop,
      String horizontalBottom) {

    /// Returns the default border set ([Border#PLAIN]).
    public static Set defaultSet() {
      return PLAIN;
    }
  }

  /// Builds a border set from a [Line.Set]. The line set's `vertical` is used for both vertical
  /// sides, and `horizontal` for both horizontal sides.
  public static Set fromLineSet(Line.Set lineSet) {
    return new Set(
        lineSet.topLeft(),
        lineSet.topRight(),
        lineSet.bottomLeft(),
        lineSet.bottomRight(),
        lineSet.vertical(),
        lineSet.vertical(),
        lineSet.horizontal(),
        lineSet.horizontal());
  }

  /// Border Set with a single line width.
  ///
  /// ```text
  /// ┌─────┐
  /// │xxxxx│
  /// │xxxxx│
  /// └─────┘
  /// ```
  public static final Set PLAIN =
      new Set(
          Line.NORMAL.topLeft(),
          Line.NORMAL.topRight(),
          Line.NORMAL.bottomLeft(),
          Line.NORMAL.bottomRight(),
          Line.NORMAL.vertical(),
          Line.NORMAL.vertical(),
          Line.NORMAL.horizontal(),
          Line.NORMAL.horizontal());

  /// Border Set with a single line width and rounded corners.
  ///
  /// ```text
  /// ╭─────╮
  /// │xxxxx│
  /// │xxxxx│
  /// ╰─────╯
  /// ```
  public static final Set ROUNDED =
      new Set(
          Line.ROUNDED.topLeft(),
          Line.ROUNDED.topRight(),
          Line.ROUNDED.bottomLeft(),
          Line.ROUNDED.bottomRight(),
          Line.ROUNDED.vertical(),
          Line.ROUNDED.vertical(),
          Line.ROUNDED.horizontal(),
          Line.ROUNDED.horizontal());

  /// Border Set with a double line width.
  ///
  /// ```text
  /// ╔═════╗
  /// ║xxxxx║
  /// ║xxxxx║
  /// ╚═════╝
  /// ```
  public static final Set DOUBLE =
      new Set(
          Line.DOUBLE.topLeft(),
          Line.DOUBLE.topRight(),
          Line.DOUBLE.bottomLeft(),
          Line.DOUBLE.bottomRight(),
          Line.DOUBLE.vertical(),
          Line.DOUBLE.vertical(),
          Line.DOUBLE.horizontal(),
          Line.DOUBLE.horizontal());

  /// Border Set with a thick line width.
  ///
  /// ```text
  /// ┏━━━━━┓
  /// ┃xxxxx┃
  /// ┃xxxxx┃
  /// ┗━━━━━┛
  /// ```
  public static final Set THICK =
      new Set(
          Line.THICK.topLeft(),
          Line.THICK.topRight(),
          Line.THICK.bottomLeft(),
          Line.THICK.bottomRight(),
          Line.THICK.vertical(),
          Line.THICK.vertical(),
          Line.THICK.horizontal(),
          Line.THICK.horizontal());

  /// Border Set with light double-dashed border lines.
  public static final Set LIGHT_DOUBLE_DASHED = fromLineSet(Line.LIGHT_DOUBLE_DASHED);

  /// Border Set with thick double-dashed border lines.
  public static final Set HEAVY_DOUBLE_DASHED = fromLineSet(Line.HEAVY_DOUBLE_DASHED);

  /// Border Set with light triple-dashed border lines.
  public static final Set LIGHT_TRIPLE_DASHED = fromLineSet(Line.LIGHT_TRIPLE_DASHED);

  /// Border Set with thick triple-dashed border lines.
  public static final Set HEAVY_TRIPLE_DASHED = fromLineSet(Line.HEAVY_TRIPLE_DASHED);

  /// Border Set with light quadruple-dashed border lines.
  public static final Set LIGHT_QUADRUPLE_DASHED = fromLineSet(Line.LIGHT_QUADRUPLE_DASHED);

  /// Border Set with thick quadruple-dashed border lines.
  public static final Set HEAVY_QUADRUPLE_DASHED = fromLineSet(Line.HEAVY_QUADRUPLE_DASHED);

  public static final String QUADRANT_TOP_LEFT = "▘";
  public static final String QUADRANT_TOP_RIGHT = "▝";
  public static final String QUADRANT_BOTTOM_LEFT = "▖";
  public static final String QUADRANT_BOTTOM_RIGHT = "▗";
  public static final String QUADRANT_TOP_HALF = "▀";
  public static final String QUADRANT_BOTTOM_HALF = "▄";
  public static final String QUADRANT_LEFT_HALF = "▌";
  public static final String QUADRANT_RIGHT_HALF = "▐";
  public static final String QUADRANT_TOP_LEFT_BOTTOM_LEFT_BOTTOM_RIGHT = "▙";
  public static final String QUADRANT_TOP_LEFT_TOP_RIGHT_BOTTOM_LEFT = "▛";
  public static final String QUADRANT_TOP_LEFT_TOP_RIGHT_BOTTOM_RIGHT = "▜";
  public static final String QUADRANT_TOP_RIGHT_BOTTOM_LEFT_BOTTOM_RIGHT = "▟";
  public static final String QUADRANT_TOP_LEFT_BOTTOM_RIGHT = "▚";
  public static final String QUADRANT_TOP_RIGHT_BOTTOM_LEFT = "▞";
  public static final String QUADRANT_BLOCK = "█";

  /// Quadrant border set drawing the border outside a block by one half cell "pixel".
  ///
  /// ```text
  /// ▛▀▀▀▀▀▜
  /// ▌xxxxx▐
  /// ▌xxxxx▐
  /// ▙▄▄▄▄▄▟
  /// ```
  public static final Set QUADRANT_OUTSIDE =
      new Set(
          QUADRANT_TOP_LEFT_TOP_RIGHT_BOTTOM_LEFT,
          QUADRANT_TOP_LEFT_TOP_RIGHT_BOTTOM_RIGHT,
          QUADRANT_TOP_LEFT_BOTTOM_LEFT_BOTTOM_RIGHT,
          QUADRANT_TOP_RIGHT_BOTTOM_LEFT_BOTTOM_RIGHT,
          QUADRANT_LEFT_HALF,
          QUADRANT_RIGHT_HALF,
          QUADRANT_TOP_HALF,
          QUADRANT_BOTTOM_HALF);

  /// Quadrant border set drawing the border inside a block by one half cell "pixel".
  ///
  /// ```text
  /// ▗▄▄▄▄▄▖
  /// ▐xxxxx▌
  /// ▐xxxxx▌
  /// ▝▀▀▀▀▀▘
  /// ```
  public static final Set QUADRANT_INSIDE =
      new Set(
          QUADRANT_BOTTOM_RIGHT,
          QUADRANT_BOTTOM_LEFT,
          QUADRANT_TOP_RIGHT,
          QUADRANT_TOP_LEFT,
          QUADRANT_RIGHT_HALF,
          QUADRANT_LEFT_HALF,
          QUADRANT_BOTTOM_HALF,
          QUADRANT_TOP_HALF);

  public static final String ONE_EIGHTH_TOP_EIGHT = "▔";
  public static final String ONE_EIGHTH_BOTTOM_EIGHT = "▁";
  public static final String ONE_EIGHTH_LEFT_EIGHT = "▏";
  public static final String ONE_EIGHTH_RIGHT_EIGHT = "▕";

  /// Wide border set based on McGugan box technique.
  ///
  /// ```text
  /// ▁▁▁▁▁▁▁
  /// ▏xxxxx▕
  /// ▏xxxxx▕
  /// ▔▔▔▔▔▔▔
  /// ```
  public static final Set ONE_EIGHTH_WIDE =
      new Set(
          ONE_EIGHTH_BOTTOM_EIGHT,
          ONE_EIGHTH_BOTTOM_EIGHT,
          ONE_EIGHTH_TOP_EIGHT,
          ONE_EIGHTH_TOP_EIGHT,
          ONE_EIGHTH_LEFT_EIGHT,
          ONE_EIGHTH_RIGHT_EIGHT,
          ONE_EIGHTH_BOTTOM_EIGHT,
          ONE_EIGHTH_TOP_EIGHT);

  /// Tall border set based on McGugan box technique.
  ///
  /// ```text
  /// ▕▔▔▏
  /// ▕xx▏
  /// ▕xx▏
  /// ▕▁▁▏
  /// ```
  public static final Set ONE_EIGHTH_TALL =
      new Set(
          ONE_EIGHTH_RIGHT_EIGHT,
          ONE_EIGHTH_LEFT_EIGHT,
          ONE_EIGHTH_RIGHT_EIGHT,
          ONE_EIGHTH_LEFT_EIGHT,
          ONE_EIGHTH_RIGHT_EIGHT,
          ONE_EIGHTH_LEFT_EIGHT,
          ONE_EIGHTH_TOP_EIGHT,
          ONE_EIGHTH_BOTTOM_EIGHT);

  /// Wide proportional (visually equal width and height) border using a set of quadrants.
  ///
  /// ```text
  /// ▄▄▄▄
  /// █xx█
  /// █xx█
  /// ▀▀▀▀
  /// ```
  public static final Set PROPORTIONAL_WIDE =
      new Set(
          QUADRANT_BOTTOM_HALF,
          QUADRANT_BOTTOM_HALF,
          QUADRANT_TOP_HALF,
          QUADRANT_TOP_HALF,
          QUADRANT_BLOCK,
          QUADRANT_BLOCK,
          QUADRANT_BOTTOM_HALF,
          QUADRANT_TOP_HALF);

  /// Tall proportional (visually equal width and height) border using a set of quadrants.
  ///
  /// ```text
  /// ▕█▀▀█
  /// ▕█xx█
  /// ▕█xx█
  /// ▕█▄▄█
  /// ```
  public static final Set PROPORTIONAL_TALL =
      new Set(
          QUADRANT_BLOCK,
          QUADRANT_BLOCK,
          QUADRANT_BLOCK,
          QUADRANT_BLOCK,
          QUADRANT_BLOCK,
          QUADRANT_BLOCK,
          QUADRANT_TOP_HALF,
          QUADRANT_BOTTOM_HALF);

  /// Solid border set (full blocks on all sides).
  ///
  /// ```text
  /// ████
  /// █xx█
  /// █xx█
  /// ████
  /// ```
  public static final Set FULL =
      new Set(
          Block.FULL,
          Block.FULL,
          Block.FULL,
          Block.FULL,
          Block.FULL,
          Block.FULL,
          Block.FULL,
          Block.FULL);

  /// Empty border set (spaces on all sides).
  ///
  /// Useful when you want to apply a border style to the title area without actually drawing a
  /// border.
  public static final Set EMPTY =
      new Set(" ", " ", " ", " ", " ", " ", " ", " ");
}
