package jatatui.core.symbols;

/// Block symbols (horizontal block levels).
public final class Block {

  private Block() {}

  public static final String FULL = "█";
  public static final String SEVEN_EIGHTHS = "▉";
  public static final String THREE_QUARTERS = "▊";
  public static final String FIVE_EIGHTHS = "▋";
  public static final String HALF = "▌";
  public static final String THREE_EIGHTHS = "▍";
  public static final String ONE_QUARTER = "▎";
  public static final String ONE_EIGHTH = "▏";

  /// A set of symbols for rendering blocks at various fill levels.
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

    /// Returns the default block set ([Block#NINE_LEVELS]).
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
