package tui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import tui.Alignment;
import tui.Buffer;
import tui.Grapheme;
import tui.Rect;
import tui.Span;
import tui.Spans;
import tui.Style;
import tui.StyledGrapheme;
import tui.Widget;
import tui.internal.Reflow;
import tui.internal.Saturating;

/// A widget to display some text.
public final class ParagraphWidget implements Widget {
  public final tui.Text text;
  public final Optional<BlockWidget> block;
  public final Style style;
  public final Optional<Wrap> wrap;
  public final Scroll scroll;
  public final Alignment alignment;

  public ParagraphWidget(
      tui.Text text,
      Optional<BlockWidget> block,
      Style style,
      Optional<Wrap> wrap,
      Scroll scroll,
      Alignment alignment) {
    this.text = text;
    this.block = block;
    this.style = style;
    this.wrap = wrap;
    this.scroll = scroll;
    this.alignment = alignment;
  }

  public static ParagraphWidget empty(tui.Text text) {
    return new ParagraphWidget(
        text, Optional.empty(), Style.DEFAULT, Optional.empty(), new Scroll(0, 0), Alignment.Left);
  }

  public ParagraphWidget withBlock(BlockWidget b) {
    return new ParagraphWidget(text, Optional.of(b), style, wrap, scroll, alignment);
  }

  public ParagraphWidget withStyle(Style s) {
    return new ParagraphWidget(text, block, s, wrap, scroll, alignment);
  }

  public ParagraphWidget withWrap(Wrap w) {
    return new ParagraphWidget(text, block, style, Optional.of(w), scroll, alignment);
  }

  public ParagraphWidget withScroll(Scroll s) {
    return new ParagraphWidget(text, block, style, wrap, s, alignment);
  }

  public ParagraphWidget withAlignment(Alignment a) {
    return new ParagraphWidget(text, block, style, wrap, scroll, a);
  }

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    Rect textArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      textArea = innerArea;
    } else {
      textArea = area;
    }

    if (textArea.height() < 1) {
      return;
    }

    List<StyledGrapheme> styled = new ArrayList<>();
    for (Spans line : text.lines()) {
      for (Span span : line.spans()) {
        styled.addAll(Arrays.asList(span.styledGraphemes(style)));
      }
      styled.add(new StyledGrapheme(new Grapheme("\n"), style));
    }
    Iterator<StyledGrapheme> iter = styled.iterator();

    Reflow.LineComposer lineComposer;
    if (wrap.isPresent()) {
      lineComposer = new Reflow.WordWrapper(iter, textArea.width(), wrap.get().trim());
    } else {
      int horizontalOffset = alignment == Alignment.Left ? scroll.x() : 0;
      lineComposer = new Reflow.LineTruncator(iter, textArea.width(), horizontalOffset);
    }

    int y = 0;
    boolean cont = true;
    while (cont) {
      Optional<Reflow.Line> nextLine = lineComposer.nextLine();
      if (nextLine.isEmpty()) {
        cont = false;
      } else {
        Reflow.Line line = nextLine.get();
        if (y >= scroll.y()) {
          int x = getLineOffset(line.width(), textArea.width(), alignment);
          for (StyledGrapheme sg : line.graphemes()) {
            String newSymbol = sg.symbol().str.isEmpty() ? " " : sg.symbol().str;
            buf.get(textArea.left() + x, textArea.top() + y - scroll.y())
                .setSymbol(newSymbol)
                .setStyle(sg.style());
            x += sg.symbol().width();
          }
        }
        y += 1;
        if (y >= textArea.height() + scroll.y()) {
          cont = false;
        }
      }
    }
  }

  public static int getLineOffset(int lineWidth, int textAreaWidth, Alignment alignment) {
    return switch (alignment) {
      case Center -> Saturating.saturatingSubUnsigned(textAreaWidth / 2, lineWidth / 2);
      case Right -> Saturating.saturatingSubUnsigned(textAreaWidth, lineWidth);
      case Left -> 0;
    };
  }

  /// Describes how to wrap text across lines.
  public record Wrap(boolean trim) {}

  public record Scroll(int y, int x) {}
}
