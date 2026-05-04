package jatatui.widgets.block;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Styled;
import jatatui.core.style.Stylize;
import jatatui.core.symbols.Border;
import jatatui.core.symbols.Merge;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.widgets.Widget;
import jatatui.widgets.Borders;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A widget that renders borders, titles, and padding around other widgets.
///
/// A `Block` creates visual containers by drawing borders around an area. It serves as a wrapper
/// or frame for other widgets and is the most pervasive helper widget in the library — most
/// built-in widgets accept an optional `Block` parameter that wraps the widget's content.
///
/// Mirrors `ratatui_widgets::block::Block` (v0.30) with a Java-flavoured fluent API:
///
/// - Construct with [#empty()] (no borders) or [#bordered()] (all borders).
/// - Build via `withFoo(...)` methods (titles, styles, borders, padding, etc.).
/// - The widget is **immutable**: every `withFoo` returns a new `Block`.
///
/// Implements [Stylize] so the entire shorthand vocabulary (`.red()`, `.bold()`, ...) is available.
public final class Block implements Widget, Stylize<Block> {

  /// Title with an optional explicit position. When the position is empty, the block's default
  /// `titlesPosition` applies. The line carries its own (optional) horizontal alignment.
  public record BlockTitle(Optional<TitlePosition> position, Line line) {
    public BlockTitle {
      Objects.requireNonNull(position, "position");
      Objects.requireNonNull(line, "line");
    }
  }

  /// Pair of (left/right) or (top/bottom) space taken by a block — returned by
  /// [#horizontalSpace()] and [#verticalSpace()].
  public record SpacePair(int first, int second) {}

  /// List of titles in insertion order.
  public final List<BlockTitle> titles;

  /// The style to be patched to all titles of the block.
  public final Style titlesStyle;

  /// Default alignment of titles that don't have one.
  public final HorizontalAlignment titlesAlignment;

  /// Default position of titles that don't have one.
  public final TitlePosition titlesPosition;

  /// Visible borders.
  public final Borders borders;

  /// Border style.
  public final Style borderStyle;

  /// Border symbol set (driven by [BorderType] or set explicitly via [#withBorderSet(Border.Set)]).
  public final Border.Set borderSet;

  /// Widget style applied to the entire block area first.
  public final Style style;

  /// Block padding (interior offsets).
  public final Padding padding;

  /// Border merging strategy (controls how borders combine with previously-rendered cells).
  public final Merge.MergeStrategy mergeBorders;

  private Block(
      List<BlockTitle> titles,
      Style titlesStyle,
      HorizontalAlignment titlesAlignment,
      TitlePosition titlesPosition,
      Borders borders,
      Style borderStyle,
      Border.Set borderSet,
      Style style,
      Padding padding,
      Merge.MergeStrategy mergeBorders) {
    this.titles = List.copyOf(titles);
    this.titlesStyle = titlesStyle;
    this.titlesAlignment = titlesAlignment;
    this.titlesPosition = titlesPosition;
    this.borders = borders;
    this.borderStyle = borderStyle;
    this.borderSet = borderSet;
    this.style = style;
    this.padding = padding;
    this.mergeBorders = mergeBorders;
  }

  // ---- Constructors ----

  /// Creates a new block with no borders or padding.
  public static Block empty() {
    return new Block(
        List.of(),
        Style.empty(),
        HorizontalAlignment.Left,
        TitlePosition.Top,
        Borders.NONE,
        Style.empty(),
        BorderType.Plain.toBorderSet(),
        Style.empty(),
        Padding.ZERO,
        Merge.MergeStrategy.Replace);
  }

  /// Creates a new block with [Borders#ALL] shown.
  public static Block bordered() {
    return empty().withBorders(Borders.ALL);
  }

  // ---- Title configuration ----

  /// Adds a title to the block using the default position (set via
  /// [#withTitlePosition(TitlePosition)], defaulting to [TitlePosition#Top]).
  public Block withTitle(Line title) {
    List<BlockTitle> next = new ArrayList<>(titles.size() + 1);
    next.addAll(titles);
    next.add(new BlockTitle(Optional.empty(), title));
    return new Block(
        next, titlesStyle, titlesAlignment, titlesPosition, borders, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  /// Convenience overload accepting a string.
  public Block withTitle(String title) {
    return withTitle(Line.from(title));
  }

  /// Adds a title to the top of the block.
  public Block withTitleTop(Line title) {
    List<BlockTitle> next = new ArrayList<>(titles.size() + 1);
    next.addAll(titles);
    next.add(new BlockTitle(Optional.of(TitlePosition.Top), title));
    return new Block(
        next, titlesStyle, titlesAlignment, titlesPosition, borders, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  /// Convenience overload accepting a string.
  public Block withTitleTop(String title) {
    return withTitleTop(Line.from(title));
  }

  /// Adds a title to the bottom of the block.
  public Block withTitleBottom(Line title) {
    List<BlockTitle> next = new ArrayList<>(titles.size() + 1);
    next.addAll(titles);
    next.add(new BlockTitle(Optional.of(TitlePosition.Bottom), title));
    return new Block(
        next, titlesStyle, titlesAlignment, titlesPosition, borders, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  /// Convenience overload accepting a string.
  public Block withTitleBottom(String title) {
    return withTitleBottom(Line.from(title));
  }

  /// Sets the style applied to every title of the block.
  public Block withTitleStyle(Style style) {
    return new Block(
        titles, style, titlesAlignment, titlesPosition, borders, borderStyle, borderSet,
        this.style, padding, mergeBorders);
  }

  /// Sets the default [HorizontalAlignment] for all block titles. Titles that explicitly set an
  /// alignment ignore this default.
  public Block withTitleAlignment(HorizontalAlignment alignment) {
    return new Block(
        titles, titlesStyle, alignment, titlesPosition, borders, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  /// Sets the default [TitlePosition] for all block titles. Titles that explicitly set a position
  /// ignore this default.
  public Block withTitlePosition(TitlePosition position) {
    return new Block(
        titles, titlesStyle, titlesAlignment, position, borders, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  // ---- Border / style configuration ----

  /// Defines the style of the borders.
  public Block withBorderStyle(Style style) {
    return new Block(
        titles, titlesStyle, titlesAlignment, titlesPosition, borders, style, borderSet,
        this.style, padding, mergeBorders);
  }

  /// Defines the style of the entire block.
  public Block withStyle(Style style) {
    return new Block(
        titles, titlesStyle, titlesAlignment, titlesPosition, borders, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  /// Defines which borders are displayed.
  public Block withBorders(Borders flag) {
    return new Block(
        titles, titlesStyle, titlesAlignment, titlesPosition, flag, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  /// Sets the symbols used to display the border via a [BorderType].
  ///
  /// Setting this overwrites any custom border set previously assigned via
  /// [#withBorderSet(Border.Set)].
  public Block withBorderType(BorderType borderType) {
    return new Block(
        titles, titlesStyle, titlesAlignment, titlesPosition, borders, borderStyle,
        borderType.toBorderSet(), style, padding, mergeBorders);
  }

  /// Sets the symbols used to display the border directly as a [Border.Set]. Setting this
  /// overwrites any [BorderType] previously assigned via [#withBorderType(BorderType)].
  public Block withBorderSet(Border.Set borderSet) {
    return new Block(
        titles, titlesStyle, titlesAlignment, titlesPosition, borders, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  /// Sets the [Padding] inside the block.
  public Block withPadding(Padding padding) {
    return new Block(
        titles, titlesStyle, titlesAlignment, titlesPosition, borders, borderStyle, borderSet,
        style, padding, mergeBorders);
  }

  /// Sets the merge strategy for overlapping border characters (defaults to
  /// [Merge.MergeStrategy#Replace]).
  public Block withMergeBorders(Merge.MergeStrategy strategy) {
    return new Block(
        titles, titlesStyle, titlesAlignment, titlesPosition, borders, borderStyle, borderSet,
        style, padding, strategy);
  }

  // ---- Geometry ----

  /// Computes the inner area of a block after subtracting space for borders, titles, and padding.
  public Rect inner(Rect area) {
    int x = area.x();
    int y = area.y();
    int width = area.width();
    int height = area.height();
    int right = saturatingAdd(x, width);
    int bottom = saturatingAdd(y, height);

    if (borders.intersects(Borders.LEFT)) {
      int newX = Math.min(saturatingAdd(x, 1), right);
      x = newX;
      width = saturatingSub(width, 1);
    }
    if (borders.intersects(Borders.TOP) || hasTitleAtPosition(TitlePosition.Top)) {
      int newY = Math.min(saturatingAdd(y, 1), bottom);
      y = newY;
      height = saturatingSub(height, 1);
    }
    if (borders.intersects(Borders.RIGHT)) {
      width = saturatingSub(width, 1);
    }
    if (borders.intersects(Borders.BOTTOM) || hasTitleAtPosition(TitlePosition.Bottom)) {
      height = saturatingSub(height, 1);
    }

    x = saturatingAdd(x, padding.left());
    y = saturatingAdd(y, padding.top());
    width = saturatingSub(width, padding.left() + padding.right());
    height = saturatingSub(height, padding.top() + padding.bottom());

    return new Rect(x, y, width, height);
  }

  private boolean hasTitleAtPosition(TitlePosition position) {
    for (BlockTitle t : titles) {
      TitlePosition pos = t.position().orElse(titlesPosition);
      if (pos == position) return true;
    }
    return false;
  }

  /// Calculates the (left, right) horizontal space the block consumes, taking borders and
  /// padding into account.
  public SpacePair horizontalSpace() {
    int left = saturatingAdd(padding.left(), borders.contains(Borders.LEFT) ? 1 : 0);
    int right = saturatingAdd(padding.right(), borders.contains(Borders.RIGHT) ? 1 : 0);
    return new SpacePair(left, right);
  }

  /// Calculates the (top, bottom) vertical space the block consumes, taking borders, padding and
  /// title positions into account.
  public SpacePair verticalSpace() {
    boolean hasTop = borders.contains(Borders.TOP) || hasTitleAtPosition(TitlePosition.Top);
    int top = padding.top() + (hasTop ? 1 : 0);
    boolean hasBottom = borders.contains(Borders.BOTTOM) || hasTitleAtPosition(TitlePosition.Bottom);
    int bottom = padding.bottom() + (hasBottom ? 1 : 0);
    return new SpacePair(top, bottom);
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Block setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Widget rendering ----

  @Override
  public void render(Rect area, Buffer buf) {
    Rect clipped = area.intersection(buf.area);
    if (clipped.isEmpty()) {
      return;
    }
    buf.setStyle(clipped, style);
    renderBorders(clipped, buf);
    renderTitles(clipped, buf);
  }

  private void renderBorders(Rect area, Buffer buf) {
    renderSides(area, buf);
    renderCorners(area, buf);
  }

  private void renderSides(Rect area, Buffer buf) {
    int left = area.left();
    int top = area.top();
    int right = area.right() - 1;
    int bottom = area.bottom() - 1;

    boolean isReplace = mergeBorders != Merge.MergeStrategy.Replace;
    int leftInset = left + (isReplace && borders.contains(Borders.LEFT) ? 1 : 0);
    int topInset = top + (isReplace && borders.contains(Borders.TOP) ? 1 : 0);
    int rightInset = right - (isReplace && borders.contains(Borders.RIGHT) ? 1 : 0);
    int bottomInset = bottom - (isReplace && borders.contains(Borders.BOTTOM) ? 1 : 0);

    if (borders.contains(Borders.LEFT)) {
      paintRange(buf, left, left, topInset, bottomInset, borderSet.verticalLeft());
    }
    if (borders.contains(Borders.TOP)) {
      paintRange(buf, leftInset, rightInset, top, top, borderSet.horizontalTop());
    }
    if (borders.contains(Borders.RIGHT)) {
      paintRange(buf, right, right, topInset, bottomInset, borderSet.verticalRight());
    }
    if (borders.contains(Borders.BOTTOM)) {
      paintRange(buf, leftInset, rightInset, bottom, bottom, borderSet.horizontalBottom());
    }
  }

  private void paintRange(Buffer buf, int x0, int x1, int y0, int y1, String symbol) {
    for (int x = x0; x <= x1; x++) {
      for (int y = y0; y <= y1; y++) {
        Cell c = buf.cellAt(x, y);
        c.mergeSymbol(symbol, mergeBorders).setStyle(borderStyle);
      }
    }
  }

  private void renderCorners(Rect area, Buffer buf) {
    int left = area.left();
    int top = area.top();
    int right = area.right() - 1;
    int bottom = area.bottom() - 1;

    if (borders.contains(Borders.RIGHT.or(Borders.BOTTOM))) {
      paintCorner(buf, right, bottom, borderSet.bottomRight());
    }
    if (borders.contains(Borders.RIGHT.or(Borders.TOP))) {
      paintCorner(buf, right, top, borderSet.topRight());
    }
    if (borders.contains(Borders.LEFT.or(Borders.BOTTOM))) {
      paintCorner(buf, left, bottom, borderSet.bottomLeft());
    }
    if (borders.contains(Borders.LEFT.or(Borders.TOP))) {
      paintCorner(buf, left, top, borderSet.topLeft());
    }
  }

  private void paintCorner(Buffer buf, int x, int y, String symbol) {
    Cell c = buf.cellAt(x, y);
    c.mergeSymbol(symbol, mergeBorders).setStyle(borderStyle);
  }

  private void renderTitles(Rect area, Buffer buf) {
    renderTitlePosition(TitlePosition.Top, area, buf);
    renderTitlePosition(TitlePosition.Bottom, area, buf);
  }

  private void renderTitlePosition(TitlePosition position, Rect area, Buffer buf) {
    // Order matters: it defines overlap behaviour. Left → center → right (later overwrites earlier).
    renderLeftTitles(position, area, buf);
    renderCenterTitles(position, area, buf);
    renderRightTitles(position, area, buf);
  }

  private void renderLeftTitles(TitlePosition position, Rect area, Buffer buf) {
    List<Line> filtered = filteredTitles(position, HorizontalAlignment.Left);
    Rect titlesArea = titlesArea(area, position);
    for (Line title : filtered) {
      if (titlesArea.isEmpty()) break;
      int titleWidth = title.width();
      int width = Math.min(titleWidth, titlesArea.width());
      Rect titleArea = new Rect(titlesArea.x(), titlesArea.y(), width, titlesArea.height());
      buf.setStyle(titleArea, titlesStyle);
      renderLine(title, titleArea, Optional.empty(), buf);
      // Bump area to the right and reduce its width.
      int advance = titleWidth + 1;
      int newX = saturatingAdd(titlesArea.x(), advance);
      int newWidth = saturatingSub(titlesArea.width(), advance);
      titlesArea = new Rect(newX, titlesArea.y(), newWidth, titlesArea.height());
    }
  }

  private void renderCenterTitles(TitlePosition position, Rect area, Buffer buf) {
    Rect baseArea = titlesArea(area, position);
    List<Line> titles0 = filteredTitles(position, HorizontalAlignment.Center);
    int totalWidth = 0;
    for (Line t : titles0) {
      totalWidth += t.width() + 1;
    }
    totalWidth = saturatingSub(totalWidth, 1);

    if (totalWidth <= baseArea.width()) {
      renderCenteredTitlesWithoutTruncation(titles0, totalWidth, baseArea, buf);
    } else {
      renderCenteredTitlesWithTruncation(titles0, totalWidth, baseArea, buf);
    }
  }

  private void renderCenteredTitlesWithoutTruncation(
      List<Line> titles0, int totalWidth, Rect area, Buffer buf) {
    int x = area.left() + saturatingSub(area.width(), totalWidth) / 2;
    Rect cur = new Rect(x, area.y(), area.width() - (x - area.left()), area.height());
    for (Line title : titles0) {
      int width = title.width();
      Rect titleArea = new Rect(cur.x(), cur.y(), width, cur.height());
      buf.setStyle(titleArea, titlesStyle);
      renderLine(title, titleArea, Optional.empty(), buf);
      int advance = width + 1;
      int newX = saturatingAdd(cur.x(), advance);
      int newWidth = saturatingSub(cur.width(), advance);
      cur = new Rect(newX, cur.y(), newWidth, cur.height());
    }
  }

  private void renderCenteredTitlesWithTruncation(
      List<Line> titles0, int totalWidth, Rect area, Buffer buf) {
    int offset = saturatingSub(totalWidth, area.width()) / 2;
    Rect cur = area;
    for (Line title : titles0) {
      if (cur.isEmpty()) break;
      int width = saturatingSub(Math.min(cur.width(), title.width()), offset);
      Rect titleArea = new Rect(cur.x(), cur.y(), width, cur.height());
      buf.setStyle(titleArea, titlesStyle);
      if (offset > 0) {
        // truncate the left side of the title to fit the area
        // mirrors upstream `title.clone().right_aligned()` — explicitly overrides line alignment
        renderLine(title.rightAligned(), titleArea, Optional.empty(), buf);
        offset = saturatingSub(saturatingSub(offset, width), 1);
      } else {
        // mirrors upstream `title.clone().left_aligned()` — explicitly overrides line alignment
        renderLine(title.leftAligned(), titleArea, Optional.empty(), buf);
      }
      int advance = width + 1;
      int newX = saturatingAdd(cur.x(), advance);
      int newWidth = saturatingSub(cur.width(), advance);
      cur = new Rect(newX, cur.y(), newWidth, cur.height());
    }
  }

  private void renderRightTitles(TitlePosition position, Rect area, Buffer buf) {
    List<Line> filtered = filteredTitles(position, HorizontalAlignment.Right);
    Rect titlesArea = titlesArea(area, position);

    // iterate in reverse so the rightmost title sits at the right edge
    for (int i = filtered.size() - 1; i >= 0; i--) {
      if (titlesArea.isEmpty()) break;
      Line title = filtered.get(i);
      int titleWidth = title.width();
      int x = Math.max(saturatingSub(titlesArea.right(), titleWidth), titlesArea.left());
      int width = Math.min(titleWidth, titlesArea.width());
      Rect titleArea = new Rect(x, titlesArea.y(), width, titlesArea.height());
      buf.setStyle(titleArea, titlesStyle);
      renderLine(title, titleArea, Optional.empty(), buf);
      // shrink area from the right
      int advance = titleWidth + 1;
      int newWidth = saturatingSub(titlesArea.width(), advance);
      titlesArea = new Rect(titlesArea.x(), titlesArea.y(), newWidth, titlesArea.height());
    }
  }

  private List<Line> filteredTitles(TitlePosition position, HorizontalAlignment alignment) {
    List<Line> out = new ArrayList<>();
    for (BlockTitle t : titles) {
      TitlePosition pos = t.position().orElse(titlesPosition);
      if (pos != position) continue;
      HorizontalAlignment align = t.line().alignment.orElse(titlesAlignment);
      if (align != alignment) continue;
      out.add(t.line());
    }
    return out;
  }

  private Rect titlesArea(Rect area, TitlePosition position) {
    int leftBorder = borders.contains(Borders.LEFT) ? 1 : 0;
    int rightBorder = borders.contains(Borders.RIGHT) ? 1 : 0;
    int x = area.left() + leftBorder;
    int y =
        switch (position) {
          case Top -> area.top();
          case Bottom -> area.bottom() - 1;
        };
    int width = saturatingSub(saturatingSub(area.width(), leftBorder), rightBorder);
    return new Rect(x, y, width, 1);
  }

  // ---- Helpers ----

  /// Renders a [Line] into `area`, using the line's alignment, falling back to
  /// `parentAlignment` when the line itself has none. Mirrors the upstream
  /// `Line::render_with_alignment`.
  private static void renderLine(Line line, Rect area, Optional<HorizontalAlignment> parentAlignment, Buffer buf) {
    Rect clipped = area.intersection(buf.area);
    if (clipped.isEmpty()) return;
    Rect oneRow = new Rect(clipped.x(), clipped.y(), clipped.width(), 1);
    int lineWidth = line.width();
    if (lineWidth == 0) return;

    buf.setStyle(oneRow, line.style);

    Optional<HorizontalAlignment> alignment =
        line.alignment.isPresent() ? line.alignment : parentAlignment;

    int areaWidth = oneRow.width();
    if (lineWidth <= areaWidth) {
      int indent =
          switch (alignment.orElse(HorizontalAlignment.Left)) {
            case Center -> Math.max(0, areaWidth - lineWidth) / 2;
            case Right -> Math.max(0, areaWidth - lineWidth);
            case Left -> 0;
          };
      Rect indented = new Rect(
          saturatingAdd(oneRow.x(), indent), oneRow.y(), saturatingSub(oneRow.width(), indent), 1);
      renderSpansAt(line, indented, 0, buf);
    } else {
      int skipWidth =
          switch (alignment.orElse(HorizontalAlignment.Left)) {
            case Center -> Math.max(0, lineWidth - areaWidth) / 2;
            case Right -> Math.max(0, lineWidth - areaWidth);
            case Left -> 0;
          };
      renderSpansAt(line, oneRow, skipWidth, buf);
    }
  }

  /// Render the spans of `line` into `area`, skipping the first `skipWidth` display columns.
  private static void renderSpansAt(Line line, Rect area, int skipWidth, Buffer buf) {
    Rect cur = area;
    int remainingSkip = skipWidth;
    for (Span span : line) {
      int spanWidth = span.width();
      if (remainingSkip >= spanWidth) {
        remainingSkip -= spanWidth;
        continue;
      }
      if (cur.isEmpty()) break;
      String content = span.content;
      Style style = line.style.patch(span.style);
      if (remainingSkip > 0) {
        // truncate the start of this span by `remainingSkip` display columns
        SkipResult skipped = skipDisplayColumns(content, remainingSkip);
        // If the skip lands in the middle of a wide grapheme, indent the area by the remainder.
        int firstGraphemeOffset = remainingSkip - skipped.actualSkipped();
        if (firstGraphemeOffset > 0) {
          int indent = Math.min(firstGraphemeOffset, cur.width());
          cur = new Rect(saturatingAdd(cur.x(), indent), cur.y(), saturatingSub(cur.width(), indent), 1);
        }
        Position end = buf.setStringn(cur.x(), cur.y(), skipped.remaining(), cur.width(), style);
        int written = Math.max(0, end.x() - cur.x());
        cur = new Rect(end.x(), cur.y(), saturatingSub(cur.width(), written), 1);
        remainingSkip = 0;
      } else {
        Position end = buf.setStringn(cur.x(), cur.y(), content, cur.width(), style);
        int written = Math.max(0, end.x() - cur.x());
        cur = new Rect(end.x(), cur.y(), saturatingSub(cur.width(), written), 1);
      }
    }
  }

  /// Skip the first `skip` display columns of `s`, returning the remainder and how many columns
  /// were actually skipped (may be `skip + 1` if the truncation lands inside a wide grapheme —
  /// upstream rounds up by skipping the whole wide grapheme and emitting an indent space).
  ///
  /// Hard rule: no `null` — uses a small record.
  private record SkipResult(String remaining, int actualSkipped) {}

  private static SkipResult skipDisplayColumns(String s, int skip) {
    java.text.BreakIterator iter = java.text.BreakIterator.getCharacterInstance();
    iter.setText(s);
    int start = iter.first();
    int end = iter.next();
    int consumed = 0;
    int idx = 0;
    while (end != java.text.BreakIterator.DONE) {
      String g = s.substring(start, end);
      int w = jatatui.core.internal.Wcwidth.width(g);
      if (consumed + w > skip) {
        // We want to skip `skip` columns; this grapheme would push us past it. Upstream behavior:
        // skip exactly `skip` columns by advancing past the partial grapheme and emitting padding.
        // We mirror by advancing past it and reporting `actualSkipped = consumed`.
        return new SkipResult(s.substring(start), consumed);
      }
      consumed += w;
      idx = end;
      start = end;
      end = iter.next();
      if (consumed == skip) {
        return new SkipResult(s.substring(idx), consumed);
      }
    }
    return new SkipResult("", consumed);
  }

  private static int saturatingAdd(int a, int b) {
    long r = (long) a + (long) b;
    if (r > Position.U16_MAX) return Position.U16_MAX;
    if (r < 0) return 0;
    return (int) r;
  }

  private static int saturatingSub(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    if (r > Position.U16_MAX) return Position.U16_MAX;
    return (int) r;
  }

  // ---- Equality / hash / debug ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Block other)) return false;
    return titles.equals(other.titles)
        && titlesStyle.equals(other.titlesStyle)
        && titlesAlignment == other.titlesAlignment
        && titlesPosition == other.titlesPosition
        && borders.equals(other.borders)
        && borderStyle.equals(other.borderStyle)
        && borderSet.equals(other.borderSet)
        && style.equals(other.style)
        && padding.equals(other.padding)
        && mergeBorders == other.mergeBorders;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        titles,
        titlesStyle,
        titlesAlignment,
        titlesPosition,
        borders,
        borderStyle,
        borderSet,
        style,
        padding,
        mergeBorders);
  }
}
