package tui.widgets.tabs;

import java.util.Optional;
import tui.Buffer;
import tui.Position;
import tui.Rect;
import tui.Span;
import tui.Spans;
import tui.Style;
import tui.Symbols;
import tui.Widget;
import tui.internal.Ranges;
import tui.internal.Saturating;
import tui.widgets.BlockWidget;

/// A widget to display available tabs in a multiple panels context.
public final class TabsWidget implements Widget {
  public final Optional<BlockWidget> block;
  public final Spans[] titles;
  public final int selected;
  public final Style style;
  public final Style highlightStyle;
  public final Span divider;

  public TabsWidget(
      Optional<BlockWidget> block,
      Spans[] titles,
      int selected,
      Style style,
      Style highlightStyle,
      Span divider) {
    this.block = block;
    this.titles = titles;
    this.selected = selected;
    this.style = style;
    this.highlightStyle = highlightStyle;
    this.divider = divider;
  }

  public static TabsWidget empty(Spans[] titles) {
    return new TabsWidget(
        Optional.empty(),
        titles,
        0,
        Style.DEFAULT,
        Style.DEFAULT,
        Span.nostyle(Symbols.line.VERTICAL));
  }

  public TabsWidget withBlock(BlockWidget b) {
    return new TabsWidget(Optional.of(b), titles, selected, style, highlightStyle, divider);
  }

  public TabsWidget withTitles(Spans[] t) {
    return new TabsWidget(block, t, selected, style, highlightStyle, divider);
  }

  public TabsWidget withSelected(int s) {
    return new TabsWidget(block, titles, s, style, highlightStyle, divider);
  }

  public TabsWidget withStyle(Style s) {
    return new TabsWidget(block, titles, selected, s, highlightStyle, divider);
  }

  public TabsWidget withHighlightStyle(Style s) {
    return new TabsWidget(block, titles, selected, style, s, divider);
  }

  public TabsWidget withDivider(Span d) {
    return new TabsWidget(block, titles, selected, style, highlightStyle, d);
  }

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    Rect tabsArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      tabsArea = innerArea;
    } else {
      tabsArea = area;
    }

    if (tabsArea.height() < 1) {
      return;
    }

    int[] x = {tabsArea.left()};
    int titlesLength = titles.length;
    Ranges.range(
        0,
        titlesLength,
        i -> {
          Spans title = titles[i];
          boolean lastTitle = titlesLength - 1 == i;
          x[0] = Saturating.saturatingAdd(x[0], 1);
          int remainingWidth = Saturating.saturatingSubUnsigned(tabsArea.right(), x[0]);
          if (remainingWidth == 0) {
            return;
          }
          Position pos = buf.setSpans(x[0], tabsArea.top(), title, remainingWidth);
          if (i == selected) {
            buf.setStyle(
                new Rect(
                    x[0],
                    tabsArea.top(),
                    Saturating.saturatingSubUnsigned(pos.x(), x[0]),
                    1),
                highlightStyle);
          }
          x[0] = Saturating.saturatingAdd(pos.x(), 1);
          int remainingWidth1 = Saturating.saturatingSubUnsigned(tabsArea.right(), x[0]);
          if (remainingWidth1 == 0 || lastTitle) {
            return;
          }
          Position pos2 = buf.setSpan(x[0], tabsArea.top(), divider, remainingWidth1);
          x[0] = pos2.x();
        });
  }
}
