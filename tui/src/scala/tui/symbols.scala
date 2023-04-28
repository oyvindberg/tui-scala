package tui

object symbols {
  object block {
    val FULL = "█"
    val SEVEN_EIGHTHS = "▉"
    val THREE_QUARTERS = "▊"
    val FIVE_EIGHTHS = "▋"
    val HALF = "▌"
    val THREE_EIGHTHS = "▍"
    val ONE_QUARTER = "▎"
    val ONE_EIGHTH = "▏"

    case class Set(
        full: String,
        sevenEighths: String,
        threeQuarters: String,
        fiveEighths: String,
        half: String,
        threeEights: String,
        oneQuarter: String,
        oneEighth: String,
        empty: String
    )

    val THREE_LEVELS: Set = Set(
      full = FULL,
      sevenEighths = FULL,
      threeQuarters = HALF,
      fiveEighths = HALF,
      half = HALF,
      threeEights = HALF,
      oneQuarter = HALF,
      oneEighth = " ",
      empty = " "
    )

    val NINE_LEVELS: Set = Set(
      full = FULL,
      sevenEighths = SEVEN_EIGHTHS,
      threeQuarters = THREE_QUARTERS,
      fiveEighths = FIVE_EIGHTHS,
      half = HALF,
      threeEights = THREE_EIGHTHS,
      oneQuarter = ONE_QUARTER,
      oneEighth = ONE_EIGHTH,
      empty = " "
    )
  }

  object bar {
    val FULL = "█"
    val SEVEN_EIGHTHS = "▇"
    val THREE_QUARTERS = "▆"
    val FIVE_EIGHTHS = "▅"
    val HALF = "▄"
    val THREE_EIGHTHS = "▃"
    val ONE_QUARTER = "▂"
    val ONE_EIGHTH = "▁"

    case class Set(
        full: String,
        sevenEighths: String,
        threeQuarters: String,
        fiveEighths: String,
        half: String,
        threeEighths: String,
        oneQuarter: String,
        oneEighth: String,
        empty: String
    )

    val THREE_LEVELS: Set = Set(
      full = FULL,
      sevenEighths = FULL,
      threeQuarters = HALF,
      fiveEighths = HALF,
      half = HALF,
      threeEighths = HALF,
      oneQuarter = HALF,
      oneEighth = " ",
      empty = " "
    )

    val NINE_LEVELS: Set = Set(
      full = FULL,
      sevenEighths = SEVEN_EIGHTHS,
      threeQuarters = THREE_QUARTERS,
      fiveEighths = FIVE_EIGHTHS,
      half = HALF,
      threeEighths = THREE_EIGHTHS,
      oneQuarter = ONE_QUARTER,
      oneEighth = ONE_EIGHTH,
      empty = " "
    )
  }

  object line {
    val VERTICAL: String = "│"
    val DOUBLE_VERTICAL: String = "║"
    val THICK_VERTICAL: String = "┃"

    val HORIZONTAL: String = "─"
    val DOUBLE_HORIZONTAL: String = "═"
    val THICK_HORIZONTAL: String = "━"

    val TOP_RIGHT: String = "┐"
    val ROUNDED_TOP_RIGHT: String = "╮"
    val DOUBLE_TOP_RIGHT: String = "╗"
    val THICK_TOP_RIGHT: String = "┓"

    val TOP_LEFT: String = "┌"
    val ROUNDED_TOP_LEFT: String = "╭"
    val DOUBLE_TOP_LEFT: String = "╔"
    val THICK_TOP_LEFT: String = "┏"

    val BOTTOM_RIGHT: String = "┘"
    val ROUNDED_BOTTOM_RIGHT: String = "╯"
    val DOUBLE_BOTTOM_RIGHT: String = "╝"
    val THICK_BOTTOM_RIGHT: String = "┛"

    val BOTTOM_LEFT: String = "└"
    val ROUNDED_BOTTOM_LEFT: String = "╰"
    val DOUBLE_BOTTOM_LEFT: String = "╚"
    val THICK_BOTTOM_LEFT: String = "┗"

    val VERTICAL_LEFT: String = "┤"
    val DOUBLE_VERTICAL_LEFT: String = "╣"
    val THICK_VERTICAL_LEFT: String = "┫"

    val VERTICAL_RIGHT: String = "├"
    val DOUBLE_VERTICAL_RIGHT: String = "╠"
    val THICK_VERTICAL_RIGHT: String = "┣"

    val HORIZONTAL_DOWN: String = "┬"
    val DOUBLE_HORIZONTAL_DOWN: String = "╦"
    val THICK_HORIZONTAL_DOWN: String = "┳"

    val HORIZONTAL_UP: String = "┴"
    val DOUBLE_HORIZONTAL_UP: String = "╩"
    val THICK_HORIZONTAL_UP: String = "┻"

    val CROSS: String = "┼"
    val DOUBLE_CROSS: String = "╬"
    val THICK_CROSS: String = "╋"

    case class Set(
        vertical: String,
        horizontal: String,
        topRight: String,
        topLeft: String,
        bottomRight: String,
        bottomLeft: String,
        verticalLeft: String,
        verticalRight: String,
        horizontalDown: String,
        horizontalUp: String,
        cross: String
    )

    val NORMAL: Set = Set(
      vertical = VERTICAL,
      horizontal = HORIZONTAL,
      topRight = TOP_RIGHT,
      topLeft = TOP_LEFT,
      bottomRight = BOTTOM_RIGHT,
      bottomLeft = BOTTOM_LEFT,
      verticalLeft = VERTICAL_LEFT,
      verticalRight = VERTICAL_RIGHT,
      horizontalDown = HORIZONTAL_DOWN,
      horizontalUp = HORIZONTAL_UP,
      cross = CROSS
    )

    val ROUNDED: Set = NORMAL.copy(
      topRight = ROUNDED_TOP_RIGHT,
      topLeft = ROUNDED_TOP_LEFT,
      bottomRight = ROUNDED_BOTTOM_RIGHT,
      bottomLeft = ROUNDED_BOTTOM_LEFT
    )

    val DOUBLE: Set = Set(
      vertical = DOUBLE_VERTICAL,
      horizontal = DOUBLE_HORIZONTAL,
      topRight = DOUBLE_TOP_RIGHT,
      topLeft = DOUBLE_TOP_LEFT,
      bottomRight = DOUBLE_BOTTOM_RIGHT,
      bottomLeft = DOUBLE_BOTTOM_LEFT,
      verticalLeft = DOUBLE_VERTICAL_LEFT,
      verticalRight = DOUBLE_VERTICAL_RIGHT,
      horizontalDown = DOUBLE_HORIZONTAL_DOWN,
      horizontalUp = DOUBLE_HORIZONTAL_UP,
      cross = DOUBLE_CROSS
    )

    val THICK: Set = Set(
      vertical = THICK_VERTICAL,
      horizontal = THICK_HORIZONTAL,
      topRight = THICK_TOP_RIGHT,
      topLeft = THICK_TOP_LEFT,
      bottomRight = THICK_BOTTOM_RIGHT,
      bottomLeft = THICK_BOTTOM_LEFT,
      verticalLeft = THICK_VERTICAL_LEFT,
      verticalRight = THICK_VERTICAL_RIGHT,
      horizontalDown = THICK_HORIZONTAL_DOWN,
      horizontalUp = THICK_HORIZONTAL_UP,
      cross = THICK_CROSS
    )
  }

  object DOT {
    val value: String = "•"
  }

  object braille {
    val BLANK: Int = 0x2800
    val DOTS: Array[(Int, Int)] = Array(
      (0x0001, 0x0008),
      (0x0002, 0x0010),
      (0x0004, 0x0020),
      (0x0040, 0x0080)
    )
  }

  /** Marker to use when plotting data points
    */
  sealed trait Marker

  object Marker {

    /** One point per cell in shape of dot
      */
    case object Dot extends Marker

    /** One point per cell in shape of a block
      */
    case object Block extends Marker

    /** Up to 8 points per cell
      */
    case object Braille extends Marker
  }
}
