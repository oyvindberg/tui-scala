package jatatui.widgets.paragraph;

import jatatui.core.buffer.Buffer;
import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.text.Line;
import jatatui.core.text.StyledGrapheme;
import jatatui.core.text.Text;
import jatatui.core.widgets.Widget;
import jatatui.widgets.block.Block;
import jatatui.widgets.reflow.LineComposer;
import jatatui.widgets.reflow.LineTruncator;
import jatatui.widgets.reflow.StyledLineInput;
import jatatui.widgets.reflow.WordWrapper;
import jatatui.widgets.reflow.WrappedLine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A widget to display some text.
///
/// Mirrors `ratatui_widgets::paragraph::Paragraph` (v0.30).
///
/// The text can be styled and aligned, optionally wrapped to the next line, optionally scrolled,
/// and optionally surrounded by a [Block].
///
/// # Example
///
/// ```java
/// Paragraph paragraph = Paragraph.of(Text.from("Hello, world!"))
///     .withBlock(Block.bordered().withTitle("Paragraph"))
///     .withStyle(Style.empty())
///     .withAlignment(HorizontalAlignment.Center)
///     .withWrap(new Wrap(true));
/// ```
public final class Paragraph implements Widget, Stylize<Paragraph> {

  /// A block to wrap the widget in.
  public final Optional<Block> block;

  /// Widget style.
  public final Style style;

  /// How to wrap the text. Empty disables wrapping (the text gets truncated instead).
  public final Optional<Wrap> wrap;

  /// The text to display.
  public final Text text;

  /// Scroll offset.
  public final Scroll scroll;

  /// Alignment of the text.
  public final HorizontalAlignment alignment;

  private Paragraph(
      Optional<Block> block,
      Style style,
      Optional<Wrap> wrap,
      Text text,
      Scroll scroll,
      HorizontalAlignment alignment) {
    this.block = block;
    this.style = style;
    this.wrap = wrap;
    this.text = text;
    this.scroll = scroll;
    this.alignment = alignment;
  }

  // ---- Constructors ----

  /// Creates a new [Paragraph] with the given text. The text is styled with [Style#empty()],
  /// not wrapped, and left-aligned by default.
  public static Paragraph of(Text text) {
    return new Paragraph(
        Optional.empty(),
        Style.empty(),
        Optional.empty(),
        text,
        Scroll.ZERO,
        HorizontalAlignment.Left);
  }

  /// Convenience overload mirroring the upstream `Paragraph::new(Into<Text>)`.
  public static Paragraph of(String text) {
    return of(Text.raw(text));
  }

  /// Convenience overload mirroring the upstream `Paragraph::new(Into<Text>)`.
  public static Paragraph of(Line line) {
    return of(Text.from(line));
  }

  /// Convenience overload mirroring the upstream `Paragraph::new(Into<Text>)`.
  public static Paragraph of(List<Line> lines) {
    return of(Text.fromLines(lines));
  }

  // ---- Fluent setters ----

  /// Surrounds the [Paragraph] with the given [Block].
  public Paragraph withBlock(Block block) {
    return new Paragraph(Optional.of(block), style, wrap, text, scroll, alignment);
  }

  /// Replaces this paragraph's [Style].
  public Paragraph withStyle(Style style) {
    return new Paragraph(block, style, wrap, text, scroll, alignment);
  }

  /// Sets the wrapping configuration for this paragraph.
  public Paragraph withWrap(Wrap wrap) {
    return new Paragraph(block, style, Optional.of(wrap), text, scroll, alignment);
  }

  /// Sets the scroll offset for this paragraph. The scroll offset is applied after the text is
  /// wrapped and aligned. Note the order: `(y, x)`, matching upstream.
  public Paragraph withScroll(Scroll scroll) {
    return new Paragraph(block, style, wrap, text, scroll, alignment);
  }

  /// Sets the [HorizontalAlignment] of the text in the paragraph.
  public Paragraph withAlignment(HorizontalAlignment alignment) {
    return new Paragraph(block, style, wrap, text, scroll, alignment);
  }

  /// Convenience shortcut for [#withAlignment(HorizontalAlignment)] with
  // [HorizontalAlignment#Left].
  public Paragraph leftAligned() {
    return withAlignment(HorizontalAlignment.Left);
  }

  /// Convenience shortcut for [#withAlignment(HorizontalAlignment)] with
  // [HorizontalAlignment#Center].
  public Paragraph centered() {
    return withAlignment(HorizontalAlignment.Center);
  }

  /// Convenience shortcut for [#withAlignment(HorizontalAlignment)] with
  // [HorizontalAlignment#Right].
  public Paragraph rightAligned() {
    return withAlignment(HorizontalAlignment.Right);
  }

  // ---- Geometry ----

  /// Calculates the number of lines needed to fully render this paragraph at the given line
  /// width.
  ///
  /// For paragraphs that do not use wrapping, this count is simply the number of lines present.
  /// Otherwise the [WordWrapper] is used to compute the actual count. Block borders/padding are
  /// included in the result.
  public int lineCount(int width) {
    if (width < 1) {
      return 0;
    }

    Block.SpacePair vSpace = block.map(Block::verticalSpace).orElse(new Block.SpacePair(0, 0));
    int top = vSpace.first();
    int bottom = vSpace.second();

    int count;
    if (wrap.isPresent()) {
      Wrap w = wrap.get();
      WordWrapper composer =
          new WordWrapper(toStyledLineInputs(text, style, alignment), width, w.trim());
      count = 0;
      while (composer.nextLine().isPresent()) {
        count += 1;
      }
    } else {
      count = text.height();
    }

    return saturatingAdd(saturatingAdd(count, top), bottom);
  }

  /// Calculates the shortest line width needed to avoid any word being wrapped or truncated.
  ///
  /// Accounts for the [Block] if a block is set.
  public int lineWidth() {
    int max = 0;
    for (Line line : text) {
      int w = line.width();
      if (w > max) max = w;
    }
    Block.SpacePair hSpace = block.map(Block::horizontalSpace).orElse(new Block.SpacePair(0, 0));
    return saturatingAdd(saturatingAdd(max, hSpace.first()), hSpace.second());
  }

  // ---- Widget rendering ----

  @Override
  public void render(Rect area, Buffer buf) {
    Rect intersected = area.intersection(buf.area);
    buf.setStyle(intersected, style);
    block.ifPresent(b -> b.render(intersected, buf));
    Rect inner = block.isPresent() ? block.get().inner(intersected) : intersected;
    renderParagraph(inner, buf);
  }

  private void renderParagraph(Rect textArea, Buffer buf) {
    if (textArea.isEmpty()) {
      return;
    }

    buf.setStyle(textArea, style);

    if (wrap.isPresent()) {
      Wrap w = wrap.get();
      WordWrapper composer =
          new WordWrapper(
              toStyledLineInputs(text, text.style, alignment), textArea.width(), w.trim());
      // compute the lines iteratively until we reach the desired scroll offset.
      for (int i = 0; i < scroll.y(); i++) {
        if (composer.nextLine().isEmpty()) {
          return;
        }
      }
      renderLines(composer, textArea, buf);
    } else {
      // skip directly to the relevant line before rendering
      Iterator<StyledLineInput> all = toStyledLineInputs(text, text.style, alignment);
      // skip `scroll.y` items
      for (int i = 0; i < scroll.y() && all.hasNext(); i++) {
        all.next();
      }
      LineTruncator composer = new LineTruncator(all, textArea.width());
      composer.setHorizontalOffset(scroll.x());
      renderLines(composer, textArea, buf);
    }
  }

  private static Iterator<StyledLineInput> toStyledLineInputs(
      Text text, Style baseStyle, HorizontalAlignment paragraphAlignment) {
    List<StyledLineInput> list = new ArrayList<>(text.lines.size());
    for (Line line : text) {
      List<StyledGrapheme> graphemes = line.styledGraphemes(baseStyle);
      HorizontalAlignment align = line.alignment.orElse(paragraphAlignment);
      list.add(new StyledLineInput(graphemes.iterator(), align));
    }
    return list.iterator();
  }

  private static void renderLines(LineComposer composer, Rect area, Buffer buf) {
    int y = 0;
    while (true) {
      Optional<WrappedLine> wrapped = composer.nextLine();
      if (wrapped.isEmpty()) break;
      renderLine(wrapped.get(), area, buf, y);
      y += 1;
      if (y >= area.height()) {
        break;
      }
    }
  }

  private static void renderLine(WrappedLine wrapped, Rect area, Buffer buf, int y) {
    int x = getLineOffset(wrapped.width(), area.width(), wrapped.alignment());
    for (StyledGrapheme g : wrapped.graphemes()) {
      String symbol = g.symbol;
      int width = Wcwidth.width(symbol);
      if (width == 0) {
        continue;
      }
      // Make sure to overwrite any previous character with a space (rather than a zero-width).
      String paint = symbol.isEmpty() ? " " : symbol;
      int px = area.left() + x;
      int py = area.top() + y;
      // Bounds check: skip cells outside the buffer (mirrors upstream's panic-free indexing —
      // upstream uses `buf[position]` which would panic out-of-bounds, but `area` has already
      // been intersected with `buf.area`, so we are inside.
      buf.cellAt(new Position(px, py)).setSymbol(paint).setStyle(g.style);
      x += Math.min(width, Position.U16_MAX);
    }
  }

  private static int getLineOffset(
      int lineWidth, int textAreaWidth, HorizontalAlignment alignment) {
    return switch (alignment) {
      case Center -> Math.max(0, textAreaWidth / 2 - lineWidth / 2);
      case Right -> Math.max(0, textAreaWidth - lineWidth);
      case Left -> 0;
    };
  }

  // ---- Stylize ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Paragraph setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Equality / hash ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Paragraph other)) return false;
    return block.equals(other.block)
        && style.equals(other.style)
        && wrap.equals(other.wrap)
        && text.equals(other.text)
        && scroll.equals(other.scroll)
        && alignment == other.alignment;
  }

  @Override
  public int hashCode() {
    return Objects.hash(block, style, wrap, text, scroll, alignment);
  }

  private static int saturatingAdd(int a, int b) {
    long r = (long) a + (long) b;
    if (r > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (r < 0) return 0;
    return (int) r;
  }
}
