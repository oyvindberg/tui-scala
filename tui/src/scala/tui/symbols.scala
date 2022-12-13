package tui

object symbols {
  object block {
    val FULL = "█";
    val SEVEN_EIGHTHS = "▉";
    val THREE_QUARTERS = "▊";
    val FIVE_EIGHTHS = "▋";
    val HALF = "▌";
    val THREE_EIGHTHS = "▍";
    val ONE_QUARTER = "▎";
    val ONE_EIGHTH = "▏";

    case class Set(
        full: String,
        seven_eighths: String,
        three_quarters: String,
        five_eighths: String,
        half: String,
        three_eighths: String,
        one_quarter: String,
        one_eighth: String,
        empty: String
    )

    val THREE_LEVELS: Set = Set(
      full = FULL,
      seven_eighths = FULL,
      three_quarters = HALF,
      five_eighths = HALF,
      half = HALF,
      three_eighths = HALF,
      one_quarter = HALF,
      one_eighth = " ",
      empty = " "
    )

    val NINE_LEVELS: Set = Set(
      full = FULL,
      seven_eighths = SEVEN_EIGHTHS,
      three_quarters = THREE_QUARTERS,
      five_eighths = FIVE_EIGHTHS,
      half = HALF,
      three_eighths = THREE_EIGHTHS,
      one_quarter = ONE_QUARTER,
      one_eighth = ONE_EIGHTH,
      empty = " "
    )
  }

  object bar {
    val FULL = "█";
    val SEVEN_EIGHTHS = "▇";
    val THREE_QUARTERS = "▆";
    val FIVE_EIGHTHS = "▅";
    val HALF = "▄";
    val THREE_EIGHTHS = "▃";
    val ONE_QUARTER = "▂";
    val ONE_EIGHTH = "▁";

    case class Set(
        full: String,
        seven_eighths: String,
        three_quarters: String,
        five_eighths: String,
        half: String,
        three_eighths: String,
        one_quarter: String,
        one_eighth: String,
        empty: String
    )

    val THREE_LEVELS: Set = Set(
      full = FULL,
      seven_eighths = FULL,
      three_quarters = HALF,
      five_eighths = HALF,
      half = HALF,
      three_eighths = HALF,
      one_quarter = HALF,
      one_eighth = " ",
      empty = " "
    )

    val NINE_LEVELS: Set = Set(
      full = FULL,
      seven_eighths = SEVEN_EIGHTHS,
      three_quarters = THREE_QUARTERS,
      five_eighths = FIVE_EIGHTHS,
      half = HALF,
      three_eighths = THREE_EIGHTHS,
      one_quarter = ONE_QUARTER,
      one_eighth = ONE_EIGHTH,
      empty = " "
    )
  }

  object line {
    val VERTICAL: String = "│";
    val DOUBLE_VERTICAL: String = "║";
    val THICK_VERTICAL: String = "┃";

    val HORIZONTAL: String = "─";
    val DOUBLE_HORIZONTAL: String = "═";
    val THICK_HORIZONTAL: String = "━";

    val TOP_RIGHT: String = "┐";
    val ROUNDED_TOP_RIGHT: String = "╮";
    val DOUBLE_TOP_RIGHT: String = "╗";
    val THICK_TOP_RIGHT: String = "┓";

    val TOP_LEFT: String = "┌";
    val ROUNDED_TOP_LEFT: String = "╭";
    val DOUBLE_TOP_LEFT: String = "╔";
    val THICK_TOP_LEFT: String = "┏";

    val BOTTOM_RIGHT: String = "┘";
    val ROUNDED_BOTTOM_RIGHT: String = "╯";
    val DOUBLE_BOTTOM_RIGHT: String = "╝";
    val THICK_BOTTOM_RIGHT: String = "┛";

    val BOTTOM_LEFT: String = "└";
    val ROUNDED_BOTTOM_LEFT: String = "╰";
    val DOUBLE_BOTTOM_LEFT: String = "╚";
    val THICK_BOTTOM_LEFT: String = "┗";

    val VERTICAL_LEFT: String = "┤";
    val DOUBLE_VERTICAL_LEFT: String = "╣";
    val THICK_VERTICAL_LEFT: String = "┫";

    val VERTICAL_RIGHT: String = "├";
    val DOUBLE_VERTICAL_RIGHT: String = "╠";
    val THICK_VERTICAL_RIGHT: String = "┣";

    val HORIZONTAL_DOWN: String = "┬";
    val DOUBLE_HORIZONTAL_DOWN: String = "╦";
    val THICK_HORIZONTAL_DOWN: String = "┳";

    val HORIZONTAL_UP: String = "┴";
    val DOUBLE_HORIZONTAL_UP: String = "╩";
    val THICK_HORIZONTAL_UP: String = "┻";

    val CROSS: String = "┼";
    val DOUBLE_CROSS: String = "╬";
    val THICK_CROSS: String = "╋";

    case class Set(
        vertical: String,
        horizontal: String,
        top_right: String,
        top_left: String,
        bottom_right: String,
        bottom_left: String,
        vertical_left: String,
        vertical_right: String,
        horizontal_down: String,
        horizontal_up: String,
        cross: String
    )

    val NORMAL: Set = Set(
      vertical = VERTICAL,
      horizontal = HORIZONTAL,
      top_right = TOP_RIGHT,
      top_left = TOP_LEFT,
      bottom_right = BOTTOM_RIGHT,
      bottom_left = BOTTOM_LEFT,
      vertical_left = VERTICAL_LEFT,
      vertical_right = VERTICAL_RIGHT,
      horizontal_down = HORIZONTAL_DOWN,
      horizontal_up = HORIZONTAL_UP,
      cross = CROSS
    )

    val ROUNDED: Set = NORMAL.copy(
      top_right = ROUNDED_TOP_RIGHT,
      top_left = ROUNDED_TOP_LEFT,
      bottom_right = ROUNDED_BOTTOM_RIGHT,
      bottom_left = ROUNDED_BOTTOM_LEFT
    )

    val DOUBLE: Set = Set(
      vertical = DOUBLE_VERTICAL,
      horizontal = DOUBLE_HORIZONTAL,
      top_right = DOUBLE_TOP_RIGHT,
      top_left = DOUBLE_TOP_LEFT,
      bottom_right = DOUBLE_BOTTOM_RIGHT,
      bottom_left = DOUBLE_BOTTOM_LEFT,
      vertical_left = DOUBLE_VERTICAL_LEFT,
      vertical_right = DOUBLE_VERTICAL_RIGHT,
      horizontal_down = DOUBLE_HORIZONTAL_DOWN,
      horizontal_up = DOUBLE_HORIZONTAL_UP,
      cross = DOUBLE_CROSS
    )

    val THICK: Set = Set(
      vertical = THICK_VERTICAL,
      horizontal = THICK_HORIZONTAL,
      top_right = THICK_TOP_RIGHT,
      top_left = THICK_TOP_LEFT,
      bottom_right = THICK_BOTTOM_RIGHT,
      bottom_left = THICK_BOTTOM_LEFT,
      vertical_left = THICK_VERTICAL_LEFT,
      vertical_right = THICK_VERTICAL_RIGHT,
      horizontal_down = THICK_HORIZONTAL_DOWN,
      horizontal_up = THICK_HORIZONTAL_UP,
      cross = THICK_CROSS
    )
  }

  object DOT {
    val value: String = "•";
  }

  object braille {
    val BLANK: Int = 0x2800;
    val DOTS: Array[(Int, Int)] = Array(
      (0x0001, 0x0008),
      (0x0002, 0x0010),
      (0x0004, 0x0020),
      (0x0040, 0x0080)
    )
  }

/// Marker to use when plotting data points
  sealed trait Marker

  object Marker {
    /// One point per cell in shape of dot
    case object Dot extends Marker

    /// One point per cell in shape of a block
    case object Block extends Marker

    /// Up to 8 points per cell
    case object Braille extends Marker
  }
}
