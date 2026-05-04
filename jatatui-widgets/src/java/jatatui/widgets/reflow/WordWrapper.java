package jatatui.widgets.reflow;

import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.text.StyledGrapheme;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/// A state machine that wraps lines on word boundaries.
///
/// Mirrors `ratatui_widgets::reflow::WordWrapper`. The internal pooling of `Vec` allocations from
/// upstream is preserved for behavioural fidelity, even though Java's GC makes the optimisation
/// less impactful.
public final class WordWrapper implements LineComposer {

  /// The given, unprocessed lines.
  private final Iterator<StyledLineInput> inputLines;
  private final int maxLineWidth;
  private final Deque<List<StyledGrapheme>> wrappedLines;
  private HorizontalAlignment currentAlignment;
  private List<StyledGrapheme> currentLine;
  /// Removes the leading whitespace from lines.
  private final boolean trim;

  // These are cached allocations that hold no state across nextLine invocations.
  private final List<StyledGrapheme> pendingWord;
  private final Deque<StyledGrapheme> pendingWhitespace;
  private final List<List<StyledGrapheme>> pendingLinePool;

  /// Create a new `WordWrapper` with the given lines and maximum line width.
  public WordWrapper(Iterator<StyledLineInput> lines, int maxLineWidth, boolean trim) {
    this.inputLines = lines;
    this.maxLineWidth = maxLineWidth;
    this.wrappedLines = new ArrayDeque<>();
    this.currentAlignment = HorizontalAlignment.Left;
    this.currentLine = new ArrayList<>();
    this.trim = trim;
    this.pendingWord = new ArrayList<>();
    this.pendingWhitespace = new ArrayDeque<>();
    this.pendingLinePool = new ArrayList<>();
  }

  @Override
  public Optional<WrappedLine> nextLine() {
    if (maxLineWidth == 0) {
      return Optional.empty();
    }

    while (true) {
      // emit next cached line if present
      if (!wrappedLines.isEmpty()) {
        List<StyledGrapheme> line = wrappedLines.pollFirst();
        int lineWidth = 0;
        for (StyledGrapheme g : line) {
          lineWidth += Wcwidth.width(g.symbol);
        }
        replaceCurrentLine(line);
        return Optional.of(new WrappedLine(currentLine, lineWidth, currentAlignment));
      }

      // otherwise, process pending wrapped lines from input
      if (!inputLines.hasNext()) {
        return Optional.empty();
      }
      StyledLineInput input = inputLines.next();
      currentAlignment = input.alignment();
      processInput(input.graphemes());
    }
  }

  /// Split an input line into wrapped lines and cache them to be emitted later.
  private void processInput(Iterator<StyledGrapheme> lineSymbols) {
    List<StyledGrapheme> pendingLine =
        pendingLinePool.isEmpty()
            ? new ArrayList<>()
            : pendingLinePool.remove(pendingLinePool.size() - 1);
    int lineWidth = 0;
    int wordWidth = 0;
    int whitespaceWidth = 0;
    boolean nonWhitespacePrevious = false;

    pendingWord.clear();
    pendingWhitespace.clear();
    pendingLine.clear();

    while (lineSymbols.hasNext()) {
      StyledGrapheme grapheme = lineSymbols.next();
      boolean isWhitespace = grapheme.isWhitespace();
      int symbolWidth = Wcwidth.width(grapheme.symbol);

      // ignore symbols wider than line limit
      if (symbolWidth > maxLineWidth) {
        continue;
      }

      boolean wordFound = nonWhitespacePrevious && isWhitespace;
      // current word would overflow after removing whitespace
      boolean trimmedOverflow =
          pendingLine.isEmpty() && trim && wordWidth + symbolWidth > maxLineWidth;
      // separated whitespace would overflow on its own
      boolean whitespaceOverflow =
          pendingLine.isEmpty() && trim && whitespaceWidth + symbolWidth > maxLineWidth;
      // current full word (including whitespace) would overflow
      boolean untrimmedOverflow =
          pendingLine.isEmpty()
              && !trim
              && wordWidth + whitespaceWidth + symbolWidth > maxLineWidth;

      // append finished segment to current line
      if (wordFound || trimmedOverflow || whitespaceOverflow || untrimmedOverflow) {
        if (!pendingLine.isEmpty() || !trim) {
          while (!pendingWhitespace.isEmpty()) {
            pendingLine.add(pendingWhitespace.pollFirst());
          }
          lineWidth += whitespaceWidth;
        }

        pendingLine.addAll(pendingWord);
        pendingWord.clear();
        lineWidth += wordWidth;

        pendingWhitespace.clear();
        whitespaceWidth = 0;
        wordWidth = 0;
      }

      // pending line fills up limit
      boolean lineFull = lineWidth >= maxLineWidth;
      // pending word would overflow line limit
      boolean pendingWordOverflow =
          symbolWidth > 0 && lineWidth + whitespaceWidth + wordWidth >= maxLineWidth;

      // add finished wrapped line to remaining lines
      if (lineFull || pendingWordOverflow) {
        int remainingWidth = saturatingSub(maxLineWidth, lineWidth);

        // mem::take(&mut pending_line)
        wrappedLines.addLast(pendingLine);
        pendingLine = new ArrayList<>();
        lineWidth = 0;

        // remove whitespace up to the end of line
        while (!pendingWhitespace.isEmpty()) {
          StyledGrapheme front = pendingWhitespace.peekFirst();
          int width = Wcwidth.width(front.symbol);
          if (width > remainingWidth) {
            break;
          }
          whitespaceWidth -= width;
          remainingWidth -= width;
          pendingWhitespace.pollFirst();
        }

        // don't count first whitespace toward next word
        if (isWhitespace && pendingWhitespace.isEmpty()) {
          continue;
        }
      }

      // append symbol to a pending buffer
      if (isWhitespace) {
        whitespaceWidth += symbolWidth;
        pendingWhitespace.addLast(grapheme);
      } else {
        wordWidth += symbolWidth;
        pendingWord.add(grapheme);
      }

      nonWhitespacePrevious = !isWhitespace;
    }

    // append remaining text parts
    if (pendingLine.isEmpty() && pendingWord.isEmpty() && !pendingWhitespace.isEmpty() && trim) {
      wrappedLines.addLast(new ArrayList<>());
    }
    if (!pendingLine.isEmpty() || !trim) {
      while (!pendingWhitespace.isEmpty()) {
        pendingLine.add(pendingWhitespace.pollFirst());
      }
    }
    pendingLine.addAll(pendingWord);
    pendingWord.clear();

    if (!pendingLine.isEmpty()) {
      wrappedLines.addLast(pendingLine);
    } else {
      // Mirror upstream: if the (empty) pending_line had capacity, return it to the pool.
      // ArrayList in Java has no exposed capacity, so we just always pool empties — this
      // preserves the same number of allocations.
      pendingLinePool.add(pendingLine);
    }
    if (wrappedLines.isEmpty()) {
      wrappedLines.addLast(new ArrayList<>());
    }
  }

  private void replaceCurrentLine(List<StyledGrapheme> line) {
    List<StyledGrapheme> cache = currentLine;
    currentLine = line;
    // Always pool — Java has no exposed capacity check.
    pendingLinePool.add(cache);
  }

  private static int saturatingSub(int a, int b) {
    return Math.max(0, a - b);
  }
}
