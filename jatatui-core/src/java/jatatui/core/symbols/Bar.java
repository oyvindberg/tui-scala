package jatatui.core.symbols;

/// Bar symbols (vertical bar levels).
public final class Bar {

  private Bar() {}

  public static final String FULL = "█";
  public static final String SEVEN_EIGHTHS = "▇";
  public static final String THREE_QUARTERS = "▆";
  public static final String FIVE_EIGHTHS = "▅";
  public static final String HALF = "▄";
  public static final String THREE_EIGHTHS = "▃";
  public static final String ONE_QUARTER = "▂";
  public static final String ONE_EIGHTH = "▁";

  /// A set of symbols for rendering bar charts at various fill levels.
  public record Set(
      String full,
      String sevenEighths,
      String threeQuarters,
      String fiveEighths,
      String half,
      String threeEighths,
      String oneQuarter,
      String oneEighth,
      String empty) {

    /// Returns the default bar set ([Bar#NINE_LEVELS]).
    public static Set defaultSet() {
      return NINE_LEVELS;
    }
  }

  public static final Set THREE_LEVELS =
      new Set(FULL, FULL, HALF, HALF, HALF, HALF, HALF, " ", " ");

  public static final Set NINE_LEVELS =
      new Set(
          FULL,
          SEVEN_EIGHTHS,
          THREE_QUARTERS,
          FIVE_EIGHTHS,
          HALF,
          THREE_EIGHTHS,
          ONE_QUARTER,
          ONE_EIGHTH,
          " ");
}
