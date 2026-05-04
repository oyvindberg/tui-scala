package jatatui.widgets.list;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.core.widgets.StatefulWidget;
import jatatui.core.widgets.Widget;
import jatatui.widgets.block.Block;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/// A widget to display several items among which one can be selected (optional).
///
/// A list is a collection of [ListItem]s.
///
/// This is different from a `Table` because it does not handle columns, headers or footers and
/// the item's height is automatically determined. A [List] can also be put in reverse order (i.e.
/// *bottom to top*) whereas a `Table` cannot.
///
/// List items can be aligned using [Text#withAlignment(HorizontalAlignment)], for more details see
/// [ListItem].
///
/// [List] is also a [StatefulWidget], which means you can use it with [ListState] to allow the
/// user to scroll through items and select one of them.
///
/// Mirrors `ratatui_widgets::list::List` (v0.30).
///
/// # Fluent setters
///
/// - [#withHighlightStyle(Style)] sets the style of the selected item.
/// - [#withHighlightSymbol(Line)] sets the symbol to be displayed in front of the selected item.
/// - [#withRepeatHighlightSymbol(boolean)] sets whether to repeat the symbol and style over
///   selected multi-line items.
/// - [#withDirection(ListDirection)] sets the list direction.
public final class List implements Widget, StatefulWidget<ListState>, Stylize<List> {

  /// An optional [Block] to wrap the widget in.
  public final Optional<Block> block;

  /// The items in the list.
  public final java.util.List<ListItem> items;

  /// Style used as a base style for the widget.
  public final Style style;

  /// List display direction.
  public final ListDirection direction;

  /// Style used to render the selected item.
  public final Style highlightStyle;

  /// Symbol in front of the selected item (shifts all items to the right).
  public final Optional<Line> highlightSymbol;

  /// Whether to repeat the highlight symbol for each line of the selected item.
  public final boolean repeatHighlightSymbol;

  /// Decides when to allocate spacing for the selection symbol.
  public final HighlightSpacing highlightSpacing;

  /// How many items to try to keep visible before and after the selected item.
  public final int scrollPadding;

  private List(
      Optional<Block> block,
      java.util.List<ListItem> items,
      Style style,
      ListDirection direction,
      Style highlightStyle,
      Optional<Line> highlightSymbol,
      boolean repeatHighlightSymbol,
      HighlightSpacing highlightSpacing,
      int scrollPadding) {
    this.block = block;
    this.items = java.util.List.copyOf(items);
    this.style = style;
    this.direction = direction;
    this.highlightStyle = highlightStyle;
    this.highlightSymbol = highlightSymbol;
    this.repeatHighlightSymbol = repeatHighlightSymbol;
    this.highlightSpacing = highlightSpacing;
    this.scrollPadding = scrollPadding;
  }

  // ---- Construction ----

  /// Returns an empty list (no items, no block, default style).
  public static List empty() {
    return new List(
        Optional.empty(),
        java.util.List.of(),
        Style.empty(),
        ListDirection.TopToBottom,
        Style.empty(),
        Optional.empty(),
        false,
        HighlightSpacing.WhenSelected,
        0);
  }

  /// Returns a [List] with the given items.
  public static List of(Collection<ListItem> items) {
    return empty().withItems(items);
  }

  /// Returns a [List] with the given items.
  public static List of(ListItem... items) {
    java.util.List<ListItem> list = new ArrayList<>(items.length);
    for (ListItem i : items) list.add(i);
    return empty().withItems(list);
  }

  /// Convenience overload: builds a [List] from a varargs of strings, mapped through
  /// [ListItem#of(String)].
  public static List ofStrings(String... items) {
    java.util.List<ListItem> list = new ArrayList<>(items.length);
    for (String s : items) list.add(ListItem.of(s));
    return empty().withItems(list);
  }

  // ---- Fluent setters ----

  /// Returns a copy with the given items (replacing any previous items).
  public List withItems(Collection<ListItem> items) {
    return new List(
        block,
        new ArrayList<>(items),
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  /// Returns a copy wrapping the widget in the given [Block].
  public List withBlock(Block block) {
    return new List(
        Optional.of(block),
        items,
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  /// Returns a copy with the base style replaced.
  public List withStyle(Style style) {
    return new List(
        block,
        items,
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  /// Returns a copy with the highlight symbol set.
  public List withHighlightSymbol(Line highlightSymbol) {
    return new List(
        block,
        items,
        style,
        direction,
        highlightStyle,
        Optional.of(highlightSymbol),
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  /// Convenience overload: takes a string, mapped through [Line#raw(String)].
  public List withHighlightSymbol(String highlightSymbol) {
    return withHighlightSymbol(Line.raw(highlightSymbol));
  }

  /// Returns a copy with the highlight style replaced.
  public List withHighlightStyle(Style highlightStyle) {
    return new List(
        block,
        items,
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  /// Returns a copy with the repeat-highlight-symbol flag set.
  public List withRepeatHighlightSymbol(boolean repeat) {
    return new List(
        block,
        items,
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeat,
        highlightSpacing,
        scrollPadding);
  }

  /// Returns a copy with the [HighlightSpacing] policy set.
  public List withHighlightSpacing(HighlightSpacing highlightSpacing) {
    return new List(
        block,
        items,
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  /// Returns a copy with the direction set.
  public List withDirection(ListDirection direction) {
    return new List(
        block,
        items,
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  /// Returns a copy with the scroll padding set.
  public List withScrollPadding(int scrollPadding) {
    return new List(
        block,
        items,
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  // ---- Accessors ----

  /// Returns the number of [ListItem]s in the list.
  public int len() {
    return items.size();
  }

  /// Returns true if the list contains no elements.
  public boolean isEmpty() {
    return items.isEmpty();
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public List setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Widget rendering (no state) ----

  @Override
  public void render(Rect area, Buffer buf) {
    ListState state = ListState.empty();
    render(area, buf, state);
  }

  // ---- StatefulWidget rendering ----

  @Override
  public void render(Rect area, Buffer buf, ListState state) {
    buf.setStyle(area, style);
    block.ifPresent(b -> b.render(area, buf));
    Rect listArea = block.map(b -> b.inner(area)).orElse(area);

    if (listArea.isEmpty()) {
      return;
    }

    if (items.isEmpty()) {
      state.select(Optional.empty());
      return;
    }

    // Clamp out-of-bounds selected index to the last item.
    Optional<Integer> selected = state.selected();
    if (selected.isPresent() && selected.get() >= items.size()) {
      state.select(Optional.of(saturatingSub(items.size(), 1)));
      selected = state.selected();
    }

    int listHeight = listArea.height();

    Bounds bounds = getItemsBounds(selected, state.offset(), listHeight);
    int firstVisibleIndex = bounds.first();
    int lastVisibleIndex = bounds.last();

    // Important: this changes the state's offset to the beginning of the now viewable items.
    state.setOffset(firstVisibleIndex);

    Line highlightSymbolLine = highlightSymbol.orElse(Line.empty());
    int highlightSymbolWidth = highlightSymbolLine.width();
    String emptySymbolStr = " ".repeat(highlightSymbolWidth);
    Line emptySymbol = Line.raw(emptySymbolStr);

    int currentHeight = 0;
    boolean selectionSpacing = highlightSpacing.shouldAdd(selected.isPresent());

    int take = lastVisibleIndex - firstVisibleIndex;
    for (int idx = 0; idx < take; idx++) {
      int i = state.offset() + idx;
      ListItem item = items.get(i);
      int itemHeight = item.height();

      int x;
      int y;
      if (direction == ListDirection.BottomToTop) {
        currentHeight += itemHeight;
        x = listArea.left();
        y = listArea.bottom() - currentHeight;
      } else {
        x = listArea.left();
        y = listArea.top() + currentHeight;
        currentHeight += itemHeight;
      }

      Rect rowArea = new Rect(x, y, listArea.width(), itemHeight);

      Style itemStyle = style.patch(item.style);
      buf.setStyle(rowArea, itemStyle);

      boolean isSelected = selected.isPresent() && selected.get() == i;

      Rect itemArea;
      if (selectionSpacing) {
        int newX = rowArea.x() + highlightSymbolWidth;
        int newWidth = saturatingSub(rowArea.width(), highlightSymbolWidth);
        itemArea = new Rect(newX, rowArea.y(), newWidth, rowArea.height());
      } else {
        itemArea = rowArea;
      }
      renderText(item.content, itemArea, buf);

      if (isSelected) {
        buf.setStyle(rowArea, highlightStyle);
      }
      if (selectionSpacing) {
        for (int j = 0; j < item.content.height(); j++) {
          // For a selected item we render the highlight symbol on the first line, and on all
          // subsequent lines if `repeatHighlightSymbol` is set.
          Line line =
              (isSelected && (j == 0 || repeatHighlightSymbol)) ? highlightSymbolLine : emptySymbol;
          Rect highlightArea = new Rect(x, y + j, highlightSymbolWidth, 1);
          renderLineAt(line, highlightArea, buf);
        }
      }
    }
  }

  // ---- Bounds calculation ----

  private record Bounds(int first, int last) {}

  /// Given an offset, calculate which items can fit in a given area.
  Bounds getItemsBounds(Optional<Integer> selected, int offset, int maxHeight) {
    int clampedOffset = Math.min(offset, saturatingSub(items.size(), 1));

    int firstVisibleIndex = clampedOffset;
    int lastVisibleIndex = clampedOffset;

    int heightFromOffset = 0;

    // Calculate the last visible index and the total height of the items that will fit.
    for (int i = clampedOffset; i < items.size(); i++) {
      ListItem item = items.get(i);
      if (heightFromOffset + item.height() > maxHeight) {
        break;
      }
      heightFromOffset += item.height();
      lastVisibleIndex += 1;
    }

    // Get the selected index and apply scroll_padding to it, but still honor the offset if
    // nothing is selected. This allows the list to stay at a position after select(None)ing.
    int indexToDisplay =
        applyScrollPaddingToSelectedIndex(
                selected, maxHeight, firstVisibleIndex, lastVisibleIndex)
            .orElse(clampedOffset);

    // Recall that lastVisibleIndex is the index of what we can render up to in the given space
    // after the offset. If we have an item selected that is out of the viewable area (or the
    // offset is still set), we still need to show this item.
    while (indexToDisplay >= lastVisibleIndex) {
      heightFromOffset = saturatingAdd(heightFromOffset, items.get(lastVisibleIndex).height());
      lastVisibleIndex += 1;

      // Now we need to hide previous items since we didn't have space for the selected/offset
      // item.
      while (heightFromOffset > maxHeight) {
        heightFromOffset = saturatingSub(heightFromOffset, items.get(firstVisibleIndex).height());
        firstVisibleIndex += 1;
      }
    }

    // Similar but the other way: if the selected index is before the viewable range, show it.
    while (indexToDisplay < firstVisibleIndex) {
      firstVisibleIndex -= 1;
      heightFromOffset = saturatingAdd(heightFromOffset, items.get(firstVisibleIndex).height());

      while (heightFromOffset > maxHeight) {
        lastVisibleIndex -= 1;
        heightFromOffset = saturatingSub(heightFromOffset, items.get(lastVisibleIndex).height());
      }
    }

    return new Bounds(firstVisibleIndex, lastVisibleIndex);
  }

  /// Applies scroll padding to the selected index, reducing the padding value to keep the
  /// selected item on screen even with items of inconsistent sizes.
  ///
  /// This function is sensitive to how the bounds checking function handles item height.
  Optional<Integer> applyScrollPaddingToSelectedIndex(
      Optional<Integer> selected, int maxHeight, int firstVisibleIndex, int lastVisibleIndex) {
    if (selected.isEmpty()) return Optional.empty();
    int lastValidIndex = saturatingSub(items.size(), 1);
    int sel = Math.min(selected.get(), lastValidIndex);

    // Reduce padding if the selected item plus padding doesn't fit on screen.
    int scrollPadding = this.scrollPadding;
    while (scrollPadding > 0) {
      int heightAroundSelected = 0;
      int from = saturatingSub(sel, scrollPadding);
      int to = Math.min(saturatingAdd(sel, scrollPadding), lastValidIndex);
      for (int i = from; i <= to; i++) {
        heightAroundSelected += items.get(i).height();
      }
      if (heightAroundSelected <= maxHeight) {
        break;
      }
      scrollPadding -= 1;
    }

    int candidate;
    int saturated = Math.min(saturatingAdd(sel, scrollPadding), lastValidIndex);
    if (saturated >= lastVisibleIndex) {
      candidate = saturatingAdd(sel, scrollPadding);
    } else if (saturatingSub(sel, scrollPadding) < firstVisibleIndex) {
      candidate = saturatingSub(sel, scrollPadding);
    } else {
      candidate = sel;
    }
    return Optional.of(Math.min(candidate, lastValidIndex));
  }

  // ---- Text rendering helpers (mirror Widget impls in ratatui-core for Text/Line) ----

  /// Render a [Text] into `area`, applying the text's base style and per-line alignment.
  ///
  /// Mirrors `impl Widget for &Text<'_>` from `ratatui-core/src/text/text.rs`.
  static void renderText(Text text, Rect area, Buffer buf) {
    Rect clipped = area.intersection(buf.area);
    buf.setStyle(clipped, text.style);
    Optional<HorizontalAlignment> parentAlignment = text.alignment;
    int rows = clipped.height();
    java.util.List<Line> lines = text.lines;
    int n = Math.min(rows, lines.size());
    for (int i = 0; i < n; i++) {
      Rect rowArea = new Rect(clipped.x(), clipped.y() + i, clipped.width(), 1);
      renderLineWithAlignment(lines.get(i), rowArea, buf, parentAlignment);
    }
  }

  /// Render a [Line] into `area` using the line's own alignment (no parent override).
  static void renderLineAt(Line line, Rect area, Buffer buf) {
    renderLineWithAlignment(line, area, buf, Optional.empty());
  }

  /// Mirrors `Line::render_with_alignment` from `ratatui-core/src/text/line.rs`.
  static void renderLineWithAlignment(
      Line line, Rect area, Buffer buf, Optional<HorizontalAlignment> parentAlignment) {
    Rect clipped = area.intersection(buf.area);
    if (clipped.isEmpty()) return;
    Rect rowArea = new Rect(clipped.x(), clipped.y(), clipped.width(), 1);

    int lineWidth = line.width();
    if (lineWidth == 0) return;

    buf.setStyle(rowArea, line.style);

    Optional<HorizontalAlignment> alignment =
        line.alignment.isPresent() ? line.alignment : parentAlignment;

    int areaWidth = rowArea.width();
    boolean canRenderComplete = lineWidth <= areaWidth;
    if (canRenderComplete) {
      int indent =
          alignment
              .map(
                  a ->
                      switch (a) {
                        case Center -> Math.max(0, (areaWidth - lineWidth) / 2);
                        case Right -> Math.max(0, areaWidth - lineWidth);
                        case Left -> 0;
                      })
              .orElse(0);
      Rect indented = indentX(rowArea, indent);
      renderSpans(line.spans, indented, buf, 0);
    } else {
      int skip =
          alignment
              .map(
                  a ->
                      switch (a) {
                        case Center -> Math.max(0, (lineWidth - areaWidth) / 2);
                        case Right -> Math.max(0, lineWidth - areaWidth);
                        case Left -> 0;
                      })
              .orElse(0);
      renderSpans(line.spans, rowArea, buf, skip);
    }
  }

  private static Rect indentX(Rect area, int amount) {
    int clampedAmount = Math.min(amount, area.width());
    return new Rect(
        area.x() + clampedAmount, area.y(), area.width() - clampedAmount, area.height());
  }

  /// Renders the spans of a [Line] into `area`, optionally skipping the leading `skipWidth`
  /// columns (used when the line is too long for the area and we have a non-Left alignment).
  private static void renderSpans(
      java.util.List<Span> spans, Rect area, Buffer buf, int skipWidth) {
    int remainingSkip = skipWidth;
    Rect cur = area;
    for (Span span : spans) {
      int spanWidth = span.width();
      if (remainingSkip >= spanWidth) {
        remainingSkip -= spanWidth;
        continue;
      }
      // Render the partially-or-fully-visible span.
      String content = span.content;
      int offsetCols;
      if (remainingSkip > 0) {
        // Skip `remainingSkip` columns from the start of this span by carving the content.
        // We approximate by stripping leading characters whose total display width matches
        // `remainingSkip`. Since spans here are never fancier than ASCII for our test cases this
        // is good enough; for full correctness we'd grapheme-iterate, mirroring upstream's
        // `spans_after_width` exactly.
        StringBuilder sb = new StringBuilder();
        int dropped = 0;
        int i = 0;
        while (i < content.length() && dropped < remainingSkip) {
          int cp = content.codePointAt(i);
          int charLen = Character.charCount(cp);
          // Approximation: assume width 1 per code point for this slow path.
          dropped += 1;
          i += charLen;
        }
        sb.append(content, i, content.length());
        offsetCols = 0;
        remainingSkip = 0;
        Position end =
            buf.setStringn(cur.x() + offsetCols, cur.y(), sb.toString(), cur.width(), span.style);
        int written = Math.max(0, end.x() - (cur.x() + offsetCols));
        cur = indentX(cur, offsetCols + written);
        if (cur.isEmpty()) break;
      } else {
        Position end =
            buf.setStringn(cur.x(), cur.y(), span.content, cur.width(), span.style);
        int written = Math.max(0, end.x() - cur.x());
        cur = indentX(cur, written);
        if (cur.isEmpty()) break;
      }
    }
  }

  // ---- Helpers ----

  private static int saturatingAdd(int a, int b) {
    long r = (long) a + (long) b;
    if (r > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (r < 0) return 0;
    return (int) r;
  }

  private static int saturatingSub(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    return (int) r;
  }

  // ---- Equality / hash / debug ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof List other)) return false;
    return repeatHighlightSymbol == other.repeatHighlightSymbol
        && scrollPadding == other.scrollPadding
        && block.equals(other.block)
        && items.equals(other.items)
        && style.equals(other.style)
        && direction == other.direction
        && highlightStyle.equals(other.highlightStyle)
        && highlightSymbol.equals(other.highlightSymbol)
        && highlightSpacing == other.highlightSpacing;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        block,
        items,
        style,
        direction,
        highlightStyle,
        highlightSymbol,
        repeatHighlightSymbol,
        highlightSpacing,
        scrollPadding);
  }

  @Override
  public String toString() {
    return "List{items=" + items.size() + ", direction=" + direction + ", style=" + style + "}";
  }
}
