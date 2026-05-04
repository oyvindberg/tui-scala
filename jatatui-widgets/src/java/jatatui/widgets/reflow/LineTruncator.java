package jatatui.widgets.reflow;

import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.text.StyledGrapheme;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/// A state machine that truncates overhanging lines.
///
/// Mirrors `ratatui_widgets::reflow::LineTruncator`.
public final class LineTruncator implements LineComposer {

  /// The given, unprocessed lines.
  private final Iterator<StyledLineInput> inputLines;
  private final int maxLineWidth;
  private final List<StyledGrapheme> currentLine;
  /// Record the offset to skip render.
  private int horizontalOffset;

  /// Create a new `LineTruncator` with the given lines and maximum line width.
  public LineTruncator(Iterator<StyledLineInput> lines, int maxLineWidth) {
    this.inputLines = lines;
    this.maxLineWidth = maxLineWidth;
    this.horizontalOffset = 0;
    this.currentLine = new ArrayList<>();
  }

  /// Set the horizontal offset to skip render.
  public void setHorizontalOffset(int horizontalOffset) {
    this.horizontalOffset = horizontalOffset;
  }

  @Override
  public Optional<WrappedLine> nextLine() {
    if (maxLineWidth == 0) {
      return Optional.empty();
    }

    currentLine.clear();
    int currentLineWidth = 0;

    boolean linesExhausted = true;
    int hOffset = horizontalOffset;
    HorizontalAlignment currentAlignment = HorizontalAlignment.Left;
    if (inputLines.hasNext()) {
      StyledLineInput input = inputLines.next();
      Iterator<StyledGrapheme> currentInputLine = input.graphemes();
      HorizontalAlignment alignment = input.alignment();
      linesExhausted = false;
      currentAlignment = alignment;

      while (currentInputLine.hasNext()) {
        StyledGrapheme grapheme = currentInputLine.next();
        String symbol = grapheme.symbol;
        // Ignore characters wider that the total max width.
        if (Wcwidth.width(symbol) > maxLineWidth) {
          continue;
        }

        if (currentLineWidth + Wcwidth.width(symbol) > maxLineWidth) {
          // Truncate line
          break;
        }

        String renderSymbol;
        if (hOffset == 0 || alignment != HorizontalAlignment.Left) {
          renderSymbol = symbol;
        } else {
          int w = Wcwidth.width(symbol);
          if (w > hOffset) {
            renderSymbol = trimOffset(symbol, hOffset);
            hOffset = 0;
          } else {
            hOffset -= w;
            renderSymbol = "";
          }
        }
        currentLineWidth += Wcwidth.width(renderSymbol);
        currentLine.add(new StyledGrapheme(renderSymbol, grapheme.style));
      }
    }

    if (linesExhausted) {
      return Optional.empty();
    }
    return Optional.of(new WrappedLine(currentLine, currentLineWidth, currentAlignment));
  }

  /// Returns a substring of `src` starting at the position equivalent to skipping `offset` columns
  /// of unicode display width. As `src` is unicode, the start offset is calculated grapheme by
  /// grapheme.
  static String trimOffset(String src, int offset) {
    int remaining = offset;
    int start = 0;
    BreakIterator boundary = BreakIterator.getCharacterInstance(Locale.getDefault());
    boundary.setText(src);
    int begin = boundary.first();
    int end = boundary.next();
    while (end != BreakIterator.DONE) {
      String c = src.substring(begin, end);
      int w = Wcwidth.width(c);
      if (w <= remaining) {
        remaining -= w;
        start += c.length();
      } else {
        break;
      }
      begin = end;
      end = boundary.next();
    }
    return src.substring(start);
  }
}
