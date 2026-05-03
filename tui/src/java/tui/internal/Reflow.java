package tui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import tui.Grapheme;
import tui.StyledGrapheme;

public final class Reflow {
  private Reflow() {}

  public static final String NBSP = " ";

  public record Line(StyledGrapheme[] graphemes, int width) {}

  /// A state machine to pack styled symbols into lines.
  public interface LineComposer {
    Optional<Line> nextLine();
  }

  /// A state machine that wraps lines on word boundaries.
  public static final class WordWrapper implements LineComposer {
    private final Iterator<StyledGrapheme> symbols;
    private final int maxLineWidth;
    /// Removes the leading whitespace from lines
    private final boolean trim;
    private List<StyledGrapheme> currentLine = new ArrayList<>();
    private List<StyledGrapheme> nextLineVar = new ArrayList<>();

    public WordWrapper(Iterator<StyledGrapheme> symbols, int maxLineWidth, boolean trim) {
      this.symbols = symbols;
      this.maxLineWidth = maxLineWidth;
      this.trim = trim;
    }

    @Override
    public Optional<Line> nextLine() {
      if (maxLineWidth == 0) {
        return Optional.empty();
      }

      List<StyledGrapheme> tmp = currentLine;
      currentLine = nextLineVar;
      nextLineVar = tmp;
      nextLineVar.clear();

      int currentLineWidth = 0;
      for (StyledGrapheme sg : currentLine) {
        currentLineWidth += sg.symbol().width();
      }

      int[] symbolsToLastWordEnd = {0};
      int[] widthToLastWordEnd = {0};
      boolean[] prevWhitespace = {false};
      boolean[] symbolsExhausted = {true};
      int[] currentLineWidthRef = {currentLineWidth};

      BreakableForeach.run(
          symbols,
          sg -> {
            symbolsExhausted[0] = false;
            boolean symbolWhitespace =
                allWhitespace(sg.symbol().str) && !sg.symbol().str.equals(NBSP);

            // Ignore characters wider that the total max width.
            if (sg.symbol().width() > maxLineWidth
                || (trim
                    && symbolWhitespace
                    && !sg.symbol().str.equals("\n")
                    && currentLineWidthRef[0] == 0)) {
              return BreakableForeach.Res.Continue;
            }

            // Break on newline and discard it.
            if (sg.symbol().str.equals("\n")) {
              if (prevWhitespace[0]) {
                currentLineWidthRef[0] = widthToLastWordEnd[0];
                takeInPlace(currentLine, symbolsToLastWordEnd[0]);
              }
              return BreakableForeach.Res.Break;
            }

            // Mark the previous symbol as word end.
            if (symbolWhitespace && !prevWhitespace[0]) {
              symbolsToLastWordEnd[0] = currentLine.size();
              widthToLastWordEnd[0] = currentLineWidthRef[0];
            }

            currentLine.add(new StyledGrapheme(sg.symbol(), sg.style()));
            currentLineWidthRef[0] += sg.symbol().width();

            if (currentLineWidthRef[0] > maxLineWidth) {
              // If there was no word break in the text, wrap at the end of the line.
              int truncateAt;
              int truncatedWidth;
              if (symbolsToLastWordEnd[0] != 0) {
                truncateAt = symbolsToLastWordEnd[0];
                truncatedWidth = widthToLastWordEnd[0];
              } else {
                truncateAt = currentLine.size() - 1;
                truncatedWidth = maxLineWidth;
              }

              // Push the remainder to the next line but strip leading whitespace:
              List<StyledGrapheme> remainder =
                  new ArrayList<>(currentLine.subList(truncateAt, currentLine.size()));
              int remainderNonwhite = -1;
              for (int idx = 0; idx < remainder.size(); idx++) {
                if (!allWhitespace(remainder.get(idx).symbol().str)) {
                  remainderNonwhite = idx;
                  break;
                }
              }
              if (remainderNonwhite != -1) {
                nextLineVar.addAll(remainder.subList(remainderNonwhite, remainder.size()));
              }
              takeInPlace(currentLine, truncateAt);
              currentLineWidthRef[0] = truncatedWidth;
              return BreakableForeach.Res.Break;
            } else {
              prevWhitespace[0] = symbolWhitespace;
              return BreakableForeach.Res.Continue;
            }
          });

      // Even if the iterator is exhausted, pass the previous remainder.
      if (symbolsExhausted[0] && currentLine.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(new Line(currentLine.toArray(new StyledGrapheme[0]), currentLineWidthRef[0]));
    }
  }

  /// A state machine that truncates overhanging lines.
  public static final class LineTruncator implements LineComposer {
    private final Iterator<StyledGrapheme> symbols;
    private final int maxLineWidth;
    private final List<StyledGrapheme> currentLine;
    /// Record the offset to skip render
    private final int initialHorizontalOffset;

    public LineTruncator(
        Iterator<StyledGrapheme> symbols, int maxLineWidth, int horizontalOffset) {
      this.symbols = symbols;
      this.maxLineWidth = maxLineWidth;
      this.currentLine = new ArrayList<>();
      this.initialHorizontalOffset = horizontalOffset;
    }

    @Override
    public Optional<Line> nextLine() {
      if (maxLineWidth == 0) {
        return Optional.empty();
      }

      currentLine.clear();
      int[] currentLineWidth = {0};
      boolean[] skipRest = {false};
      boolean[] symbolsExhausted = {true};
      int[] horizontalOffset = {initialHorizontalOffset};

      BreakableForeach.run(
          symbols,
          sg -> {
            Grapheme symbol = sg.symbol();
            symbolsExhausted[0] = false;

            // Ignore characters wider that the total max width.
            if (symbol.width() > maxLineWidth) {
              return BreakableForeach.Res.Continue;
            }

            // Break on newline and discard it.
            if (symbol.str.equals("\n")) {
              return BreakableForeach.Res.Break;
            }

            if (currentLineWidth[0] + symbol.width() > maxLineWidth) {
              // Exhaust the remainder of the line.
              skipRest[0] = true;
              return BreakableForeach.Res.Break;
            }

            Grapheme symbol1;
            if (horizontalOffset[0] == 0) {
              symbol1 = symbol;
            } else {
              int w = symbol.width();
              if (w > horizontalOffset[0]) {
                String t = trimOffset(symbol.str, horizontalOffset[0]);
                horizontalOffset[0] = 0;
                symbol1 = new Grapheme(t);
              } else {
                horizontalOffset[0] -= w;
                symbol1 = new Grapheme("");
              }
            }
            currentLineWidth[0] += symbol1.width();
            currentLine.add(new StyledGrapheme(symbol1, sg.style()));
            return BreakableForeach.Res.Continue;
          });

      if (skipRest[0]) {
        BreakableForeach.run(
            symbols,
            sg -> sg.symbol().str.equals("\n") ? BreakableForeach.Res.Break : BreakableForeach.Res.Continue);
      }

      if (symbolsExhausted[0] && currentLine.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(
          new Line(currentLine.toArray(new StyledGrapheme[0]), currentLineWidth[0]));
    }
  }

  /// This function will return a str slice which start at specified offset.
  /// As src is a unicode str, start offset has to be calculated with each character.
  public static String trimOffset(String src, int offset0) {
    int[] offset = {offset0};
    int[] start = {0};
    BreakableForeach.run(
        UnicodeSegmentation.graphemes(src, true),
        (c, idx) -> {
          if (c.width() <= offset[0]) {
            offset[0] -= c.width();
            start[0] += c.str.length();
            return BreakableForeach.Res.Continue;
          } else {
            return BreakableForeach.Res.Break;
          }
        });
    return src.substring(start[0]);
  }

  private static boolean allWhitespace(String s) {
    if (s.isEmpty()) return false;
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isWhitespace(s.charAt(i))) return false;
    }
    return true;
  }

  private static <T> void takeInPlace(List<T> list, int n) {
    while (list.size() > n) {
      list.remove(list.size() - 1);
    }
  }
}
