package tui.widgets;

import java.util.Optional;
import tui.Buffer;
import tui.Corner;
import tui.Grapheme;
import tui.Position;
import tui.Rect;
import tui.StatefulWidget;
import tui.Style;
import tui.Text;
import tui.Widget;
import tui.internal.Ranges;
import tui.internal.Saturating;

/// A widget to display several items among which one can be selected (optional)
public final class ListWidget implements Widget, StatefulWidget<ListWidget.State> {
  public final Optional<BlockWidget> block;
  public final Item[] items;
  public final Style style;
  public final Corner startCorner;
  public final Style highlightStyle;
  public final Optional<String> highlightSymbol;
  public final boolean repeatHighlightSymbol;

  public ListWidget(
      Optional<BlockWidget> block,
      Item[] items,
      Style style,
      Corner startCorner,
      Style highlightStyle,
      Optional<String> highlightSymbol,
      boolean repeatHighlightSymbol) {
    this.block = block;
    this.items = items;
    this.style = style;
    this.startCorner = startCorner;
    this.highlightStyle = highlightStyle;
    this.highlightSymbol = highlightSymbol;
    this.repeatHighlightSymbol = repeatHighlightSymbol;
  }

  public static ListWidget empty(Item[] items) {
    return new ListWidget(
        Optional.empty(),
        items,
        Style.DEFAULT,
        Corner.TopLeft,
        Style.DEFAULT,
        Optional.empty(),
        false);
  }

  public ListWidget withBlock(BlockWidget b) {
    return new ListWidget(
        Optional.of(b), items, style, startCorner, highlightStyle, highlightSymbol, repeatHighlightSymbol);
  }

  public ListWidget withItems(Item[] i) {
    return new ListWidget(
        block, i, style, startCorner, highlightStyle, highlightSymbol, repeatHighlightSymbol);
  }

  public ListWidget withStyle(Style s) {
    return new ListWidget(
        block, items, s, startCorner, highlightStyle, highlightSymbol, repeatHighlightSymbol);
  }

  public ListWidget withStartCorner(Corner c) {
    return new ListWidget(
        block, items, style, c, highlightStyle, highlightSymbol, repeatHighlightSymbol);
  }

  public ListWidget withHighlightStyle(Style s) {
    return new ListWidget(
        block, items, style, startCorner, s, highlightSymbol, repeatHighlightSymbol);
  }

  public ListWidget withHighlightSymbol(String s) {
    return new ListWidget(
        block, items, style, startCorner, highlightStyle, Optional.of(s), repeatHighlightSymbol);
  }

  public ListWidget withRepeatHighlightSymbol(boolean r) {
    return new ListWidget(
        block, items, style, startCorner, highlightStyle, highlightSymbol, r);
  }

  public Bounds getItemsBounds(Optional<Integer> selected0, int offset0, int maxHeight) {
    int offset = Math.min(offset0, Saturating.saturatingSubUnsigned(items.length, 1));
    int start = offset;
    int end = offset;
    int height = 0;
    int it = offset;
    boolean cont = true;
    while (cont && it < items.length) {
      Item item = items[it];
      if (height + item.height() > maxHeight) {
        cont = false;
      } else {
        height += item.height();
        end += 1;
        it += 1;
      }
    }

    int selected = Math.min(selected0.orElse(0), items.length - 1);
    while (selected >= end) {
      height = Saturating.saturatingAdd(height, items[end].height());
      end += 1;
      while (height > maxHeight) {
        height = Saturating.saturatingSubUnsigned(height, items[start].height());
        start += 1;
      }
    }
    while (selected < start) {
      start -= 1;
      height = Saturating.saturatingAdd(height, items[start].height());
      while (height > maxHeight) {
        end -= 1;
        height = Saturating.saturatingSubUnsigned(height, items[end].height());
      }
    }
    return new Bounds(start, end);
  }

  @Override
  public void render(Rect area, Buffer buf, State state) {
    buf.setStyle(area, style);
    Rect listArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      listArea = innerArea;
    } else {
      listArea = area;
    }

    if (listArea.width() < 1 || listArea.height() < 1) {
      return;
    }

    if (items.length == 0) {
      return;
    }
    int listHeight = listArea.height();

    Bounds bounds = getItemsBounds(state.selected, state.offset, listHeight);
    int start = bounds.start();
    int end = bounds.end();
    state.offset = start;

    String highlightSymbol1 = highlightSymbol.orElse("");
    String blankSymbol = " ".repeat(new Grapheme(highlightSymbol1).width());

    int[] currentHeight = {0};
    boolean hasSelection = state.selected.isPresent();
    Ranges.range(
        state.offset,
        state.offset + end - start,
        i -> {
          Item item = items[i];
          int x;
          int y;
          if (startCorner == Corner.BottomLeft) {
            currentHeight[0] += item.height();
            x = listArea.left();
            y = listArea.bottom() - currentHeight[0];
          } else {
            x = listArea.left();
            y = listArea.top() + currentHeight[0];
            currentHeight[0] += item.height();
          }
          Rect itemArea = new Rect(x, y, listArea.width(), item.height());

          Style itemStyle = style.patch(item.style());
          buf.setStyle(itemArea, itemStyle);

          boolean isSelected = state.selected.isPresent() && state.selected.get() == i;
          tui.Spans[] lines = item.content().lines();
          for (int j = 0; j < lines.length; j++) {
            tui.Spans line = lines[j];
            String symbol;
            if (isSelected && (j == 0 || repeatHighlightSymbol)) {
              symbol = highlightSymbol1;
            } else {
              symbol = blankSymbol;
            }
            int elemX;
            int maxElementWidth;
            if (hasSelection) {
              Position p = buf.setStringn(x, y + j, symbol, listArea.width(), itemStyle);
              elemX = p.x();
              maxElementWidth = listArea.width() - (elemX - x);
            } else {
              elemX = x;
              maxElementWidth = listArea.width();
            }
            buf.setSpans(elemX, y + j, line, maxElementWidth);
          }
          if (isSelected) {
            buf.setStyle(itemArea, highlightStyle);
          }
        });
  }

  @Override
  public void render(Rect area, Buffer buf) {
    State state = new State(0, Optional.empty());
    render(area, buf, state);
  }

  public record Bounds(int start, int end) {}

  public static final class State {
    public int offset;
    public Optional<Integer> selected;

    public State(int offset, Optional<Integer> selected) {
      this.offset = offset;
      this.selected = selected;
    }

    public static State empty() {
      return new State(0, Optional.empty());
    }

    public void select(Optional<Integer> index) {
      selected = index;
      if (index.isEmpty()) {
        offset = 0;
      }
    }
  }

  public record Item(Text content, Style style) {
    public int height() {
      return content.height();
    }

    public int width() {
      return content.width();
    }
  }
}
