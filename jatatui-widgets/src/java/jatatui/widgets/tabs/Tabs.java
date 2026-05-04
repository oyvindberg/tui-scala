package jatatui.widgets.tabs;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.symbols.Line;
import jatatui.core.text.Span;
import jatatui.core.widgets.Widget;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A widget that displays a horizontal set of Tabs with a single tab selected.
///
/// Mirrors `ratatui_widgets::tabs::Tabs` (v0.30).
///
/// Each tab title is stored as a [jatatui.core.text.Line] which can be individually styled. The
/// selected tab is set using [#withSelected(int)] and styled using [#withHighlightStyle(Style)].
/// The divider can be customized with [#withDivider(Span)]. Padding can be set with
/// [#withPadding(jatatui.core.text.Line, jatatui.core.text.Line)] or the per-side variants.
public final class Tabs implements Widget, Stylize<Tabs> {

  /// The default highlight style: reversed.
  public static final Style DEFAULT_HIGHLIGHT_STYLE = Style.fromModifier(Modifier.REVERSED);

  private final Optional<jatatui.widgets.block.Block> block;
  private final List<jatatui.core.text.Line> titles;
  private final Optional<Integer> selected;
  private final Style style;
  private final Style highlightStyle;
  private final Span divider;
  private final jatatui.core.text.Line paddingLeft;
  private final jatatui.core.text.Line paddingRight;

  private Tabs(
      Optional<jatatui.widgets.block.Block> block,
      List<jatatui.core.text.Line> titles,
      Optional<Integer> selected,
      Style style,
      Style highlightStyle,
      Span divider,
      jatatui.core.text.Line paddingLeft,
      jatatui.core.text.Line paddingRight) {
    this.block = block;
    this.titles = List.copyOf(titles);
    this.selected = selected;
    this.style = style;
    this.highlightStyle = highlightStyle;
    this.divider = divider;
    this.paddingLeft = paddingLeft;
    this.paddingRight = paddingRight;
  }

  /// Creates new `Tabs` from their titles. The first tab (index 0) is selected by default. If
  /// `titles` is empty, no tab is selected.
  public static Tabs of(List<jatatui.core.text.Line> titles) {
    Optional<Integer> selected = titles.isEmpty() ? Optional.empty() : Optional.of(0);
    return new Tabs(
        Optional.empty(),
        titles,
        selected,
        Style.empty(),
        DEFAULT_HIGHLIGHT_STYLE,
        Span.raw(Line.VERTICAL),
        jatatui.core.text.Line.from(" "),
        jatatui.core.text.Line.from(" "));
  }

  /// Creates new `Tabs` from a varargs of strings.
  public static Tabs ofStrings(String... titles) {
    List<jatatui.core.text.Line> lines = new ArrayList<>(titles.length);
    for (String t : titles) {
      lines.add(jatatui.core.text.Line.from(t));
    }
    return of(lines);
  }

  /// Returns a default `Tabs` widget with no titles.
  public static Tabs empty() {
    return of(List.of());
  }

  // ---- Builder methods ----

  /// Sets the titles. The selected tab is clamped to the last available tab; if no tab was
  /// selected before but titles become non-empty, defaults to 0.
  public Tabs withTitles(List<jatatui.core.text.Line> titles) {
    Optional<Integer> nextSelected;
    if (titles.isEmpty()) {
      nextSelected = Optional.empty();
    } else if (selected.isPresent()) {
      nextSelected = Optional.of(Math.min(selected.get(), titles.size() - 1));
    } else {
      nextSelected = Optional.of(0);
    }
    return new Tabs(
        block,
        titles,
        nextSelected,
        style,
        highlightStyle,
        divider,
        paddingLeft,
        paddingRight);
  }

  /// Convenience: sets the titles from a varargs of strings.
  public Tabs withTitlesStrings(String... titles) {
    List<jatatui.core.text.Line> lines = new ArrayList<>(titles.length);
    for (String t : titles) {
      lines.add(jatatui.core.text.Line.from(t));
    }
    return withTitles(lines);
  }

  /// Surrounds the `Tabs` with a [jatatui.widgets.block.Block].
  public Tabs withBlock(jatatui.widgets.block.Block block) {
    return new Tabs(
        Optional.of(block),
        titles,
        selected,
        style,
        highlightStyle,
        divider,
        paddingLeft,
        paddingRight);
  }

  /// Sets the selected tab.
  public Tabs withSelected(int selected) {
    return new Tabs(
        block,
        titles,
        Optional.of(selected),
        style,
        highlightStyle,
        divider,
        paddingLeft,
        paddingRight);
  }

  /// Clears the selected tab (no tab will be highlighted).
  public Tabs withoutSelected() {
    return new Tabs(
        block,
        titles,
        Optional.empty(),
        style,
        highlightStyle,
        divider,
        paddingLeft,
        paddingRight);
  }

  /// Sets the style of the entire widget.
  public Tabs withStyle(Style style) {
    return new Tabs(
        block,
        titles,
        selected,
        style,
        highlightStyle,
        divider,
        paddingLeft,
        paddingRight);
  }

  /// Sets the style for the highlighted tab.
  public Tabs withHighlightStyle(Style highlightStyle) {
    return new Tabs(
        block,
        titles,
        selected,
        style,
        highlightStyle,
        divider,
        paddingLeft,
        paddingRight);
  }

  /// Sets the divider span used between tabs.
  public Tabs withDivider(Span divider) {
    return new Tabs(
        block,
        titles,
        selected,
        style,
        highlightStyle,
        divider,
        paddingLeft,
        paddingRight);
  }

  /// Sets the divider from a string.
  public Tabs withDivider(String divider) {
    return withDivider(Span.raw(divider));
  }

  /// Sets the padding between tabs (left and right).
  public Tabs withPadding(jatatui.core.text.Line left, jatatui.core.text.Line right) {
    return new Tabs(block, titles, selected, style, highlightStyle, divider, left, right);
  }

  /// Convenience overload accepting strings.
  public Tabs withPadding(String left, String right) {
    return withPadding(jatatui.core.text.Line.from(left), jatatui.core.text.Line.from(right));
  }

  /// Sets the left side padding between tabs.
  public Tabs withPaddingLeft(jatatui.core.text.Line padding) {
    return new Tabs(
        block, titles, selected, style, highlightStyle, divider, padding, paddingRight);
  }

  /// Convenience overload accepting a string.
  public Tabs withPaddingLeft(String padding) {
    return withPaddingLeft(jatatui.core.text.Line.from(padding));
  }

  /// Sets the right side padding between tabs.
  public Tabs withPaddingRight(jatatui.core.text.Line padding) {
    return new Tabs(
        block, titles, selected, style, highlightStyle, divider, paddingLeft, padding);
  }

  /// Convenience overload accepting a string.
  public Tabs withPaddingRight(String padding) {
    return withPaddingRight(jatatui.core.text.Line.from(padding));
  }

  // ---- Accessors ----

  public List<jatatui.core.text.Line> titles() {
    return titles;
  }

  public Optional<Integer> selected() {
    return selected;
  }

  public Style highlightStyle() {
    return highlightStyle;
  }

  public Span divider() {
    return divider;
  }

  public jatatui.core.text.Line paddingLeft() {
    return paddingLeft;
  }

  public jatatui.core.text.Line paddingRight() {
    return paddingRight;
  }

  // ---- Width helpers (unicode-width) ----

  /// Returns the total width of the rendered tabs (titles + dividers + padding). Does not include
  /// any borders added by the optional block.
  public int width() {
    int titlesWidth = 0;
    for (jatatui.core.text.Line t : titles) {
      titlesWidth += t.width();
    }
    int titleCount = titles.size();
    int dividerCount = Math.max(0, titleCount - 1);
    int dividerWidth = dividerCount * divider.width();
    int leftPaddingWidth = titleCount * paddingLeft.width();
    int rightPaddingWidth = titleCount * paddingRight.width();
    return titlesWidth + dividerWidth + leftPaddingWidth + rightPaddingWidth;
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Tabs setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Widget render ----

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    block.ifPresent(b -> b.render(area, buf));
    Rect inner = block.map(b -> b.inner(area)).orElse(area);
    renderTabs(inner, buf);
  }

  private void renderTabs(Rect tabsArea, Buffer buf) {
    if (tabsArea.isEmpty()) {
      return;
    }
    int x = tabsArea.left();
    int titlesLength = titles.size();
    for (int i = 0; i < titlesLength; i++) {
      jatatui.core.text.Line title = titles.get(i);
      boolean lastTitle = (titlesLength - 1) == i;
      int remainingWidth = Math.max(0, tabsArea.right() - x);
      if (remainingWidth == 0) break;

      // Left padding
      Position pos = buf.setLine(x, tabsArea.top(), paddingLeft, remainingWidth);
      x = pos.x();
      remainingWidth = Math.max(0, tabsArea.right() - x);
      if (remainingWidth == 0) break;

      // Title
      pos = buf.setLine(x, tabsArea.top(), title, remainingWidth);
      if (selected.isPresent() && selected.get() == i) {
        buf.setStyle(
            new Rect(x, tabsArea.top(), Math.max(0, pos.x() - x), 1), highlightStyle);
      }
      x = pos.x();
      remainingWidth = Math.max(0, tabsArea.right() - x);
      if (remainingWidth == 0) break;

      // Right padding
      pos = buf.setLine(x, tabsArea.top(), paddingRight, remainingWidth);
      x = pos.x();
      remainingWidth = Math.max(0, tabsArea.right() - x);
      if (remainingWidth == 0 || lastTitle) break;

      pos = buf.setSpan(x, tabsArea.top(), divider, remainingWidth);
      x = pos.x();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tabs other)) return false;
    return Objects.equals(block, other.block)
        && titles.equals(other.titles)
        && selected.equals(other.selected)
        && style.equals(other.style)
        && highlightStyle.equals(other.highlightStyle)
        && divider.equals(other.divider)
        && paddingLeft.equals(other.paddingLeft)
        && paddingRight.equals(other.paddingRight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        block, titles, selected, style, highlightStyle, divider, paddingLeft, paddingRight);
  }
}
